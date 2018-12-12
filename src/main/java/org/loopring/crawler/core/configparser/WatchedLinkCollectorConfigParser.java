package org.loopring.crawler.core.configparser;

import org.loopring.crawler.core.LinkGenerator;
import org.loopring.crawler.core.WatchedLinkCollector;
import org.loopring.crawler.core.config.LinkGeneratorConfig;
import org.loopring.crawler.core.config.WatchedLinkCollectorConfig;
import org.loopring.crawler.models.Link;
import org.loopring.crawler.service.JpaDataService;

import lombok.Data;

@Data
public class WatchedLinkCollectorConfigParser {

    private final WatchedLinkCollectorConfig config;

    private JpaDataService jpaDataService;

    public WatchedLinkCollector parse() throws Exception {

        WatchedLinkCollector collector = new WatchedLinkCollector();
        String taskName = config.getTaskName();
        collector.setTaskName(taskName);
        collector.setSourceSiteName(config.getSourceSiteName());
        collector.setSelectorKey(config.getSelectorKey());
        collector.setTargetEntity(config.getTargetEntity());
        collector.setCrawlMethod(config.getCrawlMethod());
        collector.setCrawlType(config.getCrawlType());
        collector.setNeedUpdate(config.isNeedUpdate());
        collector.setExtraDataMap(config.getExtraDataMap());
        collector.setResultFilterClass(config.getResultFilterClass());
        String linkGeneratorClass = config.getLinkGeneratorClass();
        LinkGenerator<Link> linkGenerator = null;
        if (linkGeneratorClass != null && linkGeneratorClass.trim().length() > 0) {
            linkGenerator = (LinkGenerator<Link>) Class.forName(linkGeneratorClass).newInstance();
        } else {
            LinkGeneratorConfig linkGenConfig = config.getLinkGeneratorConfig();
            LinkGeneratorConfigParser linkGenConfigParser = new LinkGeneratorConfigParser(taskName, linkGenConfig, config
                    .getSourceSiteName());
            linkGenConfigParser.setJpaDataService(jpaDataService);
            linkGenerator = linkGenConfigParser.parse();
        }
        collector.setLinkGenerator(linkGenerator);

        return collector;
    }

}
