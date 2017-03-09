package com.processer.inter;

import com.downloader.Downloader;
import com.downloader.Request;
import com.downloader.Response;
import com.scheduler.inter.Scheduler;

import java.util.Collection;

/**
 * Created by Administrator on 2016/10/20.
 * 用于存放针对各网站的解析策略，实现此接口后将其传给spider以多线程执行。
 */
public interface Processer {

    /**
     * 负责存储信息
     */
    void processResponse(Response response);

    /**
     * 负责在存储信息中解析出种子url并加入
     */
    void addRequests(Scheduler scheduler, Response response);

    /**
     * 手动加入
     */
    void addRequests(Scheduler scheduler, Collection<Request> collection);

    /**
     * 用spider内置Downloader执行预操作,例如登录等操作
     */
    void preprocess(Downloader downloader);

    /**
     * 根据返回的response决定此request是否需要重试
     */
    boolean isNeedRetry(Response response);
}
