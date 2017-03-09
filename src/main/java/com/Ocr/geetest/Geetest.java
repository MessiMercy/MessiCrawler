package com.Ocr.geetest;

import com.downloader.Downloader;
import com.downloader.HttpConstant;
import com.downloader.Request;
import com.downloader.Response;
import com.google.common.io.Files;
import com.parser.Json;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 极验httpclient方式破解
 * Created by Administrator on 2016/12/19.
 */
public class Geetest {
    private final String gt = "a40fd3b0d712165c5d13e6f747e948d4";
    private final Downloader downloader = new Downloader();

    public static void main(String[] args) {
        int times = 0;
        for (int j = 0; j < 10; j++) {
            Geetest geetest = new Geetest();
            String info = geetest.getInfo();
            Json infoJson = new Json(info);
            String host = infoJson.getEle("staticservers[0]").getAsString();
            String fullImgSrc = "http://" + host + infoJson.getEle("fullbg").getAsString();
            String cutImgSrc = "http://" + host + infoJson.getEle("bg").getAsString();
            String gt = infoJson.getEle("gt").getAsString();
            String challenge = infoJson.getEle("challenge").getAsString();
            List<String> imgFullSrcList = new ArrayList<>(52);
            List<String> imgCutSrcList = new ArrayList<>(52);
            for (int i = 0; i < 52; i++) {
                imgFullSrcList.add(fullImgSrc);
                imgCutSrcList.add(cutImgSrc);
            }
            List<String[]> listFromTxt = null;
            try {
                listFromTxt = geetest.getListFromTxt();
            } catch (IOException e) {
                e.printStackTrace();
            }
            GeetestSelenium.combineImages(imgFullSrcList, listFromTxt, 26, 10, 58, "temp\\full.jpg", "jpg");
            GeetestSelenium.combineImages(imgCutSrcList, listFromTxt, 26, 10, 58, "temp\\cut.jpg", "jpg");
            int xDiff = GeetestSelenium.findXDiffRectangeOfTwoImage("temp\\full.jpg", "temp\\cut.jpg");
            List<int[]> trail_array = GeetestSelenium.get_trail_array(xDiff);
//            List<int[]> trail_array = SampleProcess.forthFunc(xDiff);
            for (int i = 0; i < trail_array.size(); i++) {
                int[] ints = trail_array.get(i);
                if (ints[0] > 50) {
                    trail_array.get(i)[0] /= 2;
                    ints[0] = ints[0] - trail_array.get(i)[0] / 2;
                    trail_array.add(i, ints);
                }
            }
            String a = geetest.encrypt(trail_array);
            System.out.println("偏移量： " + xDiff);
            String userresponse = geetest.getResponseString(xDiff, challenge);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            boolean b = geetest.sendVerify(gt, challenge, userresponse, a);
            if (b) {
                times++;
            }
        }
        System.out.println("成功次数： " + times);
    }

    private List<String[]> getListFromTxt() throws IOException {
        List<String> list = Files.readLines(new File("geetest.txt"), Charset.defaultCharset());
        return list.stream().map(p -> p.split(",")).collect(Collectors.toList());
    }

    private boolean sendVerify(String gt, String challenge, String userresponse, String a) {
        Request request = new Request("https://api.geetest.com/ajax.php");
        request.addHeader(HttpConstant.Header.USER_AGENT, HttpConstant.UserAgent.CHROME);
        request.addHeader(HttpConstant.Header.REFERER, "https://user.geetest.com/login");
        request.addQueryString("gt", gt);
        request.addQueryString("challenge", challenge);
        request.addQueryString("userresponse", userresponse);
        request.addQueryString("passtime", "1594");
        request.addQueryString("imgload", 141 + "");
        request.addQueryString("a", a);
        request.addQueryString("callback", "geetest_" + System.currentTimeMillis());
        Response process = downloader.process(request);
        System.out.println(process);
        return process.getContent().contains("validate");
    }

    private String getInfo() {
        Pattern regex = Pattern.compile("new\\sGeetest\\((\\{.*?\\})");
        String url = String.format("https://api.geetest.com/get.php?gt=%s&random=%d", gt, System.currentTimeMillis() - 1000);
        Request request = new Request(url);
        request.addHeader(HttpConstant.Header.USER_AGENT, HttpConstant.UserAgent.CHROME);
        request.addHeader(HttpConstant.Header.REFERER, "https://user.geetest.com/login");
        Response process = downloader.process(request);
        String content = process.getContent();
        Matcher matcher = regex.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        } else return null;
    }

    private String getResponseString(int offset, String challenge) {
        String ct = challenge.substring(32);
        if (ct.length() < 2) return "";
        int[] d = new int[ct.length()];
        char[] chars = ct.toCharArray();
        for (int i = 0; i < ct.length(); i++) {
            char f = ct.charAt(i);
            if (f > 57) d[i] = f - 87;
            else d[i] = f - 48;
        }
        int c = 36 * d[0] + d[1];
        int g = offset + c;
        ct = challenge.substring(0, 32);
        List<List<Character>> lists = new ArrayList<>(5);
        for (int j = 0; j < 5; j++) {
            lists.add(new ArrayList<>());
        }
        chars = ct.toCharArray();
        Map<Character, Integer> map = new LinkedHashMap<>();
        int k = 0;
        for (char aChar : chars) {
            if (!map.containsKey(aChar) || map.get(aChar) != 1) {
                map.put(aChar, 1);
                lists.get(k).add(aChar);
                k++;
                k %= 5;
            }
        }
        int n = g, o = 4;
        String p = "";
        List<Integer> ints = Arrays.asList(1, 2, 5, 10, 50);
        Random rd = new Random();
        while (n > 0) {
            if (n - ints.get(o) >= 0) {
                int size = lists.get(o).size();
//                System.out.println(size + " " + o + " " + n);
                int m = rd.nextInt(size);
                p += lists.get(o).get(m);
                n -= ints.get(o);
            } else {
                lists.remove(o);
//                ints.remove(o);
                o--;
            }
        }
        return p;
    }

    private String encode(int n) {
        String b = "()*,-./0123456789:?@ABCDEFGHIJKLMNOPQRSTUVWXYZ_abcdefghijklmnopqr";
        int c = b.length();
        char d = (char) 0;
        int e = Math.abs(n);
        int f = e / c;
        if (f >= c)
            f = c - 1;
        if (f != 0) {
            d = b.charAt(f);
            e %= c;
        }
        String g = "";
        if (n < 0)
            g += "!";
        if (d != 0)
            g += "$";

        return g + (d == 0 ? "" : d) + b.charAt(e);
    }

    private String encrypt(List<int[]> action) {
        List<int[]> a = action;
        String dx = "", dy = "", dt = "";
        for (int i = 0; i < a.size(); i++) {
            char replace = replace(a.get(i));
            if (replace != 0) {
                dy += replace;
            } else {
                dx += (encode(a.get(i)[0]));
                dy += (encode(a.get(i)[1]));
            }
            dt += (encode(a.get(i)[2]));
        }
        return dx + "!!" + dy + "!!" + dt;
    }


    private char replace(int[] a2) {
        int[][] b = new int[][]{new int[]{1, 0}, new int[]{2, 0}, new int[]{1, -1},
                new int[]{1, 1}, new int[]{0, 1}, new int[]{0, -1},
                new int[]{3, 0}, new int[]{2, -1}, new int[]{2, 1}};
        String c = "stuvwxyz~";
        for (int i = 0; i < b.length; i++) {
            if (a2[0] == b[i][0] && a2[1] == b[i][1])
                return c.toCharArray()[i];
        }
        return '\0';
    }

}
