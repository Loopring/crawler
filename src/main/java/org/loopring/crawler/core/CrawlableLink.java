package org.loopring.crawler.core;

import java.util.Map;

import org.loopring.crawler.models.BasicModel;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class CrawlableLink {

    private final String url;

    private final CrawlMethod crawlMethod;

    private final CrawlType crawlType;

    private final Selector rootSelector;

    private Map<String, String> extraDataMap;

    private Class<? extends BasicModel> dataClass;
}
