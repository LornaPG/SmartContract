package com.smartcontract.service;

import com.smartcontract.model.DslHistory;
import com.smartcontract.repository.DslHistoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DslHistoryService {
    private final DslHistoryRepository repository;

    @Autowired
    public DslHistoryService(DslHistoryRepository repository) {
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
