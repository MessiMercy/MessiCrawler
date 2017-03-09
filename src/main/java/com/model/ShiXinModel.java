package com.model;

/**
 * Created by Administrator on 2016/11/10.
 */
public class ShiXinModel implements Model{
    private int id;
    private String name;
    private String verifyNum;
    private int peopleOrCompany;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVerifyNum() {
        return verifyNum;
    }

    public void setVerifyNum(String verifyNum) {
        this.verifyNum = verifyNum;
    }

    /**
     * @return 如果是公司返回1，如果是个人返回0
     */
    public int getPeopleOrCompany() {
        return peopleOrCompany;
    }

    public void setPeopleOrCompany(int peopleOrCompany) {
        this.peopleOrCompany = peopleOrCompany;
    }
}
