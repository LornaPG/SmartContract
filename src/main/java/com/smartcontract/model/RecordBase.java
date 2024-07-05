package com.smartcontract.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecordBase {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long creatorId;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Date createTime = new Date();

    @JsonProperty
    private boolean deleted = false;    //因为lombok针对isXxxx生成的getter、setter方法无法兼容swagger的，所以，所有的isXxx都修改为直接xxx

    private int version;
}
