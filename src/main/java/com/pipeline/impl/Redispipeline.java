package com.pipeline.impl;

import com.model.Model;
import com.pipeline.Cachepipeline;
import redis.clients.jedis.Jedis;

import java.util.Collection;

/**
 * 将redis中的数据同步至本地
 * Created by Administrator on 2016/11/30.
 */
public abstract class Redispipeline implements Cachepipeline {
    private Jedis jedis;

    public Redispipeline(Jedis jedis) {
        this.jedis = jedis;
    }

    @Override
    public long getCacheSize(String key) {
        return jedis.llen(key);
    }

    public abstract Collection<Model> getCache(String key);

    @Override
    public abstract long saveToSql(Collection<Model> collection, String key);
}
