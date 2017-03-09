package com.Login;

import com.downloader.selenium.SeleniumDownloader;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class CmccGenSelenium extends SeleniumDownloader {

    private static final Scanner sc = new Scanner(System.in);
    private static final String STARTPAGE = "https://login.10086.cn/login.html?channelID=12003&backUrl=http://shop.10086.cn/i/?f=home";

    public static void main(String[] args) {
        CmccGenSelenium selenium = new CmccGenSelenium();
        selenium.process();
        sc.close();
    }

    @Override
    public void process() {
        WebDriver driver = this.getDriver();
        driver.get(STARTPAGE);
        By pw = By.cssSelector("#p_pwd");
        findElement(driver, By.cssSelector("#p_name"), 10).sendKeys(getProperties().getProperty("phoneNum"));// 输入账号
        findElement(driver, pw, 10).sendKeys(getProperties().getProperty("cmccPw"));// 输入密码
        By sendSms = By.cssSelector("#sms_pwd");
        waitForLoad(By.cssSelector("#getSMSPwd"), 10);
        WebElement smsText = findElement(driver, sendSms, 10);
        if (smsText.isDisplayed()) {
            System.out.println("准备发送验证码");
            driver.findElement(By.cssSelector("#getSMSPwd")).click();
            System.out.println("发送验证码成功，请输入");
            String smsPw = sc.nextLine();
            while (smsPw.equals("0")) {
                driver.findElement(By.cssSelector("#getSMSPwd")).click();// 重新获取
                smsPw = sc.nextLine();
            }
            smsText.sendKeys(smsPw);// 输入验证码按钮
        }
        System.out.println("正在登陆");
        driver.findElement(By.cssSelector("#submit_bt")).click();// 登录按钮
        By mainMenu = By.cssSelector("#stcnavmenu > ul:nth-child(2) > li > a");
        findElement(driver, mainMenu, 10).click();// 进入主菜单
        By secondMenu = By.cssSelector("#stcnavmenu > ul:nth-child(2) > li > ul > li:nth-child(2) > a");
        findElement(driver, secondMenu, 10).click();// 详单查询菜单
        findElement(driver, By.cssSelector("#month1"), 10).click();// 点击第一个月出现二次验证框
        findElement(driver, By.cssSelector("#vec_servpasswd"), 10).sendKeys(getProperties().getProperty("cmccPw"));
        findElement(driver, By.xpath("//*[@id=\"stc-send-sms\"]")).click();// 发送短信
        if (driver instanceof ChromeDriver) {
            alertHandle();
        }
        System.out.println("请输入二次验证码");
        String secondSms = sc.nextLine();
        while (secondSms.equals("0")) {
            findElement(driver, By.xpath("//*[@id=\"stc-send-sms\"]")).click();// 发送短信
            if (driver instanceof ChromeDriver) {
                alertHandle();
            }
            secondSms = sc.nextLine();
        }
        findElement(driver, By.cssSelector("#vec_smspasswd")).sendKeys(secondSms);// 输入二次验证码
        findElement(driver, By.cssSelector("#vecbtn")).click();// 提交二次验证
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        findElement(driver, By.cssSelector("#switch-data > li:nth-child(2) > a")).click();// 查看本月通话详单
        waitForLoad(By.xpath("//*[@id=\"tbody\"]"), 20);
        try {
            FileUtils.writeStringToFile(new File("test.txt"), driver.getPageSource(), false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Set<Cookie> set = driver.manage().getCookies();
        for (Cookie cookie : set) {
            System.out.println(cookie.getName() + ": " + cookie.getValue());
        }
        BufferedImage image = getScreenShots();
        try {
            ImageIO.write(image, "png", new File("temp.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        sc.close();
    }

    public static void login() {
        System.setProperty("phantomjs.binary.path", "E:\\tools\\phantomjs.exe");
        // System.setProperty("webdriver.chrome.driver",
        // "E:\\tools\\chromedriver.exe");
        WebDriver driver = new PhantomJSDriver();
        // WebDriver driver = new ChromeDriver();
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        driver.get("https://login.10086.cn/login.html?channelID=12003&backUrl=http://shop.10086.cn/i/?f=home");
        driver.findElement(By.cssSelector("#p_name")).sendKeys("18782934825");
        driver.findElement(By.cssSelector("#p_pwd")).sendKeys("920108");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        WebElement element = driver.findElement(By.cssSelector("#sms_pwd"));
        driver.findElement(By.cssSelector("#getSMSPwd")).click();
        Scanner sc = new Scanner(System.in);
        System.out.println("请输入验证码");
        String smspw = sc.nextLine();
        element.sendKeys(smspw);
        driver.findElement(By.cssSelector("#submit_bt")).click();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        driver.findElement(By.cssSelector("#stcnavmenu > ul:nth-child(2) > li > a")).click();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        driver.findElement(By.cssSelector("#stcnavmenu > ul:nth-child(2) > li > ul > li:nth-child(2) > a")).click();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        driver.findElement(By.cssSelector("#month1")).click();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        driver.findElement(By.cssSelector("#vec_servpasswd")).sendKeys("920108");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        driver.findElement(By.xpath("//*[@id=\"stc-send-sms\"]")).click();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        skipAlert(driver);
        System.out.println("请输入短信");
        String sms = sc.nextLine();
        if (sms.equals("0")) {
            driver.findElement(By.xpath("//*[@id=\"stc-send-sms\"]")).click();
            sms = sc.nextLine();
            skipAlert(driver);
        }
        driver.findElement(By.cssSelector("#vec_smspasswd")).sendKeys(sms);
        driver.findElement(By.cssSelector("#vecbtn")).click();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        driver.findElement(By.cssSelector("#switch-data > li:nth-child(2) > p")).click();
        try {
            Thread.sleep(10 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            FileUtils.writeStringToFile(new File("test.txt"), driver.getPageSource(), false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Set<Cookie> set = driver.manage().getCookies();
        for (Cookie cookie : set) {
            System.out.println(cookie.getName() + ": " + cookie.getValue());
        }
        sc.close();
    }

    private static void skipAlert(WebDriver driver) {
        try {

            Alert alert1 = driver.switchTo().alert();
            System.out.println(alert1.getText());
            alert1.accept();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
