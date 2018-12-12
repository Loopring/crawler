package org.loopring.crawler.core;

import java.util.List;

import org.loopring.crawler.models.Link;

public interface LinkGenerator<L extends Link> {

    List<L> genLinks();

    List<L> getRepeatedCrawlLinks();
}
