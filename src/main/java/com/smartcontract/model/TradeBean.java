package com.smartcontract.model;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class TradeBean {
    private String childId;

    private String parentId;

    private String tradeId;
    
    private String tradeType;
    
    private JSONObject tradeInfo;
    
    private List<ObservationBean> observations;
    
    private List<EventBean> events;
    
    private ContractStateBean contractState;
    
    private List<DealBean> deals;
}
