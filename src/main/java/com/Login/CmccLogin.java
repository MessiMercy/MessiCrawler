package com.Login;

import com.downloader.CrawlerLib;
import com.downloader.Downloader;
import com.downloader.HttpConstant;
import com.downloader.Request;
import com.pipeline.impl.SimpleFilepipeline;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.message.BasicNameValuePair;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CmccLogin {
    /**
     * 获取token值的url，使用时需要加入randomcode值
     */
    private final static String TOKENURL = "http://www.sc.10086.cn/app?service=ajaxDirect/1/System/System/javascript/&pagename=System&eventname=generateToken&ID=undefined&PAGERANDOMID=undefined&ajaxSubmitType=get&ajax_randomcode=";
    private final static String TOKENREGEX = "\"token\":\".+?\"";// 用于取token
    /**
     * 登录接口，需要加上randomcode
     */
    private final static String LOGINURL = "http://www.sc.10086.cn/app?service=ajaxDirect/1/Login/Login/javascript/&pagename=Login&eventname=ajaxLogin&record_flag=checked&ID=undefined&PAGERANDOMID=undefined&ajaxSubmitType=post&ajax_randomcode=";
    private final static CookieStore STORE = new BasicCookieStore();
    private final static HttpClient CLIENT = CrawlerLib.getInstanceClient(false, STORE);
    private final static String LOGINMESSAGE = "http://www.sc.10086.cn/app?service=ajaxDirect/1/Login/Login/javascript/&pagename=Login&eventname=sendRndPass&ID=undefined&PAGERANDOMID=undefined&ajaxSubmitType=post&ajax_randomcode=";
    /**
     * 统一接口地址
     */
    private final static String MESSAGEURL = "http://www.sc.10086.cn/app";
    private static Scanner sc = new Scanner(System.in);
    private final static Downloader fetch = new Downloader(CLIENT);

    /**
     * 总体流程： 账户名密码验证码登录->让服务器发短信->将短信发送给服务器端->获得通话详单
     */
    public static void main(String[] args) {
        Map<String, String> map = new HashMap<>();
        map.put("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.101 Safari/537.36");
        map.put("Referer", "http://www.sc.10086.cn/my/myMobile.shtml");
        login(true, map);
        // getDetailBill(map);
        searchDetailBill(map, "201610", "01", "31");
        sc.close();
    }

    /**
     * 使用密码或短信登录
     *
     * @param PwOrMs true代表用密码登录，false代表用短信
     */
    public static void login(boolean PwOrMs, Map<String, String> map) {
        Properties cmccProperties = new Properties();
        try {
            cmccProperties.load(new FileInputStream(new File("cmcc.properties")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (cmccProperties.containsKey("Cookie") && cmccProperties.getProperty("Cookie") != null) {
            String cookie = cmccProperties.getProperty("Cookie").replaceAll("\\=", "=");
            if (cookie != null) {
                System.out.println("正在从properties读取cookie: " + cookie);
                map.put("Cookie", cookie);
                CrawlerLib.storeCookie(STORE, new File("cmcc.properties"));
            }
        }
        System.out.println("是否需要重新登录，是输入1，否则为其他");
        boolean reLogin = sc.nextLine().equals("1");
        if (reLogin || !hasLogin(map)) {
            downloadVerifyCode(map, CLIENT);
            System.out.println("请去查看验证码，并输入");
            String account = cmccProperties.getProperty("account");
            System.out.println("account: " + account);
            if (PwOrMs) {
                String validateKey = sc.nextLine();
                String pw = cmccProperties.getProperty("pw");
                loginWithPw(account, pw, validateKey, map);
            } else {
                downloadVerifyCode(map, CLIENT);
                System.out.println("请去查看验证码，并输入");
                String validateKey2 = sc.nextLine();
                System.out.println("请去查看你的短信，并输入");
                sendLoginMessage(map, account);
                String message = sc.nextLine();
                loginWithMessage(account, message, validateKey2, map);
            }
            CrawlerLib.storeCookie(STORE, new File("cmcc.properties"));
        }
    }

    /**
     * 根据月份查询通话详单。
     *
     * @param yearAndMonth 例如：201609
     * @param startDay     例如：01
     * @param endDay       例如： 30
     */
    private static void searchDetailBill(Map<String, String> map, String yearAndMonth, String startDay, String endDay) {
        sendShortMessage(map, yearAndMonth, startDay, endDay);
        System.out.println("去查看你的短信，并输入！");
        String message = sc.nextLine();
        postShortMessage(message, map);
        String html = getDetailBill(map);
        new SimpleFilepipeline(new File("移动.txt")).printResult(html, false);
    }

    private static void loginWithMessage(String phoneNum, String messageNum, String validateKey,
                                         Map<String, String> map) {
        String url = "http://www.sc.10086.cn/app?service=ajaxDirect/1/Login/Login/javascript/&pagename=Login&eventname=ajaxLogin&record_flag=checked&token=undefined&ID=undefined&PAGERANDOMID=undefined&ajaxSubmitType=post&ajax_randomcode="
                + new Random().nextDouble();
        List<NameValuePair> list = new ArrayList<>();
        list.add(new BasicNameValuePair("loginChhType", "随机密码登录"));
        list.add(new BasicNameValuePair("hidden_logintype", "2"));
        list.add(new BasicNameValuePair("serialNum", phoneNum));
        list.add(new BasicNameValuePair("user_passwd1_tip", "忘记密码可用随机码登录"));
        list.add(new BasicNameValuePair("user_passwd1", ""));
        list.add(new BasicNameValuePair("user_passwd2", messageNum));
        list.add(new BasicNameValuePair("validateKey", validateKey));
        list.add(new BasicNameValuePair("vertifyErrFlag", "ok"));
        list.add(new BasicNameValuePair("loginCheckBox", "on"));
        Request request = new Request(url);
        request.setFormData(list);
        request.setHeaders(map);
        request.setMethod(HttpConstant.Method.POST);
        fetch.postEntity(CLIENT, request);
        System.out.println("正在短信登录： \r\n" + fetch.getHtml());
    }

    /**
     * 用于登录时请求发短信
     */
    private static void sendLoginMessage(Map<String, String> map, String phoneNum) {
        String url = LOGINMESSAGE + new Random().nextDouble();
        List<NameValuePair> list = new ArrayList<>();
        list.add(new BasicNameValuePair("loginChhType", "随机密码登录"));
        list.add(new BasicNameValuePair("hidden_logintype", "2"));
        list.add(new BasicNameValuePair("serialNum", phoneNum));
        list.add(new BasicNameValuePair("user_passwd1_tip", "忘记密码可用随机码登录"));
        list.add(new BasicNameValuePair("user_passwd1", "忘记密码可用随机码登录"));
        list.add(new BasicNameValuePair("user_passwd2", ""));
        list.add(new BasicNameValuePair("validateKey", "请输入验证码"));
        list.add(new BasicNameValuePair("vertifyErrFlag", ""));
        list.add(new BasicNameValuePair("loginCheckBox", "on"));
        Request request = new Request(url);
        request.setFormData(list);
        request.setHeaders(map);
        request.setMethod(HttpConstant.Method.POST);
        fetch.postEntity(CLIENT, request);
        String str = fetch.getHtml();
        System.out.println("正在发送短信： \r\n" + str);
    }

    private static String getDetailBill(Map<String, String> map) {
        String url = MESSAGEURL
                + "?service=ajaxDirect/1/fee.FeeInfo/fee.FeeInfo/javascript/&pagename=fee.FeeInfo&eventname=getDetailBill&count=0&ID=undefined&PAGERANDOMID=undefined&ajaxSubmitType=get&ajax_randomcode="
                + new Random().nextDouble();
        map.put("Referer", "http://www.sc.10086.cn/service/fee/detailBill.shtml");
        Request request = new Request(url);
        request.setHeaders(map);
        fetch.getEntity(CLIENT, request);
        return fetch.getHtml();
    }

    private static void postShortMessage(String cond_pass, Map<String, String> map) {
        String url = "http://www.sc.10086.cn/app?" + "service=ajaxDirect/1/fee.FeeInfo/fee.FeeInfo/javascript/"
                + "&pagename=fee.FeeInfo&eventname=checkRandPass" + "&cond_pass=" + cond_pass
                + "&cond_GOODS_ID=2014073100001329"
                + "&cond_GOODS_NAME=%E8%AF%A6%E5%8D%95%E6%9F%A5%E8%AF%A2&INTERFACE_MODE=17"
                + "&ID=undefined&PAGERANDOMID=undefined&ajaxSubmitType=post&ajax_randomcode="
                + new Random().nextDouble();
        map.put("Referer", "http://www.sc.10086.cn/service/product/XDCX.shtml");
        List<NameValuePair> list = new ArrayList<>();
        Request request = new Request(url);
        request.setFormData(list);
        request.setHeaders(map);
        request.setMethod(HttpConstant.Method.POST);
        fetch.postEntity(CLIENT, request);
        System.out.println(fetch.getHtml());
    }

    /**
     * 用于详单查询时短信
     */
    private static void sendShortMessage(Map<String, String> map, String yearAndMonth, String startDay, String endDay) {
        String messageUrl = getMessageUrl(yearAndMonth, startDay, endDay);
        map.put("Referer", "http://www.sc.10086.cn/service/product/XDCX.shtml");
        List<NameValuePair> list = new ArrayList<>();
        Request request = new Request(messageUrl);
        request.setFormData(list);
        request.setHeaders(map);
        fetch.postEntity(CLIENT, request);
        System.out.println(fetch.getHtml());
    }

    private static void loginWithPw(String account, String pw, String validateKey, Map<String, String> map) {
        String token = getToken(map);
        String urlWithToken = getLoginUrl(token);
        List<NameValuePair> list = getLoginPostDict(account, pw, validateKey);
        Request request = new Request(urlWithToken);
        request.setFormData(list);
        request.setHeaders(map);
        fetch.postEntity(CLIENT, request);
        String html = fetch.getHtml();
        System.out.println(html);
    }

    private static void downloadVerifyCode(Map<String, String> map, HttpClient client) {
        String downloadUrl = "http://www.sc.10086.cn/servlet/ImageServlet?ver=" + System.currentTimeMillis();
        InputStream in = fetch.downloadEntity(client, downloadUrl, map);
        try {
            BufferedImage verifyCode = ImageIO.read(in);
            ImageIO.write(verifyCode, "jpg", new File("verify.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<NameValuePair> getLoginPostDict(String account, String pw, String validateKey) {
        List<NameValuePair> list = new ArrayList<>();
        list.add(new BasicNameValuePair("loginChhType", "服务密码登录"));
        list.add(new BasicNameValuePair("hidden_logintype", "1"));
        list.add(new BasicNameValuePair("serialNum", account));
        list.add(new BasicNameValuePair("user_passwd1_tip", ""));
        list.add(new BasicNameValuePair("user_passwd1", getEncodedPw(pw)));
        list.add(new BasicNameValuePair("user_passwd2", "6位随机码"));
        list.add(new BasicNameValuePair("validateKey", validateKey));
        list.add(new BasicNameValuePair("vertifyErrFlag", "nook"));
        list.add(new BasicNameValuePair("loginCheckBox", "on"));
        return list;
    }

    /**
     * 拼接发送短信链接
     *
     * @param yearAndMonth 范例：201609
     * @param startDay     范例：01
     * @param endDay       同上
     */
    private static String getMessageUrl(String yearAndMonth, String startDay, String endDay) {
        return MESSAGEURL + "?service=ajaxDirect/1/fee.FeeInfo/fee.FeeInfo/javascript/"
                + "&pagename=fee.FeeInfo&eventname=getRandPass&cond_GOODS_ID=2014073100001329" + "&cond_queryMonth="
                + yearAndMonth + "&cond_date_begin=" + startDay + "&cond_date_end=" + endDay
                + "&cond_GOODS_NAME=%E8%AF%A6%E5%8D%95%E6%9F%A5%E8%AF%A2&cond_queryType=0"
                + "&INTERFACE_MODE=12&ID=undefined&PAGERANDOMID=undefined" + "&ajaxSubmitType=post&ajax_randomcode="
                + new Random().nextDouble();
    }

    private static String getEncodedPw(String pw) {
        return Base64.encodeBase64String(pw.getBytes());
    }

    private static String getLoginUrl(String token) {
        return getLoginurl() + new Random().nextDouble() + "&token=" + token;
    }

    private static String getToken(Map<String, String> map) {
        Request request = new Request(TOKENURL + new Random().nextDouble());
        request.setHeaders(map);
        fetch.getEntity(CLIENT, request);
        String html = fetch.getHtml();
        Pattern p = Pattern.compile(TOKENREGEX);
        Matcher matcher = p.matcher(html);
        String token = null;
        if (matcher.find()) {
            token = matcher.group();
        }
        System.out.println(token);
        if (token != null) {
            int left = token.lastIndexOf(":") + 2;
            int right = token.lastIndexOf("\"");
            return token.substring(left, right);
        }
        return null;
    }

    private static boolean hasLogin(Map<String, String> map) {
        boolean result = false;
        map.put("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.101 Safari/537.36");
        Request request = new Request("http://www.sc.10086.cn/app?service=ajaxDirect/1/fee.FeeInfo/fee.FeeInfo/javascript/&pagename=fee.FeeInfo&eventname=queryFeeinitPage&cond_GOODS_ID=2014080900001741&cond_GOODS_NAME=%E8%AF%9D%E8%B4%B9%E6%9F%A5%E8%AF%A2&ID=undefined&PAGERANDOMID=undefined&ajaxSubmitType=get&ajax_randomcode="
                + new Random().nextDouble());
        request.setHeaders(map);
        fetch.getEntity(CLIENT, request);
        String html = fetch.getHtml();
        System.out.println("话费查询情况： ");
        System.out.println(html);
        if (html.contains("实时话费")) {
            result = true;
            System.out.println("从properties里读取到已登录cookie");
        } else {
            System.out.println("检测到cookie已失效");
        }
        return result;
    }

    private static String getLoginurl() {
        return LOGINURL;
    }

    public static String getTokenurl() {
        return TOKENURL;
    }

    public static String getTokenregex() {
        return TOKENREGEX;
    }
}
