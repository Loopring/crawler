package org.loopring.crawler.models.system;

import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class CrawlerInstanceTest {

    @Test
    public void testEquals() {
        CrawlerInstance ci1 = new CrawlerInstance("1", "1.1.1.1");
        CrawlerInstance ci2 = new CrawlerInstance("2", "1.1.1.1");
        CrawlerInstance ci3 = new CrawlerInstance("2", "1.0.1.1");

        assertThat(ci1.equals(ci2)).isEqualTo(false);
        assertThat(ci3.equals(ci2)).isEqualTo(true);
    }

}
