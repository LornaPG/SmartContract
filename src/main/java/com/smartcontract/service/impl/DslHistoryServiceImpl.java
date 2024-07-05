package com.smartcontract.service.impl;

import com.smartcontract.model.DslHistory;
import com.smartcontract.repository.DslHistoryRepository;
import com.smartcontract.service.DslHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DslHistoryServiceImpl implements DslHistoryService {
    private final DslHistoryRepository repository;

    @Autowired
    public DslHistoryServiceImpl(DslHistoryRepository repository) {
        this.repository = repository;
    }

    public Integer save(DslHistory dslHistory) {
        if (dslHistory == null) {
            log.error("dslHistory is null!");
            return -1;
        }
        repository.save(dslHistory);
        return 1;
    }
}
