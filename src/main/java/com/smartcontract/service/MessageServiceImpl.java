package com.smartcontract.service;

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
import com.smartcontract.model.TradeBean;
import com.smartcontract.repository.ContractBeanRepository;
import com.smartcontract.repository.ContractTemplateBeanRepository;
import com.smartcontract.repository.DslHistoryRepository;
import com.smartcontract.repository.MessageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.smartcontract.service.ScriptProcessHelper.runPyScript;

@Service
@Slf4j
public class MessageServiceImpl implements MessageService{

    private final MessageRepository messageRepository;

    private final ContractBeanRepository contractBeanRepository;

    private final ContractTemplateBeanRepository contractTemplateBeanRepository;

    private final DslHistoryRepository dslHistoryRepository;

    @Autowired
    public MessageServiceImpl(MessageRepository messageRepository, ContractBeanRepository contractBeanRepository,
                              ContractTemplateBeanRepository contractTemplateBeanRepository,
                              DslHistoryRepository dslHistoryRepository) {
        this.messageRepository = messageRepository;
        this.contractBeanRepository = contractBeanRepository;
        this.contractTemplateBeanRepository = contractTemplateBeanRepository;
        this.dslHistoryRepository = dslHistoryRepository;
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
        // save the message
        save(message);
        MessageProcessResponse response = new MessageProcessResponse();
        MessageTitle msgTitle = MessageTitle.fromValue(message.getTitle());
        if (msgTitle != null) {
            switch (msgTitle) {
                case UPLOAD_CONTENT:
                    int resCode = uploadContent(message.getBody());
                    if (resCode == 0) {
                        response.setResCode(resCode);
                        response.setMessageUuid(message.getMessageUuid());
                        response.setResMsg("Successfully upload the new contract!");
                    } else {
                        response.setResCode(-1);
                        response.setMessageUuid(message.getMessageUuid());
                        response.setResMsg("Failed to upload the contract content!");
                    }
                    break;
                case EVENT:
                    return processEvent(message.getBody());

                default:
                    break;
            }
        }
        return response;
    }

    private int uploadContent(String msgBody) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            ContractBean contractBean = objectMapper.readValue(msgBody, ContractBean.class);
            // get the corresponding template with templateId
            String templateId = contractBean.getContractTemplateId();
            Optional<ContractTemplateBean> templateBean = contractTemplateBeanRepository.findByContractTemplateId(templateId);
            // add the default template content to the contractBean
            if (templateBean.isPresent()) {
                DealBean templateDealBean = templateBean.get().getTrades().get(0).getDeals().get(0);
                List<TradeBean> trades = new ArrayList<>();
                for (TradeBean tradeBean : contractBean.getTrades()) {
                    List<DealBean> deals = new ArrayList<>();
                    for (DealBean dealBean : tradeBean.getDeals()) {
                        dealBean.setContractState(templateDealBean.getContractState());
                        dealBean.setEvents(templateDealBean.getEvents());
                        dealBean.setObservations(templateDealBean.getObservations());
                        deals.add(dealBean);
                    }
                    tradeBean.setDeals(deals);
                    trades.add(tradeBean);
                }
                contractBean.setTrades(trades);
                contractBean.setFunctions(templateBean.get().getFunctions());
                contractBean.setInstructions(templateBean.get().getInstructions());
                contractBean.setCreateTime(new Date());
            } else {
                log.error("Failed to get the contract template bean with templateId: {}!", templateId);
                return -1;
            }
            contractBeanRepository.save(contractBean);
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Failed to save the contract bean!");
            return -1;
        }
    }

//    public static void main(String[] args) {
//        String msgBody = "{\"basicInfo\":{\"contractName\":\"20230801-JNK-MNNSB003-01\",\"contractNo\":\"20230801-JNK-MNNSB003-01\",\"contractWeight\":200000.0,\"direction\":0,\"effectiveFromDate\":\"\",\"effectiveToDate\":\"9999-12-31\",\"futuresContract\":\"SM403.CZC,SM404.CZC,SM405.CZC,SM406.CZC,SM407.CZC,SM408.CZC,SM409.CZC,SM410.CZC,SM411.CZC,SM412.CZC\",\"logicContractCode\":\"20230801-JNK-MNNSB003\",\"multiFixing\":false,\"settlePricingEnd\":\"2024-08-01 00:00:00\",\"signAddress\":\"上海市虹口区\",\"signDate\":\"2023-08-01\",\"varietyId\":\"13\"},\"contractTemplateId\":\"65b0b7636b8cb80a1102185b\",\"deleted\":false,\"parties\":[{\"address\":\"XXX\",\"bankAccountName\":\"银河德睿资本管理有限公司\",\"bankAccountNo\":\"121912493410501\",\"bankBranchName\":\"中国工商银行股份有限公司新余新城支行\",\"contact\":\"XX\",\"id\":\"1\",\"name\":\"银河德睿资本管理有限公司\",\"partyId\":\"1\",\"telephone\":\"15000000000\",\"wechat\":\"15000000000\"},{\"address\":\"\",\"contact\":\"\",\"id\":\"2983\",\"name\":\"嘉能可有限公司\",\"partyId\":\"2983\",\"telephone\":\"\",\"wechat\":\"\"}],\"trades\":[{\"deals\":[{\"dealId\":\"20230801-JNK-MNNSB003-01\",\"dealInfo\":{\"margin\":{\"detailList\":[],\"isAutoRelease\":false,\"isInitialMargin\":false,\"isMarkToMarket\":false,\"isPerformance\":false,\"isPerformanceMarkToMarket\":false,\"itemList\":[],\"objectId\":\"2983\",\"riskAddMarginRatio\":10.0,\"status\":1,\"subjectId\":\"1\"},\"riskMarginRatio\":10.0,\"indexClosePrice\":6778.0,\"paymentInfo\":{\"paymentOrder\":0,\"ratioDownPayment\":100.0,\"ratioPaymentAfterInspection\":0.0,\"ratioPaymentAfterInvoice\":0.0},\"basicInfo\":{\"$ref\":\"$.basicInfo\"}},\"dealType\":\"accumulator\",\"effectiveDate\":{\"required\":true,\"value\":\"9999-12-31\"},\"legs\":[{\"basis\":0.0,\"fixingPrice\":0.0,\"goodsCode\":\"1\",\"goodsId\":\"22053\",\"name\":\"physicalLeg\",\"payer\":\"2983\",\"receiver\":\"1\",\"referenceInstrument\":\"SM403.CZC,SM404.CZC,SM405.CZC,SM406.CZC,SM407.CZC,SM408.CZC,SM409.CZC,SM410.CZC,SM411.CZC,SM412.CZC\",\"resource\":{\"category\":\"24\",\"deliveryPoint\":\"\",\"quantity\":{\"cap\":200000.0,\"floor\":0.0},\"quantityUnit\":\"T\",\"resourceType\":1,\"variety\":\"13\"},\"sequence\":\"1\",\"settlementAmount\":\"\",\"spread\":0.0,\"taxPercentage\":0.13,\"type\":\"physicalLeg\",\"unitPrice\":0.0,\"weight\":200000.0},{\"name\":\"cashLeg\",\"payer\":\"1\",\"receiver\":\"2983\",\"referenceInstrument\":\"SM403.CZC,SM404.CZC,SM405.CZC,SM406.CZC,SM407.CZC,SM408.CZC,SM409.CZC,SM410.CZC,SM411.CZC,SM412.CZC\",\"sequence\":\"2\",\"settlementAmount\":\"\",\"type\":\"cashLeg\"},{\"name\":\"invoiceLeg\",\"payer\":\"2983\",\"receiver\":\"1\",\"sequence\":\"3\",\"type\":\"invoiceLeg\"}],\"settlementCurrency\":\"CNY\",\"tradeId\":\"20230801-JNK-MNNSB003-01\"}],\"tradeId\":\"20230801-JNK-MNNSB003-01\",\"tradeType\":\"\"}],\"version\":0}";
//        saveContractBean(msgBody);
//    }


    private MessageProcessResponse processEvent(String msgBody) {
        JSONObject dslParam = new JSONObject();
        JSONObject msgObj = JSONObject.parseObject(msgBody);
        JSONObject eventParams = (JSONObject) msgObj.get("paramHash");
        String eventName = msgObj.getString("eventName");
        String contractCode = msgObj.getString("contractCode");
        String tradeId = msgObj.getString("tradeId");
        String dealId = msgObj.getString("dealId");
        log.info("msgBody: {}", msgObj);
        Optional<ContractBean> optionalContractBean = contractBeanRepository.findTopContractBeanByBasicInfo_ContractNoOrderByCreateTimeDesc(contractCode);
        if (optionalContractBean.isPresent()) {
            ContractBean contractBean = optionalContractBean.get();
            TradeBean tradeBean = contractBean.getTrades().stream()
                    .filter(trade -> trade.getTradeId().equals(tradeId))
                    .findAny().orElse(null);
            if (tradeBean != null) {
                DealBean dealBean = tradeBean.getDeals().stream()
                        .filter(deal -> deal.getDealId().equals(dealId))
                        .findAny().orElse(null);
                if (dealBean != null) {
                    EventBean eventBean = dealBean.getEvents().stream()
                            .filter(event -> event.getName().equals(eventName))
                            .findAny().orElse(null);
                    JSONObject internalParamBean = Objects.requireNonNull(eventBean).getInternalParams();
                    JSONObject internalParams = new JSONObject();
                    ObjectMapper objectMapper = new ObjectMapper();
                    try {
                        Object contractPathObj = Configuration.defaultConfiguration().jsonProvider()
                                .parse(objectMapper.writeValueAsString(contractBean));
                        for (String key : internalParamBean.keySet()) {
                            internalParams.put(key, JsonPath.read(contractPathObj, internalParamBean.getString(key)));
                        }
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }

                    dslParam.put("internalParams", internalParams);
                    dslParam.put("externalParams", eventParams);
                    dslParam.put("variables", dealBean.getContractState().getVariables());
                    dslParam.put("script", Objects.requireNonNull(eventBean).getHandler().getScript());
                    dslParam.put("functions", contractBean.getFunctions());
//                    log.info("dslParam: {}", dslParam);
                } else {
                    log.error("dealBean null");
                }
            } else {
                log.error("tradeBean null");
            }
        }
        JSONObject dslResult = runPyScript(dslParam);
        log.info("dslResult: {}", dslResult);

        DslHistory dslHistoryRecord = new DslHistory();
        dslHistoryRecord.setDslParam(String.valueOf(dslParam));
        dslHistoryRecord.setEventNo(eventParams.getString("eventNo"));
        dslHistoryRecord.setEventName(eventName);
        dslHistoryRecord.setContractCode(contractCode);
        dslHistoryRecord.setLogicContractCode(msgObj.getString("logicContractCode"));
        dslHistoryRecord.setTradeId(tradeId);
        dslHistoryRecord.setDealId(dealId);
        dslHistoryRecord.setCreateTime(new Date());
        dslHistoryRecord.setDslResult(String.valueOf(dslResult));
//        dslHistoryRepository.save(dslHistoryRecord);

        return null;
    }


}
