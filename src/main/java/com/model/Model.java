package com.model;

import java.io.*;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 数据模型通用model
 * Created by Administrator on 2016/11/24.
 */
public interface Model {
    default String indexParams() {
        StringBuilder builder = new StringBuilder();
        Map<String, Object> listParams = this.listParams();
        listParams.forEach((k, v) -> {
            String keyAndValue = k + ": " + v.toString();
            System.out.println(keyAndValue);
            builder.append(keyAndValue).append("\r\n");
        });
        return builder.toString();
    }

    default Map<String, Object> listParams() {
        Map<String, Object> params = new LinkedHashMap<>();
        Field[] fields = this.getClass().getDeclaredFields();
        if (fields == null || fields.length == 0) return null;
        for (Field field : fields) {
            field.setAccessible(true);
            Object value = null;
            try {
                value = field.get(this);
            } catch (Exception ignore) {
            }
            if (value == null) value = "";
            params.put(field.getName(), value);
        }
        return params;
    }

    /**
     * 将对象串行化深克隆，省事但效率低
     *
     * @return 对象的深克隆
     */
    default Object deepClone() throws IOException,
            ClassNotFoundException {//将对象写到流里
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        ObjectOutputStream oo = new ObjectOutputStream(bo);
        oo.writeObject(this);//从流里读出来
        ByteArrayInputStream bi = new ByteArrayInputStream(bo.toByteArray());
        ObjectInputStream oi = new ObjectInputStream(bi);
        return (oi.readObject());
    }

}
