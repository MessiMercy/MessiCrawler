package com.proxy.source;

import com.google.gson.Gson;
import com.proxy.IPModel;
import com.proxy.validate.IPValidater;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Set;
import java.util.regex.Matcher;

/**
 * Created by Administrator on 2016/12/5.
 */
public interface IPFetcher {
    /**
     * 默认连接池
     */
    JedisPool POOL = new JedisPool("127.0.0.1", 6379);

    Set<IPModel> fetchIps();

    default int saveToRedis(Set<IPModel> modelSet) {
        if (modelSet == null || modelSet.size() == 0) return 0;
        IPValidater validater = new IPValidater();
        Gson gson = new Gson();
        final int[] i = {0};
        try (Jedis jedis = POOL.getResource()) {
            modelSet.forEach(p -> {
                jedis.sadd("ip", gson.toJson(p));
                i[0]++;
            });
        }
        return i[0];
    }

    default void getFromMatcher(Matcher matcher, Set<IPModel> resultSet) {
        while (matcher.find()) {
            String host = matcher.group(1);
            String port = matcher.group(2);
            IPModel model = new IPModel();
            model.setHost(host);
            model.setPort(Integer.valueOf(port));
            model.setCreatedTime(System.currentTimeMillis());
            resultSet.add(model);
        }
    }
}
