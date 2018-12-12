package org.loopring.crawler.models;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Entity
@Table(name = "selectors")
@Slf4j
public class SelectorItem extends BasicModel {

    @Column(name = "task_name")
    private String taskName;

    @Column(name = "selector_key")
    private String key;

    @Column(name = "site_url_base")
    private String siteUrlBase;

    @Column(name = "is_root")
    private String isRoot = "false";

    @Column
    private String name;

    @Column(name = "css_selector")
    private String cssSelector;

    @Column(name = "json_path")
    private String jsonPath;

    @Column(name = "value_type")
    private String valueType;

    @Column(name = "attr_name")
    private String attrName;

    @Column(name = "const_value")
    private String constValue;

    @Column(name = "child_uuids", columnDefinition = "text")
    private String childrenUuids;

    @Column(name = "value_parser_class")
    private String valueParserClass;

    // @OneToMany(mappedBy="parent")
    @Transient
    private List<SelectorItem> children;

    // @ManyToOne(fetch=FetchType.LAZY, cascade={CascadeType.PERSIST, CascadeType.MERGE})
    // private SelectorItem parent;

    // public String createUuid() {

    //     String[] sourceFlds = new String[]{taskName, key, siteName, isRoot + "", name, cssSelector, valueType};
    //     String source = String.join("|", sourceFlds);
    //     //log.info("source: {}", source);
    //     return Utils.fingerPrint(source);
    // }

}
