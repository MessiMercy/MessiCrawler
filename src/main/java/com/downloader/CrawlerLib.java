package com.downloader;

import com.downloader.cookie.SimpleHostCookie;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import javax.net.ssl.SSLContext;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.*;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CrawlerLib {
    private static final Pattern CONTENTTYPEREGEX = Pattern.compile("charset[\\s]*=[\\s]*(.*?)[\\s]*$");
    public static final Logger LOGGER = Logger.getLogger(CrawlerLib.class);

    static {
        PropertyConfigurator.configure("log4j.properties");
    }

    public static HttpUriRequest getResponse(String url, List<NameValuePair> list, Header[] headers, HttpHost proxy) {
        RequestConfig.Builder configBuilder = RequestConfig.custom().setConnectTimeout(6000).setSocketTimeout(6000)
                .setCookieSpec(CookieSpecs.STANDARD_STRICT).setConnectionRequestTimeout(60000);
        RequestConfig config = null;
        if (proxy == null) {
            config = configBuilder.build();
        } else {
            config = configBuilder.setProxy(proxy).build();
        }
        // use the method setCookieSpec to make the header which named
        // set-cookie effect
        HttpUriRequest request = null;
        if (list == null) {
            HttpGet get = new HttpGet(url);
            get.setConfig(config);
            request = get;
        } else {
            HttpPost post = new HttpPost(url);
            post.setConfig(config);
            HttpEntity entity = null;
            try {
                entity = new UrlEncodedFormEntity(list, "utf-8");
                post.setEntity(entity);
                request = post;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        if (request != null) {
            request.setHeader("User-Agent", getAgent());
            request.setHeader("Connection", "keep-alive");
            request.setHeaders(headers);
            System.out.println("ready to link " + url);
        }
        return request;
    }

    private static String getAgent() {
        String opera = "Opera/9.80  (Windows NT 5.2; U; zh-cn) Presto/2.9.168 Version/11.51";
        String other = "(Windows NT 5.2; U; zh-cn) Presto/2.9.168 Version/11.51";
        // String ie9 = "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1;
        // WOW64; Trident/5.0)";
        String aoyou = "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.2; SV1; .NET CLR 1.1.4322; Maxthon 2.0)";
        String qq = "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.2; SV1; .NET CLR 1.1.4322) QQBrowser/6.8.10793.201";
        String green = "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.2; SV1; .NET CLR 1.1.4322; GreenBrowser)";
        String se360 = "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.2; SV1; .NET CLR 1.1.4322; 360SE)";
        String ie9 = "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Trident/5.0";
        String safari = "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-us) AppleWebKit/534.50 (KHTML, like Gecko) Version/5.1 Safari/534.50";
        String fireFox = "Mozilla/5.0 (Windows NT 6.1; rv:2.0.1) Gecko/20100101 Firefox/4.0.1";
        String chrome = "Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/27.0.1453.94 Safari/537.36";
        String[] agent = {ie9, safari, fireFox, chrome, opera, other, aoyou, qq, green, se360};
        Random random = new Random();
        int i = random.nextInt(10);
        return agent[i];
    }

    public static CloseableHttpClient getInstanceClient() {
        return getInstanceClient(null);
    }

    public static CloseableHttpClient getInstanceClient(boolean isNeedProxy, CookieStore store, HttpHost host) {
        HttpRequestRetryHandler handler = (e, i, httpContext) -> i < 4;
        return getInstanceClientBuilder(isNeedProxy, store, host, handler).build();
    }

//    public static HttpClientBuilder getInstanceClientBuilder(boolean isNeedProxy, CookieStore store, HttpHost host) {
//
//    }

    public static CloseableHttpClient getInstanceClient(boolean isNeedProxy, CookieStore store, HttpHost host, HttpRequestRetryHandler handler) {
        return getInstanceClientBuilder(isNeedProxy, store, host, handler, getAgent(), true).build();
    }

    public static HttpClientBuilder getInstanceClientBuilder(boolean isNeedProxy, CookieStore store, HttpHost host, HttpRequestRetryHandler handler) {
        return getInstanceClientBuilder(isNeedProxy, store, host, handler, getAgent(), true);
    }

    public static CloseableHttpClient getInstanceClient(boolean isNeedProxy, CookieStore store, HttpHost host, HttpRequestRetryHandler handler, String userAgent) {
        return getInstanceClientBuilder(isNeedProxy, store, host, handler, userAgent, true).build();
    }

    /**
     * 新建一个通用httpclientbuider
     * 使用代理时，必须一起传入host对象。
     * 不传入host对象的时候，代理不会生效
     */
    public static HttpClientBuilder getInstanceClientBuilder(boolean isNeedProxy, CookieStore store, HttpHost host, HttpRequestRetryHandler handler, String userAgent, boolean useCookie) {
        org.apache.http.ssl.SSLContextBuilder context_b = SSLContextBuilder.create();
        SSLContext ssl_context = null;
        try {
            context_b.loadTrustMaterial(null, (x509Certificates, s) -> true);
            //信任所有证书，解决https证书问题
            ssl_context = context_b.build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        ConnectionSocketFactory sslSocketFactory = null;
        Registry<ConnectionSocketFactory> registry = null;
        if (ssl_context != null) {
            sslSocketFactory = new SSLConnectionSocketFactory(ssl_context, new String[]{"TLSv1", "TLSv1.1", "TLSv1.2"}, null, (s, sslSession) -> true);
            //应用多种tls协议，解决偶尔握手中断问题
            registry = RegistryBuilder.<ConnectionSocketFactory>create().register("https", sslSocketFactory).register("http", new PlainConnectionSocketFactory()).build();
        }
        PoolingHttpClientConnectionManager manager = null;
        if (registry != null) {
            manager = new PoolingHttpClientConnectionManager(registry);
        } else {
            manager = new PoolingHttpClientConnectionManager();
        }
        manager.setMaxTotal(150);
        manager.setDefaultMaxPerRoute(200);
        HttpClientBuilder builder = HttpClients.custom()
                .setConnectionTimeToLive(6000, TimeUnit.SECONDS)
                .setUserAgent(userAgent);
        if (handler != null) {
            builder.setRetryHandler(handler);
        }
        if (store != null) {
            builder.setDefaultCookieStore(store);
        }
        if (isNeedProxy && host != null) {
//            HttpHost proxy = new HttpHost("127.0.0.1", 1080);// 代理ip
            DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(host);
            builder = builder.setRoutePlanner(routePlanner);
        }
        builder.setConnectionManager(manager);//httpclient连接池
        if (!useCookie) {
            builder.disableCookieManagement();
        }
//        builder.setRetryHandler(new DefaultHttpRequestRetryHandler(10, true));
        builder.setRedirectStrategy(new AllowAllRedirectStrategy());//默认重定向所有302和307，否则httpclient只自动处理get请求导致的302和307
        return builder;
    }

    public static CloseableHttpClient getInstanceClient(boolean isNeedProxy, CookieStore store) {
        return getInstanceClient(false, store, null);
    }

    public static CloseableHttpClient getInstanceClient(CookieStore store) {
        return getInstanceClient(false, store);
    }


    public static void printResult(String resource, boolean append, File file) {
        if (!file.exists()) {
            try {
                boolean b = file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        FileWriter writer = null;
        try {
            writer = new FileWriter(file, append);
            writer.write(resource + "\r\n");
            writer.flush();
        } catch (IOException e1) {
            e1.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void printResult(String resource, boolean append) {
        printResult(resource, append, new File("test.txt"));
    }

    String ensureNotEmpty(String str) {
        return str.equals("") ? "no value" : str;
    }

    @Deprecated
    public static void addCookieFromProperties(Properties p, CookieStore store, File file) {
        String cookieStr = null;
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (p.size() == 0) {
            try {
                p.load(new FileReader(file));
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        if (p.containsKey("Cookie")) {
            System.out.println("找到cookie，并加载！！！");
            cookieStr = p.getProperty("Cookie").replaceAll(";", "\n");
        } else {
            return;
        }
        Properties newp = new Properties();
        try {
            newp.load(new ByteArrayInputStream(cookieStr.getBytes()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        newp.forEach((k, v) -> store.addCookie(new SimpleHostCookie(k.toString(), v.toString(), "")));
    }

    @Deprecated
    public static void getCookieAndStore(Properties p, CookieStore store, String fileName) {
        List<Cookie> cookieList = store.getCookies();
        System.out.println("准备保存cookie！！！size为：" + cookieList.size());
        StringBuilder cookieStr = new StringBuilder();
        for (Cookie cookie : cookieList) {
            cookieStr.append(cookie.getName()).append("=").append(cookie.getValue()).append(";");
        }
        // cookieStr.append("login=true");
        p.setProperty("Cookie", cookieStr.toString());
        File file = new File(fileName);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            p.store(new FileOutputStream(file), "Last save cookie time: " + new Date());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void storeCookie(CookieStore store, File file) {
        int cookieSize = store.getCookies().size();
        LOGGER.info("发现" + cookieSize + "条cookie，正在保存……");
        if (!file.exists()) {
            if (!file.getAbsoluteFile().getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(new FileOutputStream(file, false));
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (oos != null) {
                oos.writeObject(store);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
//        BasicCookieStore basic = null;
//        if (store instanceof BasicCookieStore) basic = (BasicCookieStore) store;
//        FileCookiePersistence fileCookiePersistence = new FileCookiePersistence();
//        if (basic == null) return;
//        byte[] serialize = fileCookiePersistence.serialize(basic);
//        fileCookiePersistence.saveCookieStore(file.getName(), serialize);
    }

    public static CookieStore readCookieFromDisk(File file) {
        if (!file.exists()) {
            LOGGER.error("file not exists!", new FileNotFoundException());
            return null;
        }
        CookieStore store = null;
        ObjectInputStream input = null;
        try {
            input = new ObjectInputStream(new FileInputStream(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (input != null) {
            try {
                Object object = input.readObject();
                store = (CookieStore) object;
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
//        BasicCookieStore store = null;
//        FileCookiePersistence persistence = new FileCookiePersistence();
//        store = persistence.recoverCookieStore(file.getName());
        return store;
    }

    public static String getCharset(Response response) {
        String charset = "utf-8";
        if (response.getContentType() != null) {
            charset = charsetRegexFunc(response.getContentType());
        } else {
            charset = charsetRegexFunc(response.getContent());
        }
        return charset;
    }

    private static String charsetRegexFunc(String content) {
        String charset = Charset.defaultCharset().displayName();
        Matcher match = CONTENTTYPEREGEX.matcher(content);
        if (match.find()) {
            String temp = match.group(1);
            if (Charset.isSupported(temp)) {
                charset = temp;
            }
        }
        return charset;
    }

    /**
     * 将相关参数传入js，利用Java自带js引擎加密密码 其中的js文件为将密码处理的js下载到本地经过编辑处理得来  算法为rsa加密，传入公钥和exponent
     *
     * @see com.downloader.encrypt.EncryptLib 无exponent版本
     */
    public static String getEncodedPw(String rsaPub, String pw, String nonce) {
        String encodedPw = "";
        ScriptEngineManager sem = new ScriptEngineManager();
        ScriptEngine engine = sem.getEngineByName("javascript");
        try {
            engine.eval(new FileReader(new File("weibo.js")));
        } catch (FileNotFoundException | ScriptException e) {
            e.printStackTrace();
        }
        if (engine instanceof Invocable) {
            Invocable in = (Invocable) engine;
            try {
                encodedPw = in.invokeFunction("get_pass", rsaPub, pw, nonce).toString();
                // get_pass方法为自己写的，根据加密方式，自行添加方法传入相关参数并将加密后的pw取出
            } catch (NoSuchMethodException | ScriptException e) {
                e.printStackTrace();
            }
        }
        return encodedPw;
    }


    public static void main(String[] args) throws Exception {
        // configure the SSLContext with a TrustManager
//        SSLContext ctx = SSLContext.getInstance("TLS");
//        ctx.init(new KeyManager[0], new TrustManager[]{new DefaultTrustManager()}, new SecureRandom());
//        SSLContext.setDefault(ctx);
//
//        URL url = new URL("https://passport.zhaopin.com/checkcode/imgrd?r=" + Math.random());
//        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
//        conn.setHostnameVerifier((arg0, arg1) -> true);
//        System.out.println(conn.getResponseCode());
//        java.nio.file.Files.copy(conn.getInputStream(), Paths.get("abc.gif"), REPLACE_EXISTING);
//        conn.disconnect();
        HttpClient client = getInstanceClient();
        Downloader down = new Downloader(client);
    }

//    public static CloseableHttpClient getSpecialClient(boolean isNeedProxy) {
//        CloseableHttpClient client;
//        org.apache.http.ssl.SSLContextBuilder context_b = SSLContextBuilder.create();
//        SSLContext ssl_context = null;
//        try {
//            context_b.loadTrustMaterial(null, (TrustStrategy) (x509Certificates, s) -> true);
//            ssl_context = context_b.build();
//        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
//            e.printStackTrace();
//        }
//        org.apache.http.conn.ssl.SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(ssl_context,
//                (s, sslSession) -> true);
//        HttpClientBuilder builder = HttpClients.custom()
//                .setSSLSocketFactory(sslSocketFactory);
//        if (isNeedProxy) {
//            HttpHost proxy = new HttpHost("127.0.0.1", 1080);// 代理ip
//            DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);
//            builder = builder.setRoutePlanner(routePlanner);
//        }
//        client = builder.build();
//        return client;
//    }

}
