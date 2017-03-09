package com.downloader.certificate;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.Socket;
import java.net.URL;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;

/**
 * Created by Administrator on 2016/11/7.
 */
public class SSLTrustManager implements javax.net.ssl.TrustManager,
        javax.net.ssl.X509TrustManager, HostnameVerifier {
    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[]{};
    }

    public boolean isServerTrusted(
            java.security.cert.X509Certificate[] certs) {
        return true;
    }

    public boolean isClientTrusted(
            java.security.cert.X509Certificate[] certs) {
        return true;
    }

    public void checkServerTrusted(
            java.security.cert.X509Certificate[] certs, String authType)
            throws java.security.cert.CertificateException {
        return;
    }

    public void checkClientTrusted(
            java.security.cert.X509Certificate[] certs, String authType)
            throws java.security.cert.CertificateException {
        return;
    }

    @Override
    public boolean verify(String urlHostName, SSLSession session) { //允许所有主机
        return true;
    }

    /**
     * 客户端使用
     */
    public static HttpURLConnection connectTrustAllServer(String strUrl) throws Exception {

        return connectTrustAllServer(strUrl, null);
    }

    /**
     * 客户端使用
     *
     * @param strUrl 要访问的地址
     * @param proxy  需要经过的代理
     * @return
     * @throws Exception
     */
    public static HttpURLConnection connectTrustAllServer(String strUrl, Proxy proxy) throws Exception {

        javax.net.ssl.TrustManager[] trustCertsmanager = new javax.net.ssl.TrustManager[1];
        javax.net.ssl.TrustManager tm = new SSLTrustManager();
        trustCertsmanager[0] = tm;
        javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext
                .getInstance("TLS");
        sc.init(null, trustCertsmanager, null);
        javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sc
                .getSocketFactory());

        HttpsURLConnection.setDefaultHostnameVerifier((HostnameVerifier) tm);

        URL url = new URL(strUrl);
        HttpURLConnection urlConn = null;
        if (proxy == null) {
            urlConn = (HttpURLConnection) url.openConnection();
        } else {
            urlConn = (HttpURLConnection) url.openConnection(proxy);
        }
        urlConn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/49.0.2623.112 Safari/537.36");
        return urlConn;
    }

    /**
     * 用于双向认证,客户端使用
     *
     * @param strUrl
     * @param proxy
     * @return
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws CertificateException
     * @throws FileNotFoundException
     * @throws IOException
     * @throws UnrecoverableKeyException
     * @throws KeyManagementException
     */
    public static HttpURLConnection connectProxyTrustCA(String strUrl, Proxy proxy) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, FileNotFoundException, IOException, UnrecoverableKeyException, KeyManagementException {
        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {

            @Override
            public boolean verify(String s, SSLSession sslsession) {

                return true;
            }
        });
        String clientKeyStoreFile = "D:/JDK8Home/tianwt/sslClientKeys";
        String clientKeyStorePwd = "123456";
        String catServerKeyPwd = "123456";

        String serverTrustKeyStoreFile = "D:/JDK8Home/tianwt/sslClientTrust";
        String serverTrustKeyStorePwd = "123456";
        KeyStore serverKeyStore = KeyStore.getInstance("JKS");
        serverKeyStore.load(new FileInputStream(clientKeyStoreFile), clientKeyStorePwd.toCharArray());

        KeyStore serverTrustKeyStore = KeyStore.getInstance("JKS");
        serverTrustKeyStore.load(new FileInputStream(serverTrustKeyStoreFile), serverTrustKeyStorePwd.toCharArray());

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(serverKeyStore, catServerKeyPwd.toCharArray());

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(serverTrustKeyStore);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());

        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());

        URL url = new URL(strUrl);
        HttpURLConnection httpURLConnection = null;
        if (proxy == null) {
            httpURLConnection = (HttpURLConnection) url.openConnection();
        } else {
            httpURLConnection = (HttpURLConnection) url.openConnection(proxy);
        }
        httpURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/49.0.2623.112 Safari/537.36");
        return httpURLConnection;

    }

    /**
     * 用于单向认证，客户端使用
     * <p>
     * server侧只需要自己的keystore文件，不需要truststore文件
     * client侧不需要自己的keystore文件，只需要truststore文件（其中包含server的公钥）。
     * 此外server侧需要在创建SSLServerSocket之后设定不需要客户端证书：setNeedClientAuth(false)
     *
     * @param strUrl
     * @param proxy
     * @return
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws CertificateException
     * @throws FileNotFoundException
     * @throws IOException
     * @throws UnrecoverableKeyException
     * @throws KeyManagementException
     */
    public static HttpURLConnection connectProxyTrustCA2(String strUrl, Proxy proxy) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, FileNotFoundException, IOException, UnrecoverableKeyException, KeyManagementException {
        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {

            @Override
            public boolean verify(String s, SSLSession sslsession) {

                return true;
            }
        });

        String serverTrustKeyStoreFile = "D:/JDK8Home/tianwt/sslClientTrust";
        String serverTrustKeyStorePwd = "123456";

        KeyStore serverTrustKeyStore = KeyStore.getInstance("JKS");
        serverTrustKeyStore.load(new FileInputStream(serverTrustKeyStoreFile), serverTrustKeyStorePwd.toCharArray());

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(serverTrustKeyStore);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, tmf.getTrustManagers(), null);

        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());

        URL url = new URL(strUrl);
        HttpURLConnection httpURLConnection = null;
        if (proxy == null) {
            httpURLConnection = (HttpURLConnection) url.openConnection();
        } else {
            httpURLConnection = (HttpURLConnection) url.openConnection(proxy);
        }
        httpURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/49.0.2623.112 Safari/537.36");
        return httpURLConnection;

    }

    /**
     * 用于双向认证
     *
     * @param socketClient 是否产生socket
     * @return
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws CertificateException
     * @throws FileNotFoundException
     * @throws IOException
     * @throws UnrecoverableKeyException
     * @throws KeyManagementException
     */
    public SSLSocket createTlsConnect(Socket socketClient) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, FileNotFoundException, IOException, UnrecoverableKeyException, KeyManagementException {

        String protocol = "TLS";
        String serverKey = "D:/JDK8Home/tianwt/sslServerKeys";
        String serverTrust = "D:/JDK8Home/tianwt/sslServerTrust";
        String serverKeyPwd = "123456";  //私钥密码
        String serverTrustPwd = "123456";  //信任证书密码
        String serverKeyStorePwd = "123456";  // keystore存储密码

        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(new FileInputStream(serverKey), serverKeyPwd.toCharArray());

        KeyStore tks = KeyStore.getInstance("JKS");
        tks.load(new FileInputStream(serverTrust), serverTrustPwd.toCharArray());

        KeyManagerFactory km = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        km.init(keyStore, serverKeyStorePwd.toCharArray());

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(tks);

        SSLContext sslContext = SSLContext.getInstance(protocol);
        sslContext.init(km.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());  //第一项是用来做服务器验证的

        SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
        SSLSocket clientSSLSocket = (SSLSocket) sslSocketFactory.createSocket(socketClient, socketClient.getInetAddress().getHostAddress(), socketClient.getPort(), true);
        clientSSLSocket.setNeedClientAuth(false);
        clientSSLSocket.setUseClientMode(false);

        return clientSSLSocket;
    }

    /**
     * 用于单向认证
     * server侧只需要自己的keystore文件，不需要truststore文件
     * client侧不需要自己的keystore文件，只需要truststore文件（其中包含server的公钥）。
     * 此外server侧需要在创建SSLServerSocket之后设定不需要客户端证书：setNeedClientAuth(false)
     *
     * @param socketClient
     * @return
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws CertificateException
     * @throws FileNotFoundException
     * @throws IOException
     * @throws UnrecoverableKeyException
     * @throws KeyManagementException
     */
    public static SSLSocket createTlsConnect2(Socket socketClient) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, FileNotFoundException, IOException, UnrecoverableKeyException, KeyManagementException {

        String protocol = "TLS";
        String serverKey = "D:/JDK8Home/tianwt/sslServerKeys";
        String serverKeyPwd = "123456";  //私钥密码
        String serverKeyStorePwd = "123456";  // keystore存储密码

        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(new FileInputStream(serverKey), serverKeyPwd.toCharArray());

        KeyManagerFactory km = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        km.init(keyStore, serverKeyStorePwd.toCharArray());

        SSLContext sslContext = SSLContext.getInstance(protocol);
        sslContext.init(km.getKeyManagers(), null, new SecureRandom());  //第一项是用来做服务器验证的

        SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
        SSLSocket clientSSLSocket = (SSLSocket) sslSocketFactory.createSocket(socketClient, socketClient.getInetAddress().getHostAddress(), socketClient.getPort(), true);
        clientSSLSocket.setNeedClientAuth(false);
        clientSSLSocket.setUseClientMode(false);

        return clientSSLSocket;
    }

    /**
     * 将普通的socket转为sslsocket，客户端和服务端均可使用
     * <p>
     * 服务端使用的时候是把普通的socket转为sslsocket,并且作为服务器套接字（注意：指的不是ServerSocket,当然ServerSocket的本质也是普通socket）
     *
     * @param remoteHost
     * @param isClient
     * @return
     */
    public static SSLSocket getTlsTrustAllSocket(Socket remoteHost, boolean isClient) {
        SSLSocket remoteSSLSocket = null;
        SSLContext context = SSLTrustManager.getTrustAllSSLContext(isClient);
        try {
            remoteSSLSocket = (SSLSocket) context.getSocketFactory().createSocket(remoteHost, remoteHost.getInetAddress().getHostName(), remoteHost.getPort(), true);
            remoteSSLSocket.setTcpNoDelay(true);
            remoteSSLSocket.setSoTimeout(5000);
            remoteSSLSocket.setNeedClientAuth(false); //这里设置为true时会强制握手
            remoteSSLSocket.setUseClientMode(isClient); //注意服务器和客户的角色选择

        } catch (IOException e) {
            e.printStackTrace();
        }
        return remoteSSLSocket;
    }

    /**
     * 用于客户端，通过所有证书验证
     *
     * @param isClient 是否生成客户端SSLContext，否则生成服务端SSLContext
     * @return
     */
    public static SSLContext getTrustAllSSLContext(boolean isClient) {
        String protocol = "TLS";
        javax.net.ssl.SSLContext sc = null;
        try {
            javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[1];
            javax.net.ssl.TrustManager tm = new SSLTrustManager();
            trustAllCerts[0] = tm;
            sc = javax.net.ssl.SSLContext
                    .getInstance(protocol);

            if (isClient) {
                sc.init(null, trustAllCerts, null); //作为客户端使用
            } else {
                String serverKeyPath = "D:/JDK8Home/tianwt/sslServerKeys";
                String serverKeyPwd = "123456";  //私钥密码
                String serverKeyStorePwd = "123456";  // keystore存储密码

                KeyStore keyStore = KeyStore.getInstance("JKS");
                keyStore.load(new FileInputStream(serverKeyPath), serverKeyPwd.toCharArray());

                KeyManagerFactory km = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                km.init(keyStore, serverKeyStorePwd.toCharArray());
                KeyManager[] keyManagers = km.getKeyManagers();
                keyManagers = Arrays.copyOf(keyManagers, keyManagers.length + 1);
                sc.init(keyManagers, null, new SecureRandom());
            }
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sc;
    }


}
