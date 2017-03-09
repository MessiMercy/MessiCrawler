package com.downloader.certificate;

/**
 * Created by Administrator on 2016/11/3.
 */

public class ConnectionConfiguration {

    /**
     * 证书文件路径
     */
    private String truststorePath;
    /**
     * 证书类型
     */
    private String truststoreType;
    /**
     * 证书文件密码
     */
    private String truststorePassword;
    /**
     * 是否验证证书链的签名有效性
     */
    private boolean verifyChainEnabled = true;
    /**
     * 是否校验根证书，注意，自签名证书没有根证书
     */
    private boolean verifyRootCAEnabled = true;
    /**
     * 是否允许通过自签名证书
     */
    private boolean selfSignedCertificateEnabled = false;
    /**
     * 是否检查证书的有效期
     */
    private boolean expiredCertificatesCheckEnabled = true;
    /**
     * 检查域名的匹配情况
     */
    private boolean notMatchingDomainCheckEnabled = true;

    private String server;
    private int port;

    public ConnectionConfiguration() {
        truststorePassword = "WlZSak5GcFVUbTlsVjJSNg==";
        truststorePath = "socket_tls_clientTrust.cer";
        truststoreType = "jks";
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public boolean isExpiredCertificatesCheckEnabled() {
        return expiredCertificatesCheckEnabled;
    }

    public void setSelfSignedCertificateEnabled(boolean selfSignedCertificateEnabled) {
        this.selfSignedCertificateEnabled = selfSignedCertificateEnabled;
    }

    public void setExpiredCertificatesCheckEnabled(boolean expiredCertificatesCheckEnabled) {
        this.expiredCertificatesCheckEnabled = expiredCertificatesCheckEnabled;
    }

    public boolean isSelfSignedCertificateEnabled() {
        return selfSignedCertificateEnabled;
    }

    public boolean isNotMatchingDomainCheckEnabled() {
        return notMatchingDomainCheckEnabled;
    }

    public boolean isVerifyRootCAEnabled() {
        return verifyRootCAEnabled;
    }

    public void setVerifyRootCAEnabled(boolean verifyRootCAEnabled) {
        this.verifyRootCAEnabled = verifyRootCAEnabled;
    }

    public void setVerifyChainEnabled(boolean verifyChainEnabled) {
        this.verifyChainEnabled = verifyChainEnabled;
    }

    public boolean isVerifyChainEnabled() {

        return verifyChainEnabled;
    }

    public String getTruststoreType() {
        return truststoreType;
    }

    public void setTruststoreType(String truststoreType) {
        this.truststoreType = truststoreType;
    }

    public String getTruststorePassword() {
        return truststorePassword;
    }

    public void setTruststorePassword(String truststorePassword) {
        this.truststorePassword = truststorePassword;
    }

    public String getTruststorePath() {
        return truststorePath;
    }

    public void setTruststorePath(String truststorePath) {
        this.truststorePath = truststorePath;
    }

    public void setNotMatchingDomainCheckEnabled(boolean notMatchingDomainCheckEnabled) {
        this.notMatchingDomainCheckEnabled = notMatchingDomainCheckEnabled;
    }

}
