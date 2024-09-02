package com.smartcontract.service.impl;

import com.smartcontract.model.GroovyScriptBean;
import com.smartcontract.repository.GroovyScriptBeanRepository;
import com.smartcontract.service.GroovyScriptService;
import lombok.extern.slf4j.Slf4j;
import org.apache.groovy.parser.antlr4.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class GroovyScriptServiceImpl implements GroovyScriptService {

    private final GroovyScriptBeanRepository repository;

    private static final int RANDOM_ID_LENGTH = 8;
    private static final int MAX_NAME_LENGTH = 32;
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    @Autowired
    public GroovyScriptServiceImpl(GroovyScriptBeanRepository repository) {
        this.repository = repository;
    }

    private static final String FILE_PREFIX = "src/main/groovy/com/smartcontract/handler/";

    // All business mode groovy scripts
    private static final List<String> HANDLER_NAME_LIST = Arrays.asList("FixedPricing", "AvgPrice");

    @Override
    public void save(List<String> handlerNames) {
        if (handlerNames == null || handlerNames.isEmpty()) {
            return;
        }
        // if the input contains OneForAll handler, then update and save all handler scripts
        if (handlerNames.contains("OneForAll")) {
            handlerNames = HANDLER_NAME_LIST;
        }
        for (String handlerName : handlerNames) {
            GroovyScriptBean bean = new GroovyScriptBean();
            try {
                List<String> codes = Files.readAllLines(Paths.get(FILE_PREFIX + handlerName + ".groovy"));
                List<String> dependencyCodes = Files.readAllLines(Paths.get(FILE_PREFIX + "OneForAll.groovy"));
                String scriptId = generateScriptId(codes, handlerName);
                bean.setScriptId(scriptId);
                bean.setScriptName(handlerName);
                bean.setContent(codes);
                bean.setDependencyContent(dependencyCodes);
                repository.save(bean);
            } catch (IOException e) {
                log.error("Failed to read groovy script for {}", handlerName);
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public String getLatestScriptId(String handlerName) {
        if (StringUtils.isEmpty(handlerName)) {
            throw new IllegalArgumentException("handlerName cannot be empty");
        }
        Optional<GroovyScriptBean> beanOptional = repository.findTopByScriptNameOrderByCreateTimeDesc(handlerName);
        return beanOptional.map(GroovyScriptBean::getScriptId).orElse(null);
    }

    private static String generateScriptId(List<String> codes, String scriptName) {
        // 参数校验
        if (StringUtils.isEmpty(scriptName) || codes == null || codes.isEmpty()) {
            throw new IllegalArgumentException("scriptName and codes cannot be empty");
        }

        // 生成时间戳部分
        String timestamp = ZonedDateTime.now(ZoneOffset.ofHours(8)).format(TIMESTAMP_FORMAT);

        // 生成随机码
        String randomId = UUID.randomUUID().toString()
                .replace("-", "")
                .substring(0, RANDOM_ID_LENGTH);

        // 构建最终ID
        return String.format("%s_%s_%s", truncateName(scriptName), timestamp, randomId);
    }

    private static String truncateName(String name) {
        return name.length() > MAX_NAME_LENGTH ? name.substring(0, MAX_NAME_LENGTH) : name;
    }
}
