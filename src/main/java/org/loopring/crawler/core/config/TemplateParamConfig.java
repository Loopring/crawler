package org.loopring.crawler.core.config;

import lombok.Data;

@Data
public class TemplateParamConfig {

    private String paramTypeStr;

    private String[] params;

    private int start;

    private int end;

    private int step;

    private String entityClassName;

    private String fieldName;

    private String valueParserClass;

    private String generatorClass;
}
