package com.smartcontract.controller;

import com.smartcontract.model.AddContractTemplateResponse;
import com.smartcontract.model.ContractTemplateBean;
import com.smartcontract.service.ContractTemplateBeanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/smartContract/contractTemplate")
public class ContractTemplateController {
    private final ContractTemplateBeanService service;

    @Autowired
    public ContractTemplateController(ContractTemplateBeanService service) {
        this.service = service;
    }

    @PostMapping(value = "/addTemplate")
    public ResponseEntity<AddContractTemplateResponse> addTemplate(@RequestBody ContractTemplateBean contractTemplate) {
        return ResponseEntity.ok(service.addTemplate(contractTemplate));
    }
}
