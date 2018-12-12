package org.loopring.crawler.core;

import java.util.Map;

import org.loopring.crawler.models.BasicModel;

public interface ResultDataHandler {

    void handle(Map<String, String> dataMap, Map<String, String> extraDataMap, Class<? extends BasicModel> dataClass);

}
