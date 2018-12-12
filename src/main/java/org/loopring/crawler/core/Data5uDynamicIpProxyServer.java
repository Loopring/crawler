package org.loopring.crawler.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jsoup.Jsoup;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Data5uDynamicIpProxyServer implements DynamicIpServer {

    private String order = "e8676ecdfef6e0a6025b6caba9aa1288";
    private String url = "http://api.ip.data5u.com/dynamic/get.html?order=" + order + "&ttl";

    private static Map<String, IpProxy> proxyMap = new ConcurrentHashMap();

    @Override
    public IpProxy get() {
        String content = null;
        try {
            content = Jsoup.connect(url)
                .ignoreContentType(true)
                .userAgent("Mozilla/5.0")
                .timeout(30 * 1000)
                .validateTLSCertificates(false).execute().body();
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }

        log.info("proxies: {}", content);
        String[] res = content.split("\n");
        List<String> ipList = new ArrayList<>();
        for (String ip : res) {
            try {
                String[] parts = ip.split(",");
                int resCode = Integer.parseInt(parts[1]);
                if (resCode > 0) {
                    ipList.add(parts[0]);
                }
            } catch (Exception e) {
            }
        }

        for (String proxyStr : ipList) {
            try {
                String[] parts = proxyStr.split(":");
                String host = parts[0];
                int port = Integer.parseInt(parts[1]);
                IpProxy proxy = new IpProxy(host, port);
                log.info("result proxy: {}", proxy);
                return proxy;
            } catch (Exception ex) {
            }
        }

        return null;
    }
}
