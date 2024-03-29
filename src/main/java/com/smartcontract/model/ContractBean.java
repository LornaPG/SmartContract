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
@Document(collection = "contractBean")
public class ContractBean extends RecordBase {

    @Id
    private ObjectId _id;

    private String contractTemplateId;

    private List<PartyBean> parties;

    private BasicInfoBean basicInfo;

    private ContractStateBean contractState;

    private List<InstructionBean> instructions;

    private List<TradeBean> trades;

    private List<FunctionBean> functions;
}
