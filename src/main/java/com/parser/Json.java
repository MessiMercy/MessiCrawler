package com.parser;

import com.google.common.io.Files;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;

public class Json {
    private JsonObject obj;

    public Json() {
    }

    public Json(String json) {
        parseJson(json);
    }

    public static void main(String[] args) throws IOException {
        String json = Files.toString(new File("D:\\workspace\\FetchLagou\\CONST_DISTRICT.txt"),
                Charset.forName("utf-8"));
        Json jsonObj = new Json(json);
        JsonObject obj = jsonObj.getObj();
        obj.entrySet().forEach(p -> {
            System.out.println(p.getKey());
        });
    }

    public void parseJson(String json) {
        StringReader reader = new StringReader(json);
        JsonParser parser = new JsonParser();
        JsonReader Jreader = new JsonReader(reader);
        Jreader.setLenient(true);
        JsonElement element = parser.parse(Jreader);
        setObj(element.getAsJsonObject());
    }

    /**
     * 利用path来解析获得目的元素，使用.分割层次结构，如果是数组则多一个[]括号内表示取第几个元素 例如：first.second[5].last
     */
    public JsonElement getEle(String jsonPath) {
        JsonElement ele = this.getObj();
        JsonObject jo = this.getObj();
        String lastKey = null;
        if (jsonPath == null) {
            return ele;
        }
        if (!jsonPath.contains(".") && !jsonPath.contains("[")) {
            lastKey = jsonPath;
        } else {
            String[] pathArr = jsonPath.split("\\.");
            System.out.println("长度： " + pathArr.length);
            for (int i = 0; i < pathArr.length - 1; i++) {
                if (pathArr[i].contains("[") && pathArr[i].contains("]")) {
                    String index = StringUtils.substringBetween(pathArr[i], "[", "]");
                    int arrIndex = Integer.valueOf(index);
                    String key = StringUtils.substringBefore(pathArr[i], "[");
                    System.out.println("正在读取： " + key);
                    JsonElement eee = jo.get(key);
                    JsonArray arr = eee.getAsJsonArray();
                    jo = arr.get(arrIndex).getAsJsonObject();
                } else {
                    System.out.println("正在读取： " + pathArr[i]);
                    jo = jo.get(pathArr[i]).getAsJsonObject();
                }
            }
            lastKey = pathArr[pathArr.length - 1];
            if (lastKey.contains("[") && lastKey.contains("]")) {
                String index = StringUtils.substringBetween(lastKey, "[", "]");
                lastKey = StringUtils.substringBeforeLast(lastKey, "[");
                int arrIndex = Integer.valueOf(index);
                System.out.println("正在读取： " + lastKey);
                ele = jo.get(lastKey).getAsJsonArray().get(arrIndex);
                return ele;
            }
        }
        System.out.println("正在读取： " + lastKey);
        ele = jo.get(lastKey);
        return ele;
    }

    public JsonObject getObj() {
        return obj;
    }

    public void setObj(JsonObject obj) {
        this.obj = obj;
    }

}