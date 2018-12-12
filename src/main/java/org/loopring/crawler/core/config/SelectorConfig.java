package org.loopring.crawler.core.config;

import lombok.Data;

@Data
public class SelectorConfig {

    private String name;

    private String cssSelector;

    private String valueType;

    private String attrName;

    private String constValue;

    private String matcher;

    private String parser;
}
