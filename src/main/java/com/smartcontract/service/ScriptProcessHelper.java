package com.smartcontract.service;

import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.io.IOException;

import groovy.lang.GroovyShell;
import groovy.lang.Script;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ScriptProcessHelper {
    private static final String FILE_PREFIX = "src/main/groovy/com/smartcontract/handler/";

    public static JSONObject runPyScript(String dealType, String eventName, JSONObject eventParam) throws IOException {
        // Concat the groovy file name
        String fileName = FILE_PREFIX;
        switch (dealType) {
            case "fixedPricing":
                fileName += "FixedPricing.groovy";
                break;
            case "spotPricing":
                fileName += "SpotPricing.groovy";
                break;
            default:
                fileName += "OneForAll.groovy";
                break;
        }
        // Create GroovyShell
        GroovyShell shell = new GroovyShell();
        // Run Groovy script
        File groovyFile = new File(fileName);
        Script script = shell.parse(groovyFile);
        // Invoke method
        Object result = script.invokeMethod(eventName, new Object[]{eventParam.get("externalParams"), eventParam.get("internalParams")});
        return (JSONObject) result;
    }
}
