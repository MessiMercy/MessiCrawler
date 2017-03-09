package com.proxy.source.impl;

import com.proxy.IPModel;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2016/12/8.
 */
public class GoubanjiaFetcher extends KuaiDailiFetcher {

    private static final String SOURCEURL = "http://www.goubanjia.com/free/gngn/index.shtml";
    private static final Pattern ipregex = Pattern.compile("(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}):(\\d{1,4})");

    public static void main() {
        GoubanjiaFetcher fetcher = new GoubanjiaFetcher();
        Set<IPModel> ipModelSet = fetcher.fetchIps();
        int i = fetcher.saveToRedis(ipModelSet);
        System.out.println("新增ip： " + i);
    }
}
