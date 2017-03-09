package com.proxy.source.impl;

import com.downloader.CrawlerLib;
import com.downloader.Downloader;
import com.downloader.Request;
import com.downloader.Response;
import com.proxy.IPModel;
import com.proxy.source.IPFetcher;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2016/12/8.
 */
public class IP66Fetcher implements IPFetcher {

    private static final String SOURCEURL = "http://www.66ip.cn/nmtq.php?getnum=200&isp=0&anonymoustype=3&start=&ports=&export=&ipaddress=&area=1&proxytype=1&api=66ip";
    private static final Pattern ipregex = Pattern.compile("(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}):(\\d{1,4})");

    @Override
    public Set<IPModel> fetchIps() {
        Downloader downloader = new Downloader();
        Request request = new Request(SOURCEURL);
        request.setTimeout(3 * 1000);
        Response process = downloader.process(request);
        Matcher matcher = ipregex.matcher(process.getContent());
        Set<IPModel> resultSet = new HashSet<>();
        getFromMatcher(matcher, resultSet);
        return resultSet;
    }

    public static void main(String[] args) {
        IP66Fetcher fetcher = new IP66Fetcher();
        Set<IPModel> ipModelSet = fetcher.fetchIps();
        int i = fetcher.saveToRedis(ipModelSet);
        CrawlerLib.LOGGER.info("找到ip数： " + i);
    }
}
