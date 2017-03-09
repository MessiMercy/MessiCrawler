package com.downloader.cookie;

import org.apache.http.cookie.Cookie;

import java.io.Serializable;
import java.util.Date;

public class SimpleHostCookie implements Cookie, Serializable {
    private String name;
    private String value;
    private String domain;

    public SimpleHostCookie(String name, String value, String host) {
        this.name = name;
        this.value = value;
        domain = host;
    }

    @Override
    public String getComment() {
        return "N/A";
    }

    @Override
    public String getCommentURL() {
        return "N/A";
    }

    @Override
    public String getDomain() {
        // TODO Auto-generated method stub
        return this.domain;
    }

    @Override
    public Date getExpiryDate() {
        return new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000 * 7);
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return this.name;
    }

    @Override
    public String getPath() {
        return "N/A";
    }

    @Override
    public int[] getPorts() {
        return null;
    }

    @Override
    public String getValue() {
        return this.value;
    }

    @Override
    public int getVersion() {
        return 0;
    }

    @Override
    public boolean isExpired(Date arg0) {
        return false;
    }

    @Override
    public boolean isPersistent() {
        return false;
    }

    @Override
    public boolean isSecure() {
        return false;
    }

}
