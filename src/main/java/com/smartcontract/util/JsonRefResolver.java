package com.smartcontract.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;

import java.util.Iterator;
import java.util.Map;

public class JsonRefResolver {
    private static final Configuration JSONPATH_CONFIG = Configuration.builder()
            .jsonProvider(new JacksonJsonNodeJsonProvider())
            .mappingProvider(new JacksonMappingProvider())
            .build();

    /**
     * 解析JSON引用并转换为目标Bean
     */
    public static <T> T resolveAndParse(String json, Class<T> valueType, ObjectMapper mapper) throws JsonProcessingException {
        JsonNode rootNode = mapper.readTree(json);
        resolveRefs(rootNode, rootNode);
        return mapper.treeToValue(rootNode, valueType);
    }

    /**
     * 递归解析JSON引用
     */
    private static void resolveRefs(JsonNode currentNode, JsonNode rootNode) {
        if (currentNode.isObject()) {
            processObjectNode((ObjectNode) currentNode, rootNode);
        } else if (currentNode.isArray()) {
            processArrayNode(currentNode, rootNode);
        }
    }

    private static void processObjectNode(ObjectNode objectNode, JsonNode rootNode) {
        Iterator<Map.Entry<String, JsonNode>> fields = objectNode.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            JsonNode valueNode = entry.getValue();

            if (isRefNode(valueNode)) {
                handleRefNode(objectNode, entry.getKey(), valueNode, rootNode);
            } else {
                resolveRefs(valueNode, rootNode);
            }
        }
    }

    private static void processArrayNode(JsonNode arrayNode, JsonNode rootNode) {
        for (JsonNode element : arrayNode) {
            resolveRefs(element, rootNode);
        }
    }

    private static boolean isRefNode(JsonNode node) {
        return node.isObject() && node.has("$ref");
    }

    private static void handleRefNode(ObjectNode parentNode, String fieldName, JsonNode refNode, JsonNode rootNode) {
        String jsonPath = refNode.get("$ref").asText();
        try {
            DocumentContext ctx = JsonPath.using(JSONPATH_CONFIG).parse(rootNode);
            JsonNode resolvedNode = ctx.read(jsonPath);

            // 创建深度拷贝并递归处理
            JsonNode copyNode = resolvedNode.deepCopy();
            resolveRefs(copyNode, rootNode);  // 处理新节点可能存在的引用
            parentNode.set(fieldName, copyNode);
        } catch (PathNotFoundException e) {
            handleMissingPath(parentNode, fieldName, jsonPath);
        } catch (Exception e) {
            handleGenericError(parentNode, fieldName, e);
        }
    }

    private static void handleMissingPath(ObjectNode node, String fieldName, String path) {
        node.put(fieldName + "_ERROR", "Invalid reference path: " + path);
        node.remove(fieldName);
        System.err.println("[WARN] Invalid JSON Path: " + path);
    }

    private static void handleGenericError(ObjectNode node, String fieldName, Exception e) {
        node.put(fieldName + "_ERROR", "Reference resolution failed: " + e.getMessage());
        node.remove(fieldName);
        System.err.println("[ERROR] Reference resolution error: " + e.getMessage());
    }
}
