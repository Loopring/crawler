package org.loopring.crawler.common;

import org.loopring.crawler.core.ValueParser;

public class JsonPathArrayFormat implements ValueParser {
    @Override
    public String parse(String institutionIds) {
        if (institutionIds == null) return null;
        institutionIds = institutionIds.replaceAll("\\]", "");
        institutionIds = institutionIds.replaceAll("\\[", "");
        institutionIds = institutionIds.replaceAll("\"", "");
        return institutionIds;
    }
}
