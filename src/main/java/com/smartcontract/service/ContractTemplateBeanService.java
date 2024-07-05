package com.smartcontract.service;

import com.smartcontract.model.AddContractTemplateResponse;
import com.smartcontract.model.ContractTemplateBean;

public interface ContractTemplateBeanService {
    ContractTemplateBean getContractTemplateBeanByTemplateId(String contractTemplateId);

    AddContractTemplateResponse addTemplate(ContractTemplateBean contractTemplate);
}
