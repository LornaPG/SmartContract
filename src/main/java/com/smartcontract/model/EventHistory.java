package com.smartcontract.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "eventHistory")
public class EventHistory extends RecordBase {
    @Id
    private ObjectId _id;

    private String contractCode;

    private String logicContractCode;

    private String tradeId;

    private String dealId;

    private String eventParam;

    private String outputResult;

    private String eventName;

    private String eventNo;

    private String revisionNumber;

    private String _class;
}
