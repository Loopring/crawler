package org.loopring.crawler.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

import org.loopring.crawler.core.Selector.ValueType;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JsoupParser {

    private Element element;

    private Selector selector;

    public JsoupParser(@NonNull Element element, @NonNull Selector selector) {

        this.element = element;
        this.selector = selector;
    }

    public Map<String, String> parse() {

        Map<String, String> resMap = new HashMap<>();
        String name = selector.getName();
        if (name == null || "".equals(name.trim())) {
        } else {
            try {
                String value = getElementAttrOrValue();
                ValueParser vp = selector.getValueParser();
                if (vp != null) {
                    value = vp.parse(value);
                }
                log.debug("element name:{}, selector: {}", element.tagName(), selector);
                log.debug("name: {}, value: {}", name, value);
                if (value != null) {
                    resMap.put(name, value);
                }
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
        }

        List<Selector> childSelectors = selector.getChildSelectors();
        if (childSelectors != null) {
            for (Selector childSelector : childSelectors) {
                try {
                    JsoupParser p = new JsoupParser(element, childSelector);
                    Map<String, String> childResMap = p.parse();
                    resMap.putAll(childResMap);
                } catch (Exception ex) {
                    log.error(ex.getMessage());
                }
            }
        }

        return resMap;
    }

    public Element getSelectedElement() {

        String cssSelector = selector.getCssSelector();
        if (cssSelector == null || "".equals(cssSelector.trim())) {
            return element;
        } else {
            return element.select(cssSelector).first();
        }
    }

    public Elements getSelectedElements() {

        String cssSelector = selector.getCssSelector();
        if (cssSelector == null || "".equals(cssSelector.trim())) {
            return new Elements(element);
        } else {
            return element.select(cssSelector);
        }
    }

    private String getElementAttrOrValue() {

        ValueType valueType = selector.getValueType();
        String cssSelector = selector.getCssSelector();

        Element e = getSelectedElement();

        if (valueType == ValueType.attr) {
            if (e == null)
                return null;
            String attrName = selector.getAttrName();
            return e.attr(attrName);
        } else if (valueType == ValueType.html) {
            if (e == null)
                return null;
            return e.html();
        } else if (valueType == ValueType.owntext) {
            if (e == null)
                return null;
            return e.ownText();
        } else if (valueType == ValueType.constant) {
            return selector.getConstValue();
        } else if (valueType == ValueType.text) {
            //Elements es = getSelectedElements();
            if (e == null)
                return null;
            return e.text();
        } else if (valueType == ValueType.textarray) {
            Elements es = getSelectedElements();
            String res = null;
            for (Element _ele : es) {
                String itemText = _ele.text();
                if (itemText == null)
                    itemText = "";
                itemText = itemText.trim();
                if (res == null) {
                    res = itemText;
                } else {
                    res = res + "," + itemText;
                }
            }
            return res;
        } else if (valueType == ValueType.attrarray) {
            Elements es = getSelectedElements();
            String attrName = selector.getAttrName();
            String res = null;
            for (Element _ele : es) {
                String itemText = _ele.attr(attrName);
                if (itemText == null)
                    itemText = "";
                itemText = itemText.trim();
                if (res == null) {
                    res = itemText;
                } else {
                    res = res + "," + itemText;
                }
            }
            return res;
        } else if (valueType == ValueType.article) {
            Elements es = getSelectedElements();
            String htmlText = es.html();
            String articleText = Jsoup.clean(htmlText, "", new Whitelist().addTags("img").addAttributes("img", "src"), new Document.OutputSettings().prettyPrint(false));
            articleText = org.jsoup.parser.Parser.unescapeEntities(articleText, false);
            return articleText;
        } else {
            log.debug("get value of value type node, return null.");
            return null;
        }
    }

}
