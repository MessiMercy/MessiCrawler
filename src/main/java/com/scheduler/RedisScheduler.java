package com.scheduler;

import com.downloader.Request;
import com.google.gson.Gson;
import redis.clients.jedis.JedisPool;

/**
 * Created by Administrator on 2017/3/9.
 */
public class RedisScheduler extends RedisAbstractScheduler {
    private static final Gson GSON = new Gson();

    public RedisScheduler(String host) {
        super(host);
    }

    public RedisScheduler(String host, int port) {
        super(host, port);
    }

    public RedisScheduler(JedisPool pool) {
        super(pool);
    }

    public RedisScheduler(JedisPool pool, String keyWord) {
        super(pool, keyWord);
    }

    public RedisScheduler(String host, int port, String keyWord) {
        super(host, port, keyWord);
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
