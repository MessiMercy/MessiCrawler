package com.scheduler;

import com.downloader.Request;
import com.downloader.encrypt.EncryptLib;
import com.duplicate.DuplicateRemover;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Collection;

/**
 * 使用redis实现分布式队列
 * Created by Administrator on 2016/11/24.
 */
public abstract class RedisAbstractScheduler extends DuplicateRemoverScheduler implements DuplicateRemover<Request> {
    private JedisPool pool;
    private static final Logger LOGGER = Logger.getLogger(RedisAbstractScheduler.class);
    private String requestSet;
    private String requestQueue;

    static {
        PropertyConfigurator.configure("log4j.properties");
    }

    public RedisAbstractScheduler(String host) {
        this(new JedisPool(new JedisPoolConfig(), host));//默认端口6379
    }

    public RedisAbstractScheduler(String host, int port) {
        this(new JedisPool(host, port));
    }

    public RedisAbstractScheduler(JedisPool pool) {
        this.pool = pool;
    }

    public RedisAbstractScheduler(JedisPool pool, String keyWord) {
        this(pool);
        if (keyWord != null) {
            requestQueue = keyWord + "Queue";
            requestSet = keyWord + "Set";
        } else {
            requestSet = "RequestSet";
            requestQueue = "RequestQueue";
        }
    }

    public RedisAbstractScheduler(String host, int port, String keyWord) {
        this(new JedisPool(host, port), keyWord);
    }

    /**
     * 将request tostring之后再md5结果存入redis的set中，用于排重
     */
    @Override
    public boolean isDuplicate(Request request) {
        String md5Result = EncryptLib.md5(request.toString());
        boolean isDuplicate = false;
        try (Jedis jedis = pool.getResource()) {
            isDuplicate = jedis.sadd(requestSet, md5Result) == 0;
        }
        return isDuplicate;
    }

    @Override
    public void resetDuplicateCheck() {
        try (Jedis jedis = pool.getResource()) {
            jedis.del(requestSet);
            getRemover().resetDuplicateCheck();
            LOGGER.info("重置查重器！");
        }
    }

    @Override
    public int getRemoverCount() {
        try (Jedis jedis = pool.getResource()) {
            int requestSetSize = jedis.scard(requestSet).intValue();
            return Math.max(this.getRemover().getRemoverCount(), requestSetSize);
        }
    }


    @Override
    public int addCollections(Collection<Request> collection) {
        try (Jedis jedis = pool.getResource()) {
            final int[] count = {0};
            collection.forEach(p -> {
                jedis.sadd(requestSet, EncryptLib.md5(p.toString()));
                jedis.lpush(requestQueue, convertToString(p));
                count[0]++;
            });
            return count[0];
        }
    }


    @Override
    public void push(Request request) {
        if (!getRemover().isDuplicate(request) && !isDuplicate(request)) {
            pushWhenNoDuplicate(request);
        } else {
            LOGGER.info("检测到重复请求： " + request.getUrl());
        }
    }

    @Override
    public synchronized Request poll() {
        try (Jedis jedis = pool.getResource()) {
            String requestJson = jedis.lpop(requestQueue);
            return convertToRequest(requestJson);
        } catch (Exception e) {
            LOGGER.error(e.toString());
        }
        return null;
    }

    @Override
    public int getSchedulerSize() {
        try (Jedis jedis = pool.getResource()) {
            return jedis.llen(requestQueue).intValue();
        }
    }

    @Override
    void pushWhenNoDuplicate(Request request) {
        try (Jedis jedis = pool.getResource()) {
            String json = convertToString(request);
            jedis.lpush(requestQueue, json);
        }
    }

    public JedisPool getPool() {
        return pool;
    }

    public void setPool(JedisPool pool) {
        this.pool = pool;
    }

    public abstract Request convertToRequest(String key);

    public abstract String convertToString(Request request);
}
