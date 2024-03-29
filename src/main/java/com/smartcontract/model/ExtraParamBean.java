package com.smartcontract.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ExtraParamBean {
    private String currentDate;

    private String priceObjectNo;

    private ScenarioBean scenario;
}
