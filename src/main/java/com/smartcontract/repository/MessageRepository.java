package com.smartcontract.repository;

import com.smartcontract.model.Message;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface MessageRepository extends MongoRepository<Message, ObjectId> {
    Optional<Message> getByMessageUuid(String messageUuid);
}
