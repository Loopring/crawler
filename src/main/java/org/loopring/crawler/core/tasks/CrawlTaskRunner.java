package org.loopring.crawler.core.tasks;

import java.util.function.Function;

import org.loopring.crawler.models.BasicModel;
import org.loopring.crawler.util.CrawlerCrudRepo;

import lombok.Builder;

@Builder
public class CrawlTaskRunner<T extends BasicModel, R extends BasicModel> implements Runnable {

    private Class<T> sourceClass;

    private Class<R> resultClass;

    private CrawlerCrudRepo<T> sourceRepo;

    private CrawlerCrudRepo<R> resultRepo;

    private CrawlTaskConfig taskConfig;

    private Function<T, R> convertFunc;

    @Override
    public void run() {

    }

}
