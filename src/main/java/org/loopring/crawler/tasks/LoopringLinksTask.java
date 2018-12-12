package org.loopring.crawler.tasks;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import org.loopring.crawler.core.tasks.WatchedLinkGenerator;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
@ComponentScan(basePackages = {"org.loopring.crawler.service"})
@EnableScheduling
@ConfigurationProperties(prefix = "loopring")
public class LoopringLinksTask extends WatchedLinkGenerator {

    @Scheduled(initialDelayString = "${loopring.link-collector.initial-delay-str}", fixedDelayString = "${loopring.link-collector.fixed-rate-str}")
    public void generate() {
        super.generate();
    }
}
