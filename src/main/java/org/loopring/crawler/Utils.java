package org.loopring.crawler;

import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Utils {

    private static final Logger LOG = LoggerFactory.getLogger(Utils.class);

    private static final SimpleDateFormat yearSDF = new SimpleDateFormat("yyyy");

    private static final SimpleDateFormat daySDF = new SimpleDateFormat("yyyy-MM-dd");

    public static String fingerPrint(String source) {

        if (source == null)
            return null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] sha1 = md.digest(source.getBytes("UTF8"));
            return DatatypeConverter.printBase64Binary(sha1);
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
            return "";
        }
    }

    public static String trimUnicodeSurrogates(String src) {

        if (src == null)
            return null;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < src.length(); i++) {
            char ch = src.charAt(i);
            if (!Character.isHighSurrogate(ch) && !Character.isLowSurrogate(ch)) {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    public static String trimStr(String src) {

        if (src == null)
            return null;
        return src.trim();
    }

    public static String joinStrs(String[] arr, String sep) {

        if (arr == null)
            return "";
        String res = "";
        for (String s : arr) {
            if ("".equals(res)) {
                res = s;
            } else {
                res = res + sep + s;
            }
        }
        return res;
    }

    public static String joinStrs(Collection<String> strs, String sep) {

        if (strs == null)
            return "";
        String[] arr = strs.toArray(new String[strs.size()]);
        return joinStrs(arr, sep);
    }

    public static String parseDateFromTimeStr(String timeStr) {

        if (timeStr == null || timeStr.trim().equals(""))
            return "";
        timeStr = timeStr.replaceAll("年", "-");
        timeStr = timeStr.replaceAll("月", "-");
        timeStr = timeStr.replaceAll("日", "");

        String[] regexes = new String[]{"\\d{4}-\\d{2}-\\d{2}", "\\d{4}-\\d{1}-\\d{2}", "\\d{4}-\\d{1}-\\d{1}", "\\d{4}-\\d{2}-\\d{1}", "\\d{2}-\\d{2}"};

        //String dateRegex = "^\\d{4}\\-(0?[1-9]|1[012])\\-(0?[1-9]|[12][0-9]|3[01])$";
        for (String dateRegex : regexes) {
            Pattern pattern = Pattern.compile(dateRegex);
            Matcher matcher = pattern.matcher(timeStr);
            if (matcher.find()) {
                if (regexes[4].equals(dateRegex)) {
                    String year = yearSDF.format(new Date());
                    return year + "-" + matcher.group();
                } else {
                    return matcher.group();
                }
            }
        }
        return "";
    }

    public static String getCurrentDate() {

        return daySDF.format(new Date());
    }

    public static String dayStrOfDate(Date d) {

        if (d == null)
            return "";
        return daySDF.format(d);
    }

    public static boolean isEmptyOrSpace(String s) {

        return s == null || s.trim().length() == 0;
    }

    public static List<String[]> allCompose(String[][] sourceData) {

        int dimension = sourceData.length;
        List<String[]> result = new ArrayList<>();
        for (int i = 0; i < dimension; i++) {
            List<String[]> tmpResult = new ArrayList<>();
            String[] singleData = sourceData[i];
            for (int j = 0; j < singleData.length; j++) {
                if (i == 0) {
                    String[] compose = new String[dimension];
                    compose[i] = singleData[j];
                    tmpResult.add(compose);
                } else {
                    for (int k = 0; k < result.size(); k++) {
                        String[] oldCompose = result.get(k);
                        String[] newCompose = new String[dimension];
                        System.arraycopy(oldCompose, 0, newCompose, 0, i);
                        newCompose[i] = singleData[j];
                        tmpResult.add(newCompose);
                    }
                }
            }
            result = tmpResult;
        }

        return result;
    }

    public static String concatHttpUrl(String base, String tail) {

        if (tail == null)
            throw new IllegalArgumentException("url must not be null:" + tail);
        if (tail.startsWith("http"))
            return tail;
        if (base == null) {
            throw new IllegalArgumentException("base url must not be null:" + base);
        }
        String url = null;
        tail = tail.replaceAll("\\.\\.", "\\.");

        if (base.endsWith("/") && tail.startsWith("/")) {
            url = base + tail.substring(1);
        } else if (!base.endsWith("/") && !tail.startsWith("/")) {
            url = base + "/" + tail;
        } else {
            url = base + tail;
        }
        return url;
    }

}
