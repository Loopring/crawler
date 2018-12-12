package org.loopring.crawler.util;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import org.loopring.crawler.Utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DataUtil {

    public static void trimAllValue(Map<String, String> dataMap) {
        for (String key : dataMap.keySet()) {
            String val = dataMap.get(key);
            if (val != null) {
                dataMap.put(key, val.trim());
            }
        }
    }

    public static String genUuidForData(Map<String, String> dataMap) {

        String uuid = null;
        String uuidFields = dataMap.get("uuidFields");
        if (uuidFields == null || uuidFields.trim().equals("")) {
            uuidFields = "url";
        }

        String[] fieldNames = uuidFields.split(",");
        String uuidRaw = "";
        for (String fn : fieldNames) {
            String val = dataMap.get(fn);
            if (val != null) {
                uuidRaw = uuidRaw + val;
            }
        }

        //log.info("uuidRaw: {}", uuidRaw);

        if (uuidRaw.length() > 0) {
            uuid = Utils.fingerPrint(uuidRaw);
        }

        return uuid;
    }

    public static void processVariables(Map<String, String> dataMap) {

        if (dataMap == null)
            return;
        for (String key : dataMap.keySet()) {
            String val = dataMap.get(key);
            if (val == null)
                continue;
            List<String> varNames = getVarNames(val);
            for (String varName : varNames) {
                String varNameTrimed = varName.trim();
                if (dataMap.containsKey(varNameTrimed)) {
                    String replacedVal = dataMap.get(varNameTrimed);
                    if (replacedVal == null) continue;
                    val = val.replaceAll("\\$\\{" + varName + "\\}", replacedVal);
                }
            }
            dataMap.put(key, val);
        }
    }

    private static List<String> getVarNames(String value) {
        List<String> resList = new ArrayList<>();
        int pos = 0;

        while( value.indexOf("${", pos) >= 0) {
            pos = value.indexOf("${", pos);
            int endPos = value.indexOf("}", pos);
            if (endPos > 0) {
                String varName = value.substring(pos + 2, endPos);
                pos = endPos;
                resList.add(varName);
            } else {
                break;
            }
        }

        return resList;
    }

    public static void mergeExtraDataMap(Map<String, String> dataMap, Map<String, String> extraMap) {

        if (extraMap == null || dataMap == null)
            return;
        for (String key : extraMap.keySet()) {
            String dataVal = dataMap.get(key);
            if (dataVal == null || dataVal.trim().equals("")) {
                dataMap.put(key, extraMap.get(key));
            }
        }
    }

    public static void addToMapIfNotExists(Map<String, String> dataMap, String key, String value) {

        if (dataMap == null || key == null || value == null)
            return;
        String valInMap = dataMap.get(key);
        if (valInMap == null || valInMap.trim().equals("")) {
            dataMap.put(key, value);
        }
    }

}
