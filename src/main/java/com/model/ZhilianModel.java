package com.model;

/**
 * Created by Administrator on 2016/10/27.
 */
public class ZhilianModel implements Model{
    private String LoginPointId;
    private String JobTitle;//职位名字
    private String JobTypeMain;//职位主类别,见CONST_ID_JOB,主类别为|之前数字，副类别为|之后数字
    private String SubJobTypeMain;//职位副类别
    private String Quantity;//招聘人数
    /**
     * 简历接收设置：1为仅系统接收，2为抄送到邮箱
     */
    private String ApplicationMethod;
    /**
     * 只能填一个邮箱
     */
    private String EmailList;
    /**
     * saveandpub为发布 saveasnotpub为保存
     */
    private String btnAddClick;
    /**
     * 全职2 兼职1 实习4 校园5
     */
    private String EmploymentType;
    /**
     * 学历 ['4', '本科'], ['3', '硕士'], ['1', '博士'], ['5', '大专'], ['13', '中技'], ['12', '中专'], ['7', '高中以下']
     */
    private String EducationLevel;
    /**
     * 工作年限： ["0", "无经验"], ["1", "1年以下"], ["103", "1-3年"], ["305", "3-5年"], ["510", "5-10年"], ["1099", "10年以上"]
     */
    private String WorkYears;
    /**
     * 工资： ["0000001000", "1000元以下"], ["0100002000", "1000-2000元"], ["0200104000", "2000-4000元"], ["0400106000", "4000-6000元"], ["0600108000", "6000-8000元"], ["0800110000", "8000-10000元"], ["1000115000", "10000-15000元"], ["1500120000", "15000-20000元"], ["2000130000", "20000-30000元"], ["3000150000", "30000-50000元"], ["5000199999", "50000元以上"]
     * 自定义格式：2345-4321
     */
    private String MonthlyPay;
    /**
     * 岗位描述 html格式
     */
    private String JobDescription;
    /**
     * 对应关系见CONST_ID_CITY.txt和CONST_DISTRICT.txt
     * 规则：首先从根据市名在CONST_ID_CITY.txt找到对应市编号cityid（对应行第一个数字），然后再在CONST_DISTRICT.txt查看是否存在该市id，如果不存在，则该值为CONST_ID_CITY的第二个数字@cityid
     * 如果存在，则选择区名。如果不选区名，则该值为该cityid,如果选区名，则该值为cityid@该区在CONST_DISTRICT中的值
     */
    private String PositionPubPlace;
    private String WorkAddress;//工作地点
    /**
     * 福利待遇 多个福利逗号分隔,指定代码，详见CONST_ID_JOB.txt
     */
    private String welfaretab;
    /**
     * 有效日期，最长60天 格式：2016-10-26
     */
    private String DateEnd;

    public String getEmploymentType() {
        return EmploymentType;
    }

    public void setEmploymentType(String employmentType) {
        EmploymentType = employmentType;
    }


    public String getLoginPointId() {
        return LoginPointId;
    }

    public void setLoginPointId(String loginPointId) {
        LoginPointId = loginPointId;
    }


    public String getJobTitle() {
        return JobTitle;
    }

    public void setJobTitle(String jobTitle) {
        JobTitle = jobTitle;
    }

    public String getJobTypeMain() {
        return JobTypeMain;
    }

    public void setJobTypeMain(String jobTypeMain) {
        JobTypeMain = jobTypeMain;
    }

    public String getSubJobTypeMain() {
        return SubJobTypeMain;
    }

    public void setSubJobTypeMain(String subJobTypeMain) {
        SubJobTypeMain = subJobTypeMain;
    }

    public String getQuantity() {
        return Quantity;
    }

    public void setQuantity(String quantity) {
        Quantity = quantity;
    }

    public String getEducationLevel() {
        return EducationLevel;
    }

    public void setEducationLevel(String educationLevel) {
        EducationLevel = educationLevel;
    }

    public String getWorkYears() {
        return WorkYears;
    }

    public void setWorkYears(String workYears) {
        WorkYears = workYears;
    }

    public String getMonthlyPay() {
        return MonthlyPay;
    }

    public void setMonthlyPay(String monthlyPay) {
        MonthlyPay = monthlyPay;
    }

    public String getJobDescription() {
        return JobDescription;
    }

    public void setJobDescription(String jobDescription) {
        JobDescription = jobDescription;
    }


    public String getWorkAddress() {
        return WorkAddress;
    }

    public void setWorkAddress(String workAddress) {
        WorkAddress = workAddress;
    }

    public String getWelfaretab() {
        return welfaretab;
    }

    public void setWelfaretab(String welfaretab) {
        this.welfaretab = welfaretab;
    }

    public String getDateEnd() {
        return DateEnd;
    }

    public void setDateEnd(String dateEnd) {
        DateEnd = dateEnd;
    }

    public String getApplicationMethod() {
        return ApplicationMethod;
    }

    public void setApplicationMethod(String applicationMethod) {
        ApplicationMethod = applicationMethod;
    }

    public String getEmailList() {
        return EmailList;
    }

    public void setEmailList(String emailList) {
        EmailList = emailList;
    }

    public String getBtnAddClick() {
        return btnAddClick;
    }

    public void setBtnAddClick(String btnAddClick) {
        this.btnAddClick = btnAddClick;
    }


    public String getPositionPubPlace() {
        return PositionPubPlace;
    }

    public void setPositionPubPlace(String positionPubPlace) {
        PositionPubPlace = positionPubPlace;
    }
}
