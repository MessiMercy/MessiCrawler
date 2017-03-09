package com.processer;

import com.downloader.Downloader;
import com.downloader.Request;
import com.downloader.Response;
import com.downloader.Spider;
import com.parser.Html;
import com.pipeline.Filepipeline;
import com.pipeline.impl.SimpleFilepipeline;
import com.processer.inter.Processer;
import com.scheduler.RedisScheduler;
import com.scheduler.inter.Scheduler;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.File;
import java.util.Collection;

/**
 * Created by Administrator on 2016/12/1.
 */
public class TestProcesser implements Processer {
    Filepipeline filepipeline = new SimpleFilepipeline(new File("downjoy.txt"));

    public static void main(String[] args) {
        TestProcesser pp = new TestProcesser();
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(10000);
        JedisPool pool = new JedisPool(config, "127.0.0.1", 6379);
        RedisScheduler scheduler = new RedisScheduler(pool);
        Spider downjoySpider = new Spider(pp);
        downjoySpider.setScheduler(scheduler);
        downjoySpider.setThreadNum(2);
        downjoySpider.setAutoAddRequest(true).setSeedUrlRegex("http://android.d.cn/game/\\d+.html");
        downjoySpider.run();
    }

    @Override
    public void processResponse(Response response) {
        Html html = new Html(response.getContent());
        String title = html.parse("h1", null, 0);
        String desc = html.parse("div.de-intro-inner", null, 0);
        String detail = html.parse("ul.de-game-info", null, 0);
        synchronized (this) {
            filepipeline.printResult("title: " + title, true);
            filepipeline.printResult("desc: " + desc, true);
            filepipeline.printResult("detail: " + detail, true);
        }
    }

    @Override
    public void addRequests(Scheduler scheduler, Response response) {

    }

    @Override
    public void addRequests(Scheduler scheduler, Collection<Request> collection) {
        collection.forEach(scheduler::push);
    }

    @Override
    public void preprocess(Downloader downloader) {

    }

    @Override
    public boolean isNeedRetry(Response response) {
        return false;
    }
}
