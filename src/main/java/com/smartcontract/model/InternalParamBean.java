package com.smartcontract.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class InternalParamBean {
    private String basicInfo;

    private String dealType;

    private String indexClosePrice;

    private String legs;

    private String margin;

    private String paymentInfo;

    private String variables;
}
