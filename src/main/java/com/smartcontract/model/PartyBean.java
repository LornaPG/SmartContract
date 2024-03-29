package com.smartcontract.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PartyBean {
    private String id;

    private String partyId;
    
    private String name;
    
    private String contact;
    
    private String telephone;
    
    private String wechat;
    
    private String email;
    
    private String address;
    
    private String bankAccountNo;
    
    private String bankAccountName;
    
    private String bankBranchName;
}
