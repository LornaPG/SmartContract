package com.smartcontract.service;

import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import groovy.lang.GroovyShell;
import groovy.lang.Script;
import lombok.extern.slf4j.Slf4j;

import static com.smartcontract.util.TypeConverter.objToMap;

@Slf4j
public class ScriptProcessHelper {
    private static final String FILE_PREFIX = "src/main/groovy/com.smartcontract.template/";
    public static Map<String, Object> runPyScript(String dealType, String eventName, JSONObject dslParam) throws IOException {
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
        Object result = script.invokeMethod(eventName, new Object[]{dslParam.get("externalParams"), dslParam.get("internalParams")});
        log.info("result is {}", result);
        return objToMap(result, String.class, Object.class);
    }
}
