package com.smartcontract.service;

import com.smartcontract.model.ContractTemplateBean;
import com.smartcontract.repository.ContractTemplateBeanRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class ContractTemplateBeanService {

    private final ContractTemplateBeanRepository repository;

    @Autowired
    public ContractTemplateBeanService(ContractTemplateBeanRepository repository) {
        this.repository = repository;
    }

    public ContractTemplateBean getContractTemplateBeanByTemplateId(String contractTemplateId) {
        Optional<ContractTemplateBean> optionalContractTemplateBean = repository
                .findByContractTemplateId(contractTemplateId);
        return optionalContractTemplateBean.orElse(null);
    }
}
