package com.proxy.validate;

import com.downloader.CrawlerLib;
import com.downloader.Downloader;
import com.downloader.Request;
import com.downloader.Response;
import com.google.gson.Gson;
import com.proxy.IPModel;
import com.proxy.pool.IPPool;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.IOException;
import java.net.Socket;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 周期性验证ip可用性
 * Created by Administrator on 2016/12/5.
 */
public class IPValidater {
    private final CookieStore STORE = new BasicCookieStore();
    private final HttpClient CLIENT = CrawlerLib.getInstanceClient(false, STORE, null, (exception, executionCount, context) -> executionCount < 2);
    private final Downloader DOWNLOADER = new Downloader(CLIENT);
    private final JedisPool pool = new JedisPool("127.0.0.1", 6379);
    private final Gson gson = new Gson();
    //    private final ExecutorService service;
//    private static Collection<IPModel> collection;
    private final static Logger LOGGER = Logger.getLogger(IPValidater.class);
    private final static String strictUrl = "http://1212.ip138.com/ic.asp";

    static {
        PropertyConfigurator.configure("log4j.properties");
    }

    /**
     * 用于取来用时的严格验证，确定是高匿并可用的ip
     *
     * @param model
     * @return
     */
    public boolean strictValidate(IPModel model) {
        boolean isUseful = false;
        Request request = new Request(strictUrl);
        request.setCharset("gb2312");
        request.setProxy(ipmodelToHost(model));
        request.setTimeout(10 * 1000);
        Response process = DOWNLOADER.process(request);
        String content = process.getContent();
        System.out.println(content);
        if (content.contains(model.getHost().substring(0, model.getHost().lastIndexOf(".")))) isUseful = true;
        else if (process.getStatusCode() == 200) {
            try (Jedis jedis = pool.getResource()) {
                Long availableIp = jedis.srem("availableIp", gson.toJson(model));
                if (availableIp != 0) {
                    System.out.println("移除: " + model);
                }
            }
        }
        return isUseful;
    }

    public boolean validate(IPModel model) {
        return validate(model, "https://www.baidu.com");
    }

    public boolean validate(IPModel model, String url) {
        boolean canConnect = false;
        Socket socket = null;
        try {
            socket = new Socket(model.getHost(), model.getPort());
        } catch (IOException e) {
            return canConnect;
        } finally {
            if (socket != null && !socket.isClosed()) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        Request request = new Request(url);
        request.setProxy(ipmodelToHost(model));
        request.setTimeout(5 * 1000);
        Response process = DOWNLOADER.process(request);
        if (process.getContent().length() > 20 && StringUtils.isEmpty(process.getError()) && process.getStatusCode() == 200) {
            canConnect = true;
            model.setLastCheckedTime(System.currentTimeMillis());
        }
        return canConnect;
    }

    public int refreshAvailableIp(Collection<IPModel> collection) {
        final int[] num = {0};
        try (Jedis jedis = pool.getResource()) {
            jedis.del("ip");
            collection.forEach(p -> {
                jedis.sadd("ip", gson.toJson(p));
                num[0]++;
            });
        }
        return num[0];
    }

    public static HttpHost ipmodelToHost(IPModel model) {
        return new HttpHost(model.getHost(), model.getPort());
    }

    public static void main(String[] args) {
        IPValidater validater = new IPValidater();
        IPPool pool = new IPPool();
        while (true) {
            Set<IPModel> allIP = pool.getAllIP();
            LOGGER.info("从redis中找到ip个数： " + allIP.size());
            List<IPModel> collect = allIP.parallelStream().filter(validater::validate).collect(Collectors.toList());
            int i = validater.refreshAvailableIp(collect);
            LOGGER.info("刷新ip成功，新验证ip个数： " + i);
            LOGGER.info("休息5分钟");
            try {
                Thread.sleep(300 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
