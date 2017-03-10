package com.downloader;

import com.downloader.thread.CountableThreadPool;
import com.processer.inter.Processer;
import com.scheduler.QueueScheduler;
import com.scheduler.inter.Scheduler;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 爬虫核心类
 * Created by Administrator on 2016/10/20.
 */
public class Spider implements Runnable {

    /**
     * 设置是否自动往scheduler添加url
     */
    private boolean isAutoAddRequest;
    /**
     * 自动添加url时，regex不能为空
     */
    private String seedUrlRegex;
    private int threadNum = 1;
    private long delayTime = 500;
    private int maxRetryTimes = 3;//默认最大重试次数为3
    private ExecutorService service;
    private Scheduler scheduler = new QueueScheduler();
    private CountableThreadPool pool;
    private Processer processer;
    private Downloader downloader;
    private HttpClient client;
    private boolean isNeedProxy;
    private AtomicInteger retryRequestCounter = new AtomicInteger(0);
    private CookieStore store = new BasicCookieStore();
    private AtomicLong pageCount = new AtomicLong(0);
    private ReentrantLock newUrlLock = new ReentrantLock();
    private Condition newUrlCondition = newUrlLock.newCondition();
    /**
     * 队列为空时等待的时间
     */
    private int emptySleepTime = 20;
    private Date startTime;
    private final Logger logger = Logger.getLogger(this.getClass());

    static {
        PropertyConfigurator.configure("log4j.properties");
    }

    public Spider(Processer processer, int threadNum) {
        this.processer = processer;
        this.threadNum = threadNum;
        init();
    }

    public Spider(Processer processer) {
        this.processer = processer;
        init();
    }


    private void init() {
//        scheduler = new QueueScheduler();
        if (service != null && !service.isShutdown()) {
            this.pool = new CountableThreadPool(threadNum, service);
        } else {
            this.pool = new CountableThreadPool(threadNum);
            service = pool.getService();
        }
        if (downloader == null) {
            downloader = new Downloader(client);
        }
        if (downloader.getClient() == null) {
            downloader.setClient(CrawlerLib.getInstanceClient());
        }
        downloader.setDelayTime(delayTime);
    }

    /**
     * 自动从response中取到符合标准的种子url加入scheduler
     *
     * @param response 从response中解析出符合规则的request
     */
    private List<Request> getRequestFromResponse(Response response) {
        List<Request> resultList = new ArrayList<>();
        Pattern pattern = Pattern.compile(seedUrlRegex);
        Matcher matcher = pattern.matcher(response.getContent());
        String oringnalUrl = response.getUrl();
//        StringUtils.substringBetween(oringnalUrl, "//", "/");
        Request request = response.getRequest();
        while (matcher.find()) {
            String seed = matcher.group();
            if (!seed.startsWith("http") || seed.length() < 10) {
                String host = request.getUrl();
                int ii = host.indexOf("/", 10);
                seed = host.substring(0, ii + 1) + seed;//针对无host的情况
            }
            Request newRequest = null;
            try {
                newRequest = (Request) request.deepClone();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            if (newRequest != null) {
                newRequest.setUrl(seed);
                if (newRequest.getUrl() != null) resultList.add(newRequest);
            }
        }
        return resultList;
    }

    public Spider addRequest(Request... requests) {
        for (Request req : requests) {
            scheduler.push(req);
        }
        return this;
    }

    public Spider addRequest(Collection<Request> collection) {
        if (collection == null || collection.size() == 0) return this;
        collection.forEach(this::addRequest);
        return this;
    }

    private void processRequst(Request request) {
        if (request.isNeedRecycle()) {
            retryRequestCounter.decrementAndGet();
        }
        if (StringUtils.isEmpty(request.getUrl())) {
            return;
        }
        Response response = downloader.process(request);
        if (request.isNeedRecycle() && request.getRetryTimes() < maxRetryTimes) {
            int retryTimes = request.getRetryTimes();
            request.addRetryTimes();
            if (retryRequestCounter.get() < 20) {
                scheduler.push(request);
                retryRequestCounter.incrementAndGet();
            }//一旦队列里面超过20个需重试的url便忽略
        }
        processer.processResponse(response);
        if (isAutoAddRequest && seedUrlRegex != null) {
            List<Request> requests = getRequestFromResponse(response);
            addRequest(requests);
            processer.addRequests(scheduler, response);
        }
        if (processer.isNeedRetry(response)) {
            request.setNeedRecycle(true);
            request.addRetryTimes();
            scheduler.push(request);
        }
    }


    @Override
    public void run() {
//        init();
        processer.preprocess(this.downloader);
        setStartTime(new Date());
        logger.info("开始工作！");
        while (!Thread.currentThread().isInterrupted()) {
            Request request = scheduler.poll();
            logger.info("scheduler size: " + scheduler.getSchedulerSize());
            if (request == null) {
                long a = System.currentTimeMillis();
                waitNewUrl();
                long b = System.currentTimeMillis();
                System.out.println(b - a);
                if (scheduler.getSchedulerSize() <= 1 && pool.getRunningThreads().get() < 1) {
                    logger.info("所有任务已执行完毕！");
                    logger.info("共执行任务数: " + pageCount);
                    long spendTime = System.currentTimeMillis() - getStartTime().getTime();
                    spendTime /= 1000;
                    logger.info("总计耗时： " + (int) spendTime / 3600 + "小时 " + (int) spendTime / 60 % 60 + "分 " + (int) (spendTime % 60) + "秒");
                    break;//执行完任务自动终结
                }
            } else {
                final Request requestFinal = request;
                pool.execute(() -> {
                    try {
                        processRequst(requestFinal);
                        logger.info("执行： " + requestFinal.getUrl());
                    } catch (Exception e) {
                        logger.info("执行： " + requestFinal.getUrl() + "失败！");
                        logger.error(e.getMessage());
                    } finally {
                        pageCount.incrementAndGet();
                        signalNewUrl();
                    }
                });
            }
        }
        if (!service.isShutdown()) {
            try {
                boolean b = service.awaitTermination(1, TimeUnit.MINUTES);
                if (!b) {
                    List<Runnable> runnables = service.shutdownNow();
                    System.out.println(runnables.size());
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("任务执行完毕,关闭service");
        }
    }

    public int getThreadNum() {
        return threadNum;
    }

    private void waitNewUrl() {
        newUrlLock.lock();
        try {
            //double check
            sleep(emptySleepTime);
            newUrlCondition.await(emptySleepTime, TimeUnit.SECONDS);
//            if (pool.getRunningThreads().get() == 0) {
//                return;
//            }
        } catch (InterruptedException e) {
            logger.info("等待新url时中断错误");
            e.printStackTrace();
        } finally {
            newUrlLock.unlock();
        }
    }

    private void signalNewUrl() {
        try {
            newUrlLock.lock();
            newUrlCondition.signalAll();
        } finally {
            newUrlLock.unlock();
        }
    }

    private void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public Spider setThreadNum(int threadNum) {
        if (threadNum >= 1) {
            this.threadNum = threadNum;
        } else {
            logger.info("线程数不能小于1，将以默认值运行");
        }
        return this;
    }

    public ExecutorService getService() {
        return service;
    }

    public void setService(ExecutorService service) {
        this.service = service;
        init();
    }


    public CountableThreadPool getPool() {
        return pool;
    }

    public void setPool(CountableThreadPool pool) {
        this.pool = pool;
    }

    public Processer getProcesser() {
        return processer;
    }

    public void setProcesser(Processer processer) {
        this.processer = processer;
    }


    public HttpClient getClient() {
        return client;
    }

    public Spider setClient(HttpClient client) {
        this.client = client;
        return this;
    }

    public long getDelayTime() {
        return delayTime;
    }

    public Spider setDelayTime(long delayTime) {
        this.delayTime = delayTime;
        return this;
    }

    public int getMaxRetryTimes() {
        return maxRetryTimes;
    }

    public Spider setMaxRetryTimes(int maxRetryTimes) {
        this.maxRetryTimes = maxRetryTimes;
        return this;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public Spider setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
        return this;
    }

    public boolean isNeedProxy() {
        return isNeedProxy;
    }

    public Spider setNeedProxy(boolean needProxy) {
        isNeedProxy = needProxy;
        return this;
    }

    public CookieStore getStore() {
        return store;
    }

    public void setStore(CookieStore store) {
        this.store = store;
    }

    public AtomicLong getPageCount() {
        return pageCount;
    }

    public void setPageCount(AtomicLong pageCount) {
        this.pageCount = pageCount;
    }

    public ReentrantLock getNewUrlLock() {
        return newUrlLock;
    }

    public void setNewUrlLock(ReentrantLock newUrlLock) {
        this.newUrlLock = newUrlLock;
    }

    public Condition getNewUrlCondition() {
        return newUrlCondition;
    }

    public void setNewUrlCondition(Condition newUrlCondition) {
        this.newUrlCondition = newUrlCondition;
    }

    public int getEmptySleepTime() {
        return emptySleepTime;
    }

    public void setEmptySleepTime(int emptySleepTime) {
        this.emptySleepTime = emptySleepTime;
    }

    public Downloader getDownloader() {
        return downloader;
    }

    public void setDownloader(Downloader downloader) {
        this.downloader = downloader;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public boolean isAutoAddRequest() {
        return isAutoAddRequest;
    }

    public Spider setAutoAddRequest(boolean autoAddRequest) {
        isAutoAddRequest = autoAddRequest;
        return this;
    }

    public String getSeedUrlRegex() {
        return seedUrlRegex;
    }

    public Spider setSeedUrlRegex(String seedUrlRegex) {
        this.seedUrlRegex = seedUrlRegex;
        return this;
    }

    public AtomicInteger getRetryRequestCounter() {
        return retryRequestCounter;
    }

    public void setRetryRequestCounter(AtomicInteger retryRequestCounter) {
        this.retryRequestCounter = retryRequestCounter;
    }
}
