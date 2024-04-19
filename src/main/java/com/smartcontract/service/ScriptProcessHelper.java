package com.smartcontract.service;

import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.smartcontract.model.FunctionBean;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import lombok.extern.slf4j.Slf4j;

import static com.smartcontract.util.TypeConverter.objToList;

@Slf4j
public class ScriptProcessHelper {
    private static final String FUNCTION_PREFIX = "// import ";
    private static final int WEIGHT_SCALE = 4;
    private static final int AMOUNT_SCALE = 2;
    public static JSONObject runPyScript(JSONObject dslParam) {
        // Create GroovyShell
        Binding binding = new Binding();
        binding.setVariable("weightScale", WEIGHT_SCALE);
        binding.setVariable("amountScale", AMOUNT_SCALE);
//        binding.setVariable("variables", dslParam.get("variables"));
        binding.setVariable("externalParams", dslParam.get("externalParams"));
        binding.setVariable("internalParams", dslParam.get("internalParams"));
        binding.setVariable("dslResult", null);
        binding.setVariable("instructionPipeline", new ArrayList<>());
        GroovyShell shell = new GroovyShell(binding);

        // Run Groovy script
        dslParam.get("script");
        String groovyScript = processScript(dslParam);
        shell.evaluate(groovyScript);

        // Convert the output format to json
        JSONObject output = new JSONObject();
        Object dslResult = binding.getVariable("dslResult");
        Object instructionPipeline = binding.getVariable("instructionPipeline");
        Object internalParamsResult = binding.getVariable("internalParams");
        output.put("dslResult", dslResult);
        output.put("internalParams", internalParamsResult);
        output.put("instructionPipeline", instructionPipeline);
        output.put("script", groovyScript);
        return output;
    }

    private static String processScript(JSONObject dslParam) {
        // Get the Groovy script from the JSON object
        // Construct the script content as a single string
        List<String> script = objToList(dslParam.get("script"), String.class);
        List<FunctionBean> functions = objToList(dslParam.get("functions"), FunctionBean.class);
        StringBuilder scriptContent = new StringBuilder();
        for (String lineString : script) {
            scriptContent.append(lineString).append("\n");
            if (lineString.startsWith(FUNCTION_PREFIX)) {
                String funcName = lineString.split(FUNCTION_PREFIX)[1];
                for (FunctionBean func : functions) {
                    if (Objects.equals(func.getFunctionName(), funcName)) {
                        List<String> funcScript = func.getScript();
                        for (String funcLineString : funcScript) {
                            scriptContent.append(funcLineString).append("\n");
                        }
                    }
                }
            }
        }
        log.info("scriptContent: {}", scriptContent);
        return String.valueOf(scriptContent);
    }
}
