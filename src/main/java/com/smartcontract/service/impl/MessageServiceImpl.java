package com.smartcontract.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.smartcontract.MessageTitle;
import com.smartcontract.model.ContractBean;
import com.smartcontract.model.ContractTemplateBean;
import com.smartcontract.model.DealBean;
import com.smartcontract.model.DslHistory;
import com.smartcontract.model.EventBean;
import com.smartcontract.model.Message;
import com.smartcontract.model.MessageProcessResponse;
import com.smartcontract.model.ReturnParamBean;
import com.smartcontract.model.TradeBean;
import com.smartcontract.repository.MessageRepository;
import com.smartcontract.service.ContractBeanService;
import com.smartcontract.service.ContractTemplateBeanService;
import com.smartcontract.service.DslHistoryService;
import com.smartcontract.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.apache.groovy.parser.antlr4.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static com.smartcontract.service.ScriptProcessHelper.runPyScript;
import static com.smartcontract.util.CustomObjectMapper.createObjectMapper;

@Service
@Slf4j
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;

    private final ContractBeanService contractBeanService;

    private final ContractTemplateBeanService contractTemplateBeanService;

    private final DslHistoryService dslHistoryService;

    private final ObjectMapper objectMapper = createObjectMapper();

    @Autowired
    public MessageServiceImpl(MessageRepository messageRepository,
                              ContractBeanService contractBeanService,
                              ContractTemplateBeanService contractTemplateBeanService,
                              DslHistoryService dslHistoryService) {
        this.messageRepository = messageRepository;
        this.contractBeanService = contractBeanService;
        this.contractTemplateBeanService = contractTemplateBeanService;
        this.dslHistoryService = dslHistoryService;
    }

    @Override
    public void save(Message message) {
        messageRepository.save(message);
    }

    @Override
    public Message getByMessageUuid(String messageUuid) {
        Optional<Message> message = messageRepository.getByMessageUuid(messageUuid);
        return message.orElse(null);
    }

    @Override
    public MessageProcessResponse route(Message message) {
        if (message == null) {
            log.error("message is null!");
            return null;
        }
        log.info("Process message: {}", message);
        // save the message
//        save(message);
        MessageProcessResponse response = new MessageProcessResponse();
        response.setMessageUuid(message.getMessageUuid());
        MessageTitle msgTitle = MessageTitle.fromValue(message.getTitle());
        if (msgTitle == null) {
            String resMsg = String.format("Invalid message title: %s!", message.getTitle());
            log.error(resMsg);
            response.setResCode(-1);
            response.setResMsg(resMsg);
            return response;
        }
        // Fail: -1, Success: 1
        Integer resCode;
        switch (msgTitle) {
            case UPLOAD_CONTENT:
            case UPLOAD_HOLD_CONTENT:
                try {
                    resCode = uploadContent(message);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
                break;
            case REMOVE_CONTENT:
                // remove from contractBean, add to contractBeanHistory
                resCode = removeContent(message);
                break;
            case EVENT:
                // Decide later for how to send dslResult event returned instruction, we may consider
                // 1. Include the SmartContract project in mgmt system;
                // 2. Consider synchronous API calls or asynchronous instruction messages for event process.
                // 3. We may only save the contract data in contractBean, use up-to-date groovy script within the project
                String msgBody = message.getBody();
                JSONObject msgObj = JSONObject.parseObject(msgBody);
                String contractCode = msgObj.getString("contractCode");
                AtomicReference<Integer> tmpCode = new AtomicReference<>();
                try {
                    tmpCode.set(processEvent(message));
                    } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                resCode = tmpCode.get();
                break;
            default:
                resCode = -1;
                break;
        }
        String resMsg;
        if (resCode.equals(-1)) {
            resMsg = String.format("Failed to process the message with messageUuid: %s", message.getMessageUuid());
        } else {
            resMsg = String.format("Succeed to process the message with messageUuid: %s", message.getMessageUuid());
        }
        response.setResCode(resCode);
        response.setResMsg(resMsg);
        return response;
    }

    private Integer uploadContent(Message message) throws JsonProcessingException {
        String msgBody = message.getBody();
        ContractBean contractBean = objectMapper.readValue(msgBody, ContractBean.class);
        // get the corresponding template with templateId
        String templateId = contractBean.getContractTemplateId();
        ContractTemplateBean templateBean = contractTemplateBeanService.getContractTemplateBeanByTemplateId(templateId);
        // add the default template content to the contractBean
        if (templateBean != null) {
            DealBean templateDealBean = templateBean.getTrades().get(0).getDeals().get(0);
            List<TradeBean> trades = new ArrayList<>();
            for (TradeBean tradeBean : contractBean.getTrades()) {
                List<DealBean> deals = new ArrayList<>();
                for (DealBean dealBean : tradeBean.getDeals()) {
//                    dealBean.setContractState(templateDealBean.getContractState());
                    dealBean.setEvents(templateDealBean.getEvents());
                    dealBean.setObservations(templateDealBean.getObservations());
                    deals.add(dealBean);
                }
                tradeBean.setDeals(deals);
                trades.add(tradeBean);
            }
            contractBean.setTrades(trades);
            contractBean.setFunctions(templateBean.getFunctions());
            contractBean.setInstructions(templateBean.getInstructions());
            contractBean.setCreateTime(new Date());
        } else {
            String resMsg = String.format("Failed to get the contract template bean with templateId: %s!", templateId);
            log.error(resMsg);
            return -1;
        }
        Integer resCode = contractBeanService.insert(contractBean);
        String resMsg = String.format("Successfully upload the content with contractNo: %s!",
                contractBean.getBasicInfo().getContractNo());
        log.info(resMsg);
        return resCode;
    }

    private Integer removeContent(Message message) {
        String contractCode = message.getBody();
        return contractBeanService.removeFromContractBean(contractCode);
    }

    private Integer processEvent(Message message) throws IOException {
        String msgBody = message.getBody();
        JSONObject dslParam = new JSONObject();
        JSONObject msgObj = JSONObject.parseObject(msgBody);
        JSONObject externalParams = (JSONObject) msgObj.get("paramHash");
        String eventName = msgObj.getString("eventName");
        String contractCode = msgObj.getString("contractCode");
        String tradeId = msgObj.getString("tradeId");
        String dealId = msgObj.getString("dealId");
        log.info("msgBody: {}", msgObj);
        ContractBean contractBean = contractBeanService.getLatestContractBeanByContractCode(contractCode);
        if (contractBean == null) {
            String resMsg = String.format("No contract bean found for contractCode %s", contractCode);
            log.error(resMsg);
            return -1;
        }

        TradeBean tradeBean = contractBean.getTrades().stream()
                .filter(trade -> trade.getTradeId().equals(tradeId))
                .findAny().orElse(null);
        if (tradeBean == null) {
            String resMsg = String.format("No trade bean found for tradeId %s", tradeId);
            log.error(resMsg);
            return -1;
        }

        DealBean dealBean = tradeBean.getDeals().stream()
                .filter(deal -> deal.getDealId().equals(dealId))
                .findAny().orElse(null);
        if (dealBean == null) {
            String resMsg = String.format("No deal bean found for dealId %s", dealId);
            log.error(resMsg);
            return -1;
        }

        EventBean eventBean = dealBean.getEvents().stream()
                .filter(event -> event.getName().equals(eventName))
                .findAny().orElse(null);

        // Read the internalParams from contractBean with json path
        JSONObject internalParamBean = Objects.requireNonNull(eventBean).getInternalParams();
        JSONObject internalParams = new JSONObject();
        String contractBeanJson = objectMapper.writeValueAsString(contractBean);
        Object contractPathObj = Configuration.defaultConfiguration().jsonProvider().parse(contractBeanJson);
        for (String key : internalParamBean.keySet()) {
            internalParams.put(key, JsonPath.read(contractPathObj, internalParamBean.getString(key)));
        }

        dslParam.put("internalParams", internalParams); // including legs, margin, variables, tradeVariables...
        dslParam.put("externalParams", externalParams);
        String dealType = dealBean.getDealType();

        Map<String, Object> dslResult = runPyScript(dealType, eventName, dslParam);
        log.info("dslResult: {}", dslResult);

        // Save the dslHistory record with dslParam and dslResult from python script
        DslHistory dslHistoryRecord = new DslHistory();
        dslHistoryRecord.setDslParam(String.valueOf(dslParam));
        dslHistoryRecord.setEventNo(externalParams.getString("eventNo"));
        dslHistoryRecord.setEventName(eventName);
        dslHistoryRecord.setContractCode(contractCode);
        dslHistoryRecord.setLogicContractCode(msgObj.getString("logicContractCode"));
        dslHistoryRecord.setTradeId(tradeId);
        dslHistoryRecord.setDealId(dealId);
        dslHistoryRecord.setCreateTime(new Date());
        dslHistoryRecord.setDslResult(String.valueOf(dslResult));
//        Integer hisResCode = dslHistoryService.save(dslHistoryRecord);
//        if (hisResCode.equals(-1)) {
//            String resMsg = String.format("Failed to save dslHistory for contractCode %s", contractCode);
//            log.error(resMsg);
//            response.setMessageUuid(message.getMessageUuid());
//            response.setResCode(hisResCode);
//            response.setResMsg(resMsg);
//            return response;
//        }

        // Write and save the variables to contractBean
        List<ReturnParamBean> returnParamsBeanList = Objects.requireNonNull(eventBean).getReturnParams();
        for (ReturnParamBean returnParamsBean: returnParamsBeanList) {
            String from = returnParamsBean.getFrom().split("\\.")[1];
            String to = returnParamsBean.getTo();
            String toContractCode = returnParamsBean.getToContractCode();
            log.info("from: {}, to: {}, toContractCode: {}", from, to, toContractCode);
            // For bundled contracts, write variables to the contractBean with specified toContractCode.
            // Otherwise, write variables to the original default contractBean.
            String toContractCodeBeanJson;
            if (!StringUtils.isEmpty(toContractCode)) {
                ContractBean toContractCodeBean = contractBeanService.getLatestContractBeanByContractCode(toContractCode);
                if (toContractCodeBean == null) {
                    String resMsg = String.format("No contract bean found for toContractCode %s", toContractCode);
                    log.error(resMsg);
                    return -1;
                }
                toContractCodeBeanJson = objectMapper.writeValueAsString(toContractCodeBean);
            } else {
                toContractCodeBeanJson = contractBeanJson;
            }
            Object fromVariables = dslResult.get(from);
            String updatedToContractCodeBeanJson = JsonPath.parse(toContractCodeBeanJson).set(to, fromVariables)
                    .jsonString();
            ContractBean updatedContractBean = objectMapper.readValue(updatedToContractCodeBeanJson,
                    ContractBean.class);
            log.info("updatedContractBean: {}", updatedContractBean.getTrades().get(0).getDeals().get(0).getContractState().getVariables());
//            contractBeanRepository.save(updatedContractBean);
        }
        String msg = String.format("Successfully processed the event %s", externalParams.getString("eventNo"));
        log.info(msg);
        return 1;
    }
}
