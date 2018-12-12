package org.loopring.crawler.core;

import java.util.List;
import java.util.Map;

public interface ResultDataFilter {

    List<Map<String, String>> filter(List<Map<String, String>> sourceDataList, CrawlableLink crawlableLink);
}
