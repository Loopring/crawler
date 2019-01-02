package org.loopring.crawler.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import org.loopring.crawler.Utils;
import org.loopring.crawler.core.ValueParser;
import org.loopring.crawler.models.BasicModel;
import org.loopring.crawler.models.NewsInfo;
import org.loopring.crawler.models.TimeCursor;
import org.loopring.crawler.models.WatchedLink;
import org.loopring.crawler.repos.WatchedLinkRepo;
import org.loopring.crawler.repos.NewsInfoRepo;
import org.loopring.crawler.repos.TimeCursorRepo;
import org.loopring.crawler.util.ConvertUtil;
import org.loopring.crawler.util.CrawlerCrudRepo;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JpaDataService {

    @Autowired
    private WatchedLinkRepo watchedLinkRepo;

    @Autowired
    private TimeCursorRepo timeCursorRepo;

    @Autowired
    private NewsInfoRepo newsInfoRepo;

    public CrawlerCrudRepo<? extends BasicModel> getRepoByEntityName(String className) {
        CrawlerCrudRepo<? extends BasicModel> crudRepo = null;
        switch (className) {
            case "org.loopring.crawler.models.CnInfo":
                crudRepo = newsInfoRepo;
                break;
            default:
                crudRepo = null;
                break;
         }

        return crudRepo;
    }

    public boolean dataExists(String url, Class<? extends BasicModel> dataClass) {
        String uuid = Utils.fingerPrint(url);
        CrawlerCrudRepo<? extends BasicModel> crudRepo = getRepoByEntityName(dataClass.getName());

        if (crudRepo == null) {
            log.warn("crudRepo not found for class: {}", dataClass.getName());
            return false;
        }

        long count = crudRepo.countByUuid(uuid);
        return count > 0;
    }

    public void saveData(Map<String, String> dataMap, Class<? extends BasicModel> dataClass, boolean needUpdate) {

        if (dataMap == null)
            return;
        if (dataClass == null) {
            throw new IllegalArgumentException("dataClass can not be null in DataPersistService!");
        }

        log.debug("save data: dataMap: {}, class: {}", dataMap, dataClass.getName());

        switch (dataClass.getName()) {
            case "org.loopring.crawler.models.WatchedLink":
                DataSaver<WatchedLink> wlSaver = new DataSaver<>(dataMap, WatchedLink.class);
                wlSaver.setDataRepo(watchedLinkRepo);
                wlSaver.setNeedUpdate(needUpdate);
                wlSaver.saveData();
                break;
            case "org.loopring.crawler.models.NewsInfo":
                DataSaver<NewsInfo> newsSaver = new DataSaver<>(dataMap, NewsInfo.class);
                newsSaver.setDataRepo(newsInfoRepo);
                newsSaver.setNeedUpdate(needUpdate);
                newsSaver.saveData();
                break;
            default:
                throw new IllegalStateException("data persist service not implemented for dataType:" + dataClass.getName());
        }
    }

    public List<String> fieldValuesFromEntity(String taskName, String entityClassName, String fieldName, String sourceSiteName, String valueParserClass) {

        if (entityClassName == null || fieldName == null) {
            throw new IllegalArgumentException("can not get field values. entity:" + entityClassName + "; fieldName: " + fieldName);
        }

        List<String> resList;
        switch (entityClassName) {
            case "org.loopring.crawler.models.CnInfo":
                CursoredDataFetcher<NewsInfo> entFetcher = new CursoredDataFetcher<>(taskName, entityClassName, fieldName, newsInfoRepo);
                entFetcher.setTimeCursorRepo(timeCursorRepo);
                entFetcher.setSourceSiteName(sourceSiteName);
                resList = entFetcher.getFieldValues();
                break;
            default:
                throw new IllegalStateException("data fetcher service not implemented for dataType:" + entityClassName);
        }

        return parseResultList(resList, valueParserClass);
    }

    private List<String> parseResultList(List<String> valueList, String valueParserClass) {

        if (valueParserClass == null || valueParserClass.trim().length() == 0)
            return valueList;
        else {
            try {
                ValueParser vp = (ValueParser) Class.forName(valueParserClass).newInstance();
                List<String> newResList = new ArrayList<>();
                for (String val : valueList) {
                    try {
                        String newVal = vp.parse(val);
                        newResList.add(newVal);
                    } catch (Exception ex2) {
                        log.error(ex2.getMessage(), ex2);
                    }
                }

                return newResList;
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
                return valueList;
            }
        }
    }

    @Data
    @Slf4j
    static class DataSaver<T extends BasicModel> {

        private final Map<String, String> dataMap;

        private final Class<T> dataClass;

        private CrawlerCrudRepo<T> dataRepo;

        private boolean needUpdate;

        public void saveData() {

            if (dataRepo == null) {
                throw new IllegalStateException("data repository is null in dataSaver, class:" + dataClass.getName());
            }
            if (dataMap == null) {
                return;
            }
            log.debug("saving data: {}", dataMap);
            T t = ConvertUtil.mapToBean(dataMap, dataClass);
            if (!t.isValid()) {
                log.info("invalid target data: {}", t);
                return;
            }
            setDataFields(t);
            T objInDB = dataRepo.findByUuid(t.getUuid());
            if (objInDB != null) {
                if (needUpdate) {
                    t.setId(objInDB.getId());
                } else {
                    log.warn("crawled data exist and do not need update. return.");
                    return;
                }
            }
            dataRepo.save(t);
        }

        private void setDataFields(T t) {

            if (WatchedLink.class == dataClass) {
                WatchedLink wl = (WatchedLink) t;
                String extraFields = dataMap.get("extraFields");
                if (extraFields != null) {
                    Map<String, String> extraDataMap = new HashMap<>();
                    String[] fieldNames = extraFields.split(",");
                    for (String fieldName : fieldNames) {
                        fieldName = fieldName.trim();
                        String val = dataMap.get(fieldName);
                        if (val != null) {
                            extraDataMap.put(fieldName, val);
                        }
                    }
                    log.debug("set extra data for WatchedLink. extraDataMap: {}", extraDataMap);
                    wl.setExtraDataMap(extraDataMap);
                }
                String url = Utils.concatHttpUrl(wl.getUrlBase(), wl.getUrl());
                wl.setUrl(url);
            }
        }
    }

    @Data
    @Slf4j
    static class CursoredDataFetcher<T extends BasicModel> {

        private final String taskName;

        private final String entityClassName;

        private final String fieldName;

        private final CrawlerCrudRepo<T> dataRepo;

        private String targetEntityName = WatchedLink.class.getName();

        private TimeCursorRepo timeCursorRepo;

        private String sourceSiteName;

        public List<String> getFieldValues() {

            List<String> resList = new ArrayList<>();
            Timestamp insertTime = getQueryTimeCursor();
            PageRequest page = new PageRequest(0, 500);

            List<T> sourceEntities = null;
            if (sourceSiteName == null || sourceSiteName.trim().equals("")) {
                sourceEntities = dataRepo.findByInsertTimeAfterOrderByInsertTimeAsc(insertTime, page);
            } else {
                log.info("query with sourceSiteName: {}", sourceSiteName);
                sourceEntities = dataRepo.findBySourceSiteNameAndInsertTimeAfterOrderByInsertTimeAsc(sourceSiteName, insertTime, page);
            }
            if (sourceEntities != null && sourceEntities.size() > 0) {
                for (T t : sourceEntities) {
                    Map<String, Object> props = ConvertUtil.beanToMap(t);
                    Object sourceSiteNameProp = props.get("sourceSiteName");
                    if (sourceSiteNameProp == null) {
                        log.warn("sourceSiteName is null, skip. entity: {}, id: {}", entityClassName, props.get("id"));
                        continue;
                    }
                    String targetSourceSite = sourceSiteNameProp.toString();
                    if (sourceSiteName == null || sourceSiteName.trim()
                            .length() == 0 || sourceSiteName.equals(targetSourceSite)) {
                        Object val = props.get(fieldName);
                        if (val != null) {
                            String valStr = val.toString();
                            if (valStr.trim().length() > 0) {
                                resList.add(valStr);
                            }
                        }
                    }
                }

                T lastSourceEntity = sourceEntities.get(sourceEntities.size() - 1);
                Timestamp lastInsertTime = lastSourceEntity.getInsertTime();
                Timestamp newInsertTime = new Timestamp(lastInsertTime.getTime() - 1000);
                updateTimeCursor(newInsertTime);

                log.info("data fetcher: 从{}中取到insertTime从{}到{}共{}条记录。", entityClassName, insertTime, lastInsertTime, sourceEntities
                        .size());
            } else {
                log.info("data fetcher: 从{}中取到0条记录。 timeCursor:{}", entityClassName, insertTime);
            }

            return resList;
        }

        private Timestamp getQueryTimeCursor() {

            Timestamp insertTime = new Timestamp(0L);
            if (timeCursorRepo == null) {
                throw new IllegalStateException("timeCursorRepo can not be null in Data Fetcher.");
            }
            TimeCursor tc = timeCursorRepo.findByTaskNameAndSourceEntityAndTargetEntity(taskName, entityClassName, targetEntityName);
            log.debug("timeCursor: {}", tc);
            if (tc != null) {
                insertTime = tc.getTimeCursor();
            }

            return insertTime;
        }

        private void updateTimeCursor(Timestamp newDate) {

            if (timeCursorRepo == null)
                return;
            TimeCursor newCursor = new TimeCursor();
            newCursor.setTaskName(taskName);
            newCursor.setSourceEntity(entityClassName);
            newCursor.setTargetEntity(targetEntityName);
            newCursor.setTimeCursor(newDate);
            String uuid = newCursor.createUuid();
            newCursor.setUuid(uuid);
            TimeCursor cursorInDB = timeCursorRepo.findByUuid(newCursor.getUuid());
            if (cursorInDB == null) {
                timeCursorRepo.save(newCursor);
            } else {
                newCursor.setId(cursorInDB.getId());
                timeCursorRepo.save(newCursor);
            }
        }

    }

}
