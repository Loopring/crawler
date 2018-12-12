package org.loopring.crawler.core.config;

import java.util.Map;

import lombok.Data;

@Data
public class WatchedLinkCollectorConfig {

    private String taskName;

    private String sourceSiteName;

    private String selectorKey;

    private String targetEntity;

    private String crawlMethod;

    private String crawlType;

    private boolean needUpdate;

    private Map<String, String> extraDataMap;

    private String linkGeneratorClass;

    private String resultFilterClass;

    private LinkGeneratorConfig linkGeneratorConfig;

}
