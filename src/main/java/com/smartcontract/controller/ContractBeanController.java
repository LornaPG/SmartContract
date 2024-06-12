package com.smartcontract.controller;

import com.smartcontract.model.ContractBean;
import com.smartcontract.service.ContractBeanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/smartContract/contractBean")
public class ContractBeanController {

    private final ContractBeanService service;

    @Autowired
    public ContractBeanController(ContractBeanService service) { this.service = service; }

    @GetMapping(value = "getLatestByContractCode")
    public ResponseEntity<ContractBean> getLatestByContractCode(@RequestParam String contractCode) {
        return ResponseEntity.ok(service.getLatestContractBeanByContractCode(contractCode));
    }
}
