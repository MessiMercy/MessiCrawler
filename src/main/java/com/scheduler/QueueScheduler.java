package com.scheduler;

import com.downloader.Request;
import com.google.common.io.Files;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Administrator on 2016/10/20.
 */
public class QueueScheduler extends DuplicateRemoverScheduler {
    private Gson gson;
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

    public synchronized int readFromFile(String fileName) throws IOException {
        List<String> requests = Files.readLines(new File(fileName), Charset.forName("utf-8"));
        AtomicInteger integer = new AtomicInteger(0);
        requests.forEach(p -> {
            Request request = gson.fromJson(p, Request.class);
            queue.offer(request);
            integer.incrementAndGet();
        });
        return integer.get();
    }

    public synchronized void toFile(String fileName) {
        if (queue.size() == 0) return;
        queue.forEach(p -> {
            String requestJson = gson.toJson(p);
            try {
                Files.append(requestJson, new File(fileName), Charset.forName("utf-8"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

}
