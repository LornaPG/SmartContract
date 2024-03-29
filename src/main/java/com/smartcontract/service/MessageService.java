package com.smartcontract.service;

import com.smartcontract.model.Message;
import com.smartcontract.model.MessageProcessResponse;

public interface MessageService {

    void save(Message message);

    Message getByMessageUuid(String messageUuid);

    MessageProcessResponse route(Message message);
}
