package org.loopring.crawler.core.config;

import java.util.List;

import lombok.Data;

@Data
public class LinkCrawlerConfig {

    private String urlBase;

    private String crawlTypeStr;

    private List<String> linkUrls;

    private UrlGeneratorConfig linkUrlGeneratorConfig;

    private UrlGeneratorConfig dataUrlGeneratorConfig;

    private String linkCssSelector;
}
