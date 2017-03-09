package com.pipeline;

import com.model.Model;

import java.util.Collection;

/**
 * 从redis缓存中存入mysql
 * Created by Administrator on 2016/11/30.
 */
public interface Cachepipeline {
    long getCacheSize(String key);

    Collection<Model> getCache(String key);

    long saveToSql(Collection<Model> collection, String key);
}
