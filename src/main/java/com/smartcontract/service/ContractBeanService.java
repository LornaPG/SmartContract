package com.smartcontract.service;

import com.smartcontract.model.ContractBean;
import com.smartcontract.repository.ContractBeanHistoryRepository;
import com.smartcontract.repository.ContractBeanRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.groovy.parser.antlr4.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class ContractBeanService {
    private final ContractBeanRepository repository;

    private final ContractBeanHistoryRepository historyRepository;

    @Autowired
    public ContractBeanService(ContractBeanRepository repository, ContractBeanHistoryRepository historyRepository) {
        this.repository = repository;
        this.historyRepository = historyRepository;
    }

    public Integer insert(ContractBean contractBean) {
        if (contractBean == null || contractBean.getBasicInfo() == null
                || StringUtils.isEmpty(contractBean.getBasicInfo().getContractNo())) {
            log.error("contractBean, basicInfo, or contractNo is null!");
            return -1;
        }
        // if there is an existing contractBean with same contractCode, move the existing one to contractBeanHistory.
        String contractCode = contractBean.getBasicInfo().getContractNo();
        Optional<ContractBean> existingContractBeanOptional = repository
                .findTopContractBeanByBasicInfo_ContractNo(contractCode);
        if (existingContractBeanOptional.isPresent()) {
            ContractBean existingContractBean = existingContractBeanOptional.get();
            historyRepository.insert(existingContractBean);
            repository.delete(existingContractBean);
        }
        repository.insert(contractBean);
        return 1;
    }

    public ContractBean getLatestContractBeanByContractCode(String contractCode) {
        Optional<ContractBean> optionalContractBean = repository
                .findTopContractBeanByBasicInfo_ContractNo(contractCode);
        return optionalContractBean.orElse(null);
    }
}
