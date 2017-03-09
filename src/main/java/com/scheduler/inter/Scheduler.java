package com.scheduler.inter;

import com.downloader.Request;

/**
 * Created by Administrator on 2016/10/20.
 */
public interface Scheduler {

    void push(Request request);

    Request poll();

    int getSchedulerSize();
}
