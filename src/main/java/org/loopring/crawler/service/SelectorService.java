package org.loopring.crawler.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.loopring.crawler.Utils;
import org.loopring.crawler.core.Selector;
import org.loopring.crawler.core.Selector.ValueType;
import org.loopring.crawler.core.ValueParser;
import org.loopring.crawler.models.SelectorItem;
import org.loopring.crawler.repos.SelectorItemRepo;
import org.loopring.crawler.util.RepoUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class SelectorService {

    @Autowired
    private SelectorItemRepo selectorItemRepo;

    private Map<String, Selector> selectorMap = new HashMap<>();

    public static Selector selectorItemToSelector(SelectorItem selectorItem) {

        String name = selectorItem.getName();
        String cssSelector = selectorItem.getCssSelector();
        String valueTypeStr = selectorItem.getValueType();
        String valueParserClass = selectorItem.getValueParserClass();

        ValueType valueType = null;
        if (valueTypeStr != null && valueTypeStr.trim().length() > 0) {
            valueType = ValueType.valueOf(valueTypeStr);
        }

        String isRootStr = selectorItem.getIsRoot();

        Selector selector = new Selector(name, cssSelector, valueType);
        selector.setAttrName(selectorItem.getAttrName());
        selector.setConstValue(selectorItem.getConstValue());
        selector.setSiteName(selectorItem.getSourceSiteName());
        selector.setJsonPath(selectorItem.getJsonPath());
        if (isRootStr != null && isRootStr.toUpperCase().equals("TRUE")) {
            selector.setIsRoot(true);
        }

        if (valueParserClass != null && valueParserClass.trim().length() > 0) {
            try {
                ValueParser vp = (ValueParser) Class.forName(valueParserClass).newInstance();
                selector.setValueParser(vp);
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
        }

        List<SelectorItem> selectorItemChildren = selectorItem.getChildren();
        if (selectorItemChildren != null) {
            List<Selector> selectorChildren = new ArrayList<>();
            for (SelectorItem si : selectorItemChildren) {
                if (si == null)
                    continue;
                Selector s = selectorItemToSelector(si);
                selectorChildren.add(s);
            }
            selector.setChildSelectors(selectorChildren);
        }

        return selector;
    }

    public void persistSelector(SelectorItem rootSelectorItem) {

        saveSelectorItem(rootSelectorItem, true);
    }

    private void saveSelectorItem(SelectorItem selectorItem, boolean isRoot) {

        String uuid = null;
        if (isRoot) {
            uuid = Utils.fingerPrint(selectorItem.getKey());
            selectorItem.setUuid(uuid);
        } else {
            if (selectorItem == null)
                return;
            uuid = selectorItem.getUuid();
        }
        List<SelectorItem> children = selectorItem.getChildren();
        if (children != null) {
            String childrenUuids = null;
            for (SelectorItem child : children) {
                child.setTaskName(selectorItem.getTaskName());
                child.setKey(selectorItem.getKey());
                child.setSourceSiteName(selectorItem.getSourceSiteName());

                String childUuid = UUID.randomUUID().toString();
                child.setUuid(childUuid);
                if (childrenUuids == null) {
                    childrenUuids = childUuid;
                } else {
                    childrenUuids = childrenUuids + "," + childUuid;
                }
            }
            selectorItem.setChildrenUuids(childrenUuids);
        }

        log.debug("selectorItem: {}, uuid: {}", selectorItem, uuid);
        if (RepoUtils.exists(selectorItemRepo, selectorItem)) {
            SelectorItem selectorItemInDB = selectorItemRepo.findByUuid(uuid);
            String oldChildrenUuids = selectorItemInDB.getChildrenUuids();
            if (oldChildrenUuids != null) {
                String[] oldUuids = oldChildrenUuids.split(",");
                for (String oldUuid : oldUuids) {
                    SelectorItem oldChildItem = selectorItemRepo.findByUuid(oldUuid);
                    if (oldChildItem != null) {
                        selectorItemRepo.delete(oldChildItem.getId());
                    }
                }
            }
            selectorItem.setId(selectorItemInDB.getId());
        }
        selectorItemRepo.save(selectorItem);

        if (children != null) {
            for (SelectorItem child : children) {
                saveSelectorItem(child, false);
            }
        }
    }

    public Selector loadSelector(String selectorKey) {

        if (selectorMap.get(selectorKey) != null) {
            return selectorMap.get(selectorKey);
        } else {
            SelectorItem rootItem = selectorItemRepo.findByKeyAndIsRoot(selectorKey, "true");
            if (rootItem == null) {
                throw new IllegalStateException("no selector configed for selectorKey: " + selectorKey);
            }

            setSelectorTree(rootItem);
            Selector selector = selectorItemToSelector(rootItem);
            selectorMap.put(selectorKey, selector);
            return selector;
        }
    }

    private void setSelectorTree(SelectorItem item) {

        if (item == null)
            return;
        String childrenUuids = item.getChildrenUuids();
        if (childrenUuids != null) {
            List<SelectorItem> children = new ArrayList<>();
            String[] uuids = childrenUuids.split(",");
            for (String uuid : uuids) {
                SelectorItem child = selectorItemRepo.findByUuid(uuid);
                log.debug("child: {}, uuid: {}", child, uuid);
                children.add(child);
            }
            item.setChildren(children);

            for (SelectorItem child : children) {
                setSelectorTree(child);
            }
        }
    }

}
