package org.loopring.crawler.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.loopring.crawler.core.ResultDataHandler;
import org.loopring.crawler.models.BasicModel;
import org.loopring.crawler.models.WatchedLink;
import org.loopring.crawler.util.DataUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CommonCrawlerDataHandler implements ResultDataHandler {

    @Autowired
    private JpaDataService jpaDataService;

    @Override
    public void handle(Map<String, String> dataMap, Map<String, String> extraDataMap, Class<? extends BasicModel> dataClass) {
        DataUtil.mergeExtraDataMap(dataMap, extraDataMap);
        DataUtil.processVariables(dataMap);
        DataUtil.trimAllValue(dataMap);
        String uuid = DataUtil.genUuidForData(dataMap);
        dataMap.put("uuid", uuid);
        log.info("saving result data: {}, uuid: {}", dataClass, uuid);
        saveData(dataMap, dataClass);
    }

    private void saveData(Map<String, String> dataMap, Class<? extends BasicModel> dataClass) {

        boolean needUpdate = true;
        if (dataMap == null || dataMap.size() == 0) {
            log.warn("crawl result is empty. return.");
            return;
        }
        if (dataClass == WatchedLink.class) {
            needUpdate = false;
        }
        jpaDataService.saveData(dataMap, dataClass, needUpdate);
    }
}
