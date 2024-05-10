package com.smartcontract.controller;

import com.smartcontract.model.Message;
import com.smartcontract.model.MessageProcessResponse;
import com.smartcontract.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/smartContract/message")
public class MessageController {

    private final MessageService messageService;

    @Autowired
    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping(value = "/route")
    public ResponseEntity<MessageProcessResponse> routeMessage(@RequestBody Message message) {
        return ResponseEntity.ok(messageService.route(message));
    }
}
