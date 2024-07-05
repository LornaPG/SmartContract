package com.smartcontract.service;

import com.smartcontract.model.ContractBean;

public interface ContractBeanService {

    Integer insert(ContractBean contractBean);

    Integer removeFromContractBean(String contractCode);

    ContractBean getLatestContractBeanByContractCode(String contractCode);
}
