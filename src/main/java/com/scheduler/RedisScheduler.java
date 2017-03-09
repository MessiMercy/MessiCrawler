package com.scheduler;

import com.downloader.Request;

/**
 * Created by Administrator on 2017/3/9.
 */
public class RedisScheduler extends RedisAbstractScheduler {

    public RedisScheduler(String host) {
        super(host);
    }

    @Override
    public Request convertToRequest(String key) {
        return GSON.fromJson(key, Request.class);
    }

    @Override
    public String convertToString(Request request) {
        return GSON.toJson(request);
    }
}
