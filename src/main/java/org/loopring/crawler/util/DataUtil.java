package org.loopring.crawler.util;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import org.loopring.crawler.Utils;
import org.loopring.crawler.models.BasicModel;
import org.loopring.crawler.models.NewsInfo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DataUtil {
	
	public static String[] tagArray = {
		"LRC,LRN,路印",
	    "ETH,Ethereum,WETH,以太坊",
	    "BTC,Bitcoin,WBTC,比特币",
	    "0x",
	    "VITE",
	    "EOS",
	    "RDN,雷电,闪电网络",
	    "GTO",
	    "XRP,Ripple,瑞波",
	    "USDT,TUSD,USDC",
	    "NEO,ONT,Ontology,小蚁,本体",
	    "BTM,比原链",
	    "MYTOKEN,MT",
	    "TRX,TRON,波场",
	    "MANA,Decentraland",
	    "SALT",
	    "APPC,AppCoins",
	    "ARP,阿普协议"
	    };
	
	public static int MAX_TAG_LENGTH = 100;

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
            // watched_link 里没有title, 所以使用url, news_info为了尽可能减少重复的新闻，所以用title
            if (dataMap.get("title") == null) {
                uuidFields = "url";
            } else {
                uuidFields = "title";
            }
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
    
    public static void genTagForNews(Map<String, String> dataMap, Class<? extends BasicModel> dataClass) {
    	if(dataClass != NewsInfo.class) {
    		return;
    	}
    	StringBuffer tagValue = new StringBuffer("区块链");
        String title = dataMap.get("title");

        for (String tag : tagArray) {
        	String[] keys = tag.split(",");
        	for(String key : keys) {
        		if(containsIgnoreCase(title, key)) {
        			tagValue.append(",").append(tag);
        			break;
        		}
        	}
        	if (tagValue.length() > MAX_TAG_LENGTH) {
        		break;
        	}
        }
    	//tagValue.append(",").append(title);
        dataMap.put("tags", tagValue.toString());
        return;
    }
    
    public static boolean containsIgnoreCase(String str, String searchStr) {
        if(str == null || searchStr == null) return false;

        final int length = searchStr.length();
        if (length == 0)
            return true;

        for (int i = str.length() - length; i >= 0; i--) {
            if (str.regionMatches(true, i, searchStr, 0, length))
                return true;
        }
        return false;
    }
}
