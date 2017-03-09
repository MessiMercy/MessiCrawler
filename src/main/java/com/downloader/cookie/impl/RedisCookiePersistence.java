package com.downloader.cookie.impl;

import com.downloader.cookie.CookiePersistence;
import org.apache.http.impl.client.BasicCookieStore;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * 将file存储于redis中
 * Created by Administrator on 2016/12/26.
 */
public class RedisCookiePersistence implements CookiePersistence {
    private JedisPool pool = new JedisPool("127.0.0.1", 6379);


    @Override
    public void saveCookieStore(String key, BasicCookieStore store) {
        try (Jedis jedis = pool.getResource()) {
            jedis.set(key.getBytes(), serialize(store).toByteArray());//虽然存储键为bytes，但是实际键还是string。可以get（key）方式取出byte的str形式
        }
    }

    @Override
    public BasicCookieStore recoverCookieStore(String key) {
        try (Jedis jedis = pool.getResource()) {
            byte[] objBytes = jedis.get(key.getBytes());//当传入键为byte[]形式的时候，取出的键也是byte[]形式
            return unserialize(key, objBytes);
        }
    }
}
