
package com.downloader;

import com.downloader.encrypt.EncryptLib;
import com.processer.inter.Processer;
import com.proxy.IPModel;
import com.proxy.pool.IPPool;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;

public class Downloader {
    private String html;
    private HttpResponse response;
    private long delayTime = 2000;
    private HttpClient client;
    private CookieStore store;
    private HttpClientContext context;
    private String userAgent;
    private IPModel proxy = new IPModel("local", 0);
    private boolean autoSwitchProxy = false;

    public Downloader() {
        store = new BasicCookieStore();
        if (this.client == null) {
            setClient(CrawlerLib.getInstanceClient(false, store));
        }
    }


    public Downloader(HttpClient client) {
//        this.store = store;
        setClient(client);
    }

    public Downloader(String userAgent, boolean useCookie) {
        store = new BasicCookieStore();
        this.userAgent = userAgent;
        if (this.client == null) {
            setClient(CrawlerLib.getInstanceClientBuilder(false, store, null, new DefaultHttpRequestRetryHandler(4, true), userAgent, useCookie).build());
        }
    }

    public Downloader(String userAgent) {
        this(userAgent, true);
    }

    public Downloader(HttpClient client, HttpClientContext context) {
        setContext(context);
        setClient(client);
        if (context.getCookieStore() == null) {
            store = new BasicCookieStore();
            context.setCookieStore(store);
        } else {
            store = context.getCookieStore();
        }
    }

    public Response process(Request request) {
        if (getClient() != null) {
            return process(getClient(), request);
        } else {
            setClient(CrawlerLib.getInstanceClient());
            return process(getClient(), request);
        }
    }

    public List<Response> process(Queue<Request> requests, Processer processer) {
        List<Response> responses = new ArrayList<>();
        while (!requests.isEmpty()) {
            Request poll = requests.poll();
            if (poll == null) continue;
            Response response = process(poll);
            if (processer.isNeedRetry(response)) {
                poll.addRetryTimes();
                if (poll.getRetryTimes() < 5) {
                    requests.offer(poll);
                } else {
                    System.out.println(String.format("检测到%s失败次数过多,将丢弃", poll.toString()));
                }
            } else {
                responses.add(response);
            }
        }
        return responses;
    }

    public Response process(HttpClient client, Request request) {
        Response res = null;
        String method = request.getMethod();
        if (method == null) {
            method = HttpConstant.Method.GET;
        }
        if (this.proxy != null && request.getProxy() == null) {
            if (this.proxy.getErrorTime() >= 5 && autoSwitchProxy) {
                synchronized (this) {
                    System.out.println("检测到此ip出错次数太多,正在切换ip");
                    IPModel newip = new IPPool().getAvailableIp(request.getUrl(), 5);
                    setProxy(newip);
                }
            }
            if (!this.proxy.getHost().contains("local"))
                request.setProxy(new HttpHost(proxy.getHost(), proxy.getPort()));
        }//如果request本身带有proxy，则用request原有的。否则用downloader带的
        if (method.equals(HttpConstant.Method.GET)) {
            res = getEntity(client, request);
        } else if (method.equals(HttpConstant.Method.POST)) {
            res = postEntity(client, request);
        }
        if (request.getProxy() != null && autoSwitchProxy) {
            request.setProxy(null);
        }//如果request的proxy为downloader自带的，请求完之后恢复为空
        try {
            Thread.sleep(getDelayTime());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return res;
    }

    public Response getEntity(HttpClient client, Request request) {
        String url = request.getUrl();
        Map<String, String> headers = request.getHeaders();
        String charset = request.getCharset();
        Response result = new Response();
        result.setRequest(request);
        HttpGet get = new HttpGet(url);
        System.out.println("connecting: " + url);
        if (charset == null) {
            charset = "UTF-8";
        }
        if (headers != null && !headers.isEmpty()) {
            for (Entry<String, String> entry : headers.entrySet()) {
                get.setHeader(entry.getKey(), entry.getValue());
            }
        }
        RequestConfig.Builder config = RequestConfig.custom().setConnectTimeout(request.getTimeout())
                .setSocketTimeout(request.getTimeout()).setCookieSpec(CookieSpecs.DEFAULT);
        if (request.getProxy() != null) {
            config.setProxy(request.getProxy());
        }
        config.setRedirectsEnabled(request.isAllowRedirect());
        get.setConfig(config.build());
        System.out.println("--------------------");
        HttpResponse response = null;
        String html = null;
        try {
            if (context != null) {
                response = client.execute(get, context);
            } else {
                response = client.execute(get);
            }
            String contentType = null;
            if (response.containsHeader("Content-Type")) {
                contentType = response.getFirstHeader("Content-Type").getValue();
            }
            if (StringUtils.isNotEmpty(contentType)) {
                if (contentType.contains("gbk")) {
                    charset = "gbk";
                } else if (contentType.contains("gb2312")) {
                    charset = "gb2312";
                }
            }
            byte[] bytes = EntityUtils.toByteArray(response.getEntity());
            String detectCharset = EncryptLib.detect(bytes);
            if (detectCharset == null) {
                detectCharset = "gb2312";
            }
            html = new String(bytes, detectCharset);
            setResponse(response);
            result.setStatusCode(response.getStatusLine().getStatusCode());
            result.setResponseHeder(response.getAllHeaders());
            result.setContent(html);
            result.setUrl(url);
            result.setContentType(contentType);
            setHtml(html);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
            result.setContent("ClientProtocolException");
            result.setError(e.getMessage());
        } catch (IOException e) {
            html = "IOException";
            System.out.println("获取" + url + "错误" + "\r\n");
            e.printStackTrace();
            result.setContent(html);
            result.setError(e.getMessage());
            result.getRequest().setNeedRecycle(true);
        } finally {
            get.abort();
            if (response != null) {
                try {
                    EntityUtils.consume(response.getEntity());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            Thread.sleep(getDelayTime());
        } catch (InterruptedException e) {
            result.setError("interrupt");
        }
        result.getRequest().setNeedRecycle(false);
        return result;
    }

    public InputStream downloadEntity(HttpClient client, String url, Map<String, String> header) {
        Request request = new Request(url);
        request.setHeaders(header);
        request.setTimeout(30 * 1000);
        try {
            return downloadEntity(request).getEntity().getContent();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public HttpResponse downloadEntity(Request request) {
        InputStream input = null;
        HttpGet get = new HttpGet(request.getUrl());
        if (request.getHeaders() != null && !request.getHeaders().isEmpty()) {
            for (Entry<String, String> entry : request.getHeaders().entrySet()) {
                get.setHeader(entry.getKey(), entry.getValue());
            }
        }
        RequestConfig.Builder config = RequestConfig.custom().setConnectTimeout(request.getTimeout())
                .setSocketTimeout(request.getTimeout());
        if (request.getProxy() != null) {
            config.setProxy(request.getProxy());
        }
        get.setConfig(config.build());
        System.out.println("--------------------");
        HttpResponse response = null;
        try {
            if (context != null) {
                client.execute(get, context);
            } else {
                response = client.execute(get);
            }
            setResponse(response);
            System.out.println("获取" + request.getUrl() + "成功" + "\r\n");
            // html = EntityUtils.toString(response.getEntity(), charset);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("获取" + request.getUrl() + "错误" + "\r\n");
            e.printStackTrace();
        }
        try {
            Thread.sleep(request.getSleepTime());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            if (response != null) {
                input = response.getEntity().getContent();
            }
        } catch (UnsupportedOperationException | IOException e) {
            e.printStackTrace();
        }
        return response;

    }

    public Response postEntity(HttpClient client, Request request) {
        String url = request.getUrl();
        List<NameValuePair> list = request.getFormData();
        if (list == null) {
            list = new ArrayList<>();
        }
        String json = request.getJsonPayload();
        Map<String, String> headers = request.getHeaders();
        String charset = request.getCharset();
        Response result = new Response();
        result.setRequest(request);
        HttpPost post = new HttpPost(url);
        System.out.println("posting: " + url);
        if (charset == null) {
            charset = "UTF-8";
        }
        if (headers != null && !headers.isEmpty()) {
            for (Entry<String, String> entry : headers.entrySet()) {
                post.setHeader(entry.getKey(), entry.getValue());
            }
        }
        RequestConfig.Builder config = RequestConfig.custom().setConnectTimeout(request.getTimeout())
                .setSocketTimeout(request.getTimeout());
        if (request.getProxy() != null) {
            config.setProxy(request.getProxy());
        }
        config.setRedirectsEnabled(true);
        post.setConfig(config.build());
        StringEntity jsonEntity = null;
        if (json != null) {
            jsonEntity = new StringEntity(json, charset);
            post.setEntity(jsonEntity);
        } else {
            UrlEncodedFormEntity entity = null;
            try {
                entity = new UrlEncodedFormEntity(list, charset);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            post.setEntity(entity);
        }
        String html = null;
        HttpResponse response = null;
        try {
            if (context != null) {
                response = client.execute(post, context);
            } else {
                response = client.execute(post);
            }
            setResponse(response);
            CrawlerLib.LOGGER.info("获取" + url + "成功");
            html = EntityUtils.toString(response.getEntity(), charset);
            setHtml(html);
            String contentType = null;
            if (response.containsHeader("Content-Type")) {
                contentType = response.getFirstHeader("Content-Type").getValue();
            }
            result.setStatusCode(response.getStatusLine().getStatusCode());
            result.setResponseHeder(response.getAllHeaders());
            result.setContent(html);
            result.setUrl(url);
            result.setContentType(contentType);
        } catch (ClientProtocolException e) {
            result.setError(e.getMessage());
            html = "ClientProtocolException";
            result.setContent(html);
            setHtml(html);
            e.printStackTrace();
        } catch (IOException e) {
            result.setError(e.getMessage());
            e.printStackTrace();
            System.out.println("获取" + url + "错误");
            html = "IOException";
            setHtml(html);
            result.setContent(html);
            result.getRequest().setNeedRecycle(true);
        } finally {
            if (response != null) {
                try {
                    EntityUtils.consume(response.getEntity());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            Thread.sleep(request.getSleepTime());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // System.out.println(result);
        result.getRequest().setNeedRecycle(false);
        return result;
    }

    public String getHtml() {
        return html;
    }

    private void setHtml(String html) {
        this.html = html;
    }

    public HttpResponse getResponse() {
        return response;
    }

    private void setResponse(HttpResponse response) {
        this.response = response;
    }

    public long getDelayTime() {
        return delayTime;
    }

    public void setDelayTime(long delayTime) {
        this.delayTime = delayTime;
    }

    public HttpClient getClient() {
        return client;
    }

    public void setClient(HttpClient client) {
        this.client = client;
    }

    public CookieStore getStore() {
        return store;
    }

    public void setStore(CookieStore store) {
        this.store = store;
    }

    public HttpClientContext getContext() {
        return context;
    }

    public void setContext(HttpClientContext context) {
        this.context = context;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public IPModel getProxy() {
        return proxy;
    }

    public void raiseErrorProxy() {
        int errorTime = this.proxy.getErrorTime();
        this.proxy.setErrorTime(++errorTime);
    }

    public void setProxy(IPModel proxy) {
        System.out.println("切换到: " + proxy.getHost() + ":" + proxy.getPort());
        this.proxy = proxy;
    }

    public void resetProxy() {
        this.proxy = null;
    }

    public boolean isAutoSwitchProxy() {
        return autoSwitchProxy;
    }

    public Downloader setAutoSwitchProxy(boolean autoSwitchProxy) {
        this.autoSwitchProxy = autoSwitchProxy;
        return this;
    }

    public void swithProxy() {
        IPModel newip = new IPPool().getRandomIp();
        setProxy(newip);
    }
}
