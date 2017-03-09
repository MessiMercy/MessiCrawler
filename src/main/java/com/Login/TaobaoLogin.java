package com.Login;

import com.downloader.*;
import com.downloader.encrypt.EncryptLib;
import com.downloader.encrypt.RSAencoder;
import com.model.TaobaoLoginModel;
import com.parser.Html;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 淘宝登录
 * Created by Administrator on 2016/11/17.
 */
public class TaobaoLogin {
    private final static Logger LOGGER = Logger.getLogger(TaobaoLogin.class);
    private static CookieStore STORE = new BasicCookieStore();
    private final static HttpClient CLIENT = CrawlerLib.getInstanceClient(STORE);
    private final static Downloader DOWNLOADER = new Downloader(CLIENT);
    private final static Map<String, String> header = new HashMap<>();
    private final static Properties pp = new Properties();
    private final static Properties params = new Properties();
    private final static String LOGINURL = "https://login.taobao.com/member/login.jhtml?redirectURL=https://buyertrade.taobao.com/trade/itemlist/list_bought_items.htm";
    private final static Pattern REDIRECTREGEX = Pattern.compile("https://passport\\.alibaba\\.com/mini_apply_st\\.js\\?site=0&token=(.*?)&callback=callback");

    static {
        PropertyConfigurator.configure("log4j.properties");
        try {
            pp.load(new FileInputStream("taobao.properties"));
            params.load(new FileInputStream("params.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        header.put(HttpConstant.Header.REFERER, "https://login.taobao.com/member/login.jhtml?redirectURL=https://buyertrade.taobao.com/trade/itemlist/list_bought_items.htm");
        header.put(HttpConstant.Header.USER_AGENT, HttpConstant.UserAgent.FIREFOX);
//        if (false) {
//            STORE = CrawlerLib.readCookieFromDisk(new File("cookie.obj"));
//        } else {
//            func();
//        }
//        STORE.getCookies().forEach(System.out::println);
//        Request testRequest = new Request("https://buyertrade.taobao.com/trade/itemlist/list_bought_items.htm");
//        testRequest.setHeaders(header);
//        Response process = DOWNLOADER.process(testRequest);
//        new SimpleFilepipeline().printResult(process.getContent(), false, "test.txt");
        TaobaoLogin login = new TaobaoLogin();
        System.out.println(login.getEncodePw(pp.getProperty("password"), pp.getProperty("publickey"), pp.getProperty("exponent")));
        try {
            System.out.println(RSAencoder.encryptByPublicKey(pp.getProperty("password"), pp.getProperty("publickey"), pp.getProperty("exponent")));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void func() {
        TaobaoLogin login = new TaobaoLogin();
        TaobaoLoginModel model = new TaobaoLoginModel();
        model.setTPL_username(pp.getProperty("account"));
        model.setTPL_redirect_url("https://buyertrade.taobao.com/trade/itemlist/list_bought_items.htm");
        model.setTPL_password_2(login.getEncodePw(pp.getProperty("password"), pp.getProperty("publickey"), pp.getProperty("exponent")));
        model.setUa(pp.getProperty("ua"));
        model.setUm_token(pp.getProperty("um_token"));
        model.setNaviVer("firefox|50");
        login.requestMainPage(model);
        List<NameValuePair> list = login.generateValuePair(model);
        String resp = login.login(list);
        String redirectUrl = login.getLoginRedirectUrl(resp);
        LOGGER.info("成功找到重定向链接： " + redirectUrl);
        String stResponse = login.requestForST(redirectUrl);
        Pattern pattern = Pattern.compile("\"st\":\"(.*?)\"");
        Matcher matcher = pattern.matcher(stResponse);
        String st = null;
        if (matcher.find()) {
            st = matcher.group(1);
        }
        if (st != null) {
            Request loginRequest = new Request("https://login.taobao.com/member/vst.htm?st=" + st + "&TPL_username=m_a_x_mx");
            loginRequest.setHeaders(header);
            Response process = DOWNLOADER.process(loginRequest);
            System.out.println(process);
            STORE.getCookies().forEach(p -> LOGGER.info(p.getName() + ": " + p.getValue()));
        } else {
            LOGGER.info("没有找到st值，系统退出！");
        }
        CrawlerLib.storeCookie(STORE, new File("cookie.obj"));
    }

    private void requestMainPage(TaobaoLoginModel model) {
        Request request = new Request(LOGINURL);
        request.setHeaders(header);
        Response response = DOWNLOADER.process(request);
        String html = response.getContent();
        Html page = new Html(html);
        String ncoToken = page.parse("#J_NcoToken", "value", 0);
        model.setNcoToken(ncoToken);
        String gvfdcre = page.parse("input[name=gvfdcre]", "value", 0);
        model.setGvfdcre(gvfdcre);
    }

    private String getEncodePw(String pw, String publicKey, String exponent) {
        String encoded = EncryptLib.rsa(pw, publicKey, exponent);
        Optional<String> op = Optional.ofNullable(encoded);
        return op.orElse("");
    }

    private List<NameValuePair> generateValuePair(TaobaoLoginModel model) {
        List<NameValuePair> list = new ArrayList<>();
        Field[] fields = model.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                list.add(new BasicNameValuePair(field.getName(), field.get(model).toString()));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        params.forEach((k, v) -> list.add(new BasicNameValuePair(k.toString(), v.toString())));
        return list;
    }

    private String login(List<NameValuePair> list) {
        Request request = new Request(LOGINURL);
        request.setMethod(HttpConstant.Method.POST);
        request.setHeaders(header);
        request.setFormData(list);
        Response response = DOWNLOADER.process(request);
        LOGGER.info(response.toString());
        return response.getContent();
    }

    private String getLoginRedirectUrl(String html) {
        Matcher tokenUrlMatcher = REDIRECTREGEX.matcher(html);
        return tokenUrlMatcher.find() ? tokenUrlMatcher.group() : null;
    }

    private String requestForST(String tokenUrl) {
        Request stRequest = new Request(tokenUrl);
        stRequest.setHeaders(header);
        Response response = DOWNLOADER.process(stRequest);
        LOGGER.info(response.toString());
        return response.getContent();
    }
}
