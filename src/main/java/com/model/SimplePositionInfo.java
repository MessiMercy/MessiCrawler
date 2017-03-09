package com.model;

public class SimplePositionInfo implements Model{
	private int companyId;
	private int positionId;
	private String jobNature;// 全职兼职
	private String financeStage;// 融资情况
	private String companyName;
	private String companyFullName;
	private String industryField;
	private String positionName;
	private String city;
	private String createTime;
	private String salary;
	private String workYear;
	private String education;
	private String positionAdvantage;
	private String district;

	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append("职位信息：\r\n-------------------------------------------------\r\n");
		result.append("公司id： " + companyId + "\r\n");
		result.append("职位id： " + positionId + "\r\n");
		result.append("全职兼职： " + jobNature + "\r\n");
		result.append("融资情况： " + financeStage + "\r\n");
		result.append("公司名字： " + companyName + "\r\n");
		result.append("所在城市： " + city + "\r\n");
		result.append("公司全称： " + companyFullName + "\r\n");
		result.append("公司业务： " + industryField + "\r\n");
		result.append("职位名称： " + positionName + "\r\n");
		result.append("融资情况： " + financeStage + "\r\n");
		result.append("创建时间： " + createTime + "\r\n");
		result.append("工资： " + salary + "\r\n");
		result.append("工作经验： " + workYear + "\r\n");
		result.append("教育情况： " + education + "\r\n");
		result.append("职位优势： " + positionAdvantage + "\r\n");
		result.append("区域： " + district + "\r\n");
		result.append("-----------------------------------------\r\n");
		return result.toString();
	}

	public int getCompanyId() {
		return companyId;
	}

	public void setCompanyId(int companyId) {
		this.companyId = companyId;
	}

	public int getPositionId() {
		return positionId;
	}

	public void setPositionId(int positionId) {
		this.positionId = positionId;
	}

	public String getJobNature() {
		return jobNature;
	}

	public void setJobNature(String jobNature) {
		this.jobNature = jobNature;
	}

	public String getFinanceStage() {
		return financeStage;
	}

	public void setFinanceStage(String financeStage) {
		this.financeStage = financeStage;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getCompanyFullName() {
		return companyFullName;
	}

	public void setCompanyFullName(String companyFullName) {
		this.companyFullName = companyFullName;
	}

	public String getIndustryField() {
		return industryField;
	}

	public void setIndustryField(String industryField) {
		this.industryField = industryField;
	}

	public String getPositionName() {
		return positionName;
	}

	public void setPositionName(String positionName) {
		this.positionName = positionName;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public String getSalary() {
		return salary;
	}

	public void setSalary(String salary) {
		this.salary = salary;
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

	public String getPositionAdvantage() {
		return positionAdvantage;
	}

	public void setPositionAdvantage(String positionAdvantage) {
		this.positionAdvantage = positionAdvantage;
	}

	public String getDistrict() {
		return district;
	}

	public void setDistrict(String district) {
		this.district = district;
	}

}
