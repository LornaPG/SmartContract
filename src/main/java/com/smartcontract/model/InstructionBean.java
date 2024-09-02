package com.smartcontract.model;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class InstructionBean {
    private String type;
    
    private JSONObject params;
}
