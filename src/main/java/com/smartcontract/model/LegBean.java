package com.smartcontract.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LegBean {
    private String sequence;
    
    private String goodsId;
    
    private String goodsCode;
    
    private String type;
    
    private String name;
    
    private String payer;
    
    private String receiver;

    private ResourceBean resource;
    
    private Double basis;
    
    private Double spread;
    
    private Double weight;
    
    private Double unitPrice;
    
    private Double fixingPrice;
    
    private String referenceInstrument;
    
    private String settlementAmount;

    private Double taxPercentage;
}
