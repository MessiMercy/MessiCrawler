package com.downloader.certificate;

/**
 * Created by Administrator on 2016/11/3.
 */

import javax.net.ssl.*;
import java.io.*;
import java.net.Socket;
import java.security.*;
import java.security.cert.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SSLX509CertificateManager {

    private static final Logger logger = Logger.getLogger("SSLX509CertificateManager");
    private static final char[] HEXDIGITS = "0123456789abcdef".toCharArray();
    private static Pattern cnPattern = Pattern.compile("(?i)(cn=)([^,]*)");
    private static Map<KeyStoreOptions, KeyStore> stores = new HashMap<KeyStoreOptions, KeyStore>();

    private static String toHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 3);
        for (int b : bytes) {
            b &= 0xff;
            sb.append(HEXDIGITS[b >> 4]);
            sb.append(HEXDIGITS[b & 15]);
            sb.append(' ');
        }
        return sb.toString();
    }


    /**
     * 开始握手等一系列密钥协商
     *
     * @param socket
     * @return
     */
    public static boolean startHandshake(SSLSocket socket) {
        try {
            logger.log(Level.INFO, "-开始握手，认证服务器证书-");
            socket.startHandshake();
            System.out.println();
            logger.log(Level.INFO, "-握手结束，结束认证服务器证书-");
        } catch (SSLException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static SSLSocket createTrustCASocket(String host, int port, ConnectionConfiguration config)
            throws Exception {
        if (config == null) {
            config = new ConnectionConfiguration();
        }
        KeyStore ks = getKeyStore(config);
        SSLContext context = SSLContext.getInstance("TLS");
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ks);
        X509TrustManager defaultTrustManager = (X509TrustManager) tmf.getTrustManagers()[0];
        CAX509TrustManager tm = new CAX509TrustManager(defaultTrustManager, ks, config);

        context.init(null, new TrustManager[]{tm}, new SecureRandom());
        SSLSocketFactory factory = context.getSocketFactory();

        logger.log(Level.INFO, "开始连接： " + host + ":" + port + "...");
        SSLSocket socket = (SSLSocket) factory.createSocket(host, port);
        socket.setSoTimeout(10000);

        config.setServer(host);
        config.setPort(port);
        // config.setTrustKeyStore(ks);
        X509Certificate certificate = (X509Certificate) ks.getCertificate(host + ":" + port);

        if (certificate != null && isValid(certificate)) {
            logger.log(Level.INFO, "-证书文件存在并且有效，无需进行握手-");
            return socket;
        }
        if (!startHandshake(socket)) {
            logger.log(Level.SEVERE, "-握手失败-");
            return null;
        }
        X509Certificate[] chain = tm.chain;
        if (chain == null || chain.length == 0) {
            logger.log(Level.SEVERE, "-证书链为空，认证失败-");
            return null;
        }

        if (config.isVerifyRootCAEnabled()) {
            boolean isValidRootCA = checkX509CertificateRootCA(ks, chain, config.isSelfSignedCertificateEnabled());
            if (!isValidRootCA) {
                return null;
            }
        }

        return socket;
    }

    /**
     * 获取keystore，防治多次加载
     *
     * @param config
     * @return
     * @throws KeyStoreException
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws CertificateException
     * @throws FileNotFoundException
     */
    private static KeyStore getKeyStore(ConnectionConfiguration config) throws KeyStoreException, IOException,
            NoSuchAlgorithmException, CertificateException, FileNotFoundException {
        KeyStore ks;
        synchronized (stores) {
            KeyStoreOptions options = new KeyStoreOptions(config.getTruststoreType(), config.getTruststorePath(),
                    config.getTruststorePassword());
            if (stores.containsKey(options)) {
                logger.log(Level.INFO, "从缓存中获取trustKeystore");
                ks = stores.get(options);

            } else {
                File file = new File(config.getTruststorePath());
                char[] password = config.getTruststorePassword().toCharArray();

                logger.log(Level.INFO, "加载" + file + "证书文件");
                ks = KeyStore.getInstance(KeyStore.getDefaultType());
                if (!file.exists()) {
                    logger.log(Level.INFO, "证书文件不存在，选择自动创建");
                    ks.load(null, password);
                } else {
                    logger.log(Level.INFO, "证书文件存在，开始加载");
                    InputStream in = new FileInputStream(file);
                    ks.load(in, password);
                    in.close();
                }
                stores.put(options, ks);
            }

        }
        return ks;
    }

    public static SSLSocket createTrustCASocket(String host, int port) throws Exception {

        return createTrustCASocket(host, port, null);
    }

    public static SSLSocket createTrustCASocket(Socket s, ConnectionConfiguration config) throws Exception {
        if (config == null) {
            config = new ConnectionConfiguration();
        }

        KeyStore ks = getKeyStore(config);
        SSLContext context = SSLContext.getInstance("TLS");
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ks);
        X509TrustManager defaultTrustManager = (X509TrustManager) tmf.getTrustManagers()[0];
        CAX509TrustManager tm = new CAX509TrustManager(defaultTrustManager, ks, config);

        context.init(null, new TrustManager[]{tm}, new SecureRandom());
        SSLSocketFactory factory = context.getSocketFactory();

        String host = s.getInetAddress().getHostName();
        int port = s.getPort();
        logger.log(Level.INFO, "开始连接： " + host + ":" + port + "...");

        SSLSocket socket = (SSLSocket) factory.createSocket(s, host, port, true);
        socket.setSoTimeout(10000);

        config.setServer(s.getInetAddress().getHostName());
        config.setPort(s.getPort());

        X509Certificate certificate = (X509Certificate) ks.getCertificate(host + ":" + s.getPort());
        if (certificate != null && isValid(certificate)) {
            logger.log(Level.INFO, "-证书文件存在并且有效，无需进行握手-");
            return socket;
        }
        if (!startHandshake(socket)) {
            return null;
        }
        X509Certificate[] chain = tm.chain;
        if (chain == null || chain.length == 0) {
            logger.log(Level.SEVERE, "-证书链为空，认证失败-");
            return null;
        }
        if (config.isVerifyRootCAEnabled()) {
            boolean isValidRootCA = checkX509CertificateRootCA(ks, chain, config.isSelfSignedCertificateEnabled());
            if (!isValidRootCA) {
                logger.log(Level.SEVERE, "根证书校验无效");
                return null;
            }
        }
        return socket;

    }

    public static SSLSocket createTrustCASocket(Socket s) throws Exception {

        return createTrustCASocket(s, null);
    }

    public static class CAX509TrustManager implements X509TrustManager {

        private final X509TrustManager tm;
        private X509Certificate[] chain;
        private KeyStore keyStore;
        private ConnectionConfiguration config;
        public MessageDigest sha1 = null;
        public MessageDigest md5 = null;

        public CAX509TrustManager(X509TrustManager tm, KeyStore ks, ConnectionConfiguration config)
                throws NoSuchAlgorithmException {
            this.tm = tm;
            this.keyStore = ks;
            sha1 = MessageDigest.getInstance("SHA1");
            md5 = MessageDigest.getInstance("MD5");
            this.config = config;
        }

        public X509Certificate[] getAcceptedIssuers() {
            return tm.getAcceptedIssuers(); // 生成证书数组，用于存储新证书
        }

        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            tm.checkClientTrusted(chain, authType); // 检查客户端
        }

        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            if (this.chain == null) {
                this.chain = getAcceptedIssuers();
            }
            if (chain != null && chain.length > 0) {
                if (!checkX509CertificateValid(chain, config)) {
                    logger.log(Level.SEVERE, "证书校验未通过");
                    return;
                }
                for (int i = 0; i < chain.length; i++) {
                    X509Certificate certificate = chain[i];
                    if (i == 0) {
                        saveCAToKeyStore(certificate, config.getServer() + ":" + config.getPort());
                    } else {
                        saveCAToKeyStore(certificate, null);
                    }
                }
            }
        }

        public void saveCAToKeyStore(X509Certificate certificate, String aliasKey) throws CertificateEncodingException {
            try {
                X509Certificate cert = certificate;
                String string = certificate.getType();
                System.out.println(string);
                System.out.println(" Subject " + cert.getSubjectDN());
                System.out.println("  Issuer  " + cert.getIssuerDN());
                sha1.update(cert.getEncoded());
                System.out.println("  sha1    " + toHexString(sha1.digest()));
                md5.update(cert.getEncoded());
                System.out.println("  md5     " + toHexString(md5.digest()));

                String alias = keyStore.getCertificateAlias(cert);
                if (alias == null || alias != null && !isValid(certificate)) {
                    if (aliasKey == null || aliasKey.length() == 0) {
                        alias = cert.getSubjectDN().getName();
                    } else {
                        alias = aliasKey;
                        logger.log(Level.INFO, "设定指定证书别名:" + alias);
                    }
                    keyStore.setCertificateEntry(alias, certificate);
                    OutputStream out = new FileOutputStream(config.getTruststorePath() + new Random().nextInt());
                    keyStore.store(out, config.getTruststorePassword().toCharArray());
                    out.close();
                    chain = Arrays.copyOf(chain, chain.length + 1);
                    chain[chain.length - 1] = certificate;
                    logger.fine(certificate.toString());
                }

            } catch (KeyStoreException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (CertificateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public static boolean isValid(X509Certificate cert) {
        if (cert == null) {
            return false;
        }
        try {
            cert.checkValidity();
        } catch (CertificateExpiredException e) {
            e.printStackTrace();
            return false;
        } catch (CertificateNotYetValidException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 校验证书的有效性
     *
     * @param chain
     * @param config
     * @return
     */
    private static boolean checkX509CertificateValid(X509Certificate[] chain, ConnectionConfiguration config) {
        boolean result = true;
        if (config.isExpiredCertificatesCheckEnabled()) {
            result = result && checkX509CertificateExpired(chain);
        }

        if (config.isVerifyChainEnabled()) {
            result = result && checkX509CertificateChain(chain);
        }

        if (config.isNotMatchingDomainCheckEnabled()) {
            result = result && checkIsMatchDomain(chain, config.getServer());
        }

        return result;

    }

    /**
     * 检查是否匹配域名
     *
     * @param x509Certificates
     * @param server
     * @return
     */
    public static boolean checkIsMatchDomain(X509Certificate[] x509Certificates, String server) {
        server = server.toLowerCase();
        List<String> peerIdentities = getPeerIdentity(x509Certificates[0]);
        if (peerIdentities.size() == 1 && peerIdentities.get(0).startsWith("*.")) {
            String peerIdentity = peerIdentities.get(0).replace("*.", "");
            if (!server.endsWith(peerIdentity)) {
                return false;
            }
        } else {
            for (int i = 0; i < peerIdentities.size(); i++) {
                String peerIdentity = peerIdentities.get(i).replace("*.", "");
                if (server.endsWith(peerIdentity)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 校验根证书
     *
     * @param trustStore
     * @param x509Certificates
     * @param isSelfSignedCertificate 是否自签名证书
     * @return
     */
    public static boolean checkX509CertificateRootCA(KeyStore trustStore, X509Certificate[] x509Certificates,
                                                     boolean isSelfSignedCertificate) {
        List<String> peerIdentities = getPeerIdentity(x509Certificates[0]);
        boolean trusted = false;
        try {
            int size = x509Certificates.length;
            trusted = trustStore.getCertificateAlias(x509Certificates[size - 1]) != null;
            if (!trusted && size == 1 && isSelfSignedCertificate) {
                logger.log(Level.WARNING, "-强制认可自签名证书-");
                trusted = true;
            }
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        if (!trusted) {
            logger.log(Level.SEVERE, "-根证书签名的网站：" + peerIdentities + "不能被信任");
        }

        return trusted;
    }

    /**
     * 检查证书是否过期
     *
     * @param x509Certificates
     * @return
     */
    public static boolean checkX509CertificateExpired(X509Certificate[] x509Certificates) {
        Date date = new Date();
        for (int i = 0; i < x509Certificates.length; i++) {
            try {
                x509Certificates[i].checkValidity(date);
            } catch (GeneralSecurityException generalsecurityexception) {
                logger.log(Level.SEVERE, "-证书已经过期-");
                return false;
            }
        }
        return true;
    }

    /**
     * 校验证书链的完整性
     *
     * @param x509Certificates
     * @return
     */
    public static boolean checkX509CertificateChain(X509Certificate[] x509Certificates) {
        Principal principalLast = null;
        List<String> peerIdentities = getPeerIdentity(x509Certificates[0]);

        for (int i = x509Certificates.length - 1; i >= 0; i--) {
            X509Certificate x509certificate = x509Certificates[i];
            Principal principalIssuer = x509certificate.getIssuerDN();
            Principal principalSubject = x509certificate.getSubjectDN();
            if (principalLast != null) {
                if (principalIssuer.equals(principalLast)) {
                    try {
                        PublicKey publickey = x509Certificates[i + 1].getPublicKey();
                        x509Certificates[i].verify(publickey);
                    } catch (GeneralSecurityException generalsecurityexception) {

                        logger.log(Level.SEVERE, "-无效的证书签名-" + peerIdentities);
                        return false;
                    }
                } else {
                    logger.log(Level.SEVERE, "-无效的证书签名-" + peerIdentities);
                    return false;
                }
            }
            principalLast = principalSubject;
        }

        return true;
    }

    /**
     * 返回所有可用的签名方式 键值对 如CN=VeriSignMPKI-2-6
     *
     * @param certificate
     * @return
     */
    private static List<String> getSubjectAlternativeNames(X509Certificate certificate) {
        List<String> identities = new ArrayList<String>();
        try {
            Collection<List<?>> altNames = certificate.getSubjectAlternativeNames();
            if (altNames == null) {
                return Collections.emptyList();
            }

            Iterator<List<?>> iterator = altNames.iterator();
            do {
                if (!iterator.hasNext())
                    break;
                List<?> altName = iterator.next();
                int size = altName.size();
                if (size >= 2) {
                    identities.add((String) altName.get(1));
                }

            } while (true);
        } catch (CertificateParsingException e) {
            e.printStackTrace();
        }
        return identities;
    }

    /**
     * 返回所有可用的签名方式的值
     *
     * @return
     */
    public static List<String> getPeerIdentity(X509Certificate x509Certificate) {
        List<String> names = getSubjectAlternativeNames(x509Certificate);
        if (names.isEmpty()) {
            String name = x509Certificate.getSubjectDN().getName();
            Matcher matcher = cnPattern.matcher(name);
            if (matcher.find()) {
                name = matcher.group(2);
            }
            names = new ArrayList<String>();
            names.add(name);
        }
        return names;
    }
}
