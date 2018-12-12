package org.loopring.crawler.util;

import java.util.Map;
import java.util.HashMap;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ConvertUtil {

    public static <T> T mapToBean(Map<String, String> dataMap, Class<T> objClass) {

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        T t = mapper.convertValue(dataMap, objClass);

        return t;
    }

    public static Map<String, Object> beanToMap(Object dataBean) {

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> props = mapper.convertValue(dataBean, Map.class);
        return props;
    }

    public static Map<String, String> beanToMap2(Object dataBean) {
        Map<String, String> resMap = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> props = mapper.convertValue(dataBean, Map.class);
        for (String key : props.keySet()) {
            Object val = props.get(key);
            if (val != null) {
                String valStr = String.valueOf(val);
                resMap.put(key, valStr);
            }
        }
        return resMap;
    }

}
