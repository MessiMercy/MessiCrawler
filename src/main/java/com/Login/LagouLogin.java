package com.Login;

import com.downloader.*;
import com.google.common.io.Files;
import com.google.gson.*;
import com.model.LagouAddressModel;
import com.model.LagouModel;
import com.parser.Json;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.message.BasicNameValuePair;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LagouLogin {
    private static CookieStore store = new BasicCookieStore();
    private static HttpClient client = CrawlerLib.getInstanceClient(false, store);
    private static Downloader fetch = new Downloader(client);
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        test();
        // downloadPost("775857377240432640");
    }

    private static void postDownload() {
        Map<String, String> map = new HashMap<>();
        map.put("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.101 Safari/537.36");
        map.put("Referer", "https://passport.lagou.com/login/login.html");
        Properties pp = new Properties();
        try {
            pp.load(new FileInputStream(new File("me.properties")));
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (pp.containsKey("Cookie")) {
            store = CrawlerLib.readCookieFromDisk(new File("cookie.obj"));
        }
        String pw = pp.getProperty("password");
        String ss = encodePw(pw);
        System.out.println(ss);
        String account = pp.getProperty("account");
        boolean hasLogin = hasLogin(map);
        if (!hasLogin) {
            login(account, ss, map, pp);
        }
        String url = "https://easy.lagou.com/can/list.json?stage=NEW&positionId=0" + "&timeStr="
                + System.currentTimeMillis();
        String posts = getPostJson(url, map);
        if (hasLogin) {
            CrawlerLib.storeCookie(store, new File("lagoucookie.obj"));
        }
        String url2 = "https://easy.lagou.com/can/list.json?stage=NEW&positionId=0" + "&timeStr="
                + (System.currentTimeMillis() - 2 * 24 * 60 * 60 * 1000);
        String anotherPost = getPostJson(url2, map);
        System.out.println(posts + "\r\n" + anotherPost);
        posts += anotherPost;
        String postRegex = "\"id\":\"\\w+\"";
        String nameRegex = "\"candidateName\":\".+?\"";
        Pattern pattern = Pattern.compile(postRegex);
        Pattern namePattern = Pattern.compile(nameRegex);
        Matcher ma = pattern.matcher(posts);
        Matcher nameMatcher = namePattern.matcher(posts);
        // List<String> list = new ArrayList<>();
        Map<String, String> list = new HashMap<>();
        while (ma.find() && nameMatcher.find()) {
            // list.add(ma.group());
            list.put(ma.group(), nameMatcher.group());
        }
        Set<Entry<String, String>> set = list.entrySet();
        for (Entry<String, String> entry : set) {
            downloadPost(controlStr(entry.getKey()), client, controlStr(entry.getValue()), map);
        }
    }

    private static String controlStr(String str) {
        return str.substring(str.indexOf(":") + 2, str.length() - 1);
    }

    public static String getPubCodeById(String id) {
        String url = "https://easy.lagou.com/resume/pubCode.json?resumeId=" + id + "&_=" + System.currentTimeMillis();
        Map<String, String> map = new HashMap<>();
        map.put("Accept-Encoding", "gzip, deflate, sdch, br");
        map.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        map.put("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.101 Safari/537.36");
        map.put("Upgrade-Insecure-Requests", "1");
        map.put("Connection", "keep-alive");
        map.put("Referer", "https://easy.lagou.com/can/index.htm?positionId=0&stage=NEW&resumeId=" + id);
        Request request = new Request(url);
        request.setHeaders(map);
        fetch.getEntity(client, request);
        String pubCodeJson = fetch.getHtml();
        JsonElement json = new JsonParser().parse(pubCodeJson);
        String pubCode = json.getAsJsonObject().get("content").getAsJsonObject().get("data").getAsJsonObject()
                .get("pubCode").getAsString();
        System.out.println("pubCode: " + pubCode);
        return pubCode;
    }

    private static void downloadPost(String id, HttpClient client, String filename, Map<String, String> map) {
        // String url = "https://easy.lagou.com/resume/" + id + ".pdfa";
        // String pubCode = getPubCodeById(id);
        String url = "https://easy.lagou.com/resume/download.htm?resumeId=" + id;
        // String url = "https://easy.lagou.com/pub/resume/" + pubCode +
        // ".pdfa";
        // Map<String, String> map = new HashMap<>();
        map.put("Accept-Encoding", "gzip, deflate, sdch, br");
        map.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        map.put("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.101 Safari/537.36");
        map.put("Upgrade-Insecure-Requests", "1");
        map.put("Connection", "keep-alive");
        // map.put("Referer", url + "&stage=NEW");
//		HttpResponse response = downloadEntity(client, url, map, "utf-8");
        fetch.downloadEntity(client, url, map);
        HttpResponse response = fetch.getResponse();
        System.out.println(response.getStatusLine().getStatusCode());
        org.apache.http.Header[] headers = response.getAllHeaders();
        for (Header header1 : headers) {
            System.out.println(header1.getName() + ": " + header1.getValue());
        }
        org.apache.http.Header header = response.getFirstHeader("Content-Disposition");
        // org.apache.http.Header header2 =
        // response.getFirstHeader("Content-Length");
        // System.out.println("Content-Length" + header2.getValue());
        String value = header.getValue();
        // String filename = " ";
        if (value.contains("filename")) {
            int left = value.lastIndexOf(".");
            int right = value.length() - 1;
            // try {
            // System.out.println(URLEncoder.encode(value, "utf-8"));
            // } catch (UnsupportedEncodingException e1) {
            // e1.printStackTrace();
            // }
            System.out.println(value);
            String suffix = value.substring(left, right);
            filename += suffix;
            System.out.println(filename);
        }
        InputStream in = null;
        // BufferedInputStream input = null;
        // try {
        try {
            in = response.getEntity().getContent();
            java.nio.file.Files.copy(in, Paths.get("post", filename));
        } catch (UnsupportedOperationException | IOException e) {
            e.printStackTrace();
        }
        // input = new BufferedInputStream(in);
        // } catch (UnsupportedOperationException e1) {
        // e1.printStackTrace();
        // } catch (IOException e1) {
        // e1.printStackTrace();
        // }
        // File dir = new File("post");
        // if (!dir.exists()) {
        // dir.mkdir();
        // }
        // FileOutputStream fo;
        // try {
        // fo = new FileOutputStream(new File(dir, filename));
        // byte[] buf = new byte[1024];
        // int length = 0;
        // System.out.println("��ʼ����:" + filename);
        // while ((length = input.read(buf, 0, buf.length)) != -1) {
        // fo.write(buf, 0, length);
        // }
        // in.close();
        // fo.close();
        // input.close();
        // } catch (IOException e) {
        // e.printStackTrace();
        // }

        System.out.println(filename + "下载完成");
    }

//	public static HttpResponse downloadEntity(HttpClient client, String url, Map<String, String> headers,
//			String charset) {
//		HttpGet get = new HttpGet(url);
//		if (charset == null) {
//			charset = "UTF-8";
//		}
//		if (headers != null && !headers.isEmpty()) {
//			Iterator<Entry<String, String>> iterator = headers.entrySet().iterator();
//			while (iterator.hasNext()) {
//				Entry<String, String> entry = iterator.next();
//				get.setHeader(entry.getKey(), entry.getValue());
//			}
//		}
//		RequestConfig.Builder config = RequestConfig.custom().setConnectTimeout(10 * 1000).setSocketTimeout(5 * 1000);
//		get.setConfig(config.build());
//		System.out.println("--------------------");
//		HttpResponse response = null;
//		try {
//			response = client.execute(get);
//			System.out.println("++++++++++++++++++++++");
//			// html = EntityUtils.toString(response.getEntity(), charset);
//		} catch (ClientProtocolException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			System.out.println("下载" + url + "失败" + "\r\n");
//			e.printStackTrace();
//		} finally {
//			// get.abort();
//			System.out.println("*****************");
//		}
//		try {
//			Thread.sleep(2000);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//		return response;
//
//	}

    private static String getPostJson(String url, Map<String, String> map) {

        // Map<String, String> map = new HashMap<>();
        map.put("X-Anit-Forge-Code", "0");
        map.put("X-Anit-Forge-Token", "None");
        map.put("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.101 Safari/537.36");
        map.put("Referer", "https://easy.lagou.com/can/index.htm?positionId=0&stage=NEW");
        StringBuilder sb = new StringBuilder();
        Request request = new Request(url);
        request.setHeaders(map);
        fetch.getEntity(client, request);
        String result1 = fetch.getHtml();
        sb.append(result1);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    private static String encodePw(String pw) {
        String en = null;
        try {
            en = URLEncoder.encode(pw, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String temp = md5(en);
        String str = "veenike";
        String result = str + temp + str;
        return md5(result);
    }

    private static String md5(String keyWord) {
        try {
            MessageDigest digest = MessageDigest.getInstance("md5");
            digest.update(keyWord.getBytes());
            return (new BigInteger(1, digest.digest()).toString(16));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void login(String userName, String encodedPass, Map<String, String> map, Properties pp) {
        Request request = new Request("https://passport.lagou.com/login/login.html");
        request.setHeaders(map);
        fetch.getEntity(client, request);
        String html = fetch.getHtml();
        String Forge_Token_regex = "X_Anti_Forge_Token.*;";
        String forge_token = null;
        String X_Anti_Forge_Code_regex = "X_Anti_Forge_Code.*;";
        String x_anti_forge = null;
        Pattern p = Pattern.compile(Forge_Token_regex);
        Matcher matcher = p.matcher(html);
        if (matcher.find()) {
            forge_token = matcher.group();
            forge_token = StringUtils.substringBetween(forge_token, "'");
            System.out.println(forge_token);
        }
        Pattern pa = Pattern.compile(X_Anti_Forge_Code_regex);
        matcher = pa.matcher(html);
        if (matcher.find()) {
            x_anti_forge = matcher.group();
            x_anti_forge = StringUtils.substringBetween(x_anti_forge, "'");
            System.out.println(x_anti_forge);
        }
        map.put("X-Anit-Forge-Code", x_anti_forge);
        map.put("X-Anit-Forge-Token", forge_token);
        List<NameValuePair> list = new ArrayList<>();
        list.add(new BasicNameValuePair("isValidate", "true"));
        list.add(new BasicNameValuePair("password", encodedPass));
        // list.add(new BasicNameValuePair("password",
        // "d111c8acc77f12cd6c25b3cd4aa91a19"));
        list.add(new BasicNameValuePair("request_form_verifyCode", ""));
        list.add(new BasicNameValuePair("submit", ""));
        list.add(new BasicNameValuePair("username", userName));
        Request request1 = new Request("https://passport.lagou.com/login/login.json");
        request1.setHeaders(map);
        request1.setFormData(list);
        request1.setMethod(HttpConstant.Method.POST);
        Response response = fetch.postEntity(client, request1);
        // HttpResponse res = fetch.getResponse();
        String str = "null";
        str = response.getContent();
        System.out.println(str);
        // getCookieAndStore(pp);
        if (str.contains("10010")) {
            downloadVerifyCode(map);
            System.out.println("验证码错误，请填写正确的验证码！");

            String verify = scanner.nextLine();
            list.remove(new BasicNameValuePair("request_form_verifyCode", ""));
            list.add(new BasicNameValuePair("request_form_verifyCode", verify));
            scanner.close();
            Request request2 = new Request("https://passport.lagou.com/login/login.json");
            request2.setFormData(list);
            request2.setHeaders(map);
            request2.setMethod(HttpConstant.Method.POST);
            Response response1 = fetch.postEntity(client, request2);
            // getCookieAndStore(pp);
            // HttpResponse resWithVerify = fetch.getResponse();
            System.out.println(response1.getContent());
        }
    }

    private static void downloadVerifyCode(Map<String, String> map) {
        String downloadUrl = "https://passport.lagou.com/vcode/create?from=register&refresh="
                + System.currentTimeMillis();
        InputStream in = fetch.downloadEntity(client, downloadUrl, map);
        try {
            BufferedImage verifyCode = ImageIO.read(in);
            ImageIO.write(verifyCode, "jpg", new File("verify.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean hasLogin(Map<String, String> map) {
        boolean result = false;
        map.put("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.101 Safari/537.36");
        Request request = new Request("https://easy.lagou.com/can/index.htm?positionId=0&stage=NEW");
        request.setHeaders(map);
        fetch.getEntity(client, request);
        String html = fetch.getHtml();
        if (html.contains("全部候选人")) {
            result = true;
            System.out.println("从properties里读取到已登录cookie");
        } else {
            System.out.println("检测到cookie已失效");
        }
        return result;
    }

    private static List<String> getAddressList(Map<String, String> map) {
        String url = "https://easy.lagou.com/workAddress/list.json";
        Request request = new Request(url);
        request.setHeaders(map);
        Response response = fetch.process(request);
        System.out.println(response);
        String content = response.getContent();
        List<String> idAddress = new ArrayList<>();
        Json json = new Json(content);
        JsonElement element = json.getEle("content.rows");
        JsonArray array = element.getAsJsonArray();
        for (int i = 0; i < array.size(); i++) {
            JsonObject e = array.get(i).getAsJsonObject();
            String id = e.get("id").getAsString();
            String province = e.get("province").getAsString();
            String city = e.get("city").getAsString();
            String district = e.get("district").getAsString();
            String detailAddress = e.get("detailAddress").getAsString();
            idAddress.add(id + "|" + province + "|" + city + "|" + district + "|" + detailAddress);
        }
        return idAddress;
    }

    private static void addForgeCode(Map<String, String> map) {
        Request request = new Request("https://passport.lagou.com/grantServiceTicket/grant.html");
        map.put(HttpConstant.Header.REFERER, "https://easy.lagou.com/position/my_offline_positions.htm");
        request.setHeaders(map);
        Response response = fetch.process(request);
        Request request1 = new Request("https://easy.lagou.com/parentposition/createPosition.htm");
        request1.setHeaders(map);
        Response response1 = fetch.process(request1);
        String html = response1.getContent();
        System.out.println(html);
        String forge_token = null;
        String forge_code = null;
        if (html != null) {
            String Forge_Token_regex = "X_Anti_Forge_Token.*;";
            String X_Anti_Forge_Code_regex = "X_Anti_Forge_Code.*;";
            Pattern p = Pattern.compile(Forge_Token_regex);
            Matcher matcher = p.matcher(html);
            if (matcher.find()) {
                forge_token = matcher.group();
                forge_token = StringUtils.substringBetween(forge_token, "'");
                System.out.println(forge_token);
            }
            Pattern pa = Pattern.compile(X_Anti_Forge_Code_regex);
            matcher = pa.matcher(html);
            if (matcher.find()) {
                forge_code = matcher.group();
                forge_code = StringUtils.substringBetween(forge_code, "'");
                System.out.println(forge_code);
            }
        }
        if (forge_token != null && forge_code != null) {
            map.put("X-Anit-Forge-Code", forge_code);
            map.put("X-Anit-Forge-Token", forge_token);
        }
        map.put(HttpConstant.Header.REFERER, "https://easy.lagou.com/parentposition/createPosition.htm");
    }

    private static void positionAdd(LagouModel model, Map<String, String> map) {
        map.forEach((a, b) -> System.out.println(a + ": " + b));
        Request request = new Request("https://easy.lagou.com/parentposition/createParentPosition.json");
        request.setHeaders(map);
        List<NameValuePair> list = new ArrayList<>();
        list.add(new BasicNameValuePair("channelId", model.getChannelId()));
        list.add(new BasicNameValuePair("positionType", model.getPositionType()));
        list.add(new BasicNameValuePair("positionName", model.getPositionName()));
        list.add(new BasicNameValuePair("positionThirdType", model.getPositionThirdType()));
        list.add(new BasicNameValuePair("department", model.getDepartment()));
        list.add(new BasicNameValuePair("jobNature", model.getJobNature()));
        list.add(new BasicNameValuePair("salaryMin", model.getSalaryMin()));
        list.add(new BasicNameValuePair("salaryMax", model.getSalaryMax()));
        list.add(new BasicNameValuePair("workYear", model.getWorkYear()));
        list.add(new BasicNameValuePair("education", model.getEducation()));
        list.add(new BasicNameValuePair("positionBrightPoint", model.getPositionBrightPoint()));
        list.add(new BasicNameValuePair("positionDesc", model.getPositionDesc()));
        list.add(new BasicNameValuePair("workAddressId", model.getWorkAddressId()));
        request.setFormData(list);
        request.setMethod(HttpConstant.Method.POST);
        request.setTimeout(10 * 1000);
        Response response = fetch.process(request);
        System.out.println(response);
    }

    private static void test() {
        Map<String, String> map = new HashMap<>();
        map.put("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.101 Safari/537.36");
        map.put("Referer", "https://passport.lagou.com/login/login.html");
        map.put("Origin", "https://easy.lagou.com");
        Properties pp = new Properties();
        try {
            pp.load(new FileReader("me.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        String account = pp.getProperty("account");
        String pw = pp.getProperty("password");
        pw = encodePw(pw);
        if (false) {
//            CrawlerLib.addCookieFromProperties(pp, store, new File("me.properties"));
            CrawlerLib.storeCookie(store, new File("lagoucookie.obj"));
        } else {
            login(account, pw, map, pp);
        }
        store.getCookies().forEach(p -> System.out.println(p.getName() + ": " + p.getValue()));
        addForgeCode(map);
        List<String> addressList = getAddressList(map);
//        CrawlerLib.getCookieAndStore(pp, store, "me.properties");
        addressList.forEach(System.out::println);
        System.out.println("请选择并输入id号，或者输入0表示新增地址");
        String id = scanner.nextLine();
        if (id.equals("0")) {
            LagouAddressModel addressModel = genAddress(map);
            String res = addAddress(addressModel, map);
            Json json = new Json(res);
            id = json.getEle("content.data.address.id").getAsString();
        }
        LagouModel model = genModel();
        model.setWorkAddressId(id);
        positionAdd(model, map);
    }

    private static LagouModel genModel() {
        LagouModel model = new LagouModel();
        model.setPositionType("后端开发");
        model.setPositionThirdType("Python");
        model.setPositionName("robot test");
        model.setDepartment("test department");
        model.setJobNature("兼职");
        model.setSalaryMin("10");
        model.setSalaryMax("15");
        model.setWorkYear("应届毕业生");
        model.setEducation("不限");
        model.setPositionBrightPoint("五险一金,年底翻倍");
        model.setPositionDesc("<p>测试测试测试测试测试测试测试测试测试测试测试测试</p>");
        return model;
    }

    /**
     * 用于解析下一级菜单，返回一个以name为键，值为属性数组。属性数组依次为id，name，parentid，code。用于组装新增地点参数
     */
    private static Map<String, String[]> getAddressObj(String str) {
        Map<String, String[]> map = new HashMap<>();
        Pattern pattern = Pattern.compile("\"id\":(\\d+),\"name\":\"(\\S+?)\",\"parentId\":(\\d*),\"code\":\"(\\d+)?\"");
        Matcher matcher = pattern.matcher(str);
        while (matcher.find()) {
            String[] addressObj = new String[4];
            int i = matcher.groupCount();
            for (int j = 1; j <= i; j++) {
//                System.out.print(matcher.group(j) + "-");
                addressObj[j - 1] = matcher.group(j);
            }
            map.put(addressObj[1], addressObj);
        }
        return map;
    }

    private static String getChildLbs(String addressCode, Map<String, String> map) {
        Request request = new Request("https://easy.lagou.com/lbs/getChildLbsInfoByCode.json?code=" + addressCode);
        request.setHeaders(map);
        Response response = fetch.process(request);
        System.out.println(response);
        return response.getContent();
    }

    private static String addAddress(LagouAddressModel model, Map<String, String> map) {
        Request request = new Request("https://easy.lagou.com/workAddress/add.json");
        request.setMethod(HttpConstant.Method.POST);
        request.setHeaders(map);
        List<NameValuePair> list = new ArrayList<>();
        list.add(new BasicNameValuePair("province", model.getProvince()));
        list.add(new BasicNameValuePair("provinceId", model.getProvinceId()));
        list.add(new BasicNameValuePair("provinceCode", model.getProvinceCode()));
        list.add(new BasicNameValuePair("city", model.getCity()));
        list.add(new BasicNameValuePair("cityId", model.getCityId()));
        list.add(new BasicNameValuePair("cityCode", model.getCityCode()));
        list.add(new BasicNameValuePair("district", model.getDistrict()));
        list.add(new BasicNameValuePair("districtId", model.getDistrictId()));
        list.add(new BasicNameValuePair("districtCode", model.getDistrictCode()));
        list.add(new BasicNameValuePair("detailAddress", model.getDetailAddress()));
        list.add(new BasicNameValuePair("lat", "0"));
        list.add(new BasicNameValuePair("lng", "0"));
        list.add(new BasicNameValuePair("bizAreaIds", ""));
        list.add(new BasicNameValuePair("bizAreaCodes", ""));
        list.add(new BasicNameValuePair("bizArea", ""));
        request.setFormData(list);
        Response response = fetch.process(request);
        System.out.println(response);
        return response.getContent();
    }

    private static LagouAddressModel genAddress(Map<String, String> map) {
        LagouAddressModel model = new LagouAddressModel();
        String provinceTable = null;
        try {
            provinceTable = Files.toString(new File("省份列表.txt"), Charset.forName("gbk"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Map<String, String[]> provinceObj = null;
        if (provinceTable != null) {
            provinceObj = getAddressObj(provinceTable);
        }
        System.out.println("请输入省份");
        String province = scanner.nextLine();
        while (!provinceObj.containsKey(province)) {
            System.out.println("输入省份有误，请重新输入！");
            province = scanner.nextLine();
        }
        model.setProvince(province);
        String[] provinceDetail = provinceObj.get(province);
        model.setProvinceId(provinceDetail[0]);
        model.setProvinceCode(provinceDetail[3]);
        String cityTable = getChildLbs(provinceDetail[3], map);
        Map<String, String[]> cityObj = null;
        if (cityTable != null) {
            cityObj = getAddressObj(cityTable);
        }
        cityObj.keySet().forEach(p -> System.out.print(p + " "));
        System.out.println("请输入城市");
        String city = scanner.nextLine();
        while (!cityObj.containsKey(city)) {
            System.out.println("输入城市名有误，请重新输入！");
            city = scanner.nextLine();
        }
        String[] cityDetail = cityObj.get(city);
        model.setCity(city);
        model.setCityCode(cityDetail[3]);
        model.setCityId(cityDetail[0]);
        String districtTable = getChildLbs(cityDetail[3], map);
        Map<String, String[]> districtObj = null;
        if (districtTable != null) {
            districtObj = getAddressObj(districtTable);
        }
        districtObj.keySet().forEach(p -> System.out.print(p + " "));
        System.out.println("请输入区名");
        String district = scanner.nextLine();
        while (!districtObj.containsKey(district)) {
            System.out.println("输入区名有误，请重新输入");
            district = scanner.nextLine();
        }
        String[] districtDetail = districtObj.get(district);
        model.setDistrict(district);
        model.setDistrictCode(districtDetail[3]);
        model.setDistrictId(districtDetail[0]);
        System.out.println("请输入具体地址！");
        model.setDetailAddress(scanner.nextLine());
        return model;
    }


}
