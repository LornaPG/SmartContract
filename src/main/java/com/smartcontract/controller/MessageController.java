package com.smartcontract.controller;

import com.smartcontract.model.Message;
import com.smartcontract.model.MessageProcessResponse;
import com.smartcontract.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/message")
public class MessageController {

    private final MessageService messageService;

    @Autowired
    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping(value = "/save")
    public ResponseEntity<?> saveMessage(@RequestBody Message message) {
        messageService.save(message);
        return ResponseEntity.ok(null);
    }

    @GetMapping(value = "/get")
    public ResponseEntity<Message> getMessage(@RequestParam String messageUuid) {
        Message message = messageService.getByMessageUuid(messageUuid);
        return ResponseEntity.ok(message);
    }

    @PostMapping(value = "/route")
    public ResponseEntity<MessageProcessResponse> routeMessage(@RequestBody Message message) {
        return ResponseEntity.ok(messageService.route(message));
    }
}
