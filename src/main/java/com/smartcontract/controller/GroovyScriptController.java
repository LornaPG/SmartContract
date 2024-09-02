package com.smartcontract.controller;

import com.smartcontract.service.GroovyScriptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/smartContract/groovyScript")
public class GroovyScriptController {

    private final GroovyScriptService service;

    @Autowired
    public GroovyScriptController(GroovyScriptService service) {
        this.service = service;
    }

    @GetMapping(value = "/save")
    public ResponseEntity<?> save(@RequestParam List<String> handlerNames) {
        service.save(handlerNames);
        return ResponseEntity.ok("Saved handler scripts successfully");
    }
}
