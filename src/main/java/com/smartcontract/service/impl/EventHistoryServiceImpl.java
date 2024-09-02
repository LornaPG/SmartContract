package com.smartcontract.service.impl;

import com.smartcontract.model.EventHistory;
import com.smartcontract.repository.EventHistoryRepository;
import com.smartcontract.service.EventHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EventHistoryServiceImpl implements EventHistoryService {
    private final EventHistoryRepository repository;

    @Autowired
    public EventHistoryServiceImpl(EventHistoryRepository repository) {
        this.repository = repository;
    }

    public Integer save(EventHistory eventHistory) {
        if (eventHistory == null) {
            log.error("eventHistory is null!");
            return -1;
        }
        repository.save(eventHistory);
        return 1;
    }
}
