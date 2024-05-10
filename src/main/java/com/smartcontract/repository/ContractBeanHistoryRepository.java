package com.smartcontract.repository;

import com.smartcontract.model.ContractBean;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ContractBeanHistoryRepository extends MongoRepository<ContractBean, ObjectId> {
}
