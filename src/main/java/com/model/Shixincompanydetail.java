package com.model;

public class Shixincompanydetail implements Model {
    private Integer id;

    private String iname;

    private String casecode;

    private String cardnum;

    private String businessentity;

    private String courtname;

    private String areaname;

    private Integer partytypename;

    private String gistid;

    private String regdate;

    private String gistunit;

    private String duty;

    private String performance;

    private String disrupttypename;

    private String publishdate;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getIname() {
        return iname;
    }

    public void setIname(String iname) {
        this.iname = iname == null ? null : iname.trim();
    }

    public String getCasecode() {
        return casecode;
    }

    public void setCasecode(String casecode) {
        this.casecode = casecode == null ? null : casecode.trim();
    }

    public String getCardnum() {
        return cardnum;
    }

    public void setCardnum(String cardnum) {
        this.cardnum = cardnum == null ? null : cardnum.trim();
    }

    public String getBusinessentity() {
        return businessentity;
    }

    public void setBusinessentity(String businessentity) {
        this.businessentity = businessentity == null ? null : businessentity.trim();
    }

    public String getCourtname() {
        return courtname;
    }

    public void setCourtname(String courtname) {
        this.courtname = courtname == null ? null : courtname.trim();
    }

    public String getAreaname() {
        return areaname;
    }

    public void setAreaname(String areaname) {
        this.areaname = areaname == null ? null : areaname.trim();
    }

    public Integer getPartytypename() {
        return partytypename;
    }

    public void setPartytypename(Integer partytypename) {
        this.partytypename = partytypename;
    }

    public String getGistid() {
        return gistid;
    }

    public void setGistid(String gistid) {
        this.gistid = gistid == null ? null : gistid.trim();
    }

    public String getRegdate() {
        return regdate;
    }

    public void setRegdate(String regdate) {
        this.regdate = regdate == null ? null : regdate.trim();
    }

    public String getGistunit() {
        return gistunit;
    }

    public void setGistunit(String gistunit) {
        this.gistunit = gistunit == null ? null : gistunit.trim();
    }

    public String getDuty() {
        return duty;
    }

    public void setDuty(String duty) {
        this.duty = duty == null ? null : duty.trim();
    }

    public String getPerformance() {
        return performance;
    }

    public void setPerformance(String performance) {
        this.performance = performance == null ? null : performance.trim();
    }

    public String getDisrupttypename() {
        return disrupttypename;
    }

    public void setDisrupttypename(String disrupttypename) {
        this.disrupttypename = disrupttypename == null ? null : disrupttypename.trim();
    }

    public String getPublishdate() {
        return publishdate;
    }

    public void setPublishdate(String publishdate) {
        this.publishdate = publishdate == null ? null : publishdate.trim();
    }
}