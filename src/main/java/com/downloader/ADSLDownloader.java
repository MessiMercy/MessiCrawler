package com.downloader;

import com.google.gson.Gson;
import com.test.GlobalLog;
import com.test.RabbitMqClientReceiver;
import com.test.RabbitMqClientSender;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeoutException;

/**
 * Created by Administrator on 2017/3/29.
 */
public class ADSLDownloader {
    private RabbitMqClientSender sender;
    private Gson gson = new Gson();
    private RabbitMqClientReceiver receiver;
    private ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<>(100);

    public ADSLDownloader(String projectName) {
        try {
            recieve(projectName, queue);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    {
        try {
            sender = new RabbitMqClientSender();
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    public void send(Request request) throws InterruptedException, TimeoutException, IOException {
        String json = gson.toJson(request);
        sender.send(json);
    }

    public String get() throws InterruptedException {
        return queue.take();
    }

    public void recieve(String projectName, ArrayBlockingQueue<String> queue) throws IOException, TimeoutException {
        receiver = new RabbitMqClientReceiver(projectName) {

            @Override
            public void handle(byte[] body) {
                try {
                    String res = new String(body, "utf-8");
                    GlobalLog.log.info("recieve : " + res);
                    queue.offer(res);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    GlobalLog.log.error(e.getMessage());
                }
            }
        };
        receiver.startReceive();
    }

    public void close() {
        try {
            this.sender.close();
            this.receiver.close();
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    public ArrayBlockingQueue<String> getQueue() {
        return queue;
    }

    public void setQueue(ArrayBlockingQueue<String> queue) {
        this.queue = queue;
    }
}
