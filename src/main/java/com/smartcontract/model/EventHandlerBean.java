package com.smartcontract.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@Deprecated
public class EventHandlerBean {

    private String requiredState;

    private List<String> script;
}
