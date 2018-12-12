/**
 * Created by kenshin on 2017/5/16.
 */
package org.loopring.crawler.core.tasks;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import org.loopring.crawler.models.SelectorItem;
import org.loopring.crawler.repos.WatchedLinkRepo;
import org.loopring.crawler.service.ClusterService;
import org.loopring.crawler.service.CommonCrawlerDataHandler;
import org.loopring.crawler.service.JpaDataService;
import org.loopring.crawler.service.SelectorService;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class WatchedLinkTask {

    private List<String> linkTasks;

    private List<String> entityTasks;

    private List<SelectorItem> allSelectors;

    private int processorThreadpoolMax;

    @Autowired
    private SelectorService selectorService;

    @Autowired
    private WatchedLinkRepo watchedLinkRepo;

    @Autowired
    private JpaDataService jpaDataService;

    @Autowired
    private CommonCrawlerDataHandler commonCrawlerDataHandler;

    @Autowired
    private ClusterService clusterService;

    @Autowired
    private Environment env;

    private static volatile boolean isSelectorSaved = false;

    private ThreadPoolExecutor linkCrawlThreadPool;
    private ThreadPoolExecutor entityCrawlThreadPool;

    @PostConstruct
    public void initSiteCrawlThreadPool() {
        if (processorThreadpoolMax <=1) processorThreadpoolMax = 2;

        int linkCrawlThreadMax = processorThreadpoolMax / 2;
        int entityCrawlThreadMax = processorThreadpoolMax / 2;
        if (linkCrawlThreadMax <= 0) linkCrawlThreadMax = 1;
        if (entityCrawlThreadMax <= 0) entityCrawlThreadMax = 1;
        if (processorThreadpoolMax % 2 != 0) entityCrawlThreadMax += 1;

        log.info("processorThreadpoolMax: {}, linkCrawlThreadMax: {}, entityCrawlThreadMax: {}", processorThreadpoolMax, linkCrawlThreadMax, entityCrawlThreadMax);

        linkCrawlThreadPool =
            new ThreadPoolExecutor(linkCrawlThreadMax, linkCrawlThreadMax,
                                   0L, TimeUnit.MILLISECONDS,
                                   new LinkedBlockingQueue<Runnable>());

        entityCrawlThreadPool =
            new ThreadPoolExecutor(entityCrawlThreadMax, entityCrawlThreadMax,
                                   0L, TimeUnit.MILLISECONDS,
                                   new LinkedBlockingQueue<Runnable>());
    }

    @Bean
    public ScheduledExecutorService springTaskScheduler() {
        // enable 5 @scheduled tasks at the same time.
        return Executors.newScheduledThreadPool(5);
    }

    public void crawlLinks() {
        executeTasks(linkTasks, linkCrawlThreadPool);
    }

    public void crawlEntities() {
        executeTasks(entityTasks, entityCrawlThreadPool);
    }

    private void executeTasks(List<String> tasks, ThreadPoolExecutor tpExecutor) {
        if (!isSelectorSaved) {
            saveSelectors();
        }
        int queueSizeMax = processorThreadpoolMax;
        if (tasks != null && tasks.size() > queueSizeMax) {
            queueSizeMax = tasks.size();
        }
        int queuedTasksCount = tpExecutor.getQueue().size();
        if (queuedTasksCount >= queueSizeMax * 2) {
            log.info("enough tasks queued for executing, return.");
            return;
        }
        for (String taskName : tasks) {
            DBWatchedLinkProcessor processor = new DBWatchedLinkProcessor(taskName);
            processor.setWatchedLinkRepo(watchedLinkRepo);
            processor.setSelectorService(selectorService);
            processor.setJpaDataService(jpaDataService);
            processor.setDataHandler(commonCrawlerDataHandler);
            processor.setClusterService(clusterService);
            processor.setEnv(env);

            try {
                Thread.sleep(20);
            } catch (InterruptedException ignore) { }

            tpExecutor.execute(processor);
        }
    }

    public synchronized void saveSelectors() {
        if (isSelectorSaved) { // double check.
            log.debug("selectors saved.");
        } else {
            isSelectorSaved = true;
            log.info("saving selectors to db now...");
            try {
                for (SelectorItem selectorItem : allSelectors) {
                    log.debug("selectorItem: {}", selectorItem);
                    selectorService.persistSelector(selectorItem);
                }
                log.info("save selectors succeeded!");
            } catch (Exception ex) {
                isSelectorSaved = false;
                log.error(ex.getMessage(), ex);
            }
        }
    }
}
