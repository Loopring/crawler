package org.loopring.crawler.util;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

public interface CrawlerCrudRepo<T> extends CrudRepository<T, Long> {

    Long countByUuid(String uuid);

    T findByUuid(String uuid);

    List<T> findByInsertTimeAfterOrderByInsertTimeAsc(Timestamp insertTime, Pageable page);

    List<T> findBySourceSiteNameAndInsertTimeAfterOrderByInsertTimeAsc(String sourceSiteName, Timestamp insertTime, Pageable page);

    //Page<T> findOrderById(Pageable page);

    Page<T> findAll(Pageable page);
}
