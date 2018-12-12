package org.loopring.crawler.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.core.env.Environment;
import org.loopring.crawler.models.BasicModel;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class CrawlProxy {

    private final CrawlableLink crawlableLink;

    private final ResultDataFilter resultDataFilter;

    private final ResultDataHandler resultDataHandler;

    private Environment env;

    public void doCrawl() throws Exception {

        CrawlMethod crawlMethod = crawlableLink.getCrawlMethod();

        if (CrawlMethod.crawljax == crawlMethod) {
            CrawljaxTask task = new CrawljaxTask(crawlableLink, resultDataFilter, resultDataHandler);
            task.setEnv(env);
            task.doCrawl();
        } else {
            String url = crawlableLink.getUrl();
            CrawlType crawlType = crawlableLink.getCrawlType();
            Selector rootSelector = crawlableLink.getRootSelector();
            Map<String, String> extraDataMap = crawlableLink.getExtraDataMap();
            Class<? extends BasicModel> dataClass = crawlableLink.getDataClass();

            WebPageCrawler crawler = null;
            if (CrawlMethod.json == crawlMethod) {
                crawler = new JsoupJsonPathCrawler(url, rootSelector, extraDataMap);
            } else if (CrawlMethod.jsoup == crawlMethod) {
                crawler = new JsoupWebPageCrawler(url, rootSelector,extraDataMap);
            } else if (CrawlMethod.file == crawlMethod) {
                crawler = new FileWebPageCrawler(url);
            }

            List<Map<String, String>> resData = new ArrayList<>();
            if (crawlType == CrawlType.single) {
                Map<String, String> data = crawler.crawlSingle();
                resData.add(data);
            } else if (crawlType == CrawlType.multiple) {
                List<Map<String, String>> datas = crawler.crawlMultiple();
                resData.addAll(datas);
            } else {
                throw new IllegalArgumentException("unknown crawlType:" + crawlType);
            }

            if (resultDataFilter != null) {
                resData = resultDataFilter.filter(resData, crawlableLink);
            }

            for (Map<String, String> dataMap : resData) {
                resultDataHandler.handle(dataMap, extraDataMap, dataClass);
            }
        }
    }
}
