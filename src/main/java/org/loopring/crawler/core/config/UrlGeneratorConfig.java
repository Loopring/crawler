package org.loopring.crawler.core.config;

import java.util.List;

import lombok.Data;

@Data
public class UrlGeneratorConfig {

    private String linkUrlTemplate;

    private List<TemplateParamConfig> paramsConfig;
}
