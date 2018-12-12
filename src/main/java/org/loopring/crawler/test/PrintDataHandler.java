package org.loopring.crawler.test;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.loopring.crawler.core.ResultDataHandler;
import org.loopring.crawler.models.BasicModel;

@Slf4j
public class PrintDataHandler implements ResultDataHandler {

    @Override
    public void handle(Map<String, String> dataMap, Map<String, String> extraDataMap, Class<? extends BasicModel> dataClass) {
        log.info("dataClass: {}", dataClass);
        log.info("dataMap: {}", dataMap);
        log.info("extraDataMap: {}", extraDataMap);
    }

}
