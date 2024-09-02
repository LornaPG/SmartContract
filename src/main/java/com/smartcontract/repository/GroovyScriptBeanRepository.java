package com.smartcontract.repository;

import com.smartcontract.model.GroovyScriptBean;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface GroovyScriptBeanRepository extends MongoRepository<GroovyScriptBean, ObjectId> {

    Optional<GroovyScriptBean> findTopByScriptNameOrderByCreateTimeDesc(String scriptName);
}
