package com.downloader;

import com.model.Model;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;

import java.io.Serializable;

public class Response implements Model, Serializable {
    private Request request;
    private String content;
    private int statusCode;
    private String url;
    private String contentType;
    private String error;
    private Header[] responseHeder;

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("-----------------------------------------\r\n");
        sb.append(request.toString()).append("\r\n");
        sb.append("statusCode = ").append(statusCode).append("\r\n");
        sb.append("url = ").append(url).append("\r\n");
        sb.append("contentType = ").append(contentType).append("\r\n");
        sb.append("Response Header: \r\n");
        if (responseHeder != null) {
            for (Header header : responseHeder) {
                sb.append(header.getName()).append(": ").append(header.getValue()).append("\r\n");
            }
        }
        if (error == null) {
            sb.append("content:\r\n").append(content).append("\r\n");
        } else {
            sb.append("error:\r\n").append(error).append("\r\n");
        }
        sb.append("-----------------------------------------");
        return sb.toString();
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Header[] getResponseHeder() {
        return responseHeder;
    }

    public void setResponseHeder(Header[] responseHeder) {
        this.responseHeder = responseHeder;
    }

    public String getFirstHeader(String key) {
        String result = null;
        Header[] headers = getResponseHeder();
        for (int i = 0; i < headers.length; i++) {
            if (StringUtils.equalsIgnoreCase(headers[i].getName(), key)) {
                result = headers[i].getValue();
            }
        }
        return result;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }


}
