package com.smartcontract.model;

import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "contractBeanHistory")
@EqualsAndHashCode(callSuper = true)
public class ContractBeanHistory extends ContractBean {
}
