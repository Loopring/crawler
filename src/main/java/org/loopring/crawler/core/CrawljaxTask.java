package org.loopring.crawler.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.net.Authenticator;
import java.net.PasswordAuthentication;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import org.springframework.core.env.Environment;
import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.core.CrawlerContext;
import com.crawljax.core.CrawljaxRunner;
import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.ProxyConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.core.plugin.OnNewStatePlugin;
import com.crawljax.core.state.Identification;
import com.crawljax.core.state.StateVertex;
import org.loopring.crawler.core.Selector.ValueType;
import org.loopring.crawler.models.BasicModel;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class CrawljaxTask {

    private final CrawlableLink crawlableLink;

    private final ResultDataFilter resultDataFilter;

    private final ResultDataHandler resultDataHandler;

    private long waitTimeAfterEvent = 1000L;

    private long waitTimeAfterReload = 1000L;

    private CrawljaxRunner crawljax;

    private Environment env;

    public void doCrawl() throws Exception {

        preCheck();

        String browserType = env.getProperty("common.crawljax.browser-type");
        log.info("crawling url: {} using crawljax and {}", crawlableLink.getUrl(), browserType);
        initCrawler();
        crawljax.call();
    }

    private void preCheck() {

        if (resultDataHandler == null) {
            throw new IllegalStateException("resultDataHandler not set.");
        }
    }

    private void initCrawler() {

        CrawljaxConfigurationBuilder builder = CrawljaxConfiguration.builderFor(crawlableLink.getUrl());

        String browserType = env.getProperty("common.crawljax.browser-type");
        log.debug("browserType: {}", browserType);
        if ("chrome".equalsIgnoreCase(browserType)) {
            builder.setBrowserConfig(new BrowserConfiguration(BrowserType.CHROME));
        } else if ("phantomjs".equalsIgnoreCase(browserType)) {
            builder.setBrowserConfig(new BrowserConfiguration(BrowserType.PHANTOMJS));
        } else {
            builder.setBrowserConfig(new BrowserConfiguration(BrowserType.FIREFOX));
        }

        builder.crawlRules().waitAfterReloadUrl(waitTimeAfterReload, TimeUnit.MILLISECONDS);
        builder.crawlRules().waitAfterEvent(waitTimeAfterEvent, TimeUnit.MILLISECONDS);
        builder.crawlRules().crawlFrames(false);
        builder.setMaximumDepth(1);
        builder.setMaximumRunTime(150L, TimeUnit.SECONDS);
        proxySetup(builder);

        builder.addPlugin(new OnNewStatePlugin() {
            @Override
            public void onNewState(CrawlerContext context, StateVertex newState) {

                log.info("state id: {}, name: {}", newState.getId(), newState.getName());
                try {
                    parseContext(context);
                    Thread.sleep(waitTimeAfterEvent + waitTimeAfterReload);
                } catch (Exception ex) {
                    log.error(ex.getMessage(), ex);
                    // Identification iden = new Identification(Identification.How.xpath, "//body");
                    // WebElement e = context.getBrowser().getWebElement(iden);
                    //log.info("body content:{}", e.getAttribute("innerHTML"));
                } finally {
                    crawljax.stop();
                }
            }

            @Override
            public String toString() {

                return "onNewStatePlugin-" + Thread.currentThread().getId();
            }
        });

        CrawljaxConfiguration config = builder.build();

        log.info("auth url: {}", config.getBasicAuthUrl());
        crawljax = new CrawljaxRunner(config);
    }

    private void proxySetup(CrawljaxConfigurationBuilder builder) {
        String isProxyOn = env.getProperty("common.crawljax.proxy.isOn");
        String dynamicClass = env.getProperty("common.crawljax.proxy.genClass");
        if ("true".equalsIgnoreCase(isProxyOn)) {
            if (dynamicClass != null && dynamicClass.trim().length() > 0) {
                try {
                    DynamicIpServer server = (DynamicIpServer) Class.forName(dynamicClass).newInstance();
                    IpProxy proxy = server.get();
                    if (proxy != null) {
                        ProxyConfiguration proxyConfig = ProxyConfiguration.manualProxyOn(proxy.getIp(), proxy.getPort());
                        builder.setProxyConfig(proxyConfig);
                    }
                } catch (Exception ex) {
                    log.error(ex.getMessage(), ex);
                }
            } else {
                setStaticProxy(builder);
            }
        }
    }

    private void setStaticProxy(CrawljaxConfigurationBuilder builder) {
        String host = env.getProperty("common.crawljax.proxy.serverHost");
        String portStr = env.getProperty("common.crawljax.proxy.serverPort");
        String userName = env.getProperty("common.crawljax.proxy.username");
        String password = env.getProperty("common.crawljax.proxy.password");

        int port = 0;
        try {
            port = Integer.parseInt(portStr);
        } catch (Exception ex) {
            throw new IllegalArgumentException("invalid proxy server port number.");
        }

        ProxyConfiguration proxy = ProxyConfiguration.manualProxyOn(host, port);
        builder.setProxyConfig(proxy);
        builder.setBasicAuth(userName, password);

        Authenticator.setDefault(new Authenticator(){
                @Override
                public PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(userName, password.toCharArray());
                }
            });

    }

    private void parseContext(CrawlerContext context) throws Exception {

        Identification iden = new Identification(Identification.How.xpath, "//body");
        WebElement e = context.getBrowser().getWebElement(iden);

        CrawlType crawlType = crawlableLink.getCrawlType();
        Selector rootSelector = crawlableLink.getRootSelector();

        List<Map<String, String>> datas = new ArrayList<>();
        if (CrawlType.multiple == crawlType) {
            List<WebElement> targetElementList = e.findElements(By.cssSelector(rootSelector.getCssSelector()));
            for (WebElement ele : targetElementList) {
                log.debug("ele:{}", ele);
                Map<String, String> dataMap = parseSingleElement(ele, rootSelector);
                datas.add(dataMap);
            }

            if (resultDataFilter != null) {
                datas = resultDataFilter.filter(datas, crawlableLink);
            }
        } else {
            String rootCssSelector = rootSelector.getCssSelector();
            WebElement targetElement = null;
            if (rootCssSelector == null || rootCssSelector.trim().equals("")) {
                targetElement = e;
            } else {
                targetElement = e.findElement(By.cssSelector(rootCssSelector));
            }

            log.debug("targetElement: {}", targetElement);
            Map<String, String> dataMap = parseSingleElement(targetElement, rootSelector);
            datas.add(dataMap);
        }

        log.info("crawljax result datas: {}", datas);

        if (datas == null || datas.size() == 0) {
            log.debug("bodyElement innerHTML: {}", e.getAttribute("innerHTML"));
            throw new IllegalStateException("no result data crawled.");
        }

        Map<String, String> extraDataMap = crawlableLink.getExtraDataMap();
        Class<? extends BasicModel> dataClass = crawlableLink.getDataClass();
        for (Map<String, String> dataMap : datas) {
            resultDataHandler.handle(dataMap, extraDataMap, dataClass);
        }
    }

    private Map<String, String> parseSingleElement(WebElement ele, Selector selector) {

        Map<String, String> dataMap = new HashMap<>();
        String name = selector.getName();
        String cssSelector = selector.getCssSelector();
        ValueType valueType = selector.getValueType();
        WebElement subEle = ele;
        Boolean isRoot = selector.getIsRoot();
        if (isRoot == null || !isRoot) {
            if (cssSelector != null && cssSelector.trim().length() > 0) {
                subEle = ele.findElement(By.cssSelector(cssSelector));
                if (subEle == null)
                    subEle = ele;
            }
        }

        try {
            switch (valueType) {
                case node:
                    break;
                case constant:
                    String value = selector.getConstValue();
                    dataMap.put(name, value);
                    break;
                case text:
                case article:
                    dataMap.put(name, subEle.getText());
                    break;
                case attr:
                    String attrName = selector.getAttrName();
                    String attrVal = subEle.getAttribute(attrName);
                    dataMap.put(name, attrVal);
                    break;
                case html:
                    String html = subEle.getAttribute("innerHTML");
                    dataMap.put(name, html);
                    break;
                default:
                    log.error("valueType: {} not supported yet.", valueType);
                    break;
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }

        List<Selector> childSelectors = selector.getChildSelectors();
        if (childSelectors != null) {
            for (Selector childSelector : childSelectors) {
                try {
                    Map<String, String> subDataMap = parseSingleElement(ele, childSelector);
                    dataMap.putAll(subDataMap);
                } catch (Exception ex) {
                    log.error(ex.getMessage(), ex);
                }
            }
        }

        return dataMap;
    }

}
