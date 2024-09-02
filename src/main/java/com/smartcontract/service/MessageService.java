package com.smartcontract.service;

import com.smartcontract.model.Message;
import com.smartcontract.model.MessageProcessResponse;

public interface MessageService {

    Message save(Message message);

    MessageProcessResponse route(Message message);
}
