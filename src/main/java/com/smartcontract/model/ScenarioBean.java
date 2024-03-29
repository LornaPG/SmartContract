package com.smartcontract.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ScenarioBean {
    private String price;

    private String reference;

    private String referenceInstrument;

    private Integer type;
}
