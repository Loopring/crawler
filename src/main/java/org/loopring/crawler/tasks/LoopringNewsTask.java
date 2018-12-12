package org.loopring.crawler.tasks;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import lombok.extern.slf4j.Slf4j;

import org.loopring.crawler.core.tasks.WatchedLinkTask;

@Slf4j
@SpringBootApplication
@ComponentScan(basePackages = {"org.loopring.crawler.service"})
@EnableScheduling
@ConfigurationProperties(prefix = "loopring")
public class LoopringNewsTask extends WatchedLinkTask {

    @Scheduled(initialDelayString = "${loopring.link-processors.initial-delay-str}", fixedDelayString = "${loopring.link-processors.fixed-rate-str}")
    public void crawlLinks() {
        super.crawlLinks();
    }

    @Scheduled(initialDelayString = "${loopring.entity-processors.initial-delay-str}", fixedDelayString = "${loopring.entity-processors.fixed-rate-str}")
    public void crawlEntities() {
        super.crawlEntities();
    }
}
