package com.Login;

import com.downloader.*;
import com.downloader.HttpConstant.Header;
import com.downloader.encrypt.EncryptLib;
import com.google.common.collect.Maps;
import com.pipeline.impl.SimpleFilepipeline;
import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.message.BasicNameValuePair;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CmccGeneralLogin {
    private final static CookieStore STORE = new BasicCookieStore();
    private final static HttpClient CLIENT = CrawlerLib.getInstanceClient(false, STORE);
    //    private final static HttpClient CLIENT = generateClient();
    private final static Downloader fetch = new Downloader(CLIENT);
    private final static String CHARSET = "UTF-8";
    private final static String SEARCHBILLSMS = "https://login.10086.cn/sendSMSpwd.action";
    private final static String ARTIFACTURL = "http://shop.10086.cn/i/v1/auth/getArtifact";
    private final static String LOGINURL = "https://login.10086.cn/login.htm";
    private final static String LOGINSMSURL = "https://login.10086.cn/sendRandomCodeAction.action";
    private final static String VERIFYCODEURL = "https://login.10086.cn/captchazh.htm?type=12";
    private final static String SUBMITSMSURL = "https://shop.10086.cn/i/v1/fee/detailbilltempidentjsonp/";
    private final static String GETBILLURL = "https://shop.10086.cn/i/v1/fee/detailbillinfojsonp/";

    private static String referer = "";

    public static void main(String[] args) {
        func();
//        funcSim();
    }

    private static void funcSim() {
        Map<String, String> map = new HashMap<>();
        map.put(Header.REFERER, "https://login.10086.cn/login.html");
        map.put(Header.USER_AGENT,
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2486.0 Safari/537.36 Edge/13.10586");
        String phoneNum = "13611969057";
        String pw = "413413";
        sendLoginSMS(phoneNum, map);
        Scanner sc = new Scanner(System.in);
        System.out.println("输入短信");
        String smsPwd = sc.nextLine();
        map.put("Referer", "https://login.10086.cn/login.html?channelID=12003&backUrl=http://shop.10086.cn/i/");
        login(phoneNum, pw, smsPwd, map);
        sendSearchSMS(phoneNum, map);
        System.out.println("输入短信");
        String searchSMS = sc.nextLine();
        sc.close();
        submitSearchSMS(phoneNum, pw, searchSMS, map);
        for (int i = 0; i < 7; i++) {
            requestForBill(phoneNum, "0" + (i + 1), 1, "201609", map);
        }
    }

    /**
     * 流程：发送短信加服务密码模拟登陆 -> 发送短信加服务密码查询账单
     */
    private static void func() {
        Map<String, String> map = new HashMap<>();
        map.put(Header.REFERER, "https://login.10086.cn/login.html");
        map.put(Header.USER_AGENT,
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2486.0 Safari/537.36 Edge/13.10586");
        String phoneNum = "18782934825";
        String pw = "920108";
        Properties p = new Properties();
//        CrawlerLib.addCookieFromProperties(p, STORE, new File("cmccGen.properties"));
        Scanner sc = new Scanner(System.in);
        if (true) {
            getVerifyCookie(map);
            sendLoginSMS(phoneNum, map);
            System.out.println("输入短信");
            String smsPwd = sc.nextLine();
            // map.put("Accept", "application/json, text/javascript, */*;
            // q=0.01");
            // map.put("X-Requested-With", "XMLHttpRequest");
            map.put("Referer", "https://login.10086.cn/login.html?channelID=12003&backUrl=http://shop.10086.cn/i/");
            login(phoneNum, pw, smsPwd, map);
            CrawlerLib.storeCookie(STORE, new File("cmccGen.properties"));
        }
        map.put("Referer", getReferer());
        Map<String, String> proxyMap = Maps.newHashMap(map);
//        proxyMap.put("", "");
        List<Cookie> list = STORE.getCookies();
        list.forEach(pp -> System.out.println(pp.getName() + ": " + pp.getValue()));
        preSendSms(phoneNum, "02", 1, "201609", map);
        for (int i = 0; i < 4; i++) {
            String str = sendSearchSMS(phoneNum, map);
            if (!str.contains("\"resultCode\":\"1\"")) {
                break;
            }
            try {
                System.out.println("休眠10秒");
                Thread.sleep(10 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("输入短信");
        String searchSMS = sc.nextLine();
        sc.close();
        submitSearchSMS(phoneNum, pw, searchSMS, map);
        for (int i = 0; i < 7; i++) {
            requestForBill(phoneNum, "0" + (i + 1), 1, "201609", map);
        }
    }

    private static void getVerifyCookie(Map<String, String> map) {
        Request request = new Request(VERIFYCODEURL);
        request.setHeaders(map);
        fetch.getEntity(CLIENT, request);
    }

    private static void sendLoginSMS(String phoneNum, Map<String, String> map) {
        List<NameValuePair> list = new ArrayList<>();
        list.add(new BasicNameValuePair("channelID", "12003"));
        list.add(new BasicNameValuePair("type", "01"));
        list.add(new BasicNameValuePair("userName", phoneNum));
        Request request = new Request(LOGINSMSURL);
        request.setCharset(CHARSET);
        request.setFormData(list);
        request.setMethod(HttpConstant.Method.POST);
        request.setHeaders(map);
        Response response = fetch.process(CLIENT, request);
        System.out.println(response.toString());
    }

    public static void login(String phoneNum, String pw, String smsPwd, Map<String, String> map) {
        Request request = new Request(LOGINURL);
        Map<String, String> query = new LinkedHashMap<>();
        query.put("account", phoneNum);
        query.put("accountType", "01");
        query.put("backUrl", "http://shop.10086.cn/i/");
        query.put("channelID", "12003");
        query.put("inputCode", "");
        query.put("password", pw);
        query.put("protocol", "https:");
        query.put("pwdType", "01");
        query.put("rememberMe", "0");
        query.put("smsPwd", smsPwd);
        query.put("timestamp", System.currentTimeMillis() + "");
        request.setMethod(HttpConstant.Method.GET);
        request.setHeaders(map);
        request.setCharset(CHARSET);
        request.setQueryStringMap(query);
        System.out.println(request.getUrl());
        Response response = fetch.process(CLIENT, request);
        System.out.println(response.toString());
        getArtifact(response.getContent(), map);
    }

    /**
     * 发送短信前伪装查账单
     */
    private static void preSendSms(String phoneNum, String billType, int curCuror, String qryMonth,
                                   Map<String, String> map) {
        requestForBill(phoneNum, billType, curCuror, qryMonth, map);
    }

    /**
     * 重复发送可以发送成功
     */
    private static String sendSearchSMS(String phoneNum, Map<String, String> map) {
        Request request = new Request(SEARCHBILLSMS);
        Map<String, String> queryMap = new HashMap<>();
        queryMap.put("callback", "result");
        queryMap.put("userName", phoneNum);
        request.setQueryStringMap(queryMap);
        request.setHeaders(map);
        System.out.println(request.toString());
        Response response = fetch.process(CLIENT, request);
        System.out.println(response);
        String resultCode = response.getContent();
        resultCode = StringUtils.substringBetween(resultCode, "(", ")");
        try {
            resultCode = URLEncoder.encode(resultCode, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return response.getContent();

    }

    private static void clickAlert(String param, Map<String, String> map) {
        String url = "http://shop.10086.cn/i/logTag/dcs.gif";
        Request request = new Request(url);
        Map<String, String> queryMap = new LinkedHashMap<>();
        queryMap.put("funCode", "010");
        queryMap.put("areaCode", "5961ms");
        queryMap.put("provice", "280");
        queryMap.put("operDesc", "https://login.10086.cn/sendSMSpwd.action");
        queryMap.put("referer", "undefined");
        queryMap.put("param", param);
        queryMap.put("currentlocation", "undefined");
        queryMap.put("logType", "INFO");
        queryMap.put("custTime", "20161102.10559.588");
        queryMap.put("screenlocation", "undefined");
        queryMap.put("version", "tracker_v6");
        queryMap.put("startTime", "20161102.10559.672");
        queryMap.put("directCode", "undefined");
        queryMap.put("channelId", "undefined");
        queryMap.put("skuIds", "undefined");
        queryMap.put("shopId", "undefined");
        request.setQueryStringMap(queryMap);
        Response response = fetch.process(request);
        System.out.println(response);
    }

    private static void submitSearchSMS(String phoneNum, String pw, String sms, Map<String, String> map) {
        Request request = new Request(SUBMITSMSURL + phoneNum);
        request.addQueryString("callback", "jQuery183044929926475494764_" + System.currentTimeMillis());
        request.addQueryString("pwdTempSerCode", EncryptLib.base64Encode(pw));
        request.addQueryString("pwdTempRandCode", EncryptLib.base64Encode(sms));
        request.addQueryString("_", System.currentTimeMillis() + "");
        request.setHeaders(map);
        Response response = fetch.process(CLIENT, request);
        System.out.println(response);
    }

    /**
     * @param billType 账单类型：01：套餐及固定费（四川不支持） 02:通话详单 03：上网详单 04：短信彩信详单 05：增值业务详单
     *                 06：代收业务详单 07：其他扣费记录
     * @param curCuror 页数。每页默认100条信息，
     * @param qryMonth 年份和月份 仅支持查询半年内，格式：201609
     */
    private static void requestForBill(String phoneNum, String billType, int curCuror, String qryMonth,
                                       Map<String, String> map) {
        Request request = new Request(GETBILLURL + phoneNum);
        request.setTimeout(5 * 1000);
        Map<String, String> queryMap = new HashMap<>();
        queryMap.put("_", System.currentTimeMillis() + "");
        queryMap.put("billType", billType);
        queryMap.put("callback", "jQuery1830" + (Math.random() + "").substring(2) + "_" + System.currentTimeMillis());
        queryMap.put("curCuror", curCuror + "");
        queryMap.put("qryMonth", qryMonth);
        queryMap.put("step", 100 + "");
        request.setQueryStringMap(queryMap);
        request.setHeaders(map);
        Response response = fetch.process(CLIENT, request);
        System.out.println(response.getContent());
        new SimpleFilepipeline(new File(billType + ".txt")).printResult(response.toString(), false);
    }

    /**
     * 保险起见，获取jsessionid-echd-cpt-cmcc-jt(其中一个cookie)
     */
    private static void getArtifact(String loginResponse, Map<String, String> map) {
        // String regex = "'artifact':'.+?'";
        String regex = "\"artifact\":\".+?\"";
        String result = null;
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(loginResponse);
        if (matcher.find()) {
            result = matcher.group();
        }
        result = StringUtils.substring(result, 12, -1);
        Request request = new Request(ARTIFACTURL);
        Map<String, String> queryMap = new HashMap<>();
        queryMap.put("backUrl", "http://shop.10086.cn/i/");
        queryMap.put("artifact", result);
        request.setMethod(HttpConstant.Method.GET);
        request.setHeaders(map);
        request.setQueryStringMap(queryMap);
        request.setAllowRedirect(false);
        Response response = fetch.process(CLIENT, request);
        setReferer(response.getFirstHeader("Location"));
        System.out.println(response);
    }

    private static String getWT_FPC() {
        String t = "2";
        String s = System.currentTimeMillis() + "";
        for (int i = 2; i < (32 - s.length()); i++) {
            t += Integer.toString((int) (Math.floor(Math.random() * 16)), 16);
        }
        long ts = System.currentTimeMillis();
        t = "id=" + t + ts;
        t += (":lv=" + ts + ":ss=" + ts);
        return t;
    }

    public static String getReferer() {
        return referer;
    }

    public static void setReferer(String referer) {
        CmccGeneralLogin.referer = referer;
    }

}
