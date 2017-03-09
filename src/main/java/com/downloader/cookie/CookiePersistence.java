package com.downloader.cookie;

import org.apache.http.impl.client.BasicCookieStore;

import java.io.*;

/**
 * Created by Administrator on 2016/12/26.
 */
public interface CookiePersistence {

    /**
     * 因为cookiestore接口没有implement serializable接口
     * <p>
     * 将对象序列化成byte[]
     */
    default ByteArrayOutputStream serialize(BasicCookieStore store) {
        if (store == null) return null;
        ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(byteArrayOut);
            out.writeObject(store);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return byteArrayOut;
    }

    /**
     * 将byte[]根据实现方式存入指定key中
     */
    void saveCookieStore(String key, BasicCookieStore store);

    /**
     * 从指定key中恢复cookiestore对象
     */
    BasicCookieStore recoverCookieStore(String key);

    default BasicCookieStore unserialize(String key, byte[] objectByteArr) {
        ByteArrayInputStream in = new ByteArrayInputStream(objectByteArr);
        BasicCookieStore store = null;
        try {
            ObjectInputStream objInput = new ObjectInputStream(in);
            Object obj = objInput.readObject();
            if (obj instanceof BasicCookieStore) store = (BasicCookieStore) obj;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return store;
    }
}
