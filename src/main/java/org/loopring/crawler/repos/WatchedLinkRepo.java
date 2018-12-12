package org.loopring.crawler.repos;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

import org.loopring.crawler.models.WatchedLink;
import org.loopring.crawler.util.CrawlerCrudRepo;

public interface WatchedLinkRepo extends CrawlerCrudRepo<WatchedLink> {
    Page<WatchedLink> findByTaskNameAndStatusLessThanOrderByIdDesc(String taskName, int status, Pageable pageable);
    Page<WatchedLink> findByTaskNameAndStatusLessThanOrderByIdAsc(String taskName, int status, Pageable pageable);
    int countByTaskNameIn(List<String> taskNames);

    @Query("SELECT COUNT(w) FROM WatchedLink w WHERE status = ?1 AND w.sourceSiteName in ?2 AND update_time >= ?3 AND update_time < ?4")
    Long countByStatus(int status, Set<String> sites, Date begin, Date end);

    @Query("SELECT w FROM WatchedLink w WHERE status = ?1 AND source_site_name = ?2 AND update_time >= ?3 AND update_time < ?4")
    List<WatchedLink> findByStatusAndSite(int status, String site, Date begin, Date end);


    @Query("update WatchedLink w set w.status=?1 where w.id in ?2")
    @Modifying
    @Transactional
    void updateStatusIn(int status, List<Long> ids);
}
