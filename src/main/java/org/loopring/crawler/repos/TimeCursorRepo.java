package org.loopring.crawler.repos;

import org.loopring.crawler.models.TimeCursor;
import org.loopring.crawler.util.CrawlerCrudRepo;

public interface TimeCursorRepo extends CrawlerCrudRepo<TimeCursor> {

    TimeCursor findByTaskNameAndSourceEntityAndTargetEntity(String taskName, String sourceEntity, String targetEntity);
}
