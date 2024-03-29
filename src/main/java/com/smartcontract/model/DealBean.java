package com.smartcontract.model;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class DealBean {
    private String tradeId;
    
    private String dealId;
    
    private String dealType;
    
    private JSONObject dealInfo;
    
    private ContractStateBean contractState;
    
    private DateBean effectiveDate;
    
    private DateBean terminationDate;
    
    private String settlementCurrency;
    
    private List<LegBean> legs;
    
    private List<ObservationBean> observations;
    
    private List<EventBean> events;
}
