package org.loopring.crawler.core;

import java.util.Map;

public interface DataProcessor {

    void process(Map<String, String> dataMap);

}
