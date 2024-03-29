package com.smartcontract.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "contractTemplateBean")
public class ContractTemplateBean {
    @Id
    private ObjectId _id;

    private String _class;

    private String contractSchemaId;

    private ContractStateBean contractState;

    private String contractTemplateId;

    private String contractVersion;

    private Boolean deleted;

    private List<FunctionBean> functions;

    private List<InstructionBean> instructions;

    private Long nextTime;

    private List<PartyBean> parties;

    private List<TradeBean> trades;

    private Integer version;
}
