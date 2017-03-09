package com.proxy.pool;

import com.downloader.Downloader;
import com.downloader.HttpConstant;
import com.downloader.Request;
import com.downloader.Response;
import com.google.gson.Gson;
import com.proxy.IPModel;
import com.proxy.validate.IPValidater;
import org.apache.http.HttpHost;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Administrator on 2016/12/5.
 */
public class IPPool {
    private final Gson gson = new Gson();
    private final JedisPool pool = new JedisPool("127.0.0.1", 6379);
    private final Downloader downloader = new Downloader(HttpConstant.UserAgent.CHROME);

    public Set<IPModel> getAllIP() {
        Set<IPModel> set = new HashSet<>();
        try (Jedis jedis = pool.getResource()) {
            Set<String> ip = jedis.sdiff("ip");
            ip.forEach(p -> set.add(gson.fromJson(p, IPModel.class)));
        }
        return set;
    }

    /**
     * 如果ip没被验证过，则重新取
     *
     * @return 返回ip池中一个随机可用ip
     */
    public IPModel getRandomIp() {
        try (Jedis jedis = pool.getResource()) {
            String ipStr = jedis.srandmember("availableIp");
            IPModel model = gson.fromJson(ipStr, IPModel.class);
            if (model.getLastCheckedTime() == 0) {
                return getRandomIp();
            } else return model;
        }
    }

    public boolean isstrictIP(IPModel model) {
        IPValidater validater = new IPValidater();
        return validater.strictValidate(model);
    }

    public List<IPModel> getRandomIp(int num) {
        try (Jedis jedis = pool.getResource()) {
            List<String> srandmember = jedis.srandmember("availableIp", num);
            return srandmember.stream().map(p -> gson.fromJson(p, IPModel.class)).collect(Collectors.toList());
        }
    }

    public IPModel getAvailableIp(String url, int retryTimes) {
        if (retryTimes == 0) return null;
        IPModel test = getRandomIp();
        Request request = new Request(url);
        request.setTimeout(20 * 1000);
        request.setProxy(new HttpHost(test.getHost(), test.getPort()));
        if (isstrictIP(test)) {
            Response process = downloader.process(request);
            System.out.println(process);
            if ((process.getError() == null || process.getStatusCode() == 200 || process.getStatusCode() == 403))
                return test;
            else return getAvailableIp(url, --retryTimes);
        } else return getAvailableIp(url, --retryTimes);
    }
}
