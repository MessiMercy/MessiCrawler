package com.scheduler;

import com.downloader.Request;
import com.duplicate.HashSetRemover;
import com.scheduler.inter.Scheduler;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

public class PriorityScheduler implements Scheduler {
    private LinkedBlockingQueue<Request> queue;
    private PriorityBlockingQueue<Request> priorityQueue;// 用来插队
    private HashSetRemover<Request> remover;

    public PriorityScheduler() {
        remover = new HashSetRemover<>();
        queue = new LinkedBlockingQueue<>();
        priorityQueue = new PriorityBlockingQueue<>();
    }

    public void test() {

    }



    @Override
    public void push(Request request) {
        if (request.isNeedRecycle()) {
            try {
                Thread.sleep(request.getSleepTime());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            priorityQueue.put(request);
        } else {
            try {
                if (!remover.isDuplicate(request)) {
                    queue.put(request);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Request poll() {
        Request result = null;
        if (priorityQueue.size() == 0) {
            try {
                result = queue.poll(60, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            try {
                result = priorityQueue.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return result;
    }


    public int getSchedulerSize() {
        return queue.size() + priorityQueue.size();
    }

    public int priortySize() {
        return priorityQueue.size();
    }

    public HashSetRemover<Request> getRemover() {
        return remover;
    }

    public void setRemover(HashSetRemover<Request> remover) {
        this.remover = remover;
    }

}
