package com.duplicate;

import com.downloader.Request;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnel;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

public class BloomFilterRemover<T> implements DuplicateRemover<T> {
    private int maxCount;// 预估最大数据量
    private AtomicInteger count;// int的线程安全类

    private BloomFilter<T> filter;

    public BloomFilterRemover(int maxCount) {
        this.maxCount = maxCount;
        filter = refreshFilter(maxCount);
    }

    @SuppressWarnings("serial")
    private BloomFilter<T> refreshFilter(int maxCount) {
        // filter =
        // BloomFilter.create(Funnels.stringFunnel(Charset.defaultCharset()),
        // maxCount);
        filter = BloomFilter.create((Funnel<T>) (arg0, arg1) -> {
            if (arg0 instanceof Request) {
                arg1.putString(((Request) arg0).getUrl() + ((Request) arg0).getMethod(),
                        Charset.forName(((Request) arg0).getCharset()));
            } else {
                arg1.putString(arg0.toString(), Charset.defaultCharset());
            }
        }, maxCount);
        return filter;
    }

    @Override
    public boolean isDuplicate(T request) {
        boolean flag = true;
        if (!filter.mightContain(request)) {
            count.incrementAndGet();
            filter.put(request);
        }
        return flag;
    }

    @Override
    public void resetDuplicateCheck() {
        refreshFilter(getMaxCount());
    }

    @Override
    public int getRemoverCount() {
        // TODO Auto-generated method stub
        return count.get();
    }

    @Override
    /**
     * 遍历collection加入filter
     * */
    public int addCollections(Collection<T> collection) {
        final int[] i = {0};
        collection.forEach(p -> {
            filter.mightContain(p);
            i[0]++;
        });
        return i[0];
    }

    public int getMaxCount() {
        return maxCount;
    }

    public AtomicInteger getCount() {
        return count;
    }

}
