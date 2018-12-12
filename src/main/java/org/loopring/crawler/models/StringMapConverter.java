package org.loopring.crawler.models;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Converter
public class StringMapConverter implements AttributeConverter<Map<String, String>, String> {

    private static final String KV_SEPARATOR = ":::";

    private static final String PAIRS_SEPARATOR = "|||";

    private static final String PAIRS_SEPARATOR_REGEX = "\\|\\|\\|";

    @Override
    public String convertToDatabaseColumn(Map<String, String> dataMap) {

        if (dataMap == null)
            return null;
        String res = null;
        for (String key : dataMap.keySet()) {
            String val = dataMap.get(key);
            if (key == null)
                continue;
            if (val == null)
                val = "";

            if (res == null) {
                res = key + KV_SEPARATOR + val;
            } else {
                res += PAIRS_SEPARATOR + key + KV_SEPARATOR + val;
            }
        }

        return res;
    }

    @Override
    public Map<String, String> convertToEntityAttribute(String dbData) {

        log.debug("convert to entity attr. dbData: {}", dbData);
        Map<String, String> dataMap = new HashMap<>();
        if (dbData == null)
            return dataMap;
        String[] kvPairs = dbData.split(PAIRS_SEPARATOR_REGEX);
        for (String kv : kvPairs) {
            String[] fields = kv.split(KV_SEPARATOR);
            if (fields.length == 0 || fields.length == 1)
                continue;
            else {
                String key = fields[0].trim();
                String val = fields[1].trim();
                dataMap.put(key, val);
            }
        }
        log.debug("dataMap: {}", dataMap);

        return dataMap;
    }

}
