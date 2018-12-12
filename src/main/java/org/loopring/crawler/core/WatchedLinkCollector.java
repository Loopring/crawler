package org.loopring.crawler.core;

import java.util.List;
import java.util.Map;

import org.loopring.crawler.Utils;
import org.loopring.crawler.models.Link;
import org.loopring.crawler.models.WatchedLink;
import org.loopring.crawler.util.CrawlerCrudRepo;
import org.loopring.crawler.util.RepoUtils;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class WatchedLinkCollector {

    private String taskName;

    private String sourceSiteName;

    private String selectorKey;

    private String targetEntity;

    private String crawlMethod;

    private String crawlType;

    private boolean needUpdate;

    private String resultFilterClass;

    private Map<String, String> extraDataMap;

    private LinkGenerator<Link> linkGenerator;

    private CrawlerCrudRepo<WatchedLink> watchedLinkCrudRepo;

    public void collect() {

        if (watchedLinkCrudRepo == null) {
            throw new IllegalStateException("watchedLinkCrudRepo can not be null. " + this.toString());
        }

        List<Link> generatedLinkList = linkGenerator.genLinks();
        int linkCount = 0;
        if (generatedLinkList != null) {
            for (Link link : generatedLinkList) {
                try {
                    WatchedLink wl = linkToWatchedLink(link);
                    int count = persistWatchedLink(wl, needUpdate);
                    linkCount += count;
                } catch (Exception ex) {
                    log.error(ex.getMessage(), ex);
                }
            }

            log.info("新增普通链接数目为：{}", linkCount);
        }

        List<Link> repeatedLinks = linkGenerator.getRepeatedCrawlLinks();
        int repeatedLinkCount = 0;
        if (repeatedLinks != null) {
            for (Link link : repeatedLinks) {
                try {
                    WatchedLink wl = linkToWatchedLink(link);
                    wl.setIsRepeated(WatchedLink.IS_REPEATED_YES);
                    int count = persistWatchedLink(wl, true);
                    repeatedLinkCount += count;
                } catch (Exception ex) {
                    log.error(ex.getMessage(), ex);
                }
            }
            log.info("新增重复爬取链接数目为：{}", repeatedLinkCount);
        }
    }

    private WatchedLink linkToWatchedLink(Link link) throws Exception {

        String uuid = genUuidForLink(link);
        String url = link.getUrl();

        WatchedLink wl = new WatchedLink();
        wl.setTitle(link.getTitle());
        wl.setUrl(url);
        wl.setUuid(uuid);
        wl.setTargetEntity(targetEntity);
        wl.setTaskName(taskName);
        wl.setSourceSiteName(sourceSiteName);
        wl.setSelectorKey(selectorKey);
        wl.setCrawlMethod(crawlMethod);
        wl.setCrawlType(crawlType);
        wl.setExtraDataMap(extraDataMap);
        wl.setDataFilterClass(resultFilterClass);

        return wl;
    }

    private String genUuidForLink(Link link) {

        String sourceStr = link.getUrl() + taskName + selectorKey;
        return Utils.fingerPrint(sourceStr);
    }

    private int persistWatchedLink(WatchedLink wl, boolean update) {

        if (!RepoUtils.exists(watchedLinkCrudRepo, wl)) {
            watchedLinkCrudRepo.save(wl);
            return 1;
        } else {
            if (update) {
                String uuid = wl.getUuid();
                WatchedLink wlInDB = watchedLinkCrudRepo.findByUuid(uuid);
                wl.setId(wlInDB.getId());
                wl.setStatus(Link.STATUS_NEED_UPDATE);
                watchedLinkCrudRepo.save(wl);
                return 1;
            }
        }
        return 0;
    }
}
