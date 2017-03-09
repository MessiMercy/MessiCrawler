package com.proxy;

import com.downloader.CrawlerLib;
import com.google.gson.Gson;
import com.proxy.pool.IPPool;
import com.proxy.source.IPFetcher;
import com.proxy.source.impl.GoubanjiaFetcher;
import com.proxy.source.impl.IP66Fetcher;
import com.proxy.source.impl.KuaiDailiFetcher;
import com.proxy.source.impl.XiciFetcher;
import com.proxy.validate.IPValidater;
import com.proxy.validate.MultiValidate;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * 用于驱动整个代理池
 * Created by Administrator on 2016/12/8.
 */
public class IPRunner {
    private static ExecutorService service = Executors.newFixedThreadPool(100);
    private static JedisPool pool = new JedisPool("127.0.0.1");
    private static Gson gson = new Gson();

    public static void main(String[] args) {
        while (!service.isShutdown()) {
            addSeed();
            validate();
            CrawlerLib.LOGGER.info("完成一次检测周期，休眠10分钟！");
            try {
                Thread.sleep(10 * 60 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void addSeed() {
        IPFetcher xicifetcher = new XiciFetcher(), ip66 = new IP66Fetcher(), kuaidaili = new KuaiDailiFetcher(), goubanjia = new GoubanjiaFetcher();
        List<IPFetcher> list = new ArrayList<>();
        list.add(xicifetcher);
        list.add(ip66);
        list.add(kuaidaili);
        list.add(goubanjia);
        list.forEach(p -> {
            Set<IPModel> ipModelSet = new HashSet<>();
            try {
                ipModelSet = p.fetchIps();
            } catch (Exception ignore) {
            }
            int i = p.saveToRedis(ipModelSet);
            CrawlerLib.LOGGER.info("成功添加ip数： " + i);
        });
    }

    private static void validate() {
        IPValidater validater = new IPValidater();
        IPPool pool = new IPPool();
        Set<IPModel> allIP = pool.getAllIP();
        List<Future> taskList = new ArrayList<>();
        allIP.forEach(p -> {
            Runnable r = new MultiValidate(p, validater);
            Future<?> submit = service.submit(r);
            taskList.add(submit);
        });
        CrawlerLib.LOGGER.info("从redis中找到ip个数： " + allIP.size());
        taskList.forEach(p -> {
            try {
                p.get(10, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                System.out.println("一个任务超时，将跳过");
            }
        });
        List<IPModel> collect = allIP.parallelStream().filter(p -> p.getCreatedTime() != 0).collect(Collectors.toList());
        int i = validater.refreshAvailableIp(collect);
        CrawlerLib.LOGGER.info("刷新ip成功，新验证ip个数： " + i);
        try (Jedis jedis = IPRunner.pool.getResource()) {
            Set<String> ip = jedis.sdiff("ip");
            jedis.del("availableIp");
            ip.forEach(p -> {
                IPModel model = gson.fromJson(p, IPModel.class);
                if (model.getLastCheckedTime() != 0 && model.getCreatedTime() != 0) {
                    jedis.sadd("availableIp", gson.toJson(model));
                }
            });
            jedis.sadd("availableIp", gson.toJson(new IPModel("local", 0)));
            jedis.sadd("availableIp", gson.toJson(new IPModel("127.0.0.1", 8087)));
        }
    }
}
