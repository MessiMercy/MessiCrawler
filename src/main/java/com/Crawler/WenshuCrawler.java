package com.Crawler;

import com.downloader.Downloader;
import com.downloader.HttpConstant;
import com.downloader.Request;
import com.downloader.Response;
import com.parser.Regex;
import org.apache.commons.lang3.StringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 用于爬取中国裁判文书网 网址：http://wenshu.court.gov.cn/
 * 关键词生成器
 * 生成规则: 基层法院->关键词->年份->文书类型(判决书,裁定书)
 */
public class WenshuCrawler {
    private Downloader downloader = new Downloader(HttpConstant.UserAgent.CHROME, false).setAutoSwitchProxy(false);
    private JedisPool pool = new JedisPool("127.0.0.1", 6379);

    public static void main(String[] args) throws IOException {
        WenshuCrawler crawler = new WenshuCrawler();
        crawler.keyWordProducer();
    }

    public void keyWordProducer() {
        try (Jedis jedis = pool.getResource()) {
            String court = jedis.spop("courts");
            String form = String.format("基层法院:%s,", court);
            List<String> list = dateProducer();
            list.forEach(p -> jedis.lpush("courtKeyWord", form.concat(p)));
        }

    }

    /**
     * 如果遇见当月案件多余2000的时候,则拆分为多个请求
     * eg:
     * 基层法院:信阳市浉河区人民法院,裁判日期:2016-12-01 TO 2016-12-31
     * 分割为:
     * 基层法院:信阳市浉河区人民法院,裁判日期:2016-12-01 TO 2016-12-15
     * 基层法院:信阳市浉河区人民法院,裁判日期:2016-12-15 TO 2016-12-31
     */
    public String[] splitKeyWord(int nums, String keyWord) {
        String[] result = new String[nums];
        Pattern compile = Pattern.compile("(\\d{4}-\\d{2}-\\d{2}) TO (\\d{4}-\\d{2}-\\d{2})");
        Matcher matcher = compile.matcher(keyWord);
        Date date1 = new Date(), date2 = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        if (matcher.find()) {
            try {
                date1 = format.parse(matcher.group(1));
                date2 = format.parse(matcher.group(2));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        Calendar calendar1 = Calendar.getInstance();
        Calendar calendar2 = Calendar.getInstance();
        calendar1.setTime(date1);
        calendar2.setTime(date2);
        int day1 = calendar1.get(Calendar.DAY_OF_MONTH);
        int day2 = calendar2.get(Calendar.DAY_OF_MONTH);
        System.out.println(String.format("day1: %d day2: %d", day1, day2));
        int diff = (calendar2.get(Calendar.DAY_OF_YEAR) - calendar1.get(Calendar.DAY_OF_YEAR)) / nums;
        System.out.println(diff);
        int orignalMonth = calendar2.get(Calendar.MONTH);
        for (int i = 0; i < result.length; i++) {
            calendar1.set(Calendar.DAY_OF_MONTH, day1 + i * diff);
            calendar2.set(Calendar.DAY_OF_MONTH, day1 + (i + 1) * diff);
            calendar2.set(Calendar.MONTH, calendar1.get(Calendar.MONTH));
            if (i == result.length - 1) {
                calendar2.set(Calendar.MONTH, orignalMonth);
                result[i] = String.format(StringUtils.substringBeforeLast(keyWord, ":") + ":%s TO %s", format.format(calendar1.getTime()), format.format(date2));
            } else
                result[i] = String.format(StringUtils.substringBeforeLast(keyWord, ":") + ":%s TO %s", format.format(calendar1.getTime()), format.format(calendar2.getTime()));
        }
        return result;
    }

    /**
     * 生成从2014年至2017的格式
     * 格式举例: 裁判日期:2017-01-01 TO 2017-01-20
     */
    private List<String> dateProducer() {
        List<String> result = new ArrayList<>();
        for (int i = 4; i < 7; i++) {
            for (int j = 1; j < 10; j++) {
                result.add(String.format("201%d-0%d-01", i, j));
            }
            result.add(String.format("201%d-10-01", i));
            result.add(String.format("201%d-11-01", i));
            result.add(String.format("201%d-12-01", i));
            result.add(String.format("201%d-12-31", i));
        }
        List<String> arr = new ArrayList<>();
        for (int i = 0; i < result.size() - 1; i++) {
            arr.add(String.format("裁判日期:%s TO %s", result.get(i), result.get(i + 1)));
        }
        return arr;
    }


    public String getProvinceCourt(String province) {
        Request request = new Request("http://wenshu.court.gov.cn/Index/GetCourt");
        request.setMethod(HttpConstant.Method.POST);
        request.addFormData("province", province);
        request.addHeader(HttpConstant.Header.REFERER, "http://wenshu.court.gov.cn/Index");
        Response process = downloader.process(request);
        System.out.println(process);
        String content = process.getContent();
        content = content.replaceAll("\\\\u0027", "").replaceAll("\"", "");
        return content;
    }

    private String getCourtKey(String content) {
        Regex regex = new Regex("key:(\\d*)", content);
        StringBuilder builder = new StringBuilder();
        List<String> list = regex.toList(1);
        list.forEach(p -> builder.append(p).append(","));
        String result = builder.toString();
        return result.substring(0, result.length() - 1);//去除最后一个,
    }

    private String getChildCourtJson(String courtKey) {
        String url = "http://wenshu.court.gov.cn/Index/GetChildAllCourt";
        Request request = new Request(url);
        request.setMethod(HttpConstant.Method.POST);
        request.addFormData("keyCodeArrayStr", courtKey);
        request.addHeader(HttpConstant.Header.REFERER, "http://wenshu.court.gov.cn/Index");
        Response process = downloader.process(request);
        System.out.println(process);
        String content = process.getContent();
        content = content.replaceAll("\\\\u0027", "").replaceAll("\"", "");
        return content;
    }

    private List<String> parseCourt(String courtJson) {
        Regex regex = new Regex("court:(.+?),", courtJson);
        return regex.toList(1);
    }
}
