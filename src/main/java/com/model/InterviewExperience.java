package com.model;

public class InterviewExperience implements Model{
	private int id;
	private int userId;
	private int usefulCount;
	private int myScore;
	private int describeScore;
	private int interviewerScore;
	private int companyScore;
	private String username;
	private String[] tagArray;
	private String content;
	private String positionName;
	private String companyName;
	private String positionType;
	private String tags;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public int getUsefulCount() {
		return usefulCount;
	}

	public void setUsefulCount(int usefulCount) {
		this.usefulCount = usefulCount;
	}

	public int getMyScore() {
		return myScore;
	}

	public void setMyScore(int myScore) {
		this.myScore = myScore;
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

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String[] getTagArray() {
		return tagArray;
	}

	public void setTagArray(String[] tagArray) {
		this.tagArray = tagArray;
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

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
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

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	private String createTime;
}
