package com.smartcontract.service;

import java.util.List;

public interface GroovyScriptService {

    /**
     * Save specified groovy scripts in MongoDB
     * @param handlerNames Groovy script names
     */
    void save(List<String> handlerNames);

    /**
     * Get the newest script version id
     * @param handlerName Groovy script name
     * @return scriptId
     */
    String getLatestScriptId(String handlerName);

}
