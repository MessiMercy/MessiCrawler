package com.processer;

import com.downloader.Downloader;
import com.downloader.Request;
import com.downloader.Response;
import com.downloader.Spider;
import com.parser.Html;
import com.processer.inter.Processer;
import com.scheduler.inter.Scheduler;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Mercy on 2016/10/25.
 */
public class IacProcesser implements Processer {
    private Spider spider;

    public static void main(String[] args) {
        IacProcesser processer = new IacProcesser();
        processer.spider = new Spider(processer, 1);
        processer.addRequests(processer.spider.getScheduler(), new ArrayList());
        System.out.println(processer.spider.getScheduler().getSchedulerSize());
        processer.spider.setThreadNum(5);
        processer.spider.setEmptySleepTime(1000);
        processer.spider.run();
    }

    @Override
    public void processResponse(Response response) {
        Html html = null;
        try {
            html = new Html(response.getContent());
        } catch (Exception e) {
            e.printStackTrace();
        }
        String text = html.parseText("#p_456547");
        System.out.println("procecing........");
        System.out.println(text);
    }

    @Override
    public void addRequests(Scheduler scheduler, Response response) {

    }

    @Override
    public void preprocess(Downloader downloader) {

    }

    @Override
    public boolean isNeedRetry(Response response) {
        return false;
    }

    @Override
    public void stop() {
        if (spider != null) {
            spider.setStop(true);
        }
    }

    @Override
    public void addRequests(Scheduler scheduler, Collection collection) {
        scheduler.push(new Request("http://omron.iacmall.com/?sort=goods_id&order=DESC&page=2"));
    }
}
