package org.loopring.crawler.repos.system;

import java.util.List;
import java.sql.Timestamp;
import org.springframework.data.repository.CrudRepository;
import org.loopring.crawler.models.system.CrawlerInstance;

public interface CrawlerInstanceRepo extends CrudRepository<CrawlerInstance, Long> {

    List<CrawlerInstance> findTop256ByProgramArgs0AndUpdateTimeAfter(String programArgs0, Timestamp updateTime);

    CrawlerInstance findFirstByUuid(String uuid);
}
