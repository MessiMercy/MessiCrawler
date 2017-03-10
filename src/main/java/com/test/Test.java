package com.test;

import com.downloader.Downloader;
import com.downloader.encrypt.EncryptLib;
import com.google.gson.Gson;
import com.proxy.IPModel;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.WriteException;
import org.apache.commons.io.FileUtils;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.w3c.dom.Document;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Created by Administrator on 2016/10/21.
 */
public class Test {
    private static Reader reader;
    private static SqlSessionFactory factory;
    private static ExecutorService service = Executors.newFixedThreadPool(10);
    private static Downloader dw = new Downloader();
    private static Logger log = Logger.getLogger(Test.class);
    private static JedisPool pool = new JedisPool("127.0.0.1", 6379);

    static {
        PropertyConfigurator.configure("log4j.properties");
        try {
            reader = Resources.getResourceAsReader("Configuration.xml");
            factory = new SqlSessionFactoryBuilder().build(reader);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException, ParseException {
        try {
            String 么么哒 = EncryptLib.aesEncrypt("么么哒", "123456");
            System.out.println(么么哒);
            String s = EncryptLib.aesDecrypt(么么哒, "123456");
            System.out.println(s);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int partition(int[] ints, int left, int right) {
        int key = ints[left];
        while (left < right) {
            while (left < right && ints[right] >= key) {
                right--;
            }
            ints[left] = ints[right];
            while (left < right && ints[left] <= key) {
                left++;
            }
            ints[right] = ints[left];
        }
        ints[left] = key;
        return left;
    }

    public static void quickSort(int[] data, int left, int right) {
        int q = 0;
        if (left < right) {
            q = partition(data, left, right);
            quickSort(data, q + 1, right);
            quickSort(data, left, q - 1);
        }
    }


    static boolean isReachable(InetAddress localInetAddr, InetAddress remoteInetAddr, int
            port, int timeout) {
        boolean isReachable = false;
        Socket socket = null;
        try {
            socket = new Socket(); // 端口号设置为 0 表示在本地挑选一个可用端口进行连接
            SocketAddress localSocketAddr = new InetSocketAddress(localInetAddr, 0);
            socket.bind(localSocketAddr);
            InetSocketAddress endpointSocketAddr = new InetSocketAddress(remoteInetAddr, port);
            socket.connect(endpointSocketAddr, timeout);
            System.out.println("SUCCESS -connection established !Local:" + localInetAddr.getHostAddress() + " remote:"
                    + remoteInetAddr.getHostAddress() + " port" + port);
            isReachable = true;
        } catch (IOException e) {
            System.out.println("FAILRE - CAN not connect! Local: " +
                    localInetAddr.getHostAddress() + " remote: " + remoteInetAddr.getHostAddress() + "port" + port);
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    System.out.println("Error occurred while closing socket..");
                }
            }
        }
        return isReachable;
    }


    public static void availableIp() {
        Jedis jedis = new Jedis("127.0.0.1", 6379);
        Set<String> ip = jedis.sdiff("availableIp");
        jedis.del("availableIp");
        Gson gson = new Gson();
        Set<String> newset = new HashSet<>();
        ip.forEach(p -> {
            IPModel model = gson.fromJson(p, IPModel.class);
            if (model.getLastCheckedTime() != 0) {
                newset.add(p);
            }
        });
        newset.forEach(p -> jedis.sadd("availableIp", p));
        jedis.close();
    }

    public static String futureTest() {
        System.out.println("任务开始，休眠5秒");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("任务结束，返回结果");
        return "accept";
    }


    public static String buildHans(int len) {
        String hans = null;
        StringBuilder result = new StringBuilder();
        try {
            hans = FileUtils.readFileToString(new File("常用汉字.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        int ll = hans.length();
        for (int i = 0; i < len; i++) {
            result.append(hans.charAt(new Random().nextInt(ll)));
        }
        return result.toString();
    }

    /**
     * 生成随机长度中文字符串
     *
     * @param length 字符串长度
     */
    public static String build(int length) {
        char[] captcha = new char[length];
        Random random = new Random();
        if (captcha.length != length) {
            captcha = new char[length];
        }
        for (int i = 0; i < length; i++) {
            captcha[i] = (char) (random.nextInt(0x9fa5 - 19968) + 0x4e00);
//            captcha[i] = (char) (random.nextInt(0x559D - 0x53E3) + 0x53E3);
        }
        return new String(captcha);
    }


    public static <T> T multiReturn(Class<T> cls, String temp) throws IllegalAccessException, InstantiationException {
        return cls.newInstance();
    }

    public static void xpath() throws Exception {
        // 解析文件，生成document对象
        DocumentBuilder builder = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder();
        System.out.println("111111111111111111");
        Document document = builder.parse(new File("C:\\Users\\Administrator\\Desktop\\test.html"));
        System.out.println("222222222222222222");
        // 生成XPath对象
        XPath xpath = XPathFactory.newInstance().newXPath();
        // 获取节点值
        String books = (String) xpath.evaluate("/meta/@content", document,
                XPathConstants.STRING);
        System.out.println("333333333333333333333");
    }

    public static void excelProcesser() throws IOException, WriteException, BiffException {
        File file = new File("D:\\Tencent\\QQdata\\408708006\\FileRecv\\downjoy.xlsx");
        System.out.println(file.exists());
        FileInputStream in = new FileInputStream(file);
        System.out.println(in);
        Workbook book = Workbook.getWorkbook(file);
        Sheet sheet = book.getSheet(0);
        Cell cell = sheet.getCell(10, 10);
        System.out.println(cell.getContents());
    }

    public static String firstUpperCase(String str) {
        if (str != null && str != "") {
            str = str.substring(0, 1).toUpperCase() + str.substring(1);
        }
        return str;
    }

    public static String getEncodedPw(String pw, String account) {
        String encodedPw = "";
        ScriptEngineManager sem = new ScriptEngineManager();
        ScriptEngine engine = sem.getEngineByName("javascript");
        System.out.println(engine == null);
        try {
            engine.eval(new FileReader(new File("xima.js")));
        } catch (FileNotFoundException | ScriptException e) {
            e.printStackTrace();
        }
        if (engine instanceof Invocable) {
            Invocable in = (Invocable) engine;
            try {
                encodedPw = in.invokeFunction("get_pass", pw, account).toString();
                // get_pass方法为自己写的，根据加密方式，自行添加方法传入相关参数并将加密后的pw取出
            } catch (NoSuchMethodException | ScriptException e) {
                e.printStackTrace();
            }
        }
        return encodedPw;
    }

}
