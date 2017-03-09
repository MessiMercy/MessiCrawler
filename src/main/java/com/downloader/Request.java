package com.downloader;

import com.downloader.HttpConstant.Method;
import com.model.Model;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;

/**
 * 实现model接口，以集成深拷贝对象
 */
public class Request implements Serializable, Model {
    private String method = Method.GET;
    private String url;
    private List<NameValuePair> formData = new ArrayList<>();
    private String jsonPayload;
    private Map<String, String> headers = new HashMap<>();
    private String charset = "UTF-8";
    private Map<String, String> queryStringMap = new LinkedHashMap<>();
    private boolean isNeedRecycle = false;
    private long sleepTime = 2 * 1000;//默认等待时间
    private int retryTimes = 0;
    private int timeout = 2000;
    private HttpHost proxy;
    private boolean isAllowRedirect = true;

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("-------------------------------------\r\n");
        sb.append(method).append("\r\n");
        sb.append(url).append("\r\n");
        sb.append("isAllowRedirect: ").append(isAllowRedirect).append("\r\n");
        if (queryStringMap != null) {
            sb.append("Query String: \r\n");
            Set<Entry<String, String>> set = queryStringMap.entrySet();
            for (Entry<String, String> entry : set) {
                sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\r\n");
            }
        }
        if (headers != null) {
            sb.append("Header: \r\n");
            Set<Entry<String, String>> set = headers.entrySet();
            for (Entry<String, String> entry : set) {
                sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\r\n");
            }
        }
        if (formData != null && formData.size() != 0) {
            sb.append("From Data:\r\n");
            for (NameValuePair pair : formData) {
                sb.append(pair.getName()).append(": ").append(pair.getValue()).append("\r\n");
            }
        }
        if (jsonPayload != null) {
            sb.append("jsonPayload: \r\n").append(jsonPayload).append("\r\n");
        }
        return sb.toString();
    }

    public int hashcode() {
        return this.url.hashCode() + this.getMethod().hashCode() + this.getFormData().hashCode() + this.getHeaders().hashCode() + this.getJsonPayload().hashCode() + this.getQueryStringMap().hashCode();
    }

    public boolean equals(Object request) {
        boolean flag = false;
        Request temp = null;
        if (request instanceof Request) {
            temp = (Request) request;
        } else {
            return false;
        }
        if (this.url.equals(temp.getUrl()) && this.getMethod().equals(temp.getMethod())) {
            if (this.jsonPayload.equals(temp.getJsonPayload()) && this.getFormData() == temp.getFormData() && this.getQueryStringMap().equals(temp.getQueryStringMap())) {
                flag = true;
            }
        }
        return flag;
    }


    private Request() {
        headers.put(HttpConstant.Header.USER_AGENT,
                "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.101 Safari/537.36");
    }

    public Request(String url) {
        this();
        if (!url.startsWith("http")) {
            setUrl("http://" + url);
        } else {
            setUrl(url);
        }
    }

    public Request(Map<String, String> queryString) {
        this();
        if (queryStringMap != null) {
            boolean flag = true;
            Set<Entry<String, String>> set = queryStringMap.entrySet();
            for (Entry<String, String> entry : set) {
                if (flag) {
                    url += ("?" + entry.getKey() + "=" + entry.getValue());
                    flag = false;
                } else {
                    url += ("&" + entry.getKey() + "=" + entry.getValue());
                }
            }
        }
    }


    public void addHeader(String name, String value) {
        headers.put(name, value);
    }

    public void addFormData(String name, String value) {
        this.formData.add(new BasicNameValuePair(name, value));
    }

    public List<NameValuePair> getFormData() {
        return formData;
    }

    public void setFormData(List<NameValuePair> formData) {
        this.formData.addAll(formData);
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers.putAll(headers);
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url, boolean append) {
        if (append) {
            this.url = url + this.url;
        } else {
            this.url = url;
        }
    }

    public void setUrl(String url) {
        setUrl(url, false);
    }

    public String getJsonPayload() {
        return jsonPayload;
    }

    public void setJsonPayload(String jsonPayload) {
        this.jsonPayload = jsonPayload;
    }

    public Map<String, String> getQueryStringMap() {
        return queryStringMap;
    }

    public void setQueryStringMap(Map<String, String> queryStringMap) {
        this.queryStringMap.putAll(queryStringMap);
        Set<Entry<String, String>> set = this.queryStringMap.entrySet();
        StringBuilder sb = new StringBuilder();
        boolean flag = true;
        for (Entry<String, String> entry : set) {
            if (flag) {
                // url += ("?" + entry.getKey() + "=" + entry.getValue());
                sb.append("?").append(entry.getKey()).append("=").append(entry.getValue());
                flag = false;
            } else {
                // url += ("&" + entry.getKey() + "=" + entry.getValue());
                sb.append("&").append(entry.getKey()).append("=").append(entry.getValue());
            }
        }
        setUrl(url + sb.toString());
    }

    public void addQueryString(String name, String value) {
        if (!getUrl().contains("?")) {
            setUrl(url + String.format("?%s=%s", name, value));
        } else {
            setUrl(url + String.format("&%s=%s", name, value));
        }
    }

    public boolean isNeedRecycle() {
        return isNeedRecycle;
    }

    public void setNeedRecycle(boolean b) {
        this.isNeedRecycle = b;
    }

    public long getSleepTime() {
        return sleepTime;
    }

    public void setSleepTime(long sleepTimeMills) {
        this.sleepTime = sleepTimeMills;
    }

    public int getRetryTimes() {
        return retryTimes;
    }

    public void setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes;
    }

    public void addRetryTimes() {
        this.retryTimes++;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeoutMills) {
        this.timeout = timeoutMills;
    }

    public HttpHost getProxy() {
        return proxy;
    }

    public void setProxy(HttpHost proxy) {
        this.proxy = proxy;
    }

    public boolean isAllowRedirect() {
        return isAllowRedirect;
    }

    public void setAllowRedirect(boolean allowRedirect) {
        isAllowRedirect = allowRedirect;
    }
}
