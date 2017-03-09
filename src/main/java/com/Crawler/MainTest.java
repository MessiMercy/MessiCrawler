package com.Crawler;

import com.downloader.CrawlerLib;
import com.downloader.Downloader;
import com.downloader.Request;
import com.downloader.Response;
import com.pipeline.impl.SimpleFilepipeline;

import java.io.File;

/**
 * Created by Administrator on 2016/10/20.
 */
public class MainTest {
    public static void main(String[] args) {
        Downloader fetch = new Downloader();
        Request request = new Request("http://giphy.com/");
        Response response = fetch.process(request);
        String charset = CrawlerLib.getCharset(response);
//        System.out.printf("charset: %s", charset, "\r\n");
//        System.out.println(request.toString() + response.toString());
        new SimpleFilepipeline(new File("test.txt")).printResult(response.getContent(), false);
    }
}
