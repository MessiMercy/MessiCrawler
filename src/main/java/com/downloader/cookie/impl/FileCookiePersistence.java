package com.downloader.cookie.impl;

import com.downloader.cookie.CookiePersistence;
import org.apache.http.impl.client.BasicCookieStore;

import java.io.*;

/**
 * 将cookie存储在file中
 * Created by Administrator on 2016/12/26.
 */
public class FileCookiePersistence implements CookiePersistence {

    @Override
    public void saveCookieStore(String key, BasicCookieStore store) {
        File keyFile = new File(key);
        try {
            ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(keyFile));
            ByteArrayInputStream in = new ByteArrayInputStream(serialize(store).toByteArray());
            ObjectInputStream objectInputStream = new ObjectInputStream(in);
            stream.writeObject(store);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public BasicCookieStore recoverCookieStore(String key) {
        File file = new File(key);
        if (!file.exists()) return null;
        BasicCookieStore store = null;
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
            store = (BasicCookieStore) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return store;
    }
}
