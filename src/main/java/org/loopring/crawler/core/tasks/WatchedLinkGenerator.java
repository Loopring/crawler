/**
 * Created by kenshin on 2017/5/16.
 */
package org.loopring.crawler.core.tasks;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import org.loopring.crawler.core.WatchedLinkCollector;
import org.loopring.crawler.core.config.WatchedLinkCollectorConfig;
import org.loopring.crawler.core.configparser.WatchedLinkCollectorConfigParser;
import org.loopring.crawler.repos.WatchedLinkRepo;
import org.loopring.crawler.service.JpaDataService;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class WatchedLinkGenerator {

    protected List<WatchedLinkCollectorConfig> watchedLinkSeeds;

    protected List<WatchedLinkCollectorConfig> executeOnlyOnceCollectors;

    @Autowired
    protected WatchedLinkRepo watchedLinkRepo;

    @Autowired
    protected JpaDataService jpaDataService;

    protected static volatile boolean onceCollectorsExecuted = false;

    public void generate() {

        log.info("link generate task begins...");
        if (watchedLinkSeeds != null) {
            log.info("watchedLinkSeeds size: {}", watchedLinkSeeds.size());
            for (WatchedLinkCollectorConfig config : watchedLinkSeeds) {
                log.info("watchedLinkCollectorConfig: {}", config);
                executeSingleCollector(config);
            }
        }
        if (!onceCollectorsExecuted && executeOnlyOnceCollectors != null) {
            for (WatchedLinkCollectorConfig config : executeOnlyOnceCollectors) {
                log.info("watchedLinkCollectorConfig: {}", config);
                executeSingleCollector(config);
            }
            onceCollectorsExecuted = true;
        }
    }

    private void executeSingleCollector(WatchedLinkCollectorConfig config) {

        try {
            WatchedLinkCollectorConfigParser parser = new WatchedLinkCollectorConfigParser(config);
            parser.setJpaDataService(jpaDataService);
            WatchedLinkCollector collector = parser.parse();
            collector.setWatchedLinkCrudRepo(watchedLinkRepo);
            collector.collect();
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }
}
