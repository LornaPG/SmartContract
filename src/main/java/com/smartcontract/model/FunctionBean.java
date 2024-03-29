package com.smartcontract.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class FunctionBean {
    private String functionName;

    private List<String> script;
}
