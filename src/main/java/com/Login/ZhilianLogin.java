package com.Login;

import com.Ocr.binary.ImageUtil;
import com.downloader.*;
import com.model.ZhilianModel;
import com.parser.Html;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.select.Elements;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * zhilian login and position creat
 * Created by Administrator on 2016/10/26.
 */
public class ZhilianLogin {
    private static final String redirectUrl = "http://rd2.zhaopin.com/s/loginmgr/loginproc_new.asp";
    private static Map<String, String> header = new HashMap<>();
    private static CookieStore store = new BasicCookieStore();
    private static CloseableHttpClient client = CrawlerLib.getInstanceClient(false, store);
    private static Downloader downloader = new Downloader(client);
    private static Scanner scanner = new Scanner(System.in);
    private String account;
    private String pw;
    private String verifyCode = null;
    private Set<Cookie> cookieSet;
    private String intdeptid;
    private String PublicPoints;
    private String CanPubPositionQty;
    private String ContractCityList;
    private String positionPubPlaceInitCityId;
    private String CompanyAddress;
    private String IsCorpUser;
    private String DepartmentId;
    private String SeqNumber;
    private List<NameValuePair> postDict = new ArrayList<>();
    private Properties pp = new Properties();

    public ZhilianLogin() {

    }

    public static void main(String[] args) throws IOException {
        header.put(HttpConstant.Header.USER_AGENT, HttpConstant.UserAgent.CHROME);
        ZhilianLogin login = new ZhilianLogin();
        Properties pp = login.getpp();
        try {
            pp.load(new FileReader("zhilian.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        login.setAccount(pp.getProperty("ACCOUT"));
        login.setPw(pp.getProperty("PASSWORD"));
//        CrawlerLib.addCookieFromProperties(login.getpp(), login.store, new File("zhilian.properties"));
//        store = CrawlerLib.readCookieFromDisk(new File("cookie.obj"));
//        if (!login.isLogin()) {
//            InputStream stream = downloader.downloadEntity(downloader.getClient(), "https://passport.zhaopin.com/checkcode/imgrd?r=" + Math.random(), header);
//            BufferedImage image = null;
//            try {
//                image = ImageIO.read(stream);
////                Files.copy(stream, Paths.get("verify.gif"), StandardCopyOption.REPLACE_EXISTING);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            ZhilianOcr main = new ZhilianOcr("3_drop");
//            String verify = null;
//            try {
//                if (image != null) {
//                    verify = main.predict(image);
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            System.out.println("已自动识别验证码： " + verify);
//            login.verifyCode = verify;
//            login.login(login.getAccount(), login.getPw(), login.getVerifyCode());
//        }
        BufferedImage verifyImage = login.getVerifyImage();
        BufferedImage image = login.processImage(verifyImage);
        ImageIO.write(image, "jpg", new File("temp", "zhilian.jpg"));
        System.out.println("请输入坐标");
        String p = scanner.nextLine();
        String verifyCode = login.submitVerify(p);
        login.login(login.getAccount(), login.getPw(), verifyCode);
//        login.redirectUrl();
//        String positionPage = login.getPositionAddPage();
//        login.setPageProperty(positionPage);
//        ZhilianModel model = new ZhilianModel();
//        login.genModel(model);
//        login.generateList(model);
//        login.postPosition();
//        List<Cookie> list = login.store.getCookies();
//        CrawlerLib.getCookieAndStore(login.pp, login.store, "zhilian.properties");
        CrawlerLib.storeCookie(store, new File("zhiliancookie.obj"));
    }

    private boolean isLogin() {
        boolean flag = true;
        Request request = new Request("http://rd2.zhaopin.com/s/homepage.asp");
        Response response = downloader.process(request);
        if (response.getContent().contains("忘记密码")) {
            flag = false;
        }
        return flag;
    }


    private void login(String account, String pw, String verifyCode) {
        Request request = new Request("https://passport.zhaopin.com/org/login");
        request.setMethod(HttpConstant.Method.POST);
        header.put(HttpConstant.Header.REFERER, "https://passport.zhaopin.com/org/login");
        request.setHeaders(header);
        request.setCharset("gb2312");
        List<NameValuePair> list = new ArrayList<>();
        list.add(new BasicNameValuePair("bkurl", ""));
        list.add(new BasicNameValuePair("LoginName", account));
        list.add(new BasicNameValuePair("Password", pw));
        list.add(new BasicNameValuePair("CheckCode", verifyCode));
        list.add(new BasicNameValuePair("IsServiceCheck", "true"));
        list.add(new BasicNameValuePair("IsServiceCheck", "false"));
        list.add(new BasicNameValuePair("servicecheckinput", "已勾选"));
        request.setFormData(list);
        Response response = downloader.process(request);
        System.out.println(response);
    }

    private BufferedImage getVerifyImage() {
        String url = "https://passport.zhaopin.com/chk/getcap?t" + System.currentTimeMillis();
        header.put(HttpConstant.Header.REFERER, "https://passport.zhaopin.com/org/login");
        InputStream imageInputStream = downloader.downloadEntity(downloader.getClient(), url, header);
        BufferedImage image = null;
        try {
            image = ImageIO.read(imageInputStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                imageInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return image;
    }

    /**
     * 将拿下来的碎片图拼装好
     *
     * @return 图的0到130行像素为客户需要点击的图，余下为标志图
     */
    private BufferedImage processImage(BufferedImage image) {
        int[] firstLineArr = new int[]{140, 238, 196, 112, 14, 126, 56, 28, 42, 168, 266, 210, 154, 182, 84, 0, 70, 98, 224, 252};
        int[] secondLineArr = new int[]{210, 84, 238, 196, 28, 140, 126, 182, 42, 98, 56, 154, 266, 168, 252, 14, 0, 224, 112, 70};
        BufferedImage firstLineImage = zhilianCombine(firstLineArr, image, 0);
        BufferedImage secondLineImage = zhilianCombine(secondLineArr, image, 85);
        return ImageUtil.verticalAddImage(firstLineImage, secondLineImage);
    }

    /**
     * @param p 用户按顺序点击的3个坐标点。示例：   57,65;191,36;119,83  分别是横纵坐标3组，横竖坐标用逗号分隔，坐标与坐标直接分号分隔
     * @return 登录时用的verify参数
     */
    private String submitVerify(String p) {
        String url = "https://passport.zhaopin.com/chk/verify?callback=jsonpCallback";
        Request request = new Request(url);
        request.setMethod(HttpConstant.Method.POST);
        request.addFormData("p", p);
        request.addFormData("time", System.currentTimeMillis() + "");
        request.setHeaders(header);
        Response process = downloader.process(request);
        System.out.println(process);
        String content = process.getContent();
        Pattern compile = Pattern.compile("\\{MessageText:\"(.*?)\"}");
        Matcher matcher = compile.matcher(content);
        String result = "";
        if (matcher.find()) {
            result = matcher.group(1);
        }
        return result;
    }

    /**
     * @param arr     截取坐标数据数组
     * @param orignal 大图
     * @return 截取后图片
     */
    public static BufferedImage zhilianCombine(int[] arr, BufferedImage orignal, int y) {
//        int[] arr = new int[]{210, 84, 238, 196, 28, 140}; //小图坐标
        BufferedImage image = new BufferedImage(84, 40, BufferedImage.TYPE_INT_RGB);
//        BufferedImage orignal = null;
//        try {
//            orignal = ImageIO.read(new File("temp.jpg"));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        List<BufferedImage> list = new ArrayList<>();
        for (int i : arr) {
//            BufferedImage subimage = orignal.getSubimage(i, 130, 14, 40);
            BufferedImage subimage = orignal.getSubimage(i, y, 14, 85);
            list.add(subimage);
        }
        final BufferedImage[] image0 = {list.get(0)};
        list.stream().skip(1).forEach(p -> image0[0] = ImageUtil.horizontalAddImage(image0[0], p));
//            ImageIO.write(image0[0], "jpg", new File("temp", "zhilian.jpg"));
        return image0[0];
    }

    private void redirectUrl() {
        Request request = new Request(redirectUrl);
        request.setHeaders(header);
        request.setCharset("gb2312");
        Response response = downloader.process(request);
        if (response.getContent().contains("选择登陆机构")) {
            System.out.println("请选择并输入分支机构id: ");
            Html html = new Html(response.getContent());
            Elements elements = html.getDocument().select("div.nr a");
            elements.forEach(ele -> System.out.println(StringEscapeUtils.unescapeHtml(ele.toString())));
            String id = scanner.nextLine();
            setIntdeptid(id);
            Request rediretToHomepage = new Request("http://rd2.zhaopin.com/s/loginmgr/loginpoint.asp?id=" + getIntdeptid() + "&BkUrl=&deplogincount=12");
            rediretToHomepage.setCharset("gb2312");
            rediretToHomepage.setHeaders(header);
            Response re = downloader.process(rediretToHomepage);
            System.out.println(re);
        }
    }

    public String getPositionAddPage() {
        Request request = new Request("http://jobads.zhaopin.com/Position/PositionAdd");
        request.setHeaders(header);
        Response response = downloader.process(request);
        System.out.println(response);
        return response.getContent();
    }

    public void setPageProperty(String page) {
        Html html = new Html(page);
        this.PublicPoints = html.parse("#PublicPoints", "value", 0);
        this.CanPubPositionQty = html.parse("#CanPubPositionQty", "value", 0);
        this.ContractCityList = html.parse("#ContractCityList", "value", 0);
        this.positionPubPlaceInitCityId = html.parse("#positionPubPlaceInitCityId", "value", 0);
        this.CompanyAddress = html.parse("#CompanyAddress", "value", 0);
        this.IsCorpUser = html.parse("#IsCorpUser", "value", 0);
        this.DepartmentId = html.parse("#DepartmentId", "value", 0);
        this.SeqNumber = html.parse("#hidSeqNumber", "value", 0);
    }

    public void generateList(ZhilianModel model) {
        postDict.add(new BasicNameValuePair("LoginPointId", intdeptid));
        postDict.add(new BasicNameValuePair("PriorityRule", "1"));
        postDict.add(new BasicNameValuePair("PublicPoints", this.PublicPoints));
        postDict.add(new BasicNameValuePair("HavePermissionToPubPosition", "True"));
        postDict.add(new BasicNameValuePair("TemplateId", ""));
        postDict.add(new BasicNameValuePair("EmploymentType", model.getEmploymentType()));
        postDict.add(new BasicNameValuePair("IsJyywl", "true"));
        postDict.add(new BasicNameValuePair("JobTitle", model.getJobTitle()));
        postDict.add(new BasicNameValuePair("JobTypeMain", model.getJobTypeMain()));
        postDict.add(new BasicNameValuePair("SubJobTypeMain", model.getSubJobTypeMain()));
        postDict.add(new BasicNameValuePair("JobTypeMinor", ""));
        postDict.add(new BasicNameValuePair("SubJobTypeMinor", ""));
        postDict.add(new BasicNameValuePair("Quantity", model.getQuantity()));
        postDict.add(new BasicNameValuePair("EducationLevel", model.getEducationLevel()));
        postDict.add(new BasicNameValuePair("WorkYears", model.getWorkYears()));
        postDict.add(new BasicNameValuePair("MonthlyPay", model.getMonthlyPay()));
        postDict.add(new BasicNameValuePair("JobDescription", model.getJobDescription()));
        postDict.add(new BasicNameValuePair("welfaretab", model.getWelfaretab()));
        postDict.add(new BasicNameValuePair("CanPubPositionQty", this.CanPubPositionQty));
        postDict.add(new BasicNameValuePair("PositionPubPlace", model.getPositionPubPlace()));
        postDict.add(new BasicNameValuePair("ContractCityList", this.ContractCityList));
        postDict.add(new BasicNameValuePair("PositionPubPlaceInitCityId", this.positionPubPlaceInitCityId));
        postDict.add(new BasicNameValuePair("WorkAddress", model.getWorkAddress()));
        postDict.add(new BasicNameValuePair("WorkAddressCoordinate", "0,0"));
        postDict.add(new BasicNameValuePair("CompanyAddress", this.CompanyAddress));
        postDict.add(new BasicNameValuePair("DateEnd", model.getDateEnd()));
        postDict.add(new BasicNameValuePair("ApplicationMethod", model.getApplicationMethod()));
        postDict.add(new BasicNameValuePair("EmailList", model.getEmailList()));
        postDict.add(new BasicNameValuePair("ApplicationMethodOptionsList", "1,2"));
        postDict.add(new BasicNameValuePair("ESUrl", ""));
        postDict.add(new BasicNameValuePair("IsCorpUser", this.IsCorpUser));
        postDict.add(new BasicNameValuePair("IsShowRootCompanyIntro", "True"));
        postDict.add(new BasicNameValuePair("IsShowSubCompanyIntro", "False"));
        postDict.add(new BasicNameValuePair("DepartmentId", this.DepartmentId));
        postDict.add(new BasicNameValuePair("FilterId", ""));
        postDict.add(new BasicNameValuePair("PositionApplyReply", "-1"));
        postDict.add(new BasicNameValuePair("JobNo", ""));
        postDict.add(new BasicNameValuePair("SeqNumber", this.SeqNumber));
        postDict.add(new BasicNameValuePair("btnAddClick", model.getBtnAddClick()));
        postDict.add(new BasicNameValuePair("editorValue", model.getJobDescription()));
    }


    public void postPosition() {
        Request request = new Request("http://jobads.zhaopin.com/Position/PositionAdd");
        request.setMethod(HttpConstant.Method.POST);
        header.put(HttpConstant.Header.REFERER, "http://jobads.zhaopin.com/Position/PositionAdd");
        request.setHeaders(header);
        request.setFormData(this.postDict);
        Response response = downloader.process(request);
        System.out.println(response);
    }

    public String getIntdeptid() {
        return intdeptid;
    }

    public void setIntdeptid(String intdeptid) {
        this.intdeptid = intdeptid;
    }

    public void genModel(ZhilianModel model) {
        model.setEmploymentType("2");
        model.setJobTitle("autoTest");
        model.setJobTypeMain("1050000");
        model.setSubJobTypeMain("136");
        model.setQuantity("1");
        model.setEducationLevel("7");
        model.setWorkYears("0");
        model.setMonthlyPay("2345-4321");
        model.setJobDescription("<p>岗位职责： <br/>1.负责系统的二次开发； <br/>2.负责版本测试及修改工作； <br/>3.完成售后支持保障工作；&nbsp; <br/>4.完成上级交办的其他工作<br/>职要求：<br/>1、国家正规大学本科以上学历，财政、财务或管理信息");
        model.setWelfaretab("10001,10002,10003");
        model.setPositionPubPlace("664@2359");
        model.setWorkAddress("杭州市黄姑山路29号");
        model.setDateEnd("2016-10-30");
        model.setApplicationMethod("2");
        model.setEmailList("test@gmail.com");
        model.setBtnAddClick("saveasnotpub");
    }

    public static String getRedirectUrl() {
        return redirectUrl;
    }

    public static Map<String, String> getHeader() {
        return header;
    }

    public static void setHeader(Map<String, String> header) {
        ZhilianLogin.header = header;
    }

    public static CookieStore getStore() {
        return store;
    }

    public static void setStore(CookieStore store) {
        ZhilianLogin.store = store;
    }

    public static CloseableHttpClient getClient() {
        return client;
    }

    public static void setClient(CloseableHttpClient client) {
        ZhilianLogin.client = client;
    }

    public static Downloader getDownloader() {
        return downloader;
    }

    public static void setDownloader(Downloader downloader) {
        ZhilianLogin.downloader = downloader;
    }

    public static Scanner getScanner() {
        return scanner;
    }

    public static void setScanner(Scanner scanner) {
        ZhilianLogin.scanner = scanner;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPw() {
        return pw;
    }

    public void setPw(String pw) {
        this.pw = pw;
    }

    public String getVerifyCode() {
        return verifyCode;
    }

    public void setVerifyCode(String verifyCode) {
        this.verifyCode = verifyCode;
    }

    public Set<Cookie> getCookieSet() {
        return cookieSet;
    }

    public void setCookieSet(Set<Cookie> cookieSet) {
        this.cookieSet = cookieSet;
    }

    public String getPublicPoints() {
        return PublicPoints;
    }

    public void setPublicPoints(String publicPoints) {
        PublicPoints = publicPoints;
    }

    public String getCanPubPositionQty() {
        return CanPubPositionQty;
    }

    public void setCanPubPositionQty(String canPubPositionQty) {
        CanPubPositionQty = canPubPositionQty;
    }

    public String getContractCityList() {
        return ContractCityList;
    }

    public void setContractCityList(String contractCityList) {
        ContractCityList = contractCityList;
    }

    public String getPositionPubPlaceInitCityId() {
        return positionPubPlaceInitCityId;
    }

    public void setPositionPubPlaceInitCityId(String positionPubPlaceInitCityId) {
        this.positionPubPlaceInitCityId = positionPubPlaceInitCityId;
    }

    public String getCompanyAddress() {
        return CompanyAddress;
    }

    public void setCompanyAddress(String companyAddress) {
        CompanyAddress = companyAddress;
    }

    public String getIsCorpUser() {
        return IsCorpUser;
    }

    public void setIsCorpUser(String isCorpUser) {
        IsCorpUser = isCorpUser;
    }

    public String getDepartmentId() {
        return DepartmentId;
    }

    public void setDepartmentId(String departmentId) {
        DepartmentId = departmentId;
    }

    public String getSeqNumber() {
        return SeqNumber;
    }

    public void setSeqNumber(String seqNumber) {
        SeqNumber = seqNumber;
    }

    public List<NameValuePair> getPostDict() {
        return postDict;
    }

    public void setPostDict(List<NameValuePair> postDict) {
        this.postDict = postDict;
    }

    public Properties getpp() {
        return pp;
    }

    public void setpp(Properties cookiePP) {
        this.pp = cookiePP;
    }
}