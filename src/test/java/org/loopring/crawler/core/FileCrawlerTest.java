package org.loopring.crawler.core;

import java.util.Base64;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import org.loopring.crawler.config.JpaConfig;
import org.loopring.crawler.test.TestAppContext;

import junit.framework.AssertionFailedError;

import lombok.Data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@Data
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {TestAppContext.class, JpaConfig.class})

public class FileCrawlerTest {

    private static final String URL = "http://www.szse.cn/main/files/2017/05/12/%E6%B7%B1%E5%9C%B3%E8%AF%81%E5%88%B8%E4%BA%A4%E6%98%93%E6%89%80%E7%8B%AC%E7%AB%8B%E8%91%A3%E4%BA%8B%E5%A4%87%E6%A1%88%E5%8A%9E%E6%B3%95%EF%BC%882017%E5%B9%B4%E4%BF%AE%E8%AE%A2%EF%BC%89.pdf";

    @Test
    public void testFileCrawler() {

        try {
            WebPageCrawler crawler = new FileWebPageCrawler(URL);
            Map<String, String> data = crawler.crawlSingle();
            assertNotNull(data.get("fileType"));
            assertNotNull(data.get("fileContent"));
            assertEquals(2, data.size());

            String file = data.get("fileContent");
            byte[] bytes = Base64.getDecoder().decode(file);
            file = new String(bytes);

            switch (data.get("fileType")) {
                case "application/pdf":
                    assertTrue(file.startsWith("%PDF")); // PDF header
                    break;
                case "image/jpeg":

                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            throw new AssertionFailedError(e.getMessage());
        }
    }
}
