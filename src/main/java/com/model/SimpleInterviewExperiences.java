package com.model;

public class SimpleInterviewExperiences implements Model{
	private int companyId;
	private int id;
	private String tagArray;
	private String username;
	private String content;
	private String positionName;
	private String positionType;
	private String createTime;
	private int describeScore;// 描述相符星级
	private int interviewerScore;// 面试星级
	private int companyScore;// 公司星级
	private SimpleCompanyInfo simpleCompanyInfo;

	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append("面试信息：\r\n-------------------------------------------------\r\n");
		result.append("公司id： " + companyId + "\r\n");
		result.append("面试id： " + id + "\r\n");
		result.append("面试标签： " + tagArray + "\r\n");
		result.append("用户名： " + username + "\r\n");
		result.append("内容： " + content + "\r\n");
		result.append("职位名称： " + positionName + "\r\n");
		result.append("职位类型： " + positionType + "\r\n");
		result.append("创建时间： " + createTime + "\r\n");
		result.append("描述相符星级： " + describeScore + "\r\n");
		result.append("面试星级： " + interviewerScore + "\r\n");
		result.append("公司星级： " + companyScore + "\r\n");
		result.append("-----------------------------------------\r\n");
		return result.toString();
	}

	public int getCompanyId() {
		return companyId;
	}

	public void setCompanyId(int companyId) {
		this.companyId = companyId;
	}

	public String getTagArray() {
		return tagArray;
	}

	public void setTagArray(String tagArray) {
		this.tagArray = tagArray;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getPositionName() {
		return positionName;
	}

	public void setPositionName(String positionName) {
		this.positionName = positionName;
	}

	public String getPositionType() {
		return positionType;
	}

	public void setPositionType(String positionType) {
		this.positionType = positionType;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public int getDescribeScore() {
		return describeScore;
	}

	public void setDescribeScore(int describeScore) {
		this.describeScore = describeScore;
	}

	public int getInterviewerScore() {
		return interviewerScore;
	}

	public void setInterviewerScore(int interviewerScore) {
		this.interviewerScore = interviewerScore;
	}

	public int getCompanyScore() {
		return companyScore;
	}

	public void setCompanyScore(int companyScore) {
		this.companyScore = companyScore;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public SimpleCompanyInfo getSimpleCompanyInfo() {
		return simpleCompanyInfo;
	}

	public void setSimpleCompanyInfo(SimpleCompanyInfo simpleCompanyInfo) {
		this.simpleCompanyInfo = simpleCompanyInfo;
	}

}
