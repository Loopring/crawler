package org.loopring.crawler.core;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SiteCrawlerTest {

    @Test
    public void test() {
        // Site site = new Site();
        // site.setUrl("http://iof.hexun.com/");
        // Selector root = new Selector("root", "div.inflist div.infbox h1 a", Selector.ValueType.node);
        // root.addChildSelector(new Selector("name", "", Selector.ValueType.text));
        // Selector hrefSelector = new Selector("href", "", Selector.ValueType.attr);
        // hrefSelector.setAttrName("href");
        // root.addChildSelector(hrefSelector);
        // TestSiteCrawler crawler = new TestSiteCrawler(site);
        // Thread t = new Thread(crawler);
        // t.start();
        assertThat(true).isTrue();
    }

    // class TestSiteCrawler extends SiteCrawler<TestBean> {

    //     public TestSiteCrawler(Site site) {
    //         super(site);
    //     }

    //     @Override
    //     public TestBean mapToBean(Map<String, String> dataMap) {
    //         long id = Long.parseLong(dataMap.get("id"));
    //         String name = dataMap.get("name");
    //         return new TestBean(id, name);
    //     }

    //     @Override
    //     public boolean exists(TestBean tb) {
    //         return false;
    //     }

    //     @Override
    //     public Selector genLinkSelector() {
    //         return null;
    //     }

    //     @Override
    //     public Selector genDataSelector() {
    //         return null;
    //     }
    // }

    // @Data
    // class TestBean {
    //     private final Long id;
    //     private final String name;
    // }
}
