package com.Login;

import com.downloader.*;
import com.downloader.cookie.SimpleHostCookie;
import com.google.common.collect.Maps;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.*;

/**
 * Created by Administrator on 2016/10/21.
 */
public class CuccLogin {
    private static final String LOGINURL = "https://uac.10010.com/portal/Service/MallLogin";
    private static CookieStore store = new BasicCookieStore();
    private HttpClient client = CrawlerLib.getInstanceClient(false, store);
    private Downloader fetch = new Downloader(client);
    private static Map<String, String> map = Maps.newHashMap();
    private static Properties pp = new Properties();
    /**
     * 通话详单查询url 对应menuid为000100030001
     */
    public static final String CALLDETAILURL = "http://iservice.10010.com/e3/static/query/callDetail";
    /**
     * 短信详单查询url 对应menuid为000100030002
     */
    public static final String SMSDETAILURL = "http://iservice.10010.com/e3/static/query/sms";
    /**
     * 流量查询url 对应menuid为000100030004
     */
    public static final String CALLFLOWURL = "http://iservice.10010.com/e3/static/query/callFlow";
    /**
     * 上网记录url 对应menuid为000100030009
     */
    public static final String CALLNETURL = "http://iservice.10010.com/e3/static/query/callNetPlayRecord";
    /**
     * mallcity的值一般情况下默认为北京，特殊情况下备份。checklogin链接里也有
     */
    private final String area = "{ 11: \"北京\", 12: \"天津\", 13: \"河北\", 14: \"山西\", 15: \"内蒙古\", 21: \"辽宁\", 22: \"吉林\", 23: \"黑龙江\", 31: \"上海\", 32: \"江苏\", 33: \"浙江\", 34: \"安徽\", 35: \"福建\", 36: \"江西\", 37: \"山东\", 41: \"河南\", 42: \"湖北\", 43: \"湖南\", 44: \"广东\", 45: \"广西\", 46: \"海南\", 50: \"重庆\", 51: \"四川\", 52: \"贵州\", 53: \"云南\", 54: \"西藏\", 61: \"陕西\", 62: \"甘肃\", 63: \"青海\", 64: \"宁夏\", 65: \"新疆\", 71: \"台湾\", 81: \"香港\", 82: \"澳门\", 91: \"国外\" }";

    public static void main(String[] args) throws IOException {
        map.put(HttpConstant.Header.USER_AGENT,
                "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.101 Safari/537.36");
        CuccLogin login = new CuccLogin();
//        CuccSelenium selenium = new CuccSelenium("18615711454", "549527");
//        selenium.process();
//        WebDriver.Options op = selenium.getDriver().manage();
//        Set<org.openqa.selenium.Cookie> set = op.getCookies();
//        set.forEach(p -> login.store.addCookie(new SimpleHostCookie(p.getName(), p.getValue())));
        if (true) {
            login.login("18615711454", "549527");
//            CrawlerLib.getCookieAndStore(pp, store, "cucc.properties");
        } else {
//            CrawlerLib.addCookieFromProperties(pp, store, new File("cucc.properties"));
        }
        login.presearch();
        List<Cookie> list = store.getCookies();
        list.forEach(p -> System.out.println(p.getName() + ": " + p.getValue()));
        login.billSearch(CALLNETURL, "000100030001", "1", "100", "2016-10-01", "2016-10-31");
    }

    /**
     * 登录电信
     */
    public void login(String userName, String pw) {
        presearch();
        Request request = new Request(LOGINURL);
        Map<String, String> queryMap = new LinkedHashMap<>();
        queryMap.put("callback", "jQuery17207583937969613204_" + System.currentTimeMillis());
        queryMap.put("req_time", System.currentTimeMillis() + "");
        queryMap.put("redirectURL", "http://iservice.10010.com/e3/");
        queryMap.put("userName", userName);
        queryMap.put("password", pw);
        queryMap.put("pwdType", "01");
        queryMap.put("productType", "01");
        queryMap.put("redirectType", "01");
        queryMap.put("rememberMe", "1");
        queryMap.put("_", System.currentTimeMillis() + "");
        request.setQueryStringMap(queryMap);
        request.setMethod(HttpConstant.Method.GET);
        map.put(HttpConstant.Header.REFERER, "https://uac.10010.com/portal/homeLogin");
        request.setHeaders(map);
        Response response = fetch.process(request);
        System.out.println(response);
    }

    /**
     * 请求结果返回时间5s以上，默认超时时间20s
     *
     * @param url       仅限查询账单相关url
     * @param beginDate 格式：2016-10-01 仅限最近半年
     * @param endDate   格式： 2016-10-21 不能超过当天,开始日期和结束日期不能跨月
     * @param menuid    与对应账单相关
     * @param pageNo    查询页数
     * @param pageSize  分为20,50,100
     */
    public void billSearch(String url, String menuid, String pageNo, String pageSize, String beginDate, String endDate) {
        Request request = new Request(url);
        request.setTimeout(20000);
        request.setMethod(HttpConstant.Method.POST);
        map.put(HttpConstant.Header.REFERER, "http://iservice.10010.com/e3/query/call_dan.html");
        map.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        map.put("X-Requested-With", "XMLHttpRequest");
        request.setHeaders(map);
        Map<String, String> queryMap = new LinkedHashMap<>();
        queryMap.put("_", System.currentTimeMillis() + "");
        queryMap.put("menuid", menuid);
        request.setQueryStringMap(queryMap);
        List<NameValuePair> list = new ArrayList<>();
        list.add(new BasicNameValuePair("pageNo", pageNo));
        list.add(new BasicNameValuePair("pageSize", pageSize));
        list.add(new BasicNameValuePair("beginDate", beginDate));
        list.add(new BasicNameValuePair("endDate", endDate));
        request.setHeaders(map);
        request.setFormData(list);
        Response response = fetch.process(request);
        System.out.println(response.toString());
    }

    public void presearch() {
        Request cityCookie = new Request("http://res.mall.10010.cn/mall/res/static/notice/upgradeNotice.js?t=" + System.currentTimeMillis() + "&_=" + System.currentTimeMillis());
        fetch.process(cityCookie);
        Request request = new Request("http://iservice.10010.com/e3/static/check/checklogin/?_=" + System.currentTimeMillis());
        request.setMethod(HttpConstant.Method.POST);
        map.put(HttpConstant.Header.REFERER, "http://iservice.10010.com/e3/query/call_dan.html");
        request.setHeaders(map);
        Response response = fetch.process(request);
        store.addCookie(new SimpleHostCookie("MENUURL", "%2Fe3%2Fnavhtml3%2FWT3%2FWT_MENU_3_001%2F081%2F112.html%3F_%3D" + System.currentTimeMillis(), "iservice.10010.com"));
        store.addCookie(new SimpleHostCookie("MIE", "00090001", "iservice.10010.com"));
        store.addCookie(new SimpleHostCookie("MII", "000100030009", "iservice.10010.com"));
        System.out.println(response.toString());
    }

}
