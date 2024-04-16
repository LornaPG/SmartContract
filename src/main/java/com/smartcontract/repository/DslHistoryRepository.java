package com.smartcontract.repository;

import com.smartcontract.model.DslHistory;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DslHistoryRepository extends MongoRepository<DslHistory, ObjectId> {
}
