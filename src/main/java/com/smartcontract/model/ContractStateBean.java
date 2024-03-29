package com.smartcontract.model;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class ContractStateBean {

    private String currentState;

    private List<String> state;

    private JSONObject variables;
}
