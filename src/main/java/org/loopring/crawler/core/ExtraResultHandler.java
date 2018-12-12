package org.loopring.crawler.core;

import java.util.Map;

public interface ExtraResultHandler {

    void handle(Map<String, String> dataMap);
}
