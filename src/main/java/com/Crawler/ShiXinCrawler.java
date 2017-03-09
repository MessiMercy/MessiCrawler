package com.Crawler;

import com.MainImage;
import com.downloader.CrawlerLib;
import com.downloader.Downloader;
import com.downloader.Request;
import com.downloader.Response;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.model.ShiXinModel;
import com.model.ShiXinModelPage;
import com.model.Shixincompanydetail;
import com.model.Shixinpersondetail;
import com.model.inter.IShiXinModelOperation;
import com.model.inter.ShiXinModelPageMapper;
import com.model.inter.ShixincompanydetailMapper;
import com.model.inter.ShixinpersondetailMapper;
import com.parser.Html;
import com.pipeline.impl.SimpleFilepipeline;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.message.BasicNameValuePair;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShiXinCrawler {
    private static CookieStore store = new BasicCookieStore();
    private static final HttpClient CLIENT = CrawlerLib.getInstanceClient(false, store);
    private static final String IMAGEURL = "http://shixin.court.gov.cn/image.jsp";// 验证码接口
    private static final String SEARCHURL = "http://shixin.court.gov.cn/findd";// 搜索接口
    private static final String DETAILURL = "http://shixin.court.gov.cn/findDetai";// 文书具体内容
    private static final String PUBLISHURL = "http://shixin.court.gov.cn/index_publish_new.jsp";// 失信人公示
    private static HashMap<String, String> map = new HashMap<>();
    private static final Logger LOGGER = Logger.getLogger(ShiXinCrawler.class);
    private static Downloader fetch = new Downloader(CLIENT);
    private static Reader reader;
    private static SqlSessionFactory factory;
    private static int page = 10;
    private static Set<String> nameSet = new HashSet<>();
    private static JedisPool pool = new JedisPool("127.0.0.1");

    static {
        try {
            reader = Resources.getResourceAsReader("Configuration.xml");
            factory = new SqlSessionFactoryBuilder().build(reader);
        } catch (IOException e) {
            e.printStackTrace();
        }
        PropertyConfigurator.configure("log4j.properties");
    }

    public static void main(String[] args) {
        map.put("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.116 Safari/537.36");
        map.put("Referer", "http://shixin.court.gov.cn/");
//        SqlSession session = factory.openSession();
//        List<ShiXinModel> publishNew = getPublishNew();
//        System.out.println(publishNew.size());
//        List<ShiXinModel> collect = publishNew.stream().filter(p -> p.getPeopleOrCompany() == 0).collect(Collectors.toList());
//        collectionStore(collect, session);
//        session.close();
//        searchKey("李%", true);
//        int pageNum = 0;
//        do {
//            try {
//                serchPeople("罗%", pageNum);
//            } catch (Exception e) {
//                LOGGER.error(e.toString());
//            }
//        } while (pageNum < page);
//        for (int i = 1; i < page; i++) {
//            try {
//                serchPeople("李%", i);
//            } catch (Exception e) {
//                LOGGER.error(e.toString());
//            }
//        }
        JedisPool pool = new JedisPool("127.0.0.1");
        try (Jedis jedis = pool.getResource()) {
            String name = null;
            while (!StringUtils.isEmpty(name = jedis.spop("shixinpeople"))) {
                searchKey(name.substring(0, 2), true);
            }
        }
    }

    /**
     * @param key             关键词
     * @param personOrCompany true为个人，false为公司
     */
    private static void searchKey(String key, boolean personOrCompany) {
        SqlSession session = factory.openSession();
        try (Jedis jedis = pool.getResource()) {


//        List<Object> list = searchShiXin(key, null, 0, personOrCompany);
//        storeDetail(list, session);
            try {
                for (int i = 1; i <= page; i++) {
                    try {
                        LOGGER.info("正在查询： " + key);
                        List<Object> shiXin = searchShiXin(key, null, 0, personOrCompany, i);
                        storeDetail(shiXin, session);
                    } catch (Exception e) {
                        System.out.println(key + "查询失败");
                        jedis.sadd("shixinpeople", key);
                    }
                    session.commit();
                    LOGGER.info("第" + i + "页保存成功");
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                session.close();
            }
        }
    }

    private static void searchKey(String key) {
        for (int i = 0; i < page; i++) {
            try {
                serchPeople(key, i);
                LOGGER.info("第" + i + "页保存成功");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static String getDetail(int id, String pCode) {
        String resultJson = null;
        String targetUrl = DETAILURL + "?id=" + id + "&pCode=" + pCode;
        // map.put("X-Requested-With", "XMLHttpRequest");
        // map.put("Accept-Encoding", "gzip, deflate, sdch");
        Request request = new Request(targetUrl);
        request.setHeaders(map);
        fetch.getEntity(CLIENT, request);
        resultJson = fetch.getHtml();
        return resultJson;
    }

    /**
     * @param personOrCompany 为true时代表查询人，false代表查询机构
     * @param pName           为名字关键字，需要两个以上汉字
     * @param pCardNum        身份证号码，或者组织机构号码
     * @param pProvince       省份代码，0为全国。各省份代码在province.properties中
     *                        查询频率需低于10秒一次
     */
    private static List<Object> searchShiXin(String pName, String pCardNum, int pProvince, boolean personOrCompany, int currentPage) {
        String pCode = picOcr();
        List<Object> resultList = new ArrayList<>();
        List<NameValuePair> postDict = new ArrayList<>();
        postDict.add(new BasicNameValuePair("currentPage", currentPage + ""));
        postDict.add(new BasicNameValuePair("pName", pName));
        postDict.add(new BasicNameValuePair("pCardNum", pCardNum));
        postDict.add(new BasicNameValuePair("pProvince", pProvince + ""));
        postDict.add(new BasicNameValuePair("pCode", pCode));
        System.out.println("验证码: " + pCode);
        LOGGER.info("验证码： " + pCode);
        String html = "null";
        Request request = new Request(SEARCHURL);
        request.setFormData(postDict);
        request.setHeaders(map);
        request.setTimeout(30 * 1000);
        Response response = fetch.postEntity(CLIENT, request);
        html = response.getContent();
//        if (page == 1) {
        int nowpage = getPage(html);
        page = nowpage <= 2 ? 2 : nowpage;
        LOGGER.info(response.getContent());
        LOGGER.info("已将发现页数设置为： " + page);
//        }
        if (html == null || html.length() == 0) {
            return resultList;
        }
        Document doc = Jsoup.parse(html);
        Elements elements = doc.select("tr[style]");
        for (Element element : elements) {
            String id = element.select("a.View").first().attr("id");
            String name = element.select("a[title]").first().text();
            LOGGER.info(element.text() + " " + id);
            System.out.println(element.text() + " " + id);
            System.out.println("------------------------");
            String detail = getDetail(Integer.valueOf(id), pCode);
            LOGGER.info(detail);
            System.out.println(detail);
            Gson gson = new Gson();
            if (personOrCompany) {
                Shixinpersondetail shixinpersondetail = gson.fromJson(detail.toLowerCase(), Shixinpersondetail.class);
                resultList.add(shixinpersondetail);
            } else {
                Shixincompanydetail shixincompanydetail = gson.fromJson(detail.toLowerCase(), Shixincompanydetail.class);
                resultList.add(shixincompanydetail);
            }
        }
        return resultList;
    }

    private static void serchPeople(String keyWord, int pageNum) {
        String pCode = picOcr();
        List<NameValuePair> postDict = new ArrayList<>();
        postDict.add(new BasicNameValuePair("currentPage", pageNum + ""));
        postDict.add(new BasicNameValuePair("pName", keyWord));
        postDict.add(new BasicNameValuePair("pCardNum", null));
        postDict.add(new BasicNameValuePair("pProvince", 0 + ""));
        postDict.add(new BasicNameValuePair("pCode", pCode));
        LOGGER.info("验证码： " + pCode);
        String html = "null";
        Request request = new Request(SEARCHURL);
        request.setFormData(postDict);
        request.setHeaders(map);
        request.setTimeout(10 * 1000);
        Response response = fetch.postEntity(CLIENT, request);
        html = response.getContent();
//        if (page == 1) {
        int nowpage = getPage(html);
        page = nowpage <= 2 ? 2 : nowpage;
        LOGGER.info(response.getContent());
        if (html.contains("验证码错误") || StringUtils.isEmpty(html) || response.getError() != null) {
            page = pageNum + 5;
        }
        LOGGER.info("已将发现页数设置为： " + page);
        Document parse = Jsoup.parse(html);
        Elements elements = parse.select("tr[style]");
        SimpleFilepipeline pipe = new SimpleFilepipeline(new File("shixinpeople.txt"));
        StringBuilder builder = new StringBuilder();
        elements.forEach(p -> {
            String name = p.select("a[title]").first().text();
            if (nameSet.add(name)) {
                builder.append(name).append("\r\n");
                try (Jedis jedis = pool.getResource()) {
                    jedis.lpush("shixin", name);
                }
            }
        });
        pipe.printResult(builder.toString(), true);
    }

    private static String picOcr() {
        Calendar cd = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM d yyyy HH:mm:ss 'GMT'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+8")); // 设置时区为GMT
        String str = sdf.format(cd.getTime());
        try {
            str = URLEncoder.encode(str, "utf-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        InputStream in = fetch.downloadEntity(CLIENT, IMAGEURL + "?date=" + str, map);
        BufferedImage image = null;
        try {
            image = ImageIO.read(in);
//            ImageIO.write(image, "bmp", new File("tem.bmp"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (image == null || image.getHeight() == 0) {
            LOGGER.info("取到图片为空，5秒后重试！");
            try {
                Thread.sleep(5 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return picOcr();
        }
        return MainImage.shiXinImageFigure(image);
    }

    private static List<ShiXinModel> getPublishNew() {
        List<ShiXinModel> resultList = new ArrayList<>();
        SqlSession session = factory.openSession();
        IShiXinModelOperation operation = session.getMapper(IShiXinModelOperation.class);
        Request request = new Request("http://shixin.court.gov.cn/index_publish_new.jsp");
        request.setTimeout(20 * 1000);
        Response response = fetch.process(request);
        Html html = null;
        try {
            html = new Html(response.getContent());
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(response.getContent());
        String s = html.parse("#TextContent1");
        String s1 = html.parse("#TextContent2");
        String[] arr = s.split(" ");
        arr = Sets.newHashSet(arr).toArray(new String[]{});
        System.out.println("找到公司数量： " + arr.length);
        for (String string : arr) {
            ShiXinModel model = new ShiXinModel();
            String[] arr1 = string.trim().split(" ");
            model.setName(arr1[0]);
            model.setVerifyNum(arr1[1]);
            model.setPeopleOrCompany(1);
            resultList.add(model);
            if (operation.selectByName(arr1[0]) == null) {
                operation.addShiXin(model);
                System.out.println("----------------------");
            }
        }
        String[] arr2 = s1.split(" ");
        arr2 = Sets.newHashSet(arr2).toArray(new String[]{});
        System.out.println("找到个人数量： " + arr2.length);
        for (String string : arr2) {
            ShiXinModel model1 = new ShiXinModel();
            String[] arr3 = string.trim().split(" ");
            model1.setName(arr3[0]);
            model1.setVerifyNum(arr3[1]);
            model1.setPeopleOrCompany(0);
            resultList.add(model1);
            if (operation.selectByName(arr3[0]) == null) {
                operation.addShiXin(model1);
                System.out.println("----------------------");
            }
        }
        session.commit();
        session.close();
        return resultList;
    }

    /**
     * 从shixin表中读取列表并存储
     */
    private static void sqlStore() {
        SqlSession session = factory.openSession();
        ShiXinModelPageMapper page = session.getMapper(ShiXinModelPageMapper.class);
        int count = page.getCount();
        for (int i = 2; i <= count / 10; i++) {
            List<ShiXinModel> models = getModels(10 * i, 10, page);
            collectionStore(models, session);
            LOGGER.info("存储完第" + i + "批");
        }
        session.close();
    }

    /**
     * @param collection 传入一个内容为人名的collection，获取并存入sql
     */
    private static void collectionStore(Collection<ShiXinModel> collection, SqlSession session) {
        collection.stream().skip(2).forEach(p -> {
            String unit = p.getName().substring(0, 2);
            int personOrCompany = p.getPeopleOrCompany();
            LOGGER.info("正在查找： " + unit);
            searchKey(unit, personOrCompany == 0);
            session.commit();
        });
    }

    private static List<ShiXinModel> getModels(int offset, int size, ShiXinModelPageMapper mapper) {
        ShiXinModelPage page = new ShiXinModelPage();
        page.setPageOffset(offset);
        page.setPageSize(size);
        return mapper.selectShiXinModelPageByOffset(page);
    }

    private static void storeDetail(List<Object> list, SqlSession session) {
        if (list == null || list.size() == 0) {
            return;
        }
        if (list.get(0) instanceof Shixinpersondetail) {
            ShixinpersondetailMapper mapper = session.getMapper(ShixinpersondetailMapper.class);
            list.forEach(p -> {
                try {
                    mapper.insert((Shixinpersondetail) p);
                    LOGGER.info("storing: " + ((Shixinpersondetail) p).getIname());
                } catch (Exception e) {
                    try {
                        LOGGER.info(((Shixinpersondetail) p).getIname() + "重复了！");
                    } catch (Exception ignore) {
                    }
                }
            });
        } else {
            ShixincompanydetailMapper mapper = session.getMapper(ShixincompanydetailMapper.class);
            list.forEach(p -> {
                try {
                    mapper.insert((Shixincompanydetail) p);
                    LOGGER.info("storing: " + ((Shixincompanydetail) p).getIname());
                } catch (Exception e) {
                    try {
                        LOGGER.info(((Shixincompanydetail) p).getIname() + "重复了！");
                    } catch (Exception ignore) {
                    }
                }
            });
        }

    }

    private static int getPage(String html) {
        if (html == null || html.length() == 0) {
            return 2;
        }
        int page = 2;
        Pattern pattern = Pattern.compile("totalPage\\s*=\\s*(\\d+);");
        Matcher matcher = pattern.matcher(html);
        if (matcher.find()) {
            try {
                page = Integer.valueOf(matcher.group(1));
                LOGGER.info("页数字符串： " + matcher.group());
            } catch (Exception ignore) {
                System.out.println("取到错误页数： " + matcher.group(1));
            }
        }
        return page;
    }

}
