package com.model;

/**
 * Created by Administrator on 2016/11/9.
 */
public class LagouModel implements Model{
    private String channelId = "0";
    /**
     * 多选一 后端开发 移动开发 前端开发 测试 运维 DBA 高端技术职位 项目管理 硬件开发 企业软件 产品经理 产品设计师 高端产品职位 视觉设计 用户研究
     * 高端设计职位 交互设计 运营 编辑 客服  高端运营职位 市场/营销 公关 销售 高端市场职位 供应链 采购 投资 人力资源 行政 财务 高端职能职位 法务
     * 投融资 风控 审计税务 高端金融职位
     * 职位类别”与“职位名称”在发布后不可修改，请谨慎填写
     */
    private String positionType;
    private String positionName;
    /**
     * positionType为主类别，从主类别下属中选一个副类别，对应关系详见职位分布.txt
     */
    private String positionThirdType;
    /**
     * 部门,任填
     */
    private String department;
    /**
     * 全职 兼职 实习 三选一
     */
    private String jobNature;
    private String salaryMin;
    /**
     * 最高工资和最低工资差距不超过2倍
     */
    private String salaryMax;
    /**
     * 不限 应届毕业生 1年以下 1-3年 3-5年 5-10年 10年以上 7选1
     */
    private String workYear;
    /**
     * 不限 大专 本科 硕士 博士 5选1
     */
    private String education;
    /**
     * 任填，多个用英文逗号隔开，每个最多5个字
     */
    private String positionBrightPoint;
    /**
     * 20-2000字html格式
     */
    private String positionDesc;
    /**
     * 从地址列表中取
     */
    private String workAddressId;

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getPositionType() {
        return positionType;
    }

    public void setPositionType(String positionType) {
        this.positionType = positionType;
    }

    public String getPositionName() {
        return positionName;
    }

    public void setPositionName(String positionName) {
        this.positionName = positionName;
    }

    public String getPositionThirdType() {
        return positionThirdType;
    }

    public void setPositionThirdType(String positionThirdType) {
        this.positionThirdType = positionThirdType;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getJobNature() {
        return jobNature;
    }

    public void setJobNature(String jobNature) {
        this.jobNature = jobNature;
    }

    public String getSalaryMin() {
        return salaryMin;
    }

    public void setSalaryMin(String salaryMin) {
        this.salaryMin = salaryMin;
    }

    public String getSalaryMax() {
        return salaryMax;
    }

    public void setSalaryMax(String salaryMax) {
        this.salaryMax = salaryMax;
    }

    public String getWorkYear() {
        return workYear;
    }

    public void setWorkYear(String workYear) {
        this.workYear = workYear;
    }

    public String getEducation() {
        return education;
    }

    public void setEducation(String education) {
        this.education = education;
    }

    public String getPositionBrightPoint() {
        return positionBrightPoint;
    }

    public void setPositionBrightPoint(String positionBrightPoint) {
        this.positionBrightPoint = positionBrightPoint;
    }

    public String getPositionDesc() {
        return positionDesc;
    }

    public void setPositionDesc(String positionDesc) {
        this.positionDesc = positionDesc;
    }

    public String getWorkAddressId() {
        return workAddressId;
    }

    public void setWorkAddressId(String workAddressId) {
        this.workAddressId = workAddressId;
    }
}
