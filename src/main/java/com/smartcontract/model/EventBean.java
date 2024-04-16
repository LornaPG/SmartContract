package com.smartcontract.model;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class EventBean {
    private String name;
    
    private List<String> ruleScript;

    private JSONObject externalParams;

    private JSONObject extraParams;

    private JSONObject internalParams;

    private List<ReturnParamBean> returnParams;

    private EventHandlerBean handler;
}
