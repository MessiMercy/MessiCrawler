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
 * 西刺代理提取器
 * Created by Administrator on 2016/12/5.
 */
public class XiciFetcher implements IPFetcher {

    Pattern hostregex = Pattern.compile("(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})");
    Pattern portregex = Pattern.compile("<td>(\\d{1,4})</td>");
    Pattern ipregex = Pattern.compile("(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}) (\\d{1,4})");

    @Override
    public Set<IPModel> fetchIps() {
        Set<IPModel> ipModelSet = new HashSet<>();
        try {
            Document document = Jsoup.connect("http://www.xicidaili.com/nn/").userAgent(HttpConstant.UserAgent.FIREFOX).get();
            String text = document.text();
            Matcher ipMatcher = ipregex.matcher(text);
            while (ipMatcher.find()) {
                IPModel model = new IPModel();
                model.setHost(ipMatcher.group(1));
                model.setPort(Integer.valueOf(ipMatcher.group(2)));
                model.setCreatedTime(System.currentTimeMillis());
                ipModelSet.add(model);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ipModelSet;
    }


    public static void main(String[] args) {
        XiciFetcher fetcher = new XiciFetcher();
        Set<IPModel> ipModelSet = fetcher.fetchIps();
        int i = fetcher.saveToRedis(ipModelSet);
        System.out.println("存入数量： " + i);
    }
}
