package com.scheduler;

import com.downloader.Request;
import com.duplicate.DuplicateRemover;
import com.duplicate.HashSetRemover;
import com.scheduler.inter.Scheduler;

/**
 * Created by Administrator on 2016/10/20.
 */
public abstract class DuplicateRemoverScheduler implements Scheduler {
    private DuplicateRemover<Request> remover = new HashSetRemover<>();

    public DuplicateRemover<Request> getRemover() {
        return remover;
    }

    public void setRemover(DuplicateRemover<Request> remover) {
        this.remover = remover;
    }

    @Override
    public void push(Request request) {
        if (!remover.isDuplicate(request)) {
            pushWhenNoDuplicate(request);
        }
    }

    abstract void pushWhenNoDuplicate(Request request);

}
