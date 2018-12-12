package org.loopring.crawler.core.config;

import java.util.List;

import lombok.Data;

@Data
public class DataCrawlerConfig {

    private String crawlTypeStr;

    private SelectorConfig itemSelector;

    private String uuidFields;

    private List<SelectorConfig> dataCssSelectors;
}
