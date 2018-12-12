package org.loopring.crawler.repos;

import org.loopring.crawler.models.SelectorItem;
import org.loopring.crawler.util.CrawlerCrudRepo;

public interface SelectorItemRepo extends CrawlerCrudRepo<SelectorItem> {

    //SelectorItem findByKeyAndSiteNameAndIsRoot(String key, String siteName, String isRoot);
    SelectorItem findByKeyAndIsRoot(String key, String isRoot);
}
