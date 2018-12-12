package org.loopring.crawler.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JsoupLoginManager {

    private static Map<String, Map<String, String>> cookiesCache = new ConcurrentHashMap<>();

    public static Map<String, String> getCookies(String siteName) throws Exception {

        if (siteName == null)
            return null;

        Map<String, String> cookies = cookiesCache.get(siteName);
        switch (siteName) {
            case "xiniudata":
                boolean isLogin = checkLoginStatus(siteName);
                if (isLogin) {
                    if (cookies == null) {
                        cookies = doLoginAndGetCookies(siteName);
                        cookiesCache.put(siteName, cookies);
                    }
                } else {
                    cookies = doLoginAndGetCookies(siteName);
                    cookiesCache.put(siteName, cookies);
                }
                break;

            default:
                //log.error("no login method configed for site: {}", siteName);
                if (cookiesCache.containsKey(siteName)) {
                    cookies = cookiesCache.get(siteName);
                } else {
//                    throw new IllegalArgumentException("no login method configed for site:" + siteName);
                    return null;
                }
        }

        return cookies;
    }

    public static void updateCookies(String siteName, Map<String, String> cookies) {
        cookiesCache.put(siteName, cookies);
    }

    private static boolean checkLoginStatus(String siteName) throws Exception {

        boolean login = false;

        switch (siteName) {
            case "xiniudata":
                String statusJson = Jsoup.connect("http://www.xiniudata.com/api/user/login/checkloginstatus")
                        .ignoreContentType(true)
                        .header("Content-Type", "application/json")
                        .method(Connection.Method.POST)
                        .requestBody("{\"payload\":{}}")
                        .execute()
                        .body();
                ReadContext ctx = JsonPath.parse(statusJson);
                String isLogin = ctx.read("$.login") + "";
                if ("true".equalsIgnoreCase(isLogin)) {
                    login = true;
                }
                break;

            default:
                throw new IllegalArgumentException("no check login method configed for site:" + siteName);
        }

        return login;
    }

    private static Map<String, String> doLoginAndGetCookies(String siteName) throws Exception {

        Map<String, String> cookies = null;

        switch (siteName) {
            case "xiniudata":
                Connection.Response res = Jsoup.connect("http://www.xiniudata.com/api/user/login/verify")
                        .ignoreContentType(true)
                        .header("Content-Type", "application/json")
                        .method(Connection.Method.POST)
                        .requestBody("{\"payload\":{\"account\":\"15902195002\",\"password\":\"kongque\",\"autoLogin\":true}}")
                        .execute();

                cookies = res.cookies();
                break;

            default:
                throw new IllegalArgumentException("no login method configed for site: " + siteName);
        }

        return cookies;
    }

}
