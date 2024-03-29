package com.smartcontract.repository;

import com.smartcontract.model.ContractTemplateBean;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ContractTemplateBeanRepository extends MongoRepository<ContractTemplateBean, ObjectId> {
    Optional<ContractTemplateBean> findByContractTemplateId(String contractTemplateId);
}
