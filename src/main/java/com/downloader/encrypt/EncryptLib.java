package com.downloader.encrypt;

import org.apache.commons.lang.StringUtils;
import org.mozilla.universalchardet.UniversalDetector;
import sun.misc.BASE64Decoder;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 简洁加密类
 * Created by Administrator on 2016/12/7.
 */
public class EncryptLib {

    /**
     * 32位md5加密方法
     *
     * @param key 关键字
     * @return 32位加密小写字符串
     */
    public static String md5(String key) {
        try {
            // 得到一个信息摘要器
            MessageDigest digest = MessageDigest.getInstance("md5");
            byte[] result = digest.digest(key.getBytes());
            StringBuilder buffer = new StringBuilder();
            // 把每一个byte 做一个与运算 0xff;
            for (byte b : result) {
                // 与运算
                int number = b & 0xff;// 加盐
                String str = Integer.toHexString(number);
                if (str.length() == 1) {
                    buffer.append("0");
                }
                buffer.append(str);
            }
            // 标准的md5加密后的结果
            return buffer.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String sha(String key) {
        try {
            return new String(Coder.encryptSHA(key.getBytes()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String base64Encode(String key) {
        try {
            return Coder.encryptBASE64(key.getBytes()).trim();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String base64Decode(String key) {
        try {
            return new String(Coder.decryptBASE64(key));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String rsa(String key, String modulus, String exponent) {
        try {
            return RSAencoder.encryptByPublicKey(key, modulus, exponent);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String rsa(String key, String publickey) {
        byte[] bytes = key.getBytes();
        byte[] encryptByPublicKey = new byte[0];
        try {
            encryptByPublicKey = RSAencoder.encryptByPublicKey(bytes, publickey);
            if (encryptByPublicKey != null) {
                return Coder.encryptBASE64(encryptByPublicKey).replaceAll("\\s", "");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 将unicode的编码转为正常中文
     */
    public static String unicode(String unicodeStr) {
        if (unicodeStr == null) {
            return null;
        }
        StringBuilder retBuf = new StringBuilder();
        int maxLoop = unicodeStr.length();
        for (int i = 0; i < maxLoop; i++) {
            if (unicodeStr.charAt(i) == '\\') {
                if ((i < maxLoop - 5)
                        && ((unicodeStr.charAt(i + 1) == 'u') || (unicodeStr
                        .charAt(i + 1) == 'U')))
                    try {
                        retBuf.append((char) Integer.parseInt(
                                unicodeStr.substring(i + 2, i + 6), 16));
                        i += 5;
                    } catch (NumberFormatException localNumberFormatException) {
                        retBuf.append(unicodeStr.charAt(i));
                    }
                else
                    retBuf.append(unicodeStr.charAt(i));
            } else {
                retBuf.append(unicodeStr.charAt(i));
            }
        }
        return retBuf.toString();
    }

    /**
     * 用于识别bytes的编码
     */
    public static String detect(byte[] content) {
        UniversalDetector detector = new UniversalDetector(null);
        //开始给一部分数据，让学习一下，官方建议是1000个byte左右（当然这1000个byte你得包含中文之类的）
        detector.handleData(content, 0, content.length);
        //识别结束必须调用这个方法
        detector.dataEnd();
        //神奇的时刻就在这个方法了，返回字符集编码。
        return detector.getDetectedCharset();
    }

    /**
     * AES加密
     *
     * @param content    待加密的内容
     * @param encryptKey 加密密钥
     * @return 加密后的byte[]
     * @throws Exception
     */
    public static byte[] aesEncryptToBytes(String content, String encryptKey) throws Exception {
        return AESEncrypt.aesEncryptToBytes(content, encryptKey);
    }

    /**
     * AES加密为base 64 code
     *
     * @param content    待加密的内容
     * @param encryptKey 加密密钥
     * @return 加密后的base 64 code
     * @throws Exception
     */
    public static String aesEncrypt(String content, String encryptKey) throws Exception {
        return AESEncrypt.base64Encode(aesEncryptToBytes(content, encryptKey));
    }

    /**
     * base 64 decode
     *
     * @param base64Code 待解码的base 64 code
     * @return 解码后的byte[]
     * @throws Exception
     */
    public static byte[] base64DecodeByte(String base64Code) throws Exception {
        return StringUtils.isEmpty(base64Code) ? null : new BASE64Decoder().decodeBuffer(base64Code);
    }

    /**
     * AES解密
     *
     * @param encryptBytes 待解密的byte[]
     * @param decryptKey   解密密钥
     * @return 解密后的String
     * @throws Exception
     */
    public static String aesDecryptByBytes(byte[] encryptBytes, String decryptKey) throws Exception {
        return AESEncrypt.aesDecryptByBytes(encryptBytes, decryptKey);
    }

    /**
     * 将base 64 code AES解密
     *
     * @param encryptStr 待解密的base 64 code
     * @param decryptKey 解密密钥
     * @return 解密后的string
     * @throws Exception
     */
    public static String aesDecrypt(String encryptStr, String decryptKey) throws Exception {
        return StringUtils.isEmpty(encryptStr) ? null : aesDecryptByBytes(base64DecodeByte(encryptStr), decryptKey);
    }
}
