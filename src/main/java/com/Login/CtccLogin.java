package com.Login;

import com.downloader.*;
import com.google.gson.JsonObject;
import com.parser.Json;
import com.pipeline.impl.SimpleFilepipeline;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.message.BasicNameValuePair;

import javax.imageio.ImageIO;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

public class CtccLogin {
    /**
     * 通用接口
     */
    private static final String GENERALURL = "http://login.189.cn/login";
    private static final Map<String, String> map = new HashMap<>();
    private static final CookieStore STORE = new BasicCookieStore();
    private static final HttpClient CLIENT = CrawlerLib.getInstanceClient(false, STORE);
    private static final Properties ctccProperties = new Properties();
    private static final Scanner sc = new Scanner(System.in);
    private static final Downloader fetch = new Downloader(CLIENT);

    /**
     * 中国电信查询详单仅能通过密码登录！
     */
    public static void main(String[] args) {
        map.put("Referer", "http://login.189.cn/login");
        map.put("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.101 Safari/537.36");
        login("18010580080", "920108", map);
        System.out.println("检查是否登录： " + hasLogin(map));
        presendSms();
        STORE.getCookies().forEach(p -> System.out.println(p.getName() + ": " + p.getValue()));
        for (int i = 0; i < 4; i++) {
            if (sendSMS("2016-10-01", "2016-10-31", map).contains("S")) {
                break;
            }
            try {
                Thread.sleep(20 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("请去后台查看你的短信验证码");
        String str = sc.nextLine();
        String str21 = searchDetailBills("2016-10-01", "2016-10-31", map, "21", str);
        String str22 = searchDetailBills("2016-10-01", "2016-10-31", map, "22", str);
        String str23 = searchDetailBills("2016-10-01", "2016-10-31", map, "23", str);
        String str24 = searchDetailBills("2016-10-01", "2016-10-31", map, "24", str);
        new SimpleFilepipeline(new File("test.txt")).printResult(str21 + "\n" + str22 + "\n" + str23 + "\n" + str24, false);
        sc.close();
    }

    /**
     * 0.详单查询时间选择请勿跨月 1.尊敬的客户，您可以查询近六个月的详单信息；
     * 2.多个号码归属于一个账号情况下，所有号码查询的费用为同一个费用信息。 3.详单查询时，短信验证码每天最多发10条。
     * 4.每月1-2号全天、3号的0点至8点及月末最后一天为系统出账期，可能无法正常查询您的套餐使用情况，敬请谅解。 5.短信有效期内可以随意查询
     *
     * @param startDay 开始日期，格式：2016-09-01
     * @param endDay   结束日期，格式如上
     * @param qryType  21为通话详单，22为数据详单，23为增值业务详单，24为短信详单
     */
    private static String searchDetailBills(String startDay, String endDay, Map<String, String> map, String qryType,
                                            String code) {
        String url = "http://sc.189.cn/service/billDetail/detailQuery.jsp?startTime=" + startDay + "&endTime=" + endDay
                + "&qryType=" + qryType + "&randomCode=";
        String randomCode = Base64.getEncoder().encodeToString((code + "").getBytes());
        url += randomCode;// 随机码为短信验证码base64加密
        Request request = new Request(url);
        request.setHeaders(map);
        request.setCharset("gbk");
        Response response = fetch.process(request);
        return response.getContent();
    }

    private static void presendSms() {
        Request idRequest = new Request("http://sc.189.cn:8088/webtrends/dcs.gif");
        Map<String, String> query = new LinkedHashMap<>();
        query.put("WT.branch", "189");
        query.put("dcssip", "sc.189.cn");
        query.put("wt.host", "sc.189.cn");
        query.put("dcsuri", "/service/v6/xdcx");
        query.put("wt.es", "http%3a%2f%2fsc.189.cn%2fservice%2fv6%2fxdcx%3ffastcode%3d20000326%26citycode%3dsc");
        query.put("citycode", "sc");
        query.put("dcsqry", "fastcode=20000326");
        fetch.process(idRequest);
//        Request request = new Request("http://sc.189.cn/service/v6/xdcx?fastcode=20000326&cityCode=sc");
//        Response response = fetch.process(request);
//        System.out.println(response);
        Request checkSession = new Request("http://www.189.cn/dqmh/my189/checkMy189Session.do");
        checkSession.setMethod(HttpConstant.Method.POST);
        List<NameValuePair> list = new ArrayList<>();
        list.add(new BasicNameValuePair("fastcode", "20000326"));
        checkSession.setFormData(list);
        System.out.println(fetch.process(checkSession));
        Request redirect = new Request("http://www.189.cn/dqmh/ssoLink.do");
        Map<String, String> queryMap = new LinkedHashMap<>();
        queryMap.put("method", "linkTo");
        queryMap.put("platNo", "10023");
        queryMap.put("toStUrl", "http://sc.189.cn/service/v6/xdcx?fastcode");
        queryMap.put("cityCode", "sc");
        redirect.setQueryStringMap(queryMap);
        redirect.setHeaders(map);
        Response response = fetch.process(redirect);
        if (response.getStatusCode() == 301) {
            String secondUrl = response.getFirstHeader("Location");
            Response response2 = fetch.process(new Request(secondUrl));
            if (response2.getStatusCode() == 302) {
                String thirdUrl = response2.getFirstHeader("Location");
                System.out.println(fetch.process(new Request(thirdUrl)));
            }
        }
    }

    /**
     * 查询日期之间不能跨月,每个省需要具体匹配,短信有有效期，有效期内可以数次查询。超过有效期需要重发短信
     *
     * @param startDay 开始日期，格式：2016-09-01
     * @param endDay   结束日期，格式如上
     */
    private static String sendSMS(String startDay, String endDay, Map<String, String> map) {
        String url = "http://sc.189.cn/service/billDetail/sendSMSAjax.jsp";
        map.put(HttpConstant.Header.REFERER, "http://sc.189.cn/service/v6/xdcx?fastcode=20000326&cityCode=sc");
        Request request = new Request(url);
        Map<String, String> query = new LinkedHashMap<>();
        query.put("dateTime1", startDay);
        query.put("dateTime2", endDay);
        request.setHeaders(map);
        request.setQueryStringMap(query);
        Response response = fetch.process(request);
        System.out.println(response.getContent());
        return response.getContent();
    }

    /**
     * 登录有时效，较短。估计1个小时
     */
    private static void login(String account, String password, Map<String, String> map) {
        try {
            ctccProperties.load(new FileInputStream(new File("ctcc.properties")));
        } catch (IOException e) {
            e.printStackTrace();
        }
//        if (ctccProperties.containsKey("Cookie") && ctccProperties.getProperty("Cookie") != null) {
//            String cookie = ctccProperties.getProperty("Cookie").replaceAll("\\=", "=");
//            if (cookie != null) {
//                System.out.println("正在从properties读取cookie: " + cookie);
//                map.put("Cookie", cookie);
//                CrawlerLib.addCookieFromProperties(ctccProperties, STORE, new File("ctcc.properties"));
//            }
//        }
//        System.out.println("是否需要重新登录，是输入1，否则为其他");
//        boolean reLogin = sc.nextLine().equals("1");
        if (true || !hasLogin(map)) {
            List<NameValuePair> list = new ArrayList<>();
            list.add(new BasicNameValuePair("Account", account));
            list.add(new BasicNameValuePair("AreaCode", ""));
            list.add(new BasicNameValuePair("Captcha", ""));
            list.add(new BasicNameValuePair("CityNo", ""));
            list.add(new BasicNameValuePair("Password", getEncodedPw(password)));
            list.add(new BasicNameValuePair("ProvinceID", getProvinceId(account, map)));
            list.add(new BasicNameValuePair("RandomFlag", "0"));
            list.add(new BasicNameValuePair("UType", "201"));
            Request request = new Request(GENERALURL);
            request.setMethod(HttpConstant.Method.POST);
            request.setFormData(list);
            request.setHeaders(map);
            Response response = fetch.process(request);
            if (response.getContent().contains("欢迎登录") || response.getContent().contains("验证码")) {
                downloadVerifyCode(map, CLIENT);
                System.out.println("请去查看你的验证码并输入");
                String verifyCode = sc.nextLine();
                list.remove(new BasicNameValuePair("Captcha", ""));
                list.add(new BasicNameValuePair("Captcha", verifyCode));
                Request request1 = new Request(GENERALURL);
                request1.setFormData(list);
                request1.setHeaders(map);
                response = fetch.process(request1);
            }
//            System.out.println("status code: " + response.getStatusCode());
            System.out.println(response);
            String redirect = response.getFirstHeader("Location");
            System.out.println("redirecting to " + redirect + " .........");
            Request request2 = new Request(redirect);
            request2.setHeaders(map);
            Response response1 = fetch.process(request2);
            System.out.println(response1);
//            CrawlerLib.getCookieAndStore(ctccProperties, STORE, "ctcc.properties");
        }
    }

    private static String getProvinceId(String phoneNum, Map<String, String> map) {
        map.put("Accept", "application/json");
        List<NameValuePair> list = new ArrayList<>();
        list.add(new BasicNameValuePair("m", "checkphone"));
        list.add(new BasicNameValuePair("phone", phoneNum));
        Request request = new Request(GENERALURL + "/ajax");
        request.setMethod(HttpConstant.Method.POST);
        request.setFormData(list);
        request.setHeaders(map);
        Response response = fetch.process(request);
        System.out.println(response);
        Json parser = new Json(response.getContent());
        JsonObject obb = parser.getObj();
        Optional<String> a = Optional.ofNullable(obb.get("ProvinceID").getAsString());
        return a.orElse("23");
    }

    public static String getEncodedPw(String pw) {
        String encodedPw = "";
        ScriptEngineManager sem = new ScriptEngineManager();
        ScriptEngine engine = sem.getEngineByName("javascript");
        try {
            engine.eval(new FileReader(new File("ctcc.js")));
        } catch (FileNotFoundException | ScriptException e) {
            e.printStackTrace();
        }
        if (engine instanceof Invocable) {
            Invocable in = (Invocable) engine;
            try {
                encodedPw = in.invokeFunction("myencrypto", pw).toString();
            } catch (NoSuchMethodException | ScriptException e) {
                e.printStackTrace();
            }
        }
        System.out.println("password: " + encodedPw);
        return encodedPw;
    }

    private static boolean hasLogin(Map<String, String> map) {
        boolean result = false;
        map.put("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.101 Safari/537.36");
        Request request = new Request("http://www.189.cn/dqmh/my189/initMy189home.do");
        request.setHeaders(map);
        Response response = fetch.process(request);
        String html = response.getContent();
        if (html.contains("我的欢")) {
            result = true;
            System.out.println("从properties里读取到已登录cookie");
        } else {
            System.out.println("检测到cookie已失效");
        }
        return result;
    }

    private static void downloadVerifyCode(Map<String, String> map, HttpClient client) {
        String downloadUrl = "http://sc.189.cn/kaptcha.jpg";
        InputStream in = fetch.downloadEntity(client, downloadUrl, map);
        try {
            BufferedImage verifyCode = ImageIO.read(in);
            ImageIO.write(verifyCode, "jpg", new File("verify.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
