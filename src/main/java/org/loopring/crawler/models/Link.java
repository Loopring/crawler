package org.loopring.crawler.models;

import javax.persistence.Column;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.MappedSuperclass;

import lombok.Data;
import lombok.ToString;

@Data
@MappedSuperclass
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@ToString(callSuper = true)
public class Link extends BasicModel {

    public static final int STATUS_NEW = 0;

    public static final int STATUS_NEED_RETRY = 5;

    public static final int STATUS_NEED_UPDATE = 20;

    public static final int STATUS_PROCESSING = 25;

    public static final int STATUS_SUCCEEDED = 30;

    public static final int STATUS_FAILED = 40;

    @Column(columnDefinition = "text")
    private String url;

    @Column
    private String title;

    @Column(name = "link_type")
    private String linkType;

    @Column
    private int status;

    @Column(name = "retry_times")
    private int retryTimes;
}
