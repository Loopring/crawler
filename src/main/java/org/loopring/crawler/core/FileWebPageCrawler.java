package org.loopring.crawler.core;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class FileWebPageCrawler implements WebPageCrawler {

    private String url;

    public FileWebPageCrawler(String url) {

        if (url == null) {
            throw new IllegalArgumentException("url is null.");
        }
        this.url = url;
    }

    @Override
    public Map<String, String> crawlSingle() throws Exception {

        log.info("开始爬取页面{}, 获取静态资源", url);
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.connect();
        InputStream input = connection.getInputStream();
        byte[] bytes = IOUtils.toByteArray(input);
        String content = Base64.getEncoder().encodeToString(bytes);
        Map<String, String> result = new HashMap<>();
        result.put("fileType",connection.getContentType());
        result.put("fileContent", content);
        return result;
    }

    @Override
    public List<Map<String, String>> crawlMultiple() throws Exception {

        throw new RuntimeException("页面资源爬虫无法以multiple方式爬取资源!\n");
    }
}
