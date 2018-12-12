package org.loopring.crawler.core;

import java.util.List;
import java.util.Map;

public interface WebPageCrawler {

    Map<String, String> crawlSingle() throws Exception;

    List<Map<String, String>> crawlMultiple() throws Exception;

    String HTTP_HEADER = "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2";
}
