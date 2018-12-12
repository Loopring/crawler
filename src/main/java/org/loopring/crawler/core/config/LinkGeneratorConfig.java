package org.loopring.crawler.core.config;

import java.util.List;

import lombok.Data;

@Data
public class LinkGeneratorConfig {

    private List<String> repeatedCrawlUrls;

    private String linkUrlTemplate;

    private List<TemplateParamConfig> paramsConfig;
}
