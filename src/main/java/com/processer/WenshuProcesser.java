package com.processer;

import com.Crawler.WenshuCrawler;
import com.Ocr.WenshuOcr;
import com.downloader.*;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.parser.Html;
import com.parser.Json;
import com.parser.Regex;
import com.processer.inter.Processer;
import com.proxy.IPModel;
import com.proxy.pool.IPPool;
import com.scheduler.inter.Scheduler;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * 文书id抓取
 * Created by Administrator on 2017/1/9.
 */
public class WenshuProcesser implements Processer {
    private Downloader downloader;
    private JedisPool pool;
    private MongoClient client;
    private MongoDatabase wenshu;
    private ExecutorService service;
    private String redisHost;
    private int redisPort;
    private String mongodbHost;
    private int mongodbPort;
    private boolean AutoSwitchProxy;
    private String NetworkHost;
    private int NetworkPort;
    private int ThreadNum;
    private boolean collectid;
    private Spider spider;

    public WenshuProcesser() {
        downloader = new Downloader(HttpConstant.UserAgent.CHROME, false).setAutoSwitchProxy(AutoSwitchProxy);
        pool = new JedisPool(redisHost, redisPort);
        client = new MongoClient(mongodbHost, mongodbPort);
        service = Executors.newFixedThreadPool(ThreadNum);
        wenshu = client.getDatabase("wenshu");
    }

    {
        Properties pp = new Properties();
        try {
            pp.load(new FileReader("wenshu.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        redisHost = pp.getProperty("RedisHost", "127.0.0.1");
        redisPort = Integer.parseInt(pp.getProperty("RedisPort", "6379"));
        mongodbHost = pp.getProperty("MongoDbHost", "127.0.0.1");
        mongodbPort = Integer.parseInt(pp.getProperty("MongoDbPort", "27017"));
        AutoSwitchProxy = Boolean.parseBoolean(pp.getProperty("AutoSwitchProxy", "false"));
        NetworkHost = pp.getProperty("NetworkHost", "local");
        NetworkPort = Integer.parseInt(pp.getProperty("NetworkPort", "0"));
        ThreadNum = Integer.parseInt(pp.getProperty("ThreadNum", "5"));
        collectid = Boolean.parseBoolean(pp.getProperty("collectId", "true"));
    }


    public static void main(String args[]) {
//        WenshuProcesser processer = new WenshuProcesser();
//        processer.getAndStoreWenshu();
//        String[] keyWords = {"公正", "担保", "利息", "查封", "抵押", "贷款", "债权人", "债务人", "冻结", "利率", "保证", "违约金", "驳回", "合同约定", "清偿", "民间借贷", "质押", "担保合同", "拍卖", "强制性规定", "不履行", "给付", "扣押", "变更", "所有权", "第三人", "租赁", "保证金", "保证合同", "抵押权", "本案争议", "承诺", "变卖", "债权转让", "不动产", "交付", "不完全履行", "借用合同", "利害关系人"};
//        for (int i = 0; i < keyWords.length; i++) {
//            processer.collectId(String.format("案件类型:执行案件,关键词:%s", keyWords[i]));
//        processer.collectId("基层法院:简阳市人民法院,裁判年份:2015");
//        }
        WenshuProcesser processer = new WenshuProcesser();
        if (!processer.NetworkHost.equals("local")) {
            if (processer.NetworkHost.equals("0")) {
                processer.downloader.setProxy(new IPPool().getAvailableIp("http://www.baidu.com", 20));
            } else {
                processer.downloader.setProxy(new IPModel(processer.NetworkHost, processer.NetworkPort));
            }
        }
        if (processer.collectid) {
            processer.service.submit((Runnable) processer::collectId);
        }
        for (int i = 0; i < processer.ThreadNum - 1; i++) {
            processer.service.submit(processer::getAndStoreWenshu);
        }
    }

    public void checkcodePass() {
        Request image = new Request("http://wenshu.court.gov.cn/User/ValidateCode");
        image.setTimeout(10 * 1000);
        image.addHeader(HttpConstant.Header.REFERER, "http://wenshu.court.gov.cn/Html_Pages/VisitRemind.html");
        BufferedImage verifyCodeImage = null;
        try {
            InputStream content = downloader.downloadEntity(image).getEntity().getContent();
            verifyCodeImage = ImageIO.read(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (verifyCodeImage == null) return;
        String verifyCode = WenshuOcr.predictWenshu(verifyCodeImage);
        System.out.println("识别验证码为： " + verifyCode);
        Request submitVerify = new Request("http://wenshu.court.gov.cn/Content/CheckVisitCode");
        submitVerify.setTimeout(10 * 1000);
        submitVerify.setMethod(HttpConstant.Method.POST);
        submitVerify.addFormData("ValidateCode", verifyCode);
        Response process = downloader.process(submitVerify);
        System.out.println(process);
        Request transfer = new Request("http://wenshu.court.gov.cn/Transfer.aspx");
        transfer.addHeader(HttpConstant.Header.REFERER, "http://wenshu.court.gov.cn/Html_Pages/VisitRemind.html");
        downloader.process(transfer);
    }

    public void getAndStoreWenshu() {
        WenshuCrawler crawler = new WenshuCrawler();
        try (Jedis jedis = pool.getResource()) {
            String docId = "";
            while (true) {
                try {
                    docId = jedis.spop("DocId");
                    if (StringUtils.isEmpty(docId)) {
                        Thread.sleep(60 * 1000);//docid内无数据,等待1分钟加入数据
                        System.out.println("检测到已无docid,等待1分钟......");
                    }
                    System.out.println("检测到id: " + docId);
                    String contentById = getContentById(docId);
                    String extractedContent = extractContent(contentById);
                    if (extractedContent == null) continue;
                    storeWenshu(extractedContent, docId);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println(docId + "提取失败");
                    jedis.sadd("DocId", docId);
                    IPModel proxy = downloader.getProxy();
                    proxy.setErrorTime(proxy.getErrorTime() + 1);
                }
            }
        }
    }

    public String getContentById(String docid) {
        IPPool pool = new IPPool();
        String url = "http://wenshu.court.gov.cn/CreateContentJS/CreateContentJS.aspx";
//        if (downloader.getProxy().getErrorTime() >= 5) {
//            System.out.println(downloader.getProxy() + "出错次数过多，正在切换ip");
//            downloader.setProxy(pool.getAvailableIp("http://wenshu.court.gov.cn", 5));
//        }
        Request request = new Request(url);
        request.setTimeout(20 * 1000);
        request.addQueryString("DocID", docid);
        request.addHeader(HttpConstant.Header.REFERER, "http://wenshu.court.gov.cn/content/content?DocID=" + docid);
        Response process = downloader.process(request);
        String content = process.getContent();
        if (process.getError() != null) {
            IPModel proxy = downloader.getProxy();
            proxy.setErrorTime(proxy.getErrorTime() + 1);
        }
        if (content.contains("访问量较大")) {
            System.out.println("检测到访问量过大，休息会儿再访问");
            try {
                Thread.sleep(10 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            content = downloader.process(request).getContent();
        }
        if (content.contains("remind") || content.contains("sorry")) {
            IPModel availableIp = pool.getAvailableIp(url, 5);
            System.out.println("检测到反爬，更换至：" + availableIp);
            downloader.setProxy(availableIp);
            content = downloader.process(request).getContent();
        }
        if (content.contains("VisitRemind")) {
            System.out.println("检测到需要输入验证码");
            checkcodePass();
            content = downloader.process(request).getContent();
        }
        content = StringUtils.remove(content, "\\");
        System.out.println(content);
        return content;
    }

    public String extractContent(String content) {
        Regex regex = new Regex("var jsonHtmlData = \"(\\{.+?\\})\"", content);
        String json = regex.toList(1).get(0);
        if (StringUtils.isEmpty(json) || json.length() < 50) return null;
        Json contentJson = new Json(json);
        String title = contentJson.getEle("Title").getAsString();
        String html = contentJson.getEle("Html").getAsString();
        String pubdate = contentJson.getEle("PubDate").getAsString();
        Html htmlText = new Html(html);
        String desc = htmlText.getDocument().text();
        String result = String.format("%s|%s|%s", title, pubdate, desc);
        System.out.println(result);
        return result;
    }

    public void storeWenshu(String extractedContent, String docId) {
        String[] split = extractedContent.split("\\|");
        try (Jedis jedis = pool.getResource()) {
            jedis.select(15);
            jedis.set(split[0], split[1] + "|" + split[2]);
        }
        MongoCollection<Document> detail = wenshu.getCollection("WenshuDetail");
        Document doc = new Document();
        doc.append("title", split[0]).append("date", split[1]).append("detail", split[2]).append("DocId", docId);
        detail.insertOne(doc);
    }

    public void collectId() {
        try (Jedis jedis = pool.getResource()) {
            WenshuCrawler crawler = new WenshuCrawler();
            while (true) {
                String courtKeyWord = jedis.lpop("courtKeyWord");
                if (StringUtils.isEmpty(courtKeyWord)) {
                    System.out.println("检测到关键词不足,正在生产关键词");
                    crawler.keyWordProducer();
                }
                collectId(courtKeyWord);
            }
        }
    }

    public void collectId(String keyWord) {
        System.out.println("正在收集: " + keyWord);
        WenshuCrawler crawler = new WenshuCrawler();
        try (Jedis jedis = pool.getResource()) {
            System.out.println("================================================");
            for (int i = 1; i <= 100; i++) {
                Request request = new Request("http://wenshu.court.gov.cn/List/ListContent");
                request.addHeader(HttpConstant.Header.REFERER, "http://wenshu.court.gov.cn/List/List?sorttype=1");
                request.setTimeout(30 * 1000);
                request.setMethod(HttpConstant.Method.POST);
                request.addFormData("Param", keyWord);
                request.addFormData("Index", i + "");
                request.addFormData("Page", "20");
                request.addFormData("Order", "法院层级");
                request.addFormData("Direction", "asc");
                System.out.println(request);
                Response process = downloader.process(request);
                String content = process.getContent();
                if (content.contains("访问量较大")) {
                    System.out.println("检测到访问量较大");
                    Thread.sleep(10 * 1000);
                    i--;
                    continue;
                }
                if (content.contains("remind")) {
                    System.out.println("检测到需要验证码");
                    checkcodePass();
                    content = downloader.process(request).getContent();
                }
                if (!content.contains("裁判") && process.getStatusCode() == 200 && process.getError()
                        == null && !content.contains("remind")) {
                    System.out.println(content);
                    System.out.println("检测到本选项已爬完");
                    break;
                }
                if (content.contains("exception")) {
                    i--;
                    System.out.println(keyWord + "出错了....");
                    continue;
                }
                if (content.trim().length() < 10) {
                    i--;
                    System.out.println("检测到返回内容为空");
                    continue;
                }
                content = StringUtils.remove(content, "\\");
                jedis.sadd("basecourt", content);
                System.out.println(content);
                Regex countRegex = new Regex("\"Count\":\"(\\d+)\"", content);
                String countStr = countRegex.toList(1).get(0);
                int count = Integer.parseInt(countStr);
                if (count > 2000) {
                    System.out.println("检测到" + keyWord + "数量超过2000,正在拆分");
                    String[] keyWords = crawler.splitKeyWord(count / 2000 + 1, keyWord);
                    System.out.println("拆分完成,总共拆分了" + (count / 2000 + 1));
                    for (String word : keyWords) {
                        System.out.println("正在执行:" + word);
                        collectId(word);
                    }
                    break;
                }
                Regex regex = new Regex("\"文书ID\":\"(.+?)\"", content);
                try {
                    List<String> list = regex.toList(1);
                    System.out.println("加入docid数： " + list.size());
                    System.out.println(list.get(0));
                    list.forEach(p -> jedis.sadd("DocId", p));
                } catch (Exception e) {
                    System.out.println(String.format("第%d页出错了", i));
                    i--;
                    downloader.raiseErrorProxy();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void processResponse(Response response) {

    }

    @Override
    public void addRequests(Scheduler scheduler, Response response) {

    }

    @Override
    public void addRequests(Scheduler scheduler, Collection<Request> collection) {

    }

    @Override
    public void preprocess(Downloader downloader) {

    }

    @Override
    public boolean isNeedRetry(Response response) {
        return false;
    }

    @Override
    public void stop() {
        if (spider != null) {
            spider.setStop(true);
        }
    }


    public Downloader getDownloader() {
        return downloader;
    }

    public void setDownloader(Downloader downloader) {
        this.downloader = downloader;
    }
}
