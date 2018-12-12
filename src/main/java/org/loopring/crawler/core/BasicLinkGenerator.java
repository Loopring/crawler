package org.loopring.crawler.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.loopring.crawler.Utils;
import org.loopring.crawler.models.Link;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class BasicLinkGenerator<L extends Link> implements LinkGenerator<L> {

    private static final String SPLIT_PIPE = "|split";

    private final String urlTemplate; // start from ${0}

    private final String[][] params;

    private final Class<? extends L> linkClass;

    private List<String> repeatedUrls;

    private boolean needSplit;

    @Override
    public List<L> genLinks() {

        List<L> resultList = new ArrayList<>();

        if (urlTemplate == null) {
            //throw new IllegalArgumentException("urlTemplate can not be null in BasicLinkGenerator.");
            return resultList;
        }

        String _urlTemplate = urlTemplate.trim();
        if (_urlTemplate.endsWith(SPLIT_PIPE)) {
            _urlTemplate = _urlTemplate.substring(0, _urlTemplate.length() - SPLIT_PIPE.length());
            needSplit = true;
        }

        if (params == null || params.length == 0) {
            String url = _urlTemplate;
            List<L> linkList = new ArrayList<>();
            List<L> links = urlToLinks(url);
            linkList.addAll(links);

            resultList.addAll(linkList);
            return resultList;
        }

        String[][] filteredParams = filterParams();
        int len = filteredParams.length;
        log.debug("filteredParams len: {}", len);

        if (len == 0)
            return resultList;

        String[] firstParams = filteredParams[0];
        if (len == 1) {
            List<L> linkList = new ArrayList<>();
            for (String p : firstParams) {
                String mark = "\\$\\{0\\}";
                String url = _urlTemplate.replaceAll(mark, p);
                List<L> links = urlToLinks(url);
                linkList.addAll(links);
            }
            resultList.addAll(linkList);
        } else {
            String[][] restParams = Arrays.<String[]>copyOfRange(filteredParams, 1, len);
            for (String p : firstParams) {
                List<String> templateList = new ArrayList<>();
                String mark = "\\$\\{0\\}";
                String newTemplate = _urlTemplate.replaceAll(mark, p);
                templateList.add(newTemplate);
                List<String> urls = genUrls(templateList, restParams, 1);
                List<L> linkList = new ArrayList<>();
                for (String url : urls) {
                    List<L> links = urlToLinks(url);
                    linkList.addAll(links);
                }
                resultList.addAll(linkList);
            }
        }

        return resultList;
    }

    @Override
    public List<L> getRepeatedCrawlLinks() {

        List<L> repeatedLinks = new ArrayList<>();

        if (repeatedUrls != null && repeatedUrls.size() > 0) {
            for (String url : repeatedUrls) {
                L l = urlToLink(url);
                if (l != null) {
                    repeatedLinks.add(l);
                }
            }
        }

        return repeatedLinks;
    }

    private List<String> genUrls(List<String> templateList, String[][] allParams, int markIndex) {

        int len = allParams.length;
        if (len == 0) {
            return templateList;
        } else {
            List<String> resList = new ArrayList<>();
            String[] firstParams = allParams[0];
            String[][] restParams = Arrays.<String[]>copyOfRange(allParams, 1, len);
            for (String temp : templateList) {
                for (String p : firstParams) {
                    String mark = "\\$\\{" + markIndex + "\\}";
                    String newTemplate = temp.replaceAll(mark, p);
                    resList.add(newTemplate);
                }
            }
            return genUrls(resList, restParams, markIndex++);
        }
    }

    private List<L> urlToLinks(String url) {

        List<L> resList = new ArrayList<>();

        if (needSplit) {
            if (url != null && url.contains(",")) {
                String[] urls = url.split(",");
                for (String _url : urls) {
                    L l = urlToLink(_url);
                    if (l != null) {
                        resList.add(l);
                    }
                }
            } else {
                L l = urlToLink(url);
                if (l != null) {
                    resList.add(l);
                }
            }
        } else {
            L l = urlToLink(url);
            if (l != null) {
                resList.add(l);
            }
        }

        return resList;
    }

    private L urlToLink(String url) {

        L l = null;
        try {
            l = linkClass.newInstance();
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        String uuid = Utils.fingerPrint(url);
        l.setUrl(url);
        l.setUuid(uuid);
        return l;
    }

    private String[][] filterParams() {

        List<String[]> paramList = new ArrayList<>();
        for (String[] paramItems : params) {
            if (paramItems != null && paramItems.length > 0) {
                paramList.add(paramItems);
            }
        }
        return paramList.toArray(new String[paramList.size()][]);
    }

}
