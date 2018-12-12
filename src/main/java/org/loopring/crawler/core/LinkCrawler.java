package org.loopring.crawler.core;

import java.util.List;

import org.loopring.crawler.models.Link;

public interface LinkCrawler<E extends Link> {

    List<E> crawlLinks();
}
