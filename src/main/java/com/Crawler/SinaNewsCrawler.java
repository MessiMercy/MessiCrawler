package com.Crawler;

import com.downloader.Downloader;
import com.downloader.HttpConstant;
import com.downloader.Request;
import com.downloader.Response;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.parser.Html;
import com.test.Test;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * sina news crawler
 * Created by Administrator on 2017/2/8.
 */
public class SinaNewsCrawler {
    static Reader reader;
    static SqlSessionFactory factory;
    static ExecutorService service = Executors.newFixedThreadPool(10);
    static Downloader dw = new Downloader();
    static MongoClient mongoClient = new MongoClient("127.0.0.1");
    static JedisPool pool = new JedisPool("127.0.0.1");
    static MongoDatabase test = mongoClient.getDatabase("test");

    static {
        PropertyConfigurator.configure("log4j.properties");
        try {
            reader = Resources.getResourceAsReader("Configuration.xml");
            factory = new SqlSessionFactoryBuilder().build(reader);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static Logger log = Logger.getLogger(Test.class);

    public static void main(String[] args) throws IOException, ParseException, InterruptedException {
    }


    /**
     * 将redis中的新闻按域名分类,以便制定结构抓取策略
     */
    public static void splitDomain(JedisPool pool, String key) {
        try (Jedis jedis = pool.getResource()) {
            String temp = "";
            jedis.select(3);
            while ((temp = jedis.spop(key)) != null) {
                if (temp.startsWith("http")) {
                    String withoutHttp = temp.substring(7);
                    String domain = withoutHttp.substring(0, withoutHttp.indexOf("/"));
                    jedis.sadd(domain, temp);
                }
            }
        }
    }


    public static void urldetail(JedisPool pool, MongoCollection news, String url, String charset) {
        try (Jedis jedis = pool.getResource()) {
            jedis.select(3);
            String tempurl = "";
            while ((tempurl = jedis.spop("news.dichan.sina.com.cn")) != null) {
                tempurl = tempurl.split("\\|")[0];
                Request r = new Request(tempurl);
                r.addHeader(HttpConstant.Header.REFERER, "http://search.sina.com.cn/?q=%CF%FB%B7%D1%BD%F0%C8%DA&range=all&c=news&sort=time");
                r.setSleepTime(0);
                r.setCharset(charset);
                Response process = dw.process(r);
                if (process.getStatusCode() == 200) {
                    try {
                        news(news, "#divContent", process.getContent(), url);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static void news(MongoCollection<org.bson.Document> collection, String selector, String content, String url) {
        Html html = new Html(content);
        String parse = html.parse(selector);
        String title = html.getDocument().title();
        org.bson.Document document = new org.bson.Document();
        document.append("title", title).append("content", parse).append("url", url);
        System.out.println(document.toJson());
        collection.insertOne(document);
    }


}
