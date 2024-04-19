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
import com.smartcontract.model.ReturnParamBean;
import com.smartcontract.model.TradeBean;
import com.smartcontract.repository.ContractBeanRepository;
import com.smartcontract.repository.ContractTemplateBeanRepository;
import com.smartcontract.repository.DslHistoryRepository;
import com.smartcontract.repository.MessageRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.groovy.parser.antlr4.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.smartcontract.service.ScriptProcessHelper.runPyScript;
import static com.smartcontract.util.CustomObjectMapper.createObjectMapper;

@Service
@Slf4j
public class MessageServiceImpl implements MessageService{

    private final MessageRepository messageRepository;

    private final ContractBeanRepository contractBeanRepository;

    private final ContractTemplateBeanRepository contractTemplateBeanRepository;

    private final DslHistoryRepository dslHistoryRepository;

    private final ObjectMapper objectMapper = createObjectMapper();

    private static final String TEST_STRING = "{\n" +
            "  \"dslResult\": {\n" +
            "    \"contractNo\": \"20240311-BDFZ-EGNSB005-01\",\n" +
            "    \"dealId\": \"20240311-BDFZ-EGNSB005-01\",\n" +
            "    \"tradeId\": \"20240311-BDFZ-EGNSB005-01\"\n" +
            "  },\n" +
            "  \"internalParams\": {\n" +
            "    \"variables\": {\n" +
            "      \"marginObservationCount\": 0.0,\n" +
            "      \"feeDetailBatchCount\": 0.0,\n" +
            "      \"priceObjects\": [\n" +
            "        {\n" +
            "          \"batchNo\": \"1\",\n" +
            "          \"variationMarginData\": [\n" +
            "            {\n" +
            "              \"isFirst\": 1.0,\n" +
            "              \"markContractCodeType\": 1.0,\n" +
            "              \"dateRange\": {\n" +
            "                \"dateList\": [\n" +
            "                  \"currentDate()\"\n" +
            "                ]\n" +
            "              },\n" +
            "              \"goodsMarginToSettle\": 0.0,\n" +
            "              \"marginBatchNo\": \"0\"\n" +
            "            }\n" +
            "          ],\n" +
            "          \"objectId\": \"2\"\n" +
            "        }\n" +
            "      ],\n" +
            "      \"priceObjectCount\": 1.0\n" +
            "    },\n" +
            "    \"paymentInfo\": {\n" +
            "      \"ratioPaymentAfterInspection\": 0.0,\n" +
            "      \"ratioDownPayment\": 90.0,\n" +
            "      \"paymentOrder\": 1,\n" +
            "      \"ratioPaymentAfterInvoice\": 10.0,\n" +
            "      \"selectExchangeType\": -1,\n" +
            "      \"paymentMethod\": \"0\"\n" +
            "    }\n" +
            "  },\n" +
            "  \"script\": [\n" +
            "    \"// import sum\",\n" +
            "    \"def invoiceLeg = internalParams.legs.find { it.type.equals('invoiceLeg') }\",\n" +
            "    \"def strPayBatch = ''\",\n" +
            "    \"if(invoiceLeg != null) {\",\n" +
            "    \"  def idx = variables.settlementBatches.findIndexOf({it -> it.batchNo.equals(externalParams.settlementBatchNo)})\",\n" +
            "    \"  if(idx >= 0) {\",\n" +
            "    \"    if (externalParams.payerId.equals(invoiceLeg.payer)) {\",\n" +
            "    \"      variables.settlementBatches[idx].invoiceWeight += externalParams.invoiceWeight\",\n" +
            "    \"      variables.settlementBatches[idx].invoiceAmount += externalParams.invoiceAmount\",\n" +
            "    \"    } else {\",\n" +
            "    \"      variables.settlementBatches[idx].invoiceWeight -= externalParams.invoiceWeight\",\n" +
            "    \"      variables.settlementBatches[idx].invoiceAmount -= externalParams.invoiceAmount\",\n" +
            "    \"    }\",\n" +
            "    \"    if(variables.settlementBatches[idx].isSell == 0) { // only purchase contract has invoice subject\",\n" +
            "    \"      def idxPayInvoice = variables.paymentBatches.findIndexOf({it -> (it.settlementBatchNo.equals(externalParams.settlementBatchNo) && it.paymentType == 1)})\",\n" +
            "    \"      strPayBatch = variables.paymentBatches[idxPayInvoice].batchNo\",\n" +
            "    \"      variables.paymentBatches[idxPayInvoice].weightExpected += externalParams.invoiceWeight\",\n" +
            "    \"      def newExpCash = externalParams.invoiceWeight * variables.settlementBatches[idx].settlementPrice * internalParams.paymentInfo.ratioPaymentAfterInvoice / 100\",\n" +
            "    \"      variables.paymentBatches[idxPayInvoice].cashExpected = (variables.paymentBatches[idxPayInvoice].cashExpected + newExpCash).round(2)\",\n" +
            "    \"      variables.settlementBatches[idx].expectedCashAfterInvoice = variables.paymentBatches[idxPayInvoice].cashExpected\",\n" +
            "    \"    }\",\n" +
            "    \"  }\",\n" +
            "    \"}\",\n" +
            "    \"if(instructionPipeline.indexOf('invoiceAcknowledgmentInstruction') < 0) {\",\n" +
            "    \"  instructionPipeline.add('invoiceAcknowledgmentInstruction')\",\n" +
            "    \"}\",\n" +
            "    \"dslResult = [\",\n" +
            "    \"  eventNo: externalParams.eventNo,\",\n" +
            "    \"  settlementBatchNo: externalParams.settlementBatchNo,\",\n" +
            "    \"  paymentBatchNo: strPayBatch,\",\n" +
            "    \"  tradeId: externalParams.tradeId,\",\n" +
            "    \"  dealId: externalParams.dealId,\",\n" +
            "    \"  status: sum(0,1)\",\n" +
            "    \"]\"\n" +
            "  ]\n" +
            "}\n";

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
        if (msgTitle == null) {
            String resMsg = String.format("Invalid message title: %s!", message.getTitle());
            log.error(resMsg);
            response.setMessageUuid(message.getMessageUuid());
            response.setResCode(-1);
            response.setResMsg(resMsg);
            return response;
        }
        switch (msgTitle) {
            case UPLOAD_CONTENT:
                try {
                    response = uploadContent(message);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
                break;
            case EVENT:
                try {
                    response = processEvent(message);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
                break;
            default:
                break;
        }
        return response;
    }

    private MessageProcessResponse uploadContent(Message message) throws JsonProcessingException {
        MessageProcessResponse response = new MessageProcessResponse();
        String msgBody = message.getBody();
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
            String resMsg = String.format("Failed to get the contract template bean with templateId: %s!", templateId);
            log.error(resMsg);
            response.setMessageUuid(message.getMessageUuid());
            response.setResCode(-1);
            response.setResMsg(resMsg);
            return response;
        }
        contractBeanRepository.save(contractBean);
        String resMsg = String.format("Successfully upload the content with contractNo: %s!",
                contractBean.getBasicInfo().getContractNo());
        response.setMessageUuid(message.getMessageUuid());
        response.setResCode(1);
        response.setResMsg(resMsg);
        return response;
    }

//    public static void main(String[] args) {
//        String msgBody = "{\"basicInfo\":{\"contractName\":\"20230801-JNK-MNNSB003-01\",\"contractNo\":\"20230801-JNK-MNNSB003-01\",\"contractWeight\":200000.0,\"direction\":0,\"effectiveFromDate\":\"\",\"effectiveToDate\":\"9999-12-31\",\"futuresContract\":\"SM403.CZC,SM404.CZC,SM405.CZC,SM406.CZC,SM407.CZC,SM408.CZC,SM409.CZC,SM410.CZC,SM411.CZC,SM412.CZC\",\"logicContractCode\":\"20230801-JNK-MNNSB003\",\"multiFixing\":false,\"settlePricingEnd\":\"2024-08-01 00:00:00\",\"signAddress\":\"上海市虹口区\",\"signDate\":\"2023-08-01\",\"varietyId\":\"13\"},\"contractTemplateId\":\"65b0b7636b8cb80a1102185b\",\"deleted\":false,\"parties\":[{\"address\":\"XXX\",\"bankAccountName\":\"银河德睿资本管理有限公司\",\"bankAccountNo\":\"121912493410501\",\"bankBranchName\":\"中国工商银行股份有限公司新余新城支行\",\"contact\":\"XX\",\"id\":\"1\",\"name\":\"银河德睿资本管理有限公司\",\"partyId\":\"1\",\"telephone\":\"15000000000\",\"wechat\":\"15000000000\"},{\"address\":\"\",\"contact\":\"\",\"id\":\"2983\",\"name\":\"嘉能可有限公司\",\"partyId\":\"2983\",\"telephone\":\"\",\"wechat\":\"\"}],\"trades\":[{\"deals\":[{\"dealId\":\"20230801-JNK-MNNSB003-01\",\"dealInfo\":{\"margin\":{\"detailList\":[],\"isAutoRelease\":false,\"isInitialMargin\":false,\"isMarkToMarket\":false,\"isPerformance\":false,\"isPerformanceMarkToMarket\":false,\"itemList\":[],\"objectId\":\"2983\",\"riskAddMarginRatio\":10.0,\"status\":1,\"subjectId\":\"1\"},\"riskMarginRatio\":10.0,\"indexClosePrice\":6778.0,\"paymentInfo\":{\"paymentOrder\":0,\"ratioDownPayment\":100.0,\"ratioPaymentAfterInspection\":0.0,\"ratioPaymentAfterInvoice\":0.0},\"basicInfo\":{\"$ref\":\"$.basicInfo\"}},\"dealType\":\"accumulator\",\"effectiveDate\":{\"required\":true,\"value\":\"9999-12-31\"},\"legs\":[{\"basis\":0.0,\"fixingPrice\":0.0,\"goodsCode\":\"1\",\"goodsId\":\"22053\",\"name\":\"physicalLeg\",\"payer\":\"2983\",\"receiver\":\"1\",\"referenceInstrument\":\"SM403.CZC,SM404.CZC,SM405.CZC,SM406.CZC,SM407.CZC,SM408.CZC,SM409.CZC,SM410.CZC,SM411.CZC,SM412.CZC\",\"resource\":{\"category\":\"24\",\"deliveryPoint\":\"\",\"quantity\":{\"cap\":200000.0,\"floor\":0.0},\"quantityUnit\":\"T\",\"resourceType\":1,\"variety\":\"13\"},\"sequence\":\"1\",\"settlementAmount\":\"\",\"spread\":0.0,\"taxPercentage\":0.13,\"type\":\"physicalLeg\",\"unitPrice\":0.0,\"weight\":200000.0},{\"name\":\"cashLeg\",\"payer\":\"1\",\"receiver\":\"2983\",\"referenceInstrument\":\"SM403.CZC,SM404.CZC,SM405.CZC,SM406.CZC,SM407.CZC,SM408.CZC,SM409.CZC,SM410.CZC,SM411.CZC,SM412.CZC\",\"sequence\":\"2\",\"settlementAmount\":\"\",\"type\":\"cashLeg\"},{\"name\":\"invoiceLeg\",\"payer\":\"2983\",\"receiver\":\"1\",\"sequence\":\"3\",\"type\":\"invoiceLeg\"}],\"settlementCurrency\":\"CNY\",\"tradeId\":\"20230801-JNK-MNNSB003-01\"}],\"tradeId\":\"20230801-JNK-MNNSB003-01\",\"tradeType\":\"\"}],\"version\":0}";
//        saveContractBean(msgBody);
//    }


    private MessageProcessResponse processEvent(Message message) throws JsonProcessingException {
        MessageProcessResponse response = new MessageProcessResponse();
        String msgBody = message.getBody();
        JSONObject dslParam = new JSONObject();
        JSONObject msgObj = JSONObject.parseObject(msgBody);
        JSONObject eventParams = (JSONObject) msgObj.get("paramHash");
        String eventName = msgObj.getString("eventName");
        String contractCode = msgObj.getString("contractCode");
        String tradeId = msgObj.getString("tradeId");
        String dealId = msgObj.getString("dealId");
        log.info("msgBody: {}", msgObj);

        Optional<ContractBean> optionalContractBean = contractBeanRepository
                .findTopContractBeanByBasicInfo_ContractNoOrderByCreateTimeDesc(contractCode);
        if (!optionalContractBean.isPresent()) {
            String resMsg = String.format("No contract bean found for contractCode %s", contractCode);
            log.error(resMsg);
            response.setMessageUuid(message.getMessageUuid());
            response.setResCode(-1);
            response.setResMsg(resMsg);
            return response;
        }
        ContractBean contractBean = optionalContractBean.get();

        TradeBean tradeBean = contractBean.getTrades().stream()
                .filter(trade -> trade.getTradeId().equals(tradeId))
                .findAny().orElse(null);
        if (tradeBean == null) {
            String resMsg = String.format("No trade bean found for tradeId %s", tradeId);
            log.error(resMsg);
            response.setMessageUuid(message.getMessageUuid());
            response.setResCode(-1);
            response.setResMsg(resMsg);
            return response;
        }

        DealBean dealBean = tradeBean.getDeals().stream()
                .filter(deal -> deal.getDealId().equals(dealId))
                .findAny().orElse(null);
        if (dealBean == null) {
            String resMsg = String.format("No deal bean found for dealId %s", dealId);
            log.error(resMsg);
            response.setMessageUuid(message.getMessageUuid());
            response.setResCode(-1);
            response.setResMsg(resMsg);
            return response;
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
        dslParam.put("externalParams", eventParams);
        dslParam.put("script", Objects.requireNonNull(eventBean).getHandler().getScript());
        dslParam.put("functions", contractBean.getFunctions());
//                    log.info("dslParam: {}", dslParam);

//        JSONObject dslResult = runPyScript(dslParam);
        JSONObject dslResult = objectMapper.readValue(TEST_STRING, JSONObject.class);
//        log.info("dslResult: {}", dslResult);

        // Save the dslHistory record with dslParam and dslResult from python script
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
                Optional<ContractBean> toContractCodeBeanOptional = contractBeanRepository
                        .findTopContractBeanByBasicInfo_ContractNoOrderByCreateTimeDesc(toContractCode);
                if (!toContractCodeBeanOptional.isPresent()) {
                    String resMsg = String.format("No contract bean found for toContractCode %s", toContractCode);
                    log.error(resMsg);
                    response.setMessageUuid(message.getMessageUuid());
                    response.setResCode(-1);
                    response.setResMsg(resMsg);
                    return response;
                }
                toContractCodeBeanJson = objectMapper.writeValueAsString(toContractCodeBeanOptional.get());
            } else {
                toContractCodeBeanJson = contractBeanJson;
            }
            Object fromVariables = ((HashMap<?, ?>) dslResult.get("internalParams")).get(from);
            String updatedToContractCodeBeanJson = JsonPath.parse(toContractCodeBeanJson).set(to, fromVariables)
                    .jsonString();
            ContractBean updatedContractBean = objectMapper.readValue(updatedToContractCodeBeanJson,
                    ContractBean.class);
            log.info("updatedContractBean: {}", updatedContractBean.getTrades().get(0).getDeals().get(0).getContractState().getVariables());
//            contractBeanRepository.save(updatedContractBean);
        }
        response.setMessageUuid(message.getMessageUuid());
        response.setResCode(1);
        String msg = String.format("Successfully processed the event %s", eventParams.getString("eventNo"));
        response.setResMsg(msg);
        return response;
    }
}
