package com.smartcontract.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class BasicInfoBean {

    private String contractName;

    private String logicContractCode;

    private String contractNo;

    private String parentRevisionNumber;

    private String revisionNumber;
    
    private String snapshotRepository;
    
    private Boolean lastVersion;
    
    private String signAddress;
    
    private String signDate;
    
    private String effectiveFromDate;
    
    private String effectiveToDate;

    // ===============均价字段================
    private String pricingPeriodStart;

    private String priceEndDate;
    
    private List<String> pricingPeriodDates;
    
    private Integer pricingContractType;

    private String pricingContract;

    private Boolean multiFixing;
    
    private String settlePricingEnd;
    
    private String optionExpirationDate;
    
    private String futuresContract;
    
    private Double admissionPrice;
    
    private Double exercisePrice;
    
    private Double isExerciseWhenEqual;
    
    private Double isCompensationTaxIncluded;

    // 累沽累购新增字段
    private String varietyId;

    private Double contractWeight;
    
    private Double contractBatches;
    
    private Double contractEntrustPrice;

    // ===========买断式回购和代采=============
    //方向 0 买入 1 卖出
    private Integer direction;
}
