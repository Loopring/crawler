package org.loopring.crawler.service;

import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import java.net.InetAddress;
import java.sql.Timestamp;
import javax.annotation.PostConstruct;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Scheduled;

import org.loopring.crawler.util.Shard;
import org.loopring.crawler.Main;
import org.loopring.crawler.models.system.CrawlerInstance;
import org.loopring.crawler.repos.system.CrawlerInstanceRepo;

import lombok.extern.slf4j.Slf4j;
import lombok.Getter;
import lombok.Setter;

@Slf4j
@Component
@ConfigurationProperties(prefix = "cluster")
public class ClusterService {

    @Getter @Setter
    private boolean clusterOn = true;

    private String instanceId;

    private Shard<CrawlerInstance> cluster;

    private List<CrawlerInstance> allInstances;

    @Autowired
    private CrawlerInstanceRepo crawlerInstanceRepo;

    @PostConstruct
    public void init() {
        instanceId = UUID.randomUUID().toString();
        log.info("current crawler instance id: {}", instanceId);
        registerMe();
        buildCluster();

        Runtime.getRuntime().addShutdownHook(new Thread(){
                @Override
                public void run() {
                    unregisterMe();
                }
            });
    }

    public boolean isAssignedToMe(String key) {
        if (!clusterOn) return true;
        if (cluster == null || cluster.size() <= 1) return true;

        CrawlerInstance ci = cluster.getShardInfo(key);
        log.debug("uuid: {}, current instance id: {}, shard to: {}", key, instanceId, ci.getUuid());

        if (instanceId.equals(ci.getUuid())) {
            return true;
        } else {
            return false;
        }
    }

    public void printClusterIds() {
        List<String> uuids = new ArrayList<>();
        for (CrawlerInstance ci : allInstances) {
            uuids.add(ci.getUuid());
        }
        log.info("cluster uuids: {}", uuids);
    }

    @Scheduled(initialDelayString = "3000", fixedRateString = "10000")
    public void heartBeat() {
        registerMe();
        buildCluster();
    }

    private void registerMe() {
        String ip = "";
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }

        CrawlerInstance me = new CrawlerInstance(instanceId, ip);
        me.setProgramArgs0(Main.getArgs0());
        CrawlerInstance ciInDB = crawlerInstanceRepo.findFirstByUuid(instanceId);
        if (ciInDB != null) {
            me.setId(ciInDB.getId());
        }
        crawlerInstanceRepo.save(me);
    }

    private void unregisterMe() {
        CrawlerInstance ciInDB = crawlerInstanceRepo.findFirstByUuid(instanceId);
        if (ciInDB != null) {
            crawlerInstanceRepo.delete(ciInDB);
        }
    }

    private void buildCluster() {
        long currMillis = System.currentTimeMillis();
        Timestamp updateTimeCondition = new Timestamp(currMillis - 10 * 10000);
        List<CrawlerInstance> crawlersInDB = crawlerInstanceRepo.findTop256ByProgramArgs0AndUpdateTimeAfter(Main.getArgs0(), updateTimeCondition);
        if (crawlersInDB == null || crawlersInDB.size() == 0) {
            cluster = null;
            return;
        }

        if (!crawlersInDB.contains(new CrawlerInstance(instanceId, ""))) {
            registerMe();
        }

        if (allInstances == null) {
            allInstances = crawlersInDB;
        } else {
            if (allInstances.containsAll(crawlersInDB) &&
                crawlersInDB.containsAll(allInstances)) {
                log.debug("cluster no change.");
                return;
            } else {
                log.info("cluster changed. before: {}, after: {}", allInstances, crawlersInDB);
                allInstances = crawlersInDB;
            }
        }

        log.info("rebuild shard.");
        cluster = new Shard<>(allInstances);
    }

}
