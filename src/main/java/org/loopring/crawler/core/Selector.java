package org.loopring.crawler.core;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class Selector {

    private String siteName;

    private Boolean isRoot;

    private String name;

    private String cssSelector;

    private String jsonPath;

    private ValueType valueType;

    private String attrName;

    private String constValue;

    private List<Selector> childSelectors;

    private ValueParser valueParser;

    public Selector() {

    }
    //private ValueParser valueParser;

    public Selector(String name, String cssSelector, ValueType valueType) {

        this.name = name;
        this.cssSelector = cssSelector;
        this.valueType = valueType;
    }

    public void addChildSelector(Selector child) {

        if (childSelectors == null) {
            childSelectors = new ArrayList<>();
        }
        childSelectors.add(child);
    }

    public enum ValueType {
        node, text, owntext, attr, textarray, attrarray, textarrayhash, attrarrayhash, html, article, constant, multiple
    }

}
