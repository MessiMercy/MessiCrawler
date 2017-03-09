package com.model;

/**
 * Created by Administrator on 2016/11/17.
 */
public class TaobaoLoginModel implements Model{
    private String TPL_username;
    private String ncoToken;
    private String TPL_redirect_url;
    private String gvfdcre;
    private String TPL_password_2;
    private String ua;
    private String um_token;
    private String naviVer;

    public String getNaviVer() {
        return naviVer;
    }

    public void setNaviVer(String naviVer) {
        this.naviVer = naviVer;
    }

    public String getTPL_username() {
        return TPL_username;
    }

    public void setTPL_username(String TPL_username) {
        this.TPL_username = TPL_username;
    }

    public String getNcoToken() {
        return ncoToken;
    }

    public void setNcoToken(String ncoToken) {
        this.ncoToken = ncoToken;
    }

    public String getTPL_redirect_url() {
        return TPL_redirect_url;
    }

    public void setTPL_redirect_url(String TPL_redirect_url) {
        this.TPL_redirect_url = TPL_redirect_url;
    }

    public String getGvfdcre() {
        return gvfdcre;
    }

    public void setGvfdcre(String gvfdcre) {
        this.gvfdcre = gvfdcre;
    }

    public String getTPL_password_2() {
        return TPL_password_2;
    }

    public void setTPL_password_2(String TPL_password_2) {
        this.TPL_password_2 = TPL_password_2;
    }

    public String getUa() {
        return ua;
    }

    public void setUa(String ua) {
        this.ua = ua;
    }

    public String getUm_token() {
        return um_token;
    }

    public void setUm_token(String um_token) {
        this.um_token = um_token;
    }
}
