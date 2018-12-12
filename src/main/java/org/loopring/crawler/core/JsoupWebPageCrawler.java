package org.loopring.crawler.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import org.loopring.crawler.util.JsoupLoginManager;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class JsoupWebPageCrawler implements WebPageCrawler {

    private String url;

    private Selector rootSelector;

    private Map<String, String> extraDataMap;

    public JsoupWebPageCrawler(String url, Selector rootSelector, Map<String, String> extraDataMap) {

        if (url == null) {
            throw new IllegalArgumentException("url is null.");
        }
        this.url = url;
        this.rootSelector = rootSelector;
        this.extraDataMap = extraDataMap;
    }

    @Override
    public Map<String, String> crawlSingle() throws Exception {

        Map<String, String> result = new HashMap<>();
        try {
            log.info("开始爬取页面{}, 并解析单个结果", url);
            //Document doc = Jsoup.connect(url).userAgent(HTTP_HEADER).timeout(30 * 1000).get();
            Document doc = fetchDocument();
            log.debug("doc: {}", doc);
            JsoupParser parser = new JsoupParser(doc, rootSelector);
            result = parser.parse();
        } catch (HttpStatusException e) {
            if (url.endsWith(".pdf")) {
                log.info("saving resource from link:{}\n", url);
            } else {
                throw e;
            }
        }
        return result;
    }

    @Override
    public List<Map<String, String>> crawlMultiple() throws Exception {

        log.info("开始爬取页面{}, 并解析多个结果", url);
        List<Map<String, String>> result = new ArrayList<>();
        //Document doc = Jsoup.connect(url).userAgent(HTTP_HEADER).timeout(60 * 1000).get();
        Document doc = fetchDocument();
        log.debug("doc: {}", doc);
        JsoupParser rootParser = new JsoupParser(doc, rootSelector);
        Elements targetElementList = rootParser.getSelectedElements();
        log.info("targetElementList size: {}", targetElementList.size());
        for (Element ele : targetElementList) {
            try {
                JsoupParser itemParser = new JsoupParser(ele, rootSelector);
                Map<String, String> item = itemParser.parse();
                if (item != null && item.size() > 0) {
                    result.add(item);
                }
            } catch (Exception ex2) {
                log.error(ex2.getMessage());
            }
        }
        return result;
    }

    private Document fetchDocument() throws Exception {
        String siteName = null;
        if (extraDataMap != null) {
            siteName = extraDataMap.get("siteName");
        }

        Connection conn = Jsoup.connect(url)
            .ignoreContentType(true)
            .userAgent(HTTP_HEADER)
            .timeout(60 * 1000)
            .validateTLSCertificates(false);

        Map<String, String> cookies = JsoupLoginManager.getCookies(siteName);
        if (cookies == null || cookies.size() == 0) {
            cookies = parseCookieFromExtraMap();
        }

        log.debug("cookies: {}", cookies);
        if (cookies != null && cookies.size() > 0) {
            conn = conn.cookies(cookies);
        }

        Connection.Response resp = conn.execute();
        if (siteName != null && siteName.trim().length() > 0) {
            JsoupLoginManager.updateCookies(siteName,resp.cookies());
            log.debug("cookies after: {}", resp.cookies());
        }

        return resp.parse();
    }

    private Map<String, String> parseCookieFromExtraMap() {
        if (extraDataMap == null || extraDataMap.size() == 0) return null;
        Map<String, String> cookieMap = new HashMap<>();
        String CookiePre = "COOKIE_";
        for (String key : extraDataMap.keySet()) {
            if (key.startsWith(CookiePre)) {
                String cookieKey = key.substring(CookiePre.length());
                String cookieVal = extraDataMap.get(key);
                cookieMap.put(cookieKey, cookieVal);
            }
        }

        return cookieMap;
    }
}
