package org.loopring.crawler.core;

import java.util.ArrayList;
import java.util.List;

import org.loopring.crawler.Utils;
import org.loopring.crawler.models.Link;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class PagingLinkGenerator<L extends Link> implements LinkGenerator<L> {

    private final String urlTemplate; // start from ${0}

    private final Class<L> linkClass;

    private List<String> repeatedUrls;

    private int start = 0;

    private int end = 2000;  // max pagination.

    private int step = 1;

    @Override
    public List<L> genLinks() {

        String mark = "\\$\\{0\\}";
        List<L> linkList = new ArrayList<>();
        //List<List<L>> resList = new ArrayList<>();
        for (int i = start; i < end; i += step) {
            String url = urlTemplate.replaceAll(mark, i + "");
            L l = urlToLink(url);
            if (l != null) {
                linkList.add(l);
            }
        }
        //resList.add(linkList);
        return linkList;
    }

    @Override
    public List<L> getRepeatedCrawlLinks() {

        List<L> repeatedLinks = new ArrayList<>();
        for (String url : repeatedUrls) {
            L l = urlToLink(url);
            if (l != null) {
                repeatedLinks.add(l);
            }
        }

        return repeatedLinks;
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

}
