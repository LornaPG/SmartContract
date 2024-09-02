package com.smartcontract.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "groovyScriptBean")
public class GroovyScriptBean extends RecordBase {
    @Id
    private ObjectId _id;

    private String _class;

    @Indexed(unique = true)
    private String scriptId;

    private String scriptName;

    private List<String> content;

    private List<String> dependencyContent; // OneForAll.groovy
}
