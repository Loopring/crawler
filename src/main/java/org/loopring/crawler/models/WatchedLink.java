package org.loopring.crawler.models;

import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Table;

import lombok.Data;
import lombok.ToString;

@Entity
@Table(name = "watched_link")
@Data
@ToString(callSuper = true)
public class WatchedLink extends Link {

    public static final String CRAWL_METHOD_JSOUP = "jsoup";

    public static final String CRAWL_METHOD_CRAWLJAX = "crawljax";

    public static final int IS_REPEATED_NO = 0;

    public static final int IS_REPEATED_YES = 1;

    @Column(name = "selector_key")
    private String selectorKey;

    @Column(name = "crawl_type")
    private String crawlType; // single, multiple.

    @Column(name = "crawl_method")
    private String crawlMethod;    // jsoup, crawljax.

    @Column(name = "task_name")
    private String taskName;

    @Column(name = "target_entity")
    private String targetEntity;

    @Column(name = "data_filter_class")
    private String dataFilterClass;

    @Column(name = "url_base")
    private String urlBase;

    @Column(name = "is_repeated")
    private Integer isRepeated = 0;

    @Column(name = "parent_watched_link_uuid")
    private String parentWatchedLinkUuid;

    @Convert(converter = StringMapConverter.class)
    @Column(name = "extra_data_map", columnDefinition = "text")
    private Map<String, String> extraDataMap;

    @Column(name = "error_msg")
    private String errorMsg;
}
