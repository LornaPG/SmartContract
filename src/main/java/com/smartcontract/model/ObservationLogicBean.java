package com.smartcontract.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ObservationLogicBean {

    private String requiredState;

    private List<String> script;
}
