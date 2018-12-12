package org.loopring.crawler.core.configparser;

import java.util.ArrayList;
import java.util.List;

import org.loopring.crawler.core.BasicLinkGenerator;
import org.loopring.crawler.core.LinkGenerator;
import org.loopring.crawler.core.UrlParamGenerator;
import org.loopring.crawler.core.UrlParamType;
import org.loopring.crawler.core.config.LinkGeneratorConfig;
import org.loopring.crawler.core.config.TemplateParamConfig;
import org.loopring.crawler.models.Link;
import org.loopring.crawler.service.JpaDataService;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class LinkGeneratorConfigParser {

    private final String taskName;

    private final LinkGeneratorConfig config;

    private final String sourceSiteName;

    private JpaDataService jpaDataService;

    public LinkGenerator<Link> parse() throws Exception {

        List<String> repeatedCrawlUrls = config.getRepeatedCrawlUrls();
        String urlTemplate = config.getLinkUrlTemplate();
        List<TemplateParamConfig> paramsConfig = config.getParamsConfig();
        String[][] params = paramsConfigToParams(paramsConfig);
        BasicLinkGenerator<Link> basicLinkGen = new BasicLinkGenerator<Link>(urlTemplate, params, Link.class);
        basicLinkGen.setRepeatedUrls(repeatedCrawlUrls);
        return basicLinkGen;
    }

    private String[][] paramsConfigToParams(List<TemplateParamConfig> paramsConfig) throws Exception {

        if (paramsConfig == null || paramsConfig.size() == 0)
            return null;
        int len = paramsConfig.size();
        String[][] result = new String[len][];

        for (int i = 0; i < len; i++) {
            TemplateParamConfig tc = paramsConfig.get(i);
            String paramTypeStr = tc.getParamTypeStr();
            UrlParamType pt = UrlParamType.valueOf(paramTypeStr);
            if (UrlParamType.constStringSeq == pt) {
                result[i] = tc.getParams();
            } else if (UrlParamType.numberSeq == pt) {
                int start = tc.getStart();
                int end = tc.getEnd();
                int step = tc.getStep();
                List<String> paramList = new ArrayList<>();
                for (int j = start; j <= end; j += step) {
                    paramList.add(j + "");
                }
                result[i] = paramList.toArray(new String[paramList.size()]);
            } else if (UrlParamType.dbData == pt) {
                String entityClassName = tc.getEntityClassName();
                String fieldName = tc.getFieldName();
                String valueParserClass = tc.getValueParserClass();
                List<String> fieldValues = jpaDataService.fieldValuesFromEntity(taskName, entityClassName, fieldName, sourceSiteName, valueParserClass);

                result[i] = fieldValues.toArray(new String[fieldValues.size()]);
            } else if (UrlParamType.generator == pt) {
                String generatorClass = tc.getGeneratorClass();
                UrlParamGenerator generator = (UrlParamGenerator) Class.forName(generatorClass).newInstance();
                result[i] = generator.generate();
            } else {
                throw new IllegalArgumentException("invalid paramTypeStr: " + paramTypeStr);
            }
        }

        return result;
    }

}
