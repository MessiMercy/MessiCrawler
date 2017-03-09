package com.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 正则解析器
 * Created by Administrator on 2017/1/3.
 */
public class Regex {
    private String regex;
    private Matcher matcher;

    public Regex(String regex, String content) {
        this.regex = regex;
        matcher = Pattern.compile(regex).matcher(content);
    }

    public boolean fullMatches() {
        return matcher.matches();
    }

    public boolean containMatches() {
        return matcher.lookingAt();
    }

    public List<String> toList(int group) {
        List<String> result = new ArrayList<>();
        System.out.println("准备匹配" + this.regex);
        while (matcher.find()) {
            result.add(matcher.group(group));
        }
        return result;
    }

    public static void main(String[] args) {
        String ts = "cat cat cat cattie cat";
        Matcher a = Pattern.compile("a").matcher(ts);
        String ab = a.replaceAll("ab");
        System.out.println(String.format("orignal: %s,new: %s", ts, ab));
    }

    public String getRegex() {
        return matcher.pattern().pattern();
    }

    public void setRegex(String regex) {
        matcher.usePattern(Pattern.compile(regex));
    }

    public Matcher getMatcher() {
        return matcher;
    }

    public void setMatcher(Matcher matcher) {
        this.matcher = matcher;
    }
}
