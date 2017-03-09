package com.proxy.source.impl;

import com.downloader.HttpConstant;
import com.proxy.IPModel;
import com.proxy.source.IPFetcher;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2016/12/8.
 */
public class KuaiDailiFetcher implements IPFetcher {

    private static final String SOURCEURL = "http://www.kuaidaili.com/free/";
    private static final Pattern ipregex = Pattern.compile("(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}) (\\d{1,4})");

    @Override
    public Set<IPModel> fetchIps() {
        Document document = null;
        try {
            document = Jsoup.connect(SOURCEURL).userAgent(HttpConstant.UserAgent.CHROME).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (document == null) return null;
        String text = document.text();
        System.out.println(text);
        Matcher matcher = ipregex.matcher(text);
        Set<IPModel> resultSet = new HashSet<>();
        getFromMatcher(matcher, resultSet);
        return resultSet;
    }

    public static void main(String[] args) {
        KuaiDailiFetcher fetcher = new KuaiDailiFetcher();
        Set<IPModel> ipModelSet = fetcher.fetchIps();
        int i = fetcher.saveToRedis(ipModelSet);
        System.out.println("存入ip： " + i);
    }
}
