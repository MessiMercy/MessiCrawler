package com.downloader.thread;

import com.downloader.Downloader;
import com.downloader.HttpConstant;
import com.downloader.Request;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Administrator on 2016/12/29.
 */
public class MultiDownload {
    private static ExecutorService service;
    private Request request;
    private Downloader downloader;
    private int threadNum;

    MultiDownload(Downloader downloader, Request request, int threadNum) {
        service = Executors.newFixedThreadPool(threadNum);
        this.request = request;
        this.downloader = downloader;
        this.threadNum = threadNum;
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Downloader downloader = new Downloader(HttpConstant.UserAgent.CHROME);
        Request request = new Request("http://down.androidgame-store.com/201612291355/4AC763328FB3CEB74650568648312A8C/new/game1/61/34661/lmybl_1482902536854.apk?f=web_1");
        request.addHeader(HttpConstant.Header.REFERER, "http://android.d.cn/game/76154.html");
        MultiDownload download = new MultiDownload(downloader, request, 10);
        download.download();
    }

    /**
     * @return 如果该请求支持断点续传，则返回文件length。否则返回-1
     */
    private long isRangeable(Downloader downloader, Request request) {
        request.addHeader("Range", "bytes=0-10");
        HttpResponse response = downloader.downloadEntity(request);
        if (response.getStatusLine().getStatusCode() == 206) {
            String rangeValue = response.getFirstHeader("Content-Range").getValue();
            String length = StringUtils.substringAfter(rangeValue, "/");
            return Long.parseLong(length);
        } else return -1;
    }

    private Request[] splitRequest(Request request, long length, int threads) throws IOException, ClassNotFoundException {
        Request[] requests = new Request[threads];
        long pcsLength = length / threads;
        for (int i = 0; i < requests.length; i++) {
            requests[i] = (Request) request.deepClone();
            if (i == threads - 1) {
                requests[i].addHeader("Range", String.format("bytes=%d-%d", pcsLength * i, length));
            } else {
                requests[i].addHeader("Range", String.format("bytes=%d-%d", pcsLength * i, pcsLength * (i + 1) - 1));
            }
        }
        return requests;
    }

    /**
     * 多线程下载文件
     */
    public void download() throws IOException, ClassNotFoundException {
        long rangeable = isRangeable(downloader, request);
        if (rangeable == -1) {
            HttpResponse response = downloader.downloadEntity(request);
            Files.copy(response.getEntity().getContent(), Paths.get(getFileName(request.getUrl())), StandardCopyOption.REPLACE_EXISTING);
            return;
        }
        System.out.println("检测到可多线下载！");
        Request[] requests = splitRequest(request, rangeable, threadNum);
        List<Future<File>> taskList = new ArrayList<>();
//        final int[] part = {0};
        AtomicInteger part = new AtomicInteger(0);
        for (Request smallRequst : requests) {
            taskList.add(service.submit(() -> {
                HttpResponse response = downloader.downloadEntity(smallRequst);
                String fileName = getFileName(request.getUrl()) + "_part_" + part.getAndIncrement();
                try {
                    Files.copy(response.getEntity().getContent(), Paths.get(fileName), StandardCopyOption.REPLACE_EXISTING);
                } catch (Exception e) {
                    Files.copy(response.getEntity().getContent(), Paths.get(fileName), StandardCopyOption.REPLACE_EXISTING);
                }
                return new File(fileName);
            }));
        }
        File[] files = new File[requests.length];
        final int[] i = {0};
        taskList.forEach(p -> {
            try {
                files[i[0]++] = p.get(10, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                e.printStackTrace();
            }
        });
        System.out.println("下载完成,准备组合！");
        combine(new File(getFileName(request.getUrl())), files);
        System.out.println("组合完成,准备删除临时文件！");
        Arrays.stream(files).forEach(File::deleteOnExit);
        System.out.println("下载完成！");
        if (!service.isShutdown()) {
            service.shutdown();
        }
    }

    private String getFileName(String url) {
        if (url == null) {
            return null;
        }
        final int start = url.lastIndexOf("/");
        if (start != -1) {
            final int end = url.indexOf("?", start + 1);
            if (end != -1) {
                return url.substring(start + 1, end);
            }
        }
        return null;
    }


    private void combine(File out, File... files) throws IOException {
        if (!out.exists()) out.createNewFile();
        FileOutputStream fileOutputStream = new FileOutputStream(out);
        for (File file : files) {
            if (!file.exists()) continue;
            FileInputStream inputStream = new FileInputStream(file);
            copy(inputStream, fileOutputStream, 0, file.length());
            inputStream.close();
        }
        fileOutputStream.close();
    }

    private long copy(InputStream source, OutputStream sink, long start, long length)
            throws IOException {
        long nread = 0L;
//        byte[] buf = new byte[1];
        source.skip(start);
        int n;
        while ((n = source.read()) != -1) {
            nread++;
            if (nread <= length) {
//                sink.write(buf, 0, n);
                sink.write(n);
            } else {
                break;
            }
        }
        return nread;
    }
}
