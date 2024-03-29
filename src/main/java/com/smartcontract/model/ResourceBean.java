package com.smartcontract.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ResourceBean {
    private Integer resourceType;
    
    private String variety;
    
    private String category;
    
    private QuantityBean quantity;
    
    private String quantityUnit;
    
    private String deliveryPoint;
}
