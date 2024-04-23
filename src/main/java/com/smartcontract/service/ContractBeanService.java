package com.smartcontract.service;

import com.smartcontract.model.ContractBean;
import com.smartcontract.repository.ContractBeanRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class ContractBeanService {
    private final ContractBeanRepository repository;

    @Autowired
    public ContractBeanService(ContractBeanRepository repository) {
        this.repository = repository;
    }

    public Integer save(ContractBean contractBean) {
        if (contractBean == null) {
            log.error("contractBean is null!");
            return -1;
        }
        repository.save(contractBean);
        return 1;
    }

    public ContractBean getLatestContractBeanByContractCode(String contractCode) {
        Optional<ContractBean> optionalContractBean = repository
                .findTopContractBeanByBasicInfo_ContractNoOrderByCreateTimeDesc(contractCode);
        return optionalContractBean.orElse(null);
    }
}
