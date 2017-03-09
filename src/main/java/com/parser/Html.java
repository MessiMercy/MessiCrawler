package com.parser;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class Html {
    private Document doc;

    public Html(String html) {
        if (StringUtils.isEmpty(html)) {
            return;
        }
        try {
            if (isUrl(html)) {
                doc = Jsoup.connect(html).get();
            } else {
                doc = Jsoup.parse(html);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private Html() {

    }

    public Html(Document doc) {
        this.doc = doc;
    }

    /**
     * @param isAttrOrText 如果是attr则输入attr的名字， 否则为null
     * @param rule         为css selector标志
     * @deprecated
     */
    public Stack<String> parseHtml(String rule, String isAttrOrText) {
        Stack<String> result = new Stack<>();
        if (doc != null) {
            Elements elements = doc.select(rule);
            if (isAttrOrText != null) {
                for (Element element : elements) {
                    result.push(element.attr(isAttrOrText));
                }
            } else {
                for (Element element : elements) {
                    String str = element.text();
                    if (str.length() < 5) {
                        result.push(element.html());
                    } else {
                        result.push(str);
                    }
                }
            }
        }
        return result;
    }

    public String parseText(String rule) {
        return parseHtml(rule, null).pop();
    }

    public Document getDocument() {
        return doc;
    }

    private void setDocument(Document document) {
        this.doc = document;
    }

    /**
     * @param attrOrText 如果是attr则输入attr的名字， 否则为null
     * @param rule       为css selector
     */
    public List<String> parse(String rule, String attrOrText) {
        List<String> list = new ArrayList<>();
        if (doc == null) return list;
        Elements elements = doc.select(rule);
        if (attrOrText == null || attrOrText.length() == 0) {
            elements.forEach(p -> list.add(p.text()));
        } else {
            elements.forEach(p -> list.add(p.attr(attrOrText)));
        }
        return list;
    }

    public String parse(String rule) {
        return parse(rule, null, 0);
    }

    public String parse(String rule, String attrOrText, int index) {
        List<String> parse = parse(rule, attrOrText);
        return parse == null || parse.size() == 0 ? null : parse.get(0);
    }

    private boolean isUrl(String key) {
        boolean flag = false;
        try {
            URI uri = new URI(key);
            flag = true;
        } catch (URISyntaxException ignored) {
        }
        return flag;
    }

    public static void main(String[] args) throws IOException {
        Html html = null;
        try {
            html = new Html("www.10086.cn/sc/index_280_280.html");
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<String> parse = html.parse("a.lianjie1", "");
        parse.forEach(System.out::println);
    }

}
