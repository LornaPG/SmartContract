package com.smartcontract.service.impl;

import com.smartcontract.model.AddContractTemplateResponse;
import com.smartcontract.model.ContractTemplateBean;
import com.smartcontract.repository.ContractTemplateBeanRepository;
import com.smartcontract.service.ContractTemplateBeanService;
import lombok.extern.slf4j.Slf4j;
import org.apache.groovy.parser.antlr4.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class ContractTemplateBeanServiceImpl implements ContractTemplateBeanService {
    private final ContractTemplateBeanRepository repository;

    @Autowired
    public ContractTemplateBeanServiceImpl(ContractTemplateBeanRepository repository) {
        this.repository = repository;
    }

    @Override
    public ContractTemplateBean getContractTemplateBeanByTemplateId(String contractTemplateId) {
        if (StringUtils.isEmpty(contractTemplateId)) {
            return null;
        }
        Optional<ContractTemplateBean> optionalContractTemplateBean = repository
                .findByContractTemplateId(contractTemplateId);
        return optionalContractTemplateBean.orElse(null);
    }

    @Override
    public AddContractTemplateResponse addTemplate(ContractTemplateBean contractTemplate) {
        AddContractTemplateResponse response = new AddContractTemplateResponse();
        if (contractTemplate == null) {
            response.setCode(-1);
            response.setMsg("Input contract template is null!");
            response.setTemplateId("");
            return response;
        }
        ContractTemplateBean newTemplate = repository.insert(contractTemplate);
        response.setTemplateId(newTemplate.get_id().toString());
        response.setMsg("Successfully add the template!");
        response.setCode(1);
        return response;
    }
}
