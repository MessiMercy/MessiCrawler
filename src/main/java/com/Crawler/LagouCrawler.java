package com.Crawler;

import com.downloader.*;
import com.duplicate.HashSetRemover;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.model.*;
import com.model.inter.ISimpleCompanyInfoOperation;
import com.model.inter.ISimpleInterviewExperiencesOperation;
import com.model.inter.ISimplePositionInfoOperation;
import com.parser.Html;
import com.scheduler.PriorityScheduler;
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

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

public class LagouCrawler {
    public static Logger logger = Logger.getLogger(LagouCrawler.class);
    private static CookieStore store = new BasicCookieStore();
    private static final String GONGSIURL = "http://www.lagou.com/gongsi/0-0-0";// 全国
    private static final String ZHAOPINURL = "http://www.lagou.com/zhaopin/";
    private static final String GONGSIAJAX = "http://www.lagou.com/gongsi/129-0-0.json";
    private static final String POSITIONURL = "http://www.lagou.com/gongsi/searchPosition.json";
    private static final String SEARCHURL = "http://www.lagou.com/jobs/companyAjax.json";
    public static HashMap<String, String> map = new HashMap<>();
    // private CrawlerLib lib;
    private static final HttpClient CLIENT = CrawlerLib.getInstanceClient(false, store);
    private static final String CHARSET = "UTF-8";
    // private static final Stack<Integer> companyIdStore = new Stack<>();
    private static final PriorityScheduler queue = new PriorityScheduler();
    private static SqlSessionFactory factory;
    private static HashSetRemover<Integer> remover = new HashSetRemover<>();

    static {
        try {
            PropertyConfigurator.configure("log4j.properties");
            Reader reader = Resources.getResourceAsReader("Configuration.xml");
            factory = new SqlSessionFactoryBuilder().build(reader);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static SqlSessionFactory getFactory() {
        return factory;
    }

    public static void main(String[] args) {
        // search();
        // collectId();
        readIDandStore();
    }


    private static void readIDandStore() {
        Stack<Integer> myStack = new Stack<>();
        try {
            FileReader reader = new FileReader("companyIds.txt");
            BufferedReader bfr = new BufferedReader(reader);
            String temp = null;
            while ((temp = bfr.readLine()) != null) {
                try {
                    myStack.add(Integer.valueOf(temp));
                } catch (Exception ignored) {
                }
            }
            System.out.println("已读取id数： " + myStack.size());
            bfr.close();
            reader.close();
            while (!myStack.isEmpty()) {
                int companyId = myStack.pop();
                try {
                    storeAllDetails(companyId);
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
                System.out.println("目前进度： " + myStack.size());
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                FileWriter fileWriter = new FileWriter(new File("leftIds.txt"), true);
                while (!myStack.isEmpty()) {
                    fileWriter.write(myStack.pop() + "\r\n");
                }
                fileWriter.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        // storeAllDetails(125138);
        // Filepipeline pipe = new Filepipeline();
        // pipe.printResult(companyAjax, true, "companyAjax.txt");

        // for (Integer integer : arr) {
        // System.out.println(integer);
        // }
    }

    public static void collectId() {
        List<Integer> allId = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            String companyAjax = getCompanyAjax(i);
            List<Integer> ids = getCompanyIdList(companyAjax);
            System.out.println("找到id数: " + ids.size());
            allId.addAll(ids);
        }
        try {
            System.out.println("总计找到id数： " + allId.size());
            FileWriter writer = new FileWriter(new File("companyIds.txt"), true);
            for (Integer integer : allId) {
                writer.write(integer + "\r\n");
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<Integer> getCompanyIdList(String companyAjax) {
        System.out.println(companyAjax.length());
        List<Integer> arr = new ArrayList<>();
        JsonParser parser = new JsonParser();
        JsonObject obj = parser.parse(companyAjax).getAsJsonObject();
        JsonArray array = obj.get("result").getAsJsonArray();
        System.out.println("jsonarray的size为： " + array.size());
        for (JsonElement jsonElement : array) {
            int id = jsonElement.getAsJsonObject().get("companyId").getAsInt();
            if (!remover.isDuplicate(id)) {
                arr.add(id);
            }
        }
        return arr;
    }

    private static void search() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("输入你需要查找的公司名字");
        String keyWord = scanner.nextLine();
        String resultAjax = getSearchCompany(keyWord);
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(resultAjax);
        JsonElement contentElement = element.getAsJsonObject().get("content");
        int resultNums = contentElement.getAsJsonObject().get("totalCount").getAsInt();
        System.out.println("总共查询到" + resultNums + "个结果");
        if (resultNums == 0) {
            search();
        }
        JsonArray array = contentElement.getAsJsonObject().get("result").getAsJsonArray();
        String temp = null;
        System.out.println("输入数字查看其中结果");
        while (!(temp = scanner.nextLine()).equals("exit")) {
            JsonElement jsonElement = array.get(Integer.valueOf(temp));
            int companyID = jsonElement.getAsJsonObject().get("companyId").getAsInt();
            String[] companyAndInterView = getCompanyAndInterView(companyID);
            String interViewInfo = companyAndInterView[0];
            String companyInfo = companyAndInterView[1];
            String positionAjax = getCompanyPositionInfo(companyID);
            SimpleCompanyInfo info = getCompanyInfo(companyInfo);
            List<SimpleInterviewExperiences> list = getInterview(interViewInfo);
            List<SimplePositionInfo> po = getPositionInfo(positionAjax);
            System.out.println(info.toString());
            for (SimplePositionInfo simplePositionInfo : po) {
                System.out.println(simplePositionInfo.toString());
            }
            for (SimpleInterviewExperiences interview : list) {
                System.out.println(interview.toString());
            }
        }
        // Filepipeline pipe = new Filepipeline();
        // pipe.printResult(resultAjax, false, "resultAjax.txt");
        scanner.close();
    }

    public static void func() {
        map.put("Referer", "http://www.lagou.com/");
        map.put("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.116 Safari/537.36");
        Downloader fetch = new Downloader();
        Request request = new Request(ZHAOPINURL);
        request.setHeaders(map);
        request.setCharset(CHARSET);
        Response response = fetch.getEntity(CLIENT, request);
        String zhaopinHtml = fetch.getHtml();
        Html parser = new Html(zhaopinHtml);
        List<String> companyidStack = parser.parse("li.con_list_item", "data-companyid");
        System.out.println(companyidStack.size());
        for (String string : companyidStack) {
            System.out.println("processing: " + string);
            String companyUrl = getCompanyUrl(Integer.valueOf(string));
            Downloader fetch2 = new Downloader();
            Request request1 = new Request(companyUrl);
            request1.setHeaders(map);
            Response response1 = fetch2.getEntity(CLIENT, request1);
            String companyDetailHtml = fetch2.getHtml();
            System.out.println("companyDetailHtml`s length: " + companyDetailHtml.length());
            // CrawlerLib.printResult(companyDetailHtml, true);
            Html parser2 = new Html(companyDetailHtml);
            String detailJson = parser2.parse("#companyInfoData");
            String interviewJson = null;
            try {
                interviewJson = parser2.getDocument().select("#interviewExperiencesData").first().html();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            List<NameValuePair> postDict = getPostDict(string);
            String positionJson = "null";
            Downloader fetch3 = new Downloader();
            Request request2 = new Request(POSITIONURL);
            request2.setFormData(postDict);
            request2.setHeaders(map);
            request2.setMethod(HttpConstant.Method.POST);
            fetch3.postEntity(CLIENT, request2);
            positionJson = fetch3.getHtml();
            CrawlerLib.printResult(detailJson, true, new File("companyInfoData"));// 打印公司detail
            CrawlerLib.printResult(positionJson, true, new File("PositionInfo"));// 打印职位detail
            CrawlerLib.printResult(interviewJson, true, new File("interviewExperiencesData"));// 打印面试信息
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 在拉勾全国分类下返回公司信息ajax
     *
     * @param pageNum 最多为20页
     */
    private static String getCompanyAjax(int pageNum) {
        HashMap<String, String> headersMap = new HashMap<>();
        headersMap.put("Referer", "http://www.lagou.com/gongsi/0-0-0");
        headersMap.put("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.116 Safari/537.36");
        headersMap.put("X-Requested-With", "XMLHttpRequest");
        headersMap.put("X-Anit-Forge-Token", "None");
        headersMap.put("X-Anit-Forge-Code", "0");
        List<NameValuePair> list = new ArrayList<>();
        list.add(new BasicNameValuePair("first", "false"));
        list.add(new BasicNameValuePair("pn", pageNum + ""));
        list.add(new BasicNameValuePair("sortField", "0"));
        list.add(new BasicNameValuePair("havemark", "0"));
        Downloader fetch = new Downloader();
        Request request = new Request(GONGSIAJAX);
        request.setMethod(HttpConstant.Method.POST);
        request.setHeaders(headersMap);
        request.setFormData(list);
        fetch.postEntity(CLIENT, request);
        return fetch.getHtml();// 结果在result的array下，详情见抓取指南。可以此获取companyID
    }

    /**
     * 通过companyId得到公司面试评价信息，和公司信息，部分公司没有评价 string[0]为评价信息，string[1]为公司信息
     */
    private static String[] getCompanyAndInterView(int companyId) {
        String[] result = new String[2];
        String companyUrl = getCompanyUrl(companyId);
        Downloader fetch = new Downloader();
        Request request = new Request(companyUrl);
        request.setHeaders(map);
        fetch.getEntity(CLIENT, request);
        String companyDetailHtml = fetch.getHtml();
        Document doc = Jsoup.parse(companyDetailHtml);
        try {
            result[0] = doc.select("#interviewExperiencesData").first().html();
            result[1] = doc.select("#companyInfoData").first().html();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;

    }

    /**
     * 用于传入companyid得到公司职位信息而组成post的表单。
     */
    private static List<NameValuePair> getPostDict(String companyId) {
        List<NameValuePair> postDict = new ArrayList<>();
        System.out.println("****************" + companyId + "***************");
        postDict.add(new BasicNameValuePair("companyId", companyId));
        postDict.add(new BasicNameValuePair("positionFirstType", "全部"));
        postDict.add(new BasicNameValuePair("pageNo", "1"));
        postDict.add(new BasicNameValuePair("pageSize", "10"));
        return postDict;
    }

    /**
     * 用于传入companyId，得到公司招聘职位信息
     */
    private static String getCompanyPositionInfo(int companyId) {
        HashMap<String, String> map = new HashMap<>();
        map.put("Referer", "http://www.lagou.com/");
        map.put("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.116 Safari/537.36");
        List<NameValuePair> postDict = getPostDict(companyId + "");
        Downloader fetch = new Downloader();
        Request request = new Request(POSITIONURL);
        request.setMethod(HttpConstant.Method.POST);
        request.setFormData(postDict);
        request.setHeaders(map);
        fetch.postEntity(CLIENT, request);
        return fetch.getHtml();

    }

    /**
     * 通过companyId组装公司详情页
     */
    private static String getCompanyUrl(int companyId) {
        return "http://www.lagou.com/gongsi/" + companyId + ".html";
    }

    /**
     * 通过传入公司名，获取返回json,默认地址为成都.搜索结果在content.result数组下，具体参见爬取指南
     */
    private static String getSearchCompany(String keyWord) {
        String defaultUrl = SEARCHURL + "?city=%E6%88%90%E9%83%BD&needAddtionalResult=false";
        map.put("Referer",
                "http://www.lagou.com/jobs/list_%E5%BD%93%E4%B9%90?city=%E6%88%90%E9%83%BD&cl=false&fromSearch=true&labelWords=&suginput=");
        List<NameValuePair> list = new ArrayList<>();
        list.add(new BasicNameValuePair("first", "true"));
        list.add(new BasicNameValuePair("pn", "1"));
        list.add(new BasicNameValuePair("kd", keyWord));
        Downloader fetch = new Downloader();
        Request request = new Request(defaultUrl);
        request.setMethod(HttpConstant.Method.POST);
        request.setFormData(list);
        request.setHeaders(map);
        fetch.postEntity(CLIENT, request);
        return fetch.getHtml();

    }

    public static void test() {
        String job = "{'labels': ['绩效奖金','带薪年假','专项奖金','节日礼物','帅哥多','管理规范','技能培训','领导好','年度旅游'],'baseInfo': {'companyId': 45496,'industryField': '其他,企业服务','companySize': '2000人以上','city': '成都','financeStage': '未融资'},'leaders': [],'userType': false,'history': [],'pageType': 1,'coreInfo': {'companyId': 45496,'logo': 'i/image/M00/03/DC/Cgp3O1bEFraAThpjAAATlD6XITM677.jpg','companyName': '成都链家房地产经纪有限公司','companyShortName': '链家','approve': 0,'companyUrl': '','companyIntroduce': '选择链家，选择成功','isFirst': false},'dataInfo': {'positionCount': 7,'resumeProcessRate': 92,'resumeProcessTime': 2,'experienceCount': 4,'lastLoginTime': '今天'},'companyId': 45496,'products': [],'introduction': {'companyId': 45496,'companyProfile': '链家成立于2001年，目前已覆盖北京、上海、深圳、重庆、大连、天津、南京、成都、青岛、杭州等29个城市，门店约6000家，旗下经纪人超过5万名，2015年交易额预计将达4000亿元。\n<br />成都链家于2011年10月正式进驻成都，以成都市二手房买卖、租赁、新房分销、商业地产、金融按揭服务为主，截止2015年12月成都链家门店已有500余家，着力打造为中国西南区域地产经纪的航空母舰！','pictures': []},'isCompanyHr': false}";
        Type type = new TypeToken<CompanyInfo>() {
        }.getType();
        Gson gson = new GsonBuilder().create();
        // gson.getAdapter(coreInfo.class);
        CompanyInfo info = gson.fromJson(job, type);
        JsonElement parser = new JsonParser().parse(job);
        String conpanyName = parser.getAsJsonObject().get("coreInfo").getAsJsonObject().get("companyName")
                .getAsString();
        System.out.println(conpanyName);
        System.out.println(info.getCoreInfo().getCompanyName());
        System.out.println(info.getIntroduction().getCompanyProfile());
        System.out.println(info.getCompanyIntroduce());
        String[] labels = info.getLabels();
        for (String string : labels) {
            System.out.println(string);
        }

        CompanyInfo info2 = new CompanyInfo();
        InterviewExperience experience = new InterviewExperience();
        PositionInfo positionInfo = new PositionInfo();
        info2.setCity("成都");
        info2.setCompanyProfile("速来");
        info2.setCompanyName("当乐");
        positionInfo.setCompanyFullName("当乐科技有限公司");
        positionInfo.setCompanyName("当乐");
        positionInfo.setCity("成都");
        positionInfo.setCompanySize("200+");
        positionInfo.setDistrict("高新区");
        experience.setCompanyName("当乐");
        experience.setCompanyScore(1);
        experience.setContent("领导");
        experience.setMyScore(1);
        experience.setPositionName("小喽啰");
        List<PositionInfo> positionInfos = new ArrayList<>();
        List<InterviewExperience> interviewExperiences = new ArrayList<>();
        positionInfos.add(positionInfo);
        interviewExperiences.add(experience);
        info2.setPositionInfos(positionInfos);
        info2.setInterviewExperiences(interviewExperiences);
        String myJson = new Gson().toJson(info2, CompanyInfo.class);
        CrawlerLib.printResult(myJson, true);
    }

    private static void storeAllDetails(int companyId) {
        SqlSession session = factory.openSession();
        ISimplePositionInfoOperation positionInfoOperation = session.getMapper(ISimplePositionInfoOperation.class);
        ISimpleInterviewExperiencesOperation interviewExperiencesOperation = session
                .getMapper(ISimpleInterviewExperiencesOperation.class);
        ISimpleCompanyInfoOperation companyInfoOperation = session.getMapper(ISimpleCompanyInfoOperation.class);
        String[] companyAndInterView = getCompanyAndInterView(companyId);
        String interViewInfo = companyAndInterView[0];
        String companyInfo = companyAndInterView[1];
        String positionAjax = getCompanyPositionInfo(companyId);
        SimpleCompanyInfo info = getCompanyInfo(companyInfo);
        List<SimpleInterviewExperiences> list = getInterview(interViewInfo);
        List<SimplePositionInfo> po = getPositionInfo(positionAjax);
        System.out.println("正在存储职位信息，条数为：" + po.size());
        for (SimplePositionInfo simplePositionInfo : po) {
            if (positionInfoOperation.selectPositionByID(simplePositionInfo.getPositionId()) != null) {
                positionInfoOperation.deleteSimplePositionInfo(simplePositionInfo.getPositionId());
                System.out.println("正在删除重复信息");
            }
            positionInfoOperation.addPosition(simplePositionInfo);
        }
        System.out.println("正在存储面试简介信息，条数为：" + list.size());
        for (SimpleInterviewExperiences interview : list) {
            if (interviewExperiencesOperation.selectCompanyInterviews(interview.getId()) != null) {
                interviewExperiencesOperation.deleteSimpleInterviewExperiences(interview.getId());
                System.out.println("正在删除重复信息");
            }
            interviewExperiencesOperation.addInterview(interview);
        }
        System.out.println("正在存储公司信息");
        if (companyInfoOperation.selectCompanyByID(info.getCompanyId()) != null) {
            companyInfoOperation.deleteSimpleCompanyInfo(info.getCompanyId());
            System.out.println("正在删除重复信息");
        }
        companyInfoOperation.addCompany(info);
        session.commit();
        session.close();
    }

    private static SimpleCompanyInfo getCompanyInfo(String companyJson) {
        SimpleCompanyInfo info = new SimpleCompanyInfo();
        JsonParser parser = new JsonParser();
        JsonObject oo = parser.parse(companyJson).getAsJsonObject();
        System.out.println("公司信息是否为null： " + (oo == null));
        try {
            if (oo != null) {
                int companyId = oo.get("companyId").getAsInt();
                int positionCount = oo.get("dataInfo").getAsJsonObject().get("positionCount").getAsInt();
                int resumeProcessRate = oo.get("dataInfo").getAsJsonObject().get("resumeProcessRate").getAsInt();
                int resumeProcessTime = oo.get("dataInfo").getAsJsonObject().get("resumeProcessTime").getAsInt();
                int experienceCount = oo.get("dataInfo").getAsJsonObject().get("experienceCount").getAsInt();
                String city = oo.get("baseInfo").getAsJsonObject().get("city").getAsString();
                String detailAddress = oo.get("addressList").getAsJsonArray().get(0).getAsJsonObject().get("detailAddress")
                        .getAsString();
                String industryField = oo.get("baseInfo").getAsJsonObject().get("industryField").getAsString();
                String companySize = oo.get("baseInfo").getAsJsonObject().get("companySize").getAsString();
                String financeStage = oo.get("baseInfo").getAsJsonObject().get("financeStage").getAsString();
                String companyProfile = oo.get("introduction").getAsJsonObject().get("companyProfile").getAsString();
                String lastLoginTime = oo.get("dataInfo").getAsJsonObject().get("lastLoginTime").getAsString();
                info.setCompanyId(companyId);
                info.setPositionCount(positionCount);
                info.setResumeProcessRate(resumeProcessRate);
                info.setResumeProcessTime(resumeProcessTime);
                info.setExperienceCount(experienceCount);
                info.setCity(city);
                info.setDetailAddress(detailAddress);
                info.setIndustryField(industryField);
                info.setCompanySize(companySize);
                info.setFinanceStage(financeStage);
                info.setCompanyProfile(companyProfile);
                info.setLastLoginTime(lastLoginTime);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return info;
    }

    private static List<SimpleInterviewExperiences> getInterview(String interviewJson) {
        List<SimpleInterviewExperiences> list = new ArrayList<>();
        JsonParser parser = new JsonParser();
        JsonReader reader = new JsonReader(new StringReader(interviewJson));
        reader.setLenient(true);
        JsonElement element = parser.parse(reader);
        JsonArray array = element.getAsJsonObject().get("result").getAsJsonArray();
        // Gson gson = new Gson();
        for (JsonElement jsonElement : array) {
            SimpleInterviewExperiences experience = new SimpleInterviewExperiences();
            try {
                experience.setId(jsonElement.getAsJsonObject().get("id").getAsInt());
                experience.setCompanyId(jsonElement.getAsJsonObject().get("companyId").getAsInt());
                experience.setCompanyScore(jsonElement.getAsJsonObject().get("companyScore").getAsInt());
                experience.setContent(jsonElement.getAsJsonObject().get("content").getAsString());
                experience.setCreateTime(jsonElement.getAsJsonObject().get("createTime").getAsString());
                experience.setDescribeScore(jsonElement.getAsJsonObject().get("describeScore").getAsInt());
                experience.setPositionName(jsonElement.getAsJsonObject().get("positionName").getAsString());
                experience.setPositionType(jsonElement.getAsJsonObject().get("positionType").getAsString());
                experience.setInterviewerScore(jsonElement.getAsJsonObject().get("interviewerScore").getAsInt());
                experience.setUsername(jsonElement.getAsJsonObject().get("username").getAsString());
                experience.setTagArray(jsonElement.getAsJsonObject().get("tagArray").getAsJsonArray().toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            list.add(experience);
        }
        return list;
    }

    private static List<SimplePositionInfo> getPositionInfo(String positionJson) {
        List<SimplePositionInfo> infos = new ArrayList<>();
        JsonParser parser = new JsonParser();
        JsonObject element = parser.parse(positionJson).getAsJsonObject();
        JsonArray array = element.get("content").getAsJsonObject().get("data").getAsJsonObject().get("page")
                .getAsJsonObject().get("result").getAsJsonArray();
        Gson gson = new Gson();
        for (JsonElement jsonElement : array) {
            SimplePositionInfo info = gson.fromJson(jsonElement, SimplePositionInfo.class);
            infos.add(info);
        }
        return infos;
    }

    public static void sqlTest() {
        SqlSession session = factory.openSession();
        ISimpleInterviewExperiencesOperation operation = session.getMapper(ISimpleInterviewExperiencesOperation.class);
        SimpleInterviewExperiences interview = new SimpleInterviewExperiences();
        interview.setCompanyId(8080);
        interview.setCompanyScore(5);
        interview.setContent("adasc测试测试");
        interview.setUsername("呵呵");
        operation.addInterview(interview);
        session.commit();
        List<SimpleInterviewExperiences> in = operation.selectInterviewByID(8080);
        for (SimpleInterviewExperiences simpleInterviewExperiences : in) {
            System.out.println(simpleInterviewExperiences.toString());
        }
        session.close();
    }

    public static HashSetRemover getRemover() {
        return remover;
    }


}
