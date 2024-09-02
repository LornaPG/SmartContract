package com.smartcontract.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "message")
public class Message extends RecordBase{

    @Id
    private ObjectId id;

    @Indexed(unique = true)
    private String messageUuid;

    private String parentMessageUuid;

    private String originMessageUuid;

    private String senderSubId;

    private Integer senderType;

    private String senderCompId;

    private String senderLocationId;

    private String targetSubId;

    private Integer targetType;

    private String targetCompId;

    private String targetLocationId;

    private String onBehalfOfCompId;

    private String onBehalfOfSubId;

    private String onBehalfOfLocationId;

    private String deliverToCompId;

    private String deliverToSubId;

    private String deliverToLocationId;

    private Date sendingTime;

    private Date origSendingTime;

    private Boolean possDupFlag;

    private Integer type;

    private Integer range;

    private String title;

    private String body;

    private Boolean read;
}

