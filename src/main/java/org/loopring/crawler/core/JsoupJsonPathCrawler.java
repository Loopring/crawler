package org.loopring.crawler.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.AbstractMap;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import org.loopring.crawler.core.Selector.ValueType;
import org.loopring.crawler.util.JsoupLoginManager;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import com.jayway.jsonpath.TypeRef;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class JsoupJsonPathCrawler implements WebPageCrawler {

    private final String url;

    private final Selector rootSelector;

    private final Map<String, String> extraDataMap;

    @Override
    public Map<String, String> crawlSingle() throws Exception {
        log.info("开始爬取页面{}, 并用jsonpath解析单个结果", url);
        Map<String, String> retMap = new HashMap<>();
        String json = fetchJson();

        ReadContext rc = JsonPath.parse(json);
        if (json != null) {
            ReadContext ctx = JsonPath.parse(json);
            List<Map<String, String>> resList = parseJson(ctx, rootSelector);
            if (resList != null && resList.size() > 0) {
                retMap = resList.get(0);
            }
        }

        return retMap;
    }

    @Override
    public List<Map<String, String>> crawlMultiple() throws Exception {
        log.info("开始爬取页面{}, 并用jsonpath解析多个结果", url);
        List<Map<String, String>> resList = null;
        String json = fetchJson();
        rootSelector.setValueType(ValueType.multiple);
        if (json != null) {
            json = json.trim();
            ReadContext ctx = JsonPath.parse(json);
            resList = parseJson(ctx, rootSelector);
        }

        if (resList != null) {
            log.info("result size: {}", resList.size());
        }

        return resList;
    }

    private List<Map<String, String>> parseJson(ReadContext ctx, Selector selector) {
        List<Map<String, String>> resDataList = new ArrayList<>();
        Map<String, String> singleValueMap = new HashMap<>();
        ValueType valueType = selector.getValueType();
        String path = selector.getJsonPath();
        List<Selector> childSelectors = selector.getChildSelectors();

        List<Map<String, String>> subDatas = new ArrayList<>();
        if (ValueType.multiple == valueType) {
            log.debug("get multiple nodes: {}, ctx: {}", path, ctx.json());
            List<Object> dataObjs = getNodeList(ctx, path);
            if (dataObjs != null && dataObjs.size() == 0
                && selector.getIsRoot()) {
                log.info("no result found.");
                return resDataList;
            }
            Selector emptyRootSelecotr = new Selector();
            emptyRootSelecotr.setChildSelectors(selector.getChildSelectors());

            for (Object dataJson : dataObjs) {
                try {
                    ReadContext subCtx = JsonPath.parse(dataJson);
                    List<Map<String, String>> subItemDatas =
                        parseJson(subCtx, emptyRootSelecotr);
                    subDatas.addAll(subItemDatas);
                } catch (Exception ex) {
                    log.info(ex.getMessage(), ex);
                }
            }

        } else {
            try {
                Map.Entry<String, String> entry = parseItemValue(ctx, selector);
                log.debug("entry: {}", entry);
                if (entry != null && entry.getValue() != null) {
                    singleValueMap.put(entry.getKey(), entry.getValue());
                }
            } catch (Exception ex) {
                log.info(ex.getMessage(), ex);
            }

            if (childSelectors != null && childSelectors.size() > 0) {
                Object childJson = getSingleNode(ctx, path);
                for (Selector childSelector : childSelectors) {
                    ReadContext childCtx = JsonPath.parse(childJson);
                    List<Map<String, String>> childItemDatas =
                        parseJson(childCtx, childSelector);
                    subDatas = joinMaps(subDatas, childItemDatas);
                }
            }
        }

        if (resDataList.size() == 0) {
            resDataList.add(singleValueMap);
        }

        if (subDatas != null && subDatas.size() > 0) {
            resDataList = joinMaps(resDataList, subDatas);
        }

        for (Map<String, String> data : resDataList) {
            data.putAll(singleValueMap);
        }

        return resDataList;
    }

    private List<Object> getNodeList(ReadContext ctx, String path) {
        log.debug("ctx: {}, path:{}", ctx.json(), path);
        List<Object> dataObjs = new ArrayList<>();
        if (path != null && path.trim().length() > 0) {
            dataObjs = ctx.read(path, List.class);
        }
        else {
            dataObjs.add(ctx.json());
        }

        return dataObjs;
    }

    private Object getSingleNode(ReadContext ctx, String path) {
        Object target = null;
        if (path != null && path.trim().length() > 0) {
            target = ctx.read(path, Object.class);
        } else {
            target = ctx.json();
        }

        return target;
    }

    private List<Map<String, String>> joinMaps(List<Map<String, String>> mapList1,
                                               List<Map<String, String>> mapList2) {
        if (mapList2 == null || mapList2.size() == 0) return mapList1;
        if (mapList1 == null || mapList1.size() == 0) return mapList2;

        List<Map<String, String>> resMapList = new ArrayList<>();
        for (Map<String, String> data1 : mapList1) {
            for (Map<String, String> data2 : mapList2) {
                Map<String, String> data = new HashMap<>();
                data.putAll(data1);
                data.putAll(data2);
                resMapList.add(data);
            }
        }

        return resMapList;
    }

    private Map.Entry<String, String> parseItemValue(ReadContext rc, Selector selector) throws Exception {
        //log.info("rc:{}, selector: {}", rc.json(), selector);
        String name = selector.getName();
        String jsonPath = selector.getJsonPath();
        ValueType valueType = selector.getValueType();
        String val = null;
        if (name == null || name.trim().equals("")
            || ValueType.node == valueType
            || ValueType.multiple == valueType) {
            return null;
        }

        if (jsonPath == null || jsonPath.trim().equals("")) {
            if (ValueType.constant == valueType) {
                val = selector.getConstValue();
            }
        } else {
            if (ValueType.article == valueType) {
                List<String> valItems = rc.read(jsonPath);
                for (String valItem : valItems) {
                    String v = valItem;
                    if (val == null) {
                        val = v;
                    } else {
                        val = val + "\n" + v;
                    }
                }
            } else {
                val = rc.read(jsonPath) + "";
            }

            ValueParser vp = selector.getValueParser();
            if (vp != null) {
                val = vp.parse(val);
            }
        }

        return new AbstractMap.SimpleEntry(name, val);
    }

    private String fetchJson() throws Exception {

        String apiUrl = extraDataMap.get("apiUrl");
        if (apiUrl == null || apiUrl.trim().length() == 0) {
            apiUrl = url;
        }

        Connection conn = Jsoup.connect(apiUrl)
                .ignoreContentType(true)
                .userAgent(HTTP_HEADER)
                .timeout(30 * 1000)
                .validateTLSCertificates(false);

        String method = extraDataMap.get("method");
        String rawBody = extraDataMap.get("rawBody");

        if ("post".equalsIgnoreCase(method)) {
            conn = conn.method(Connection.Method.POST).header("Content-Type", "application/json");

            if (rawBody != null && rawBody.trim().length() > 0) {
                conn = conn.requestBody(rawBody);
            }
        }

        String needLogin = extraDataMap.get("needLogin");
        String siteName = extraDataMap.get("siteName");
        if ("true".equalsIgnoreCase(needLogin)) {
            Map<String, String> loginCookies = JsoupLoginManager.getCookies(siteName);
            if (loginCookies != null) {
                conn = conn.cookies(loginCookies);
            }
        }

        return conn.execute().body();
    }

}
