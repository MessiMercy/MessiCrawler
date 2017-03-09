package com.Login;

import com.downloader.*;
import com.downloader.cookie.CookiePersistence;
import com.downloader.cookie.impl.FileCookiePersistence;
import com.downloader.encrypt.EncryptLib;
import com.parser.Html;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.message.BasicNameValuePair;

import java.util.*;

/**
 * Created by Administrator on 2016/12/13.
 */
public class JdLogin {
    private Downloader DOWNLOADER = new Downloader(HttpConstant.UserAgent.CHROME);
    private final String LOGINURL = "https://passport.jd.com/uc/loginService";
    private final String LOGINPAGEURL = "https://passport.jd.com/uc/login";
    private final String rsaPubKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDlOJu6TyygqxfWT7eLtGDwajtNFOb9I5XRb6khyfD1Yt3YiCgQWMNW649887VGJiGr/L5i2osbl8C9+WJTeucF+S76xFxdU6jE0NQ+Z+zEdhUTooNRaY5nZiu5PgDB0ED/ZKBUSLKL7eibMxZtMlUDHjm4gwQco1KRMDSmXSMkDwIDAQAB";
    private final String chromeFingerprint = "59f576227f67904c6b507cedb7f9b488";
    private static Map<String, String> headers = new HashMap<>();

    public static void main(String[] args) {
        JdLogin login = new JdLogin();
        CookiePersistence persistence = new FileCookiePersistence();
        if (false) {
            login.login("18782934825", "m.a.x.920108");
        } else {
            BasicCookieStore store = persistence.recoverCookieStore("jd");
            HttpClient client = CrawlerLib.getInstanceClient(false, store);
            login.DOWNLOADER = new Downloader(client);
            login.DOWNLOADER.setStore(store);
        }
        login.getAddressList();
        persistence.saveCookieStore("jd", (BasicCookieStore) login.DOWNLOADER.getStore());
    }

    public void getAddressList() {
        Request request = new Request("https://order.jd.com/center/list.action");
        Response process = DOWNLOADER.process(request);
        Html html = new Html(process.getContent());
        List<String> parse = html.parse("div.pc", null);
        parse.forEach(System.out::println);
    }

    public void login(String account, String pw) {
        Request loginRequest = new Request(LOGINURL);
        loginRequest.setMethod(HttpConstant.Method.POST);
        List<NameValuePair> formData = new ArrayList<>();
        getParamFromPage(formData, getLoginPage());
        formData.add(new BasicNameValuePair("eid", ""));
        formData.add(new BasicNameValuePair("loginname", account));
        formData.add(new BasicNameValuePair("nloginpwd", EncryptLib.rsa(pw, rsaPubKey)));
        formData.add(new BasicNameValuePair("chkRememberMe", "on"));
        formData.add(new BasicNameValuePair("authcode", ""));
        loginRequest.setFormData(formData);
        String uuid = null;
        for (NameValuePair pair : formData) {
            if (pair.getName().equals("uuid")) {
                uuid = pair.getValue();
            }
        }
        Map<String, String> queryString = new LinkedHashMap<>();
        queryString.put("uuid", uuid);
        queryString.put("ReturnUrl", "https://order.jd.com/center/list.action");
        queryString.put("r", new Random().nextDouble() + "");
        queryString.put("version", "2015");
        loginRequest.setQueryStringMap(queryString);
        loginRequest.setHeaders(headers);
        Response response = DOWNLOADER.process(loginRequest);
        System.out.println(response);
    }

    public String getLoginPage() {
        Request loginPage = new Request(LOGINPAGEURL);
        Response process = DOWNLOADER.process(loginPage);
        System.out.println(process);
        return process.getContent();
    }

    public void getParamFromPage(List<NameValuePair> list, String page) {
        Html html = new Html(page);
        String uuid = html.parse("#uuid", "value", 0);
        String _t = html.parse("#token", "value", 0);
        String loginType = html.parse("#loginType", "value", 0);
        list.add(new BasicNameValuePair("uuid", uuid));
        list.add(new BasicNameValuePair("_t", _t));
        list.add(new BasicNameValuePair("loginType", loginType));
    }


}
