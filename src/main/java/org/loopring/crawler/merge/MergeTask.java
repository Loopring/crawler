package org.loopring.crawler.merge;

import java.util.List;
import java.util.Map;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import org.jsoup.Jsoup;
import org.jsoup.Connection;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.Getter;
import lombok.Setter;
import lombok.Data;

import org.loopring.crawler.models.*;
import org.loopring.crawler.service.*;
import org.loopring.crawler.util.CrawlerCrudRepo;
import org.loopring.crawler.util.ConvertUtil;

@Slf4j
@SpringBootApplication
@ComponentScan(basePackages = {"org.loopring.crawler.service"})
@ConfigurationProperties(prefix = "mergetask")
public class MergeTask implements CommandLineRunner {

    public static final int PAGE_MAX = 10000000;
    public static final int PAGE_SIZE = 50;

    @Getter @Setter
    private List<String> mergeEntities;

    @Getter @Setter
    private String mergeServiceUrl;

    @Getter @Setter
    private String needUpdate = "false";

    @Autowired
    private JpaDataService jpaDataService;

    public void run(String... args) {
        for (String entityName : mergeEntities) {
            log.info("entityName: {}", entityName);
            try {
                mergeSingle(entityName);
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
        }

        log.info("merge task completed.");
    }

    private void mergeSingle(String entity) throws Exception {
        CrawlerCrudRepo<? extends BasicModel> fromRepo = jpaDataService.getRepoByEntityName(entity);

        if (fromRepo == null) {
            throw new IllegalArgumentException("can not get repository of " + entity);
        }

        int count = 0;

        for (int i = 0; i < PAGE_MAX; i++) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignore) { }

            Page<? extends BasicModel> pagedData = fromRepo.findAll(new PageRequest(i, PAGE_SIZE));

            log.info("pagedData: {}", pagedData);

            List<? extends BasicModel> mergedList = pagedData.getContent();
            log.info("mergedList size: {}", mergedList.size());
            if (mergedList == null || mergedList.size() == 0) {
                break;
            }

            for (BasicModel dataObj : mergedList) {
                String uuid = dataObj.getUuid();
                dataObj.setId(null);
                try {
                    Map<String, String> dataMap = ConvertUtil.beanToMap2(dataObj);
                    postData(dataMap, entity);
                    count ++;
                } catch (Exception ex) {
                    log.error(ex.getMessage(), ex);
                    continue;
                }
            }
            log.info("page {} completed.", i);
        }

        log.info("table merge for {} completed. merged {} records.", entity, count );
    }

    private void postData(Map<String, String> dataMap, String className) throws Exception {
        MergeRequestBody body = new MergeRequestBody(dataMap, className, needUpdate);
        String dataJson = new ObjectMapper().writeValueAsString(body);

        log.info("dataJson: {}", dataJson);
        String res = Jsoup.connect(mergeServiceUrl)
            .method(Connection.Method.POST)
            .header("Content-Type", "application/json;charset=UTF-8")
            .ignoreContentType(true)
            .requestBody(dataJson)
            .userAgent("Mozilla")
            .execute().body();

        log.info("res: {}", res);
    }


    @Data
    class MergeRequestBody {
        private final Map<String, String> dataMap;
        private final String className;
        private final String needUpdate;
    }

}
