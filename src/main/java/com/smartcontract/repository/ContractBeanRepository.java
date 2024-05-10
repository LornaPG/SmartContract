package com.smartcontract.repository;

import com.smartcontract.model.ContractBean;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ContractBeanRepository extends MongoRepository<ContractBean, ObjectId> {

    Optional<ContractBean> findTopContractBeanByBasicInfo_ContractNo(String contractNo);
}
