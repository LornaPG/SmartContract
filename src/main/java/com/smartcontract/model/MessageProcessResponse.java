package com.smartcontract.model;

import lombok.Data;

@Data
public class MessageProcessResponse {
    private String messageUuid;
    private Integer status;
    private String errMsg;
}
