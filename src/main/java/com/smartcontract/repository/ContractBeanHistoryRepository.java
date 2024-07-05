package com.smartcontract.repository;

import com.smartcontract.model.ContractBeanHistory;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ContractBeanHistoryRepository extends MongoRepository<ContractBeanHistory, ObjectId> {
}
