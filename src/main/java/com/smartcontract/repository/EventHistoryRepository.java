package com.smartcontract.repository;

import com.smartcontract.model.EventHistory;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface EventHistoryRepository extends MongoRepository<EventHistory, ObjectId> {
}
