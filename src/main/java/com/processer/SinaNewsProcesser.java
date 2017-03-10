package com.processer;

import com.downloader.*;
import com.downloader.encrypt.EncryptLib;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.mongodb.MongoClient;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.parser.ContentExtractor;
import com.parser.Json;
import com.parser.News;
import com.parser.Regex;
import com.processer.inter.Processer;
import com.scheduler.RedisAbstractScheduler;
import com.scheduler.inter.Scheduler;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSessionFactory;
import org.bson.Document;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.Reader;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * sina news processer
 * Created by Administrator on 2017/2/21.
 */
public class SinaNewsProcesser extends RedisAbstractScheduler implements Processer {
    private Reader reader;
    private SqlSessionFactory factory;
    private ExecutorService service = Executors.newFixedThreadPool(10);
    private Downloader dw = new Downloader(HttpConstant.UserAgent.CHROME, false);
    private MongoClient mongoClient = new MongoClient("127.0.0.1");
    private static JedisPool pool = new JedisPool("127.0.0.1");
    private MongoDatabase test = mongoClient.getDatabase("test");

    public SinaNewsProcesser(JedisPool pool, String keyWord) {
        super(pool, keyWord);
    }

    public static void main(String[] args) throws InterruptedException {
        SinaNewsProcesser processer = new SinaNewsProcesser(pool, "");
        processer.searchButton("三道堰");
    }


    public void searchButton(String keyWord) {
        SinaNewsProcesser processer = new SinaNewsProcesser(pool, "news");
        service.submit(() -> processer.searchNews(pool, keyWord));
        Spider spider = new Spider(processer, 5).setScheduler(processer).setNeedProxy(false).setMaxRetryTimes(3);
        spider.setService(service);
        spider.run();
        processer.resetDuplicateCheck();
        System.out.println("-------------------------end-------------------------");
    }

    private Document figureNews(Response urlAndDate) {
        News news = null;
        try {
            news = ContentExtractor.getNewsByHtml(urlAndDate.getContent());
        } catch (Exception e) {
            e.printStackTrace();
        }
        Document parse = new Document();
        if (news != null) {
            parse.append("title", news.getTitle()).append("content", news.getContent()).append("contentHtml", news.getContentElement().toString());
        }
        parse.append("time", urlAndDate.getRequest().getCharset()).append("url", urlAndDate.getUrl());
        System.out.println(parse.toJson());
        return parse;
    }

    private void searchNews(JedisPool pool, String keyWord) {
        dw.setDelayTime(0);
        int pages = 100;
        int counter = 0;
        for (int i = 1; i <= pages; i++) {
            Request request = new Request("http://api.search.sina.com.cn/?q=" + keyWord + "&nums=20&range=all&c=news&page=" + i + "&sort=time&ie=utf-8&from=dfz_api&callback=jsonp" + System.currentTimeMillis());
            request.addHeader(HttpConstant.Header.REFERER, "http://search.sina.com.cn/?c=news&q=%CF%FB%B7%D1%BD%F0%C8%DA&range=all&num=20");
            request.setSleepTime(0);
            request.setTimeout(10 * 1000);
            Response process = dw.process(request);
            if (i == 1 && counter < 20) {
                Regex regex = new Regex("\"count\":\"(\\d+)\"", process.getContent());
                String s = regex.toList(1).get(0);
                int count = Integer.parseInt(s);
                pages = count / 20 + 1;
                counter++;
                System.out.println("page nums set to " + pages);
            }
            if (process.getContent().length() < 300) {
                if (counter < 20) {
                    counter++;
                    i--;
                } else continue;
            } else counter = 0;
            String decode = EncryptLib.unicode(process.getContent());
            System.out.println(decode);
            try {
                storeUrlSeed(pool, decode);
            } catch (Exception e) {
                System.out.println(String.format("第%d页出错", i));
                e.printStackTrace();
            }
        }
    }

    private void storeUrlSeed(JedisPool pool, String e) {
        int leftIndex = e.indexOf("(") + 1;
        int rightIndex = e.length() - 2;
        String result = e.substring(leftIndex, rightIndex);
        Json json = new Json(result);
        JsonArray arr = json.getEle("result.list").getAsJsonArray();
        System.out.println("读出结果数: " + arr.size());
        try (Jedis jedis = pool.getResource()) {
            for (JsonElement element : arr) {
                String datetime = element.getAsJsonObject().get("datetime").getAsString();
                String url = element.getAsJsonObject().get("url").getAsString();
                Long num = jedis.sadd("newsSet", EncryptLib.md5(url + "|" + datetime));
                if (num != 0L) {
                    jedis.lpush("newsQueue", url + "|" + datetime);
                }
            }
        }
    }

    @Override
    public void processResponse(Response response) {
        Document document = figureNews(response);
        String url = response.getUrl();
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            System.out.println(String.format("正在处理: %s", url));
            MongoCollection<Document> collection = test.getCollection("news2");
            if (url != null) {
                jedis.sadd("newsCopy", url);
            }
            collection.insertOne(document);
        } catch (MongoWriteException e1) {
            System.out.println("检测到" + url + "重复");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("检测到" + url + "出错");
            if (jedis != null) {
                jedis.sadd("newsError", url);
            }
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    @Override
    public void addRequests(Scheduler scheduler, Response response) {

    }

    @Override
    public void addRequests(Scheduler scheduler, Collection<Request> collection) {

    }

    @Override
    public void preprocess(Downloader downloader) {
        downloader.setDelayTime(0);
    }

    @Override
    public boolean isNeedRetry(Response response) {
        return StringUtils.isEmpty(response.getError());
    }

    @Override
    public Request convertToRequest(String key) {
        String[] split = key.split("\\|");
        Request r;
        if (split.length == 2) {
            r = new Request(split[0]);
            r.setCharset(split[1]);
        } else {
            r = new Request(key);
        }
        r.setSleepTime(0);
        r.addHeader(HttpConstant.Header.REFERER, "http://search.sina.com.cn/");
        r.setTimeout(10 * 1000);
        return r;
    }

    @Override
    public String convertToString(Request request) {
        return request.getUrl();
    }

}
