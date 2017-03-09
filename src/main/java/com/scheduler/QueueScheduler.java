package com.scheduler;

import com.downloader.Request;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by Administrator on 2016/10/20.
 */
public class QueueScheduler extends DuplicateRemoverScheduler {
    private BlockingQueue<Request> queue = new LinkedBlockingDeque<>();


    @Override
    void pushWhenNoDuplicate(Request request) {
        queue.add(request);
    }


    @Override
    public synchronized Request poll() {
        return queue.poll();
    }

    @Override
    public int getSchedulerSize() {
        return queue.size();
    }

    public BlockingQueue<Request> getQueue() {
        return queue;
    }

    public void setQueue(BlockingQueue<Request> queue) {
        this.queue = queue;
    }

    public int getRemoverSize() {
        return getRemover().getRemoverCount();
    }

}
