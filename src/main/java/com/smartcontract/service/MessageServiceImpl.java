package com.smartcontract.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcontract.MessageTitle;
import com.smartcontract.model.ContractBean;
import com.smartcontract.model.ContractTemplateBean;
import com.smartcontract.model.DealBean;
import com.smartcontract.model.Message;
import com.smartcontract.model.MessageProcessResponse;
import com.smartcontract.model.TradeBean;
import com.smartcontract.repository.ContractBeanRepository;
import com.smartcontract.repository.ContractTemplateBeanRepository;
import com.smartcontract.repository.MessageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class MessageServiceImpl implements MessageService{

    private final MessageRepository messageRepository;

    private final ContractBeanRepository contractBeanRepository;

    private final ContractTemplateBeanRepository contractTemplateBeanRepository;

    @Autowired
    public MessageServiceImpl(MessageRepository messageRepository, ContractBeanRepository contractBeanRepository,
                              ContractTemplateBeanRepository contractTemplateBeanRepository) {
        this.messageRepository = messageRepository;
        this.contractBeanRepository = contractBeanRepository;
        this.contractTemplateBeanRepository = contractTemplateBeanRepository;
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
                        response.setStatus(resCode);
                        response.setMessageUuid(message.getMessageUuid());
                        response.setErrMsg("");
                    } else {
                        response.setStatus(-1);
                        response.setMessageUuid(message.getMessageUuid());
                        response.setErrMsg("Failed to upload the contract content!");
                    }
                    break;
                case EVENT:
                    return processEvent(message);

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
            System.out.println("contractNo: " + contractBean.getTrades());
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


    private MessageProcessResponse processEvent(Message message) {
        return null;
    }


}
