package org.loopring.crawler.core.tasks;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import org.loopring.crawler.core.CrawlMethod;
import org.loopring.crawler.core.CrawlProxy;
import org.loopring.crawler.core.CrawlType;
import org.loopring.crawler.core.CrawlableLink;
import org.loopring.crawler.core.ResultDataFilter;
import org.loopring.crawler.core.Selector;
import org.loopring.crawler.models.BasicModel;
import org.loopring.crawler.models.Link;
import org.loopring.crawler.models.WatchedLink;
import org.loopring.crawler.repos.WatchedLinkRepo;
import org.loopring.crawler.service.ClusterService;
import org.loopring.crawler.service.CommonCrawlerDataHandler;
import org.loopring.crawler.service.JpaDataService;
import org.loopring.crawler.service.SelectorService;
import org.loopring.crawler.util.DataUtil;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@ConfigurationProperties()
public class DBWatchedLinkProcessor implements Runnable {

    public static final int MAX_RETRY_TIMES = 2;
    public static final int pageSize = 10;

    private final String taskName;

    private WatchedLinkRepo watchedLinkRepo;

    private SelectorService selectorService;

    private JpaDataService jpaDataService;

    private CommonCrawlerDataHandler dataHandler;

    private ClusterService clusterService;

    private Environment env;

    private static Map<String, Integer> pageNoMap = new ConcurrentHashMap<>();

    @Override
    public void run() {
        List<WatchedLink> linksFromDB = null;
        String orderBy = "desc";
        Long interval;
        try {
            orderBy = env.getProperty("common.processor.query.orderBy");
            if (orderBy != null) {
                orderBy = orderBy.trim();
            }
        } catch (Exception ignore) { }
        try {
            String intervalStr = env.getProperty("fundinfo.single-sleep-interval");
            interval = Long.parseLong(intervalStr);
        } catch (Exception e) {
            interval = 2000L;
        }

        synchronized (this) {
            try {
                Integer pageNo = pageNoMap.get(taskName);
                if (pageNo == null) pageNo = 0;

                Page<WatchedLink> linksPage = null;
                if ("asc".equalsIgnoreCase(orderBy)) {
                    log.info("query links by is asc.");
                    linksPage = watchedLinkRepo.findByTaskNameAndStatusLessThanOrderByIdAsc(taskName, Link.STATUS_PROCESSING, new PageRequest(pageNo, pageSize));
                } else {
                    linksPage = watchedLinkRepo.findByTaskNameAndStatusLessThanOrderByIdDesc(taskName, Link.STATUS_PROCESSING, new PageRequest(pageNo, pageSize));
                }

                pageNo ++;
                int totalPage = linksPage.getTotalPages();
                linksFromDB = linksPage.getContent();
                if (linksFromDB == null || linksFromDB.size() ==0) {
                    if (pageNo > totalPage) {
                        pageNo = 0;
                    }
                }
                pageNoMap.put(taskName, pageNo);
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
        }

        long ts1 = System.currentTimeMillis();

        List<WatchedLink> linksAssigedToMe = lockLinksAssigendToMe(linksFromDB);
        if (linksAssigedToMe == null ||  linksAssigedToMe.size() == 0) {
            log.info("no more watched link for task: {}", taskName);
            return;
        }
        long ts2 = System.currentTimeMillis();
        log.info("time cost for lock links: {} milli-second", ts2 - ts1);

        for (WatchedLink wl : linksAssigedToMe) {
            try {
                crawlSingle(wl);
                updateWatchedLinkStatus(wl, Link.STATUS_SUCCEEDED, "");
                Thread.sleep(interval);
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
                updateWatchedLinkStatus(wl, Link.STATUS_NEED_RETRY, ex.getMessage());
            }
        }
    }

    private List<WatchedLink> lockLinksAssigendToMe(List<WatchedLink> watchedLinks) {
        List<WatchedLink> resList = new ArrayList<>();
        if (watchedLinks == null || watchedLinks.size() == 0) return resList;

        List<Long> resIds = new ArrayList<>();
        for (WatchedLink wl : watchedLinks) {
            boolean isAssignedToMe = clusterService.isAssignedToMe(wl.getUuid());
            if (isAssignedToMe) {
                resList.add(wl);
                resIds.add(wl.getId());
            }
        }

        log.info("link list size from db:{}, size assigned to me: {}", watchedLinks.size(), resList.size());

        if (resIds.size() > 0) {
            watchedLinkRepo.updateStatusIn(Link.STATUS_PROCESSING, resIds);
        }

        return resList;
    }

    private void crawlSingle(WatchedLink wl) throws Exception {
        boolean needCrawl = checkNeedCrawl(wl);
        if (!needCrawl) {
            return;
        }

        log.info("crawling watched link: {}", wl);
        CrawlableLink cl = watchedLink2CrawlableLink(wl);
        ResultDataFilter dataFilter = createResultDataFilter(wl);

        CrawlProxy proxy = new CrawlProxy(cl, dataFilter, dataHandler);
        proxy.setEnv(env);
        proxy.doCrawl();
    }

    private void updateWatchedLinkStatus(WatchedLink wl, int status, String errMsg) {
        if (errMsg != null && errMsg.length() > 1024) {
            errMsg = errMsg.substring(0, 1024);
        }

        Integer isRepeated = wl.getIsRepeated();
        int retryTimes = wl.getRetryTimes();
        Timestamp updateTs = new Timestamp(System.currentTimeMillis());
        wl.setRetryTimes(retryTimes + 1);
        wl.setErrorMsg(errMsg);
        wl.setUpdateTime(updateTs);

        if (isRepeated == null || isRepeated == 0) {
            wl.setStatus(status);
            if (retryTimes > MAX_RETRY_TIMES) {
                wl.setStatus(Link.STATUS_FAILED);
            }
        } else {
            wl.setStatus(Link.STATUS_NEW);
        }
        watchedLinkRepo.save(wl);
    }

    private boolean checkNeedCrawl(WatchedLink wl) throws Exception {
        String url = wl.getUrl();
        int status = wl.getStatus();
        boolean needUpdate = false;
        if (status == Link.STATUS_NEED_UPDATE) {
            needUpdate = true;
        }

        String dataClassName = wl.getTargetEntity();
        Class<? extends BasicModel> dataClass = (Class<? extends BasicModel>) Class.forName(dataClassName);
        boolean dataExists = jpaDataService.dataExists(url, dataClass);
        if (dataExists && !needUpdate) {
            log.info("data exists and not need update, return. dataClass: {}, url: {}", dataClassName, url);
            return false;
        } else {
            return true;
        }
    }

    private CrawlableLink watchedLink2CrawlableLink(WatchedLink wl) throws Exception {

        String url = wl.getUrl();
        String crawlMethod = wl.getCrawlMethod();
        String crawlType = wl.getCrawlType();
        String selectorKey = wl.getSelectorKey();
        String targetClass = wl.getTargetEntity();

        CrawlMethod cm = null;
        try {
            cm = CrawlMethod.valueOf(crawlMethod);
        } catch (Exception ex) {
            cm = CrawlMethod.jsoup;
        }

        CrawlType ct = null;
        try {
            ct = CrawlType.valueOf(crawlType);
        } catch (Exception ex) {
            ct = CrawlType.single;
        }

        Selector selector = selectorService.loadSelector(selectorKey);
        log.debug("key: {}, selector: {}", selectorKey, selector);

        Class<? extends BasicModel> dataClass = (Class<? extends BasicModel>) Class.forName(targetClass);

        CrawlableLink crawlableLink = new CrawlableLink(url, cm, ct, selector);
        Map<String, String> extraDataMap = createExtraDataMap(wl, selector);
        crawlableLink.setExtraDataMap(extraDataMap);
        crawlableLink.setDataClass(dataClass);

        return crawlableLink;
    }

    private Map<String, String> createExtraDataMap(WatchedLink wl, Selector selector) {

        Map<String, String> extraDataMap = wl.getExtraDataMap();
        if (extraDataMap == null) {
            extraDataMap = new HashMap<>();
        }

        String sourceSiteName = wl.getSourceSiteName();
        String sourceSiteName2 = selector.getSiteName();
        String url = wl.getUrl();
        String title = wl.getTitle();

        DataUtil.addToMapIfNotExists(extraDataMap, "sourceSiteName", sourceSiteName);
        DataUtil.addToMapIfNotExists(extraDataMap, "sourceSiteName", sourceSiteName2);
        DataUtil.addToMapIfNotExists(extraDataMap, "url", url);
        DataUtil.addToMapIfNotExists(extraDataMap, "title", title);
        extraDataMap.put("wluuid", wl.getUuid());

        return extraDataMap;
    }

    private ResultDataFilter createResultDataFilter(WatchedLink wl) throws Exception {

        String dataFilterClassName = wl.getDataFilterClass();
        if (dataFilterClassName == null || dataFilterClassName.trim().equals("")) {
            return null;
        }

        try {
            Class<? extends ResultDataFilter> filterClass = (Class<? extends ResultDataFilter>) Class.forName(dataFilterClassName);
            return filterClass.newInstance();
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            throw ex;
        }
    }

}
