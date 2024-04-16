package com.smartcontract.model;

import lombok.Data;

@Data
public class MessageProcessResponse {
    private String messageUuid;
    private Integer resCode;
    private String resMsg;
}
