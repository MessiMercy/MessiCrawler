package com.Login;

import com.downloader.selenium.SeleniumDownloader;
import com.pipeline.impl.SimpleFilepipeline;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.io.File;

/**
 * taobao login use selenium
 * Created by Administrator on 2016/11/17.
 */
public class TaobaoSeleniumLogin extends SeleniumDownloader {

    static Logger logger = Logger.getLogger(TaobaoSeleniumLogin.class);

    static {
        PropertyConfigurator.configure("log4j.properties");
    }

    public static void main(String[] args) {
        TaobaoSeleniumLogin login = new TaobaoSeleniumLogin();
        login.process();
    }

    @Override
    public void process() {
        WebDriver driver = getDriver();
        driver.get("https://login.taobao.com/member/login.jhtml?redirectURL=https%3A%2F%2Fbuyertrade.taobao.com%2Ftrade%2Fitemlist%2Flist_bought_items.htm");
        WebElement userName = findElement(driver, By.cssSelector("#TPL_username_1"));
        userName.sendKeys("18782934825");
        WebElement passWord = findElement(driver, By.cssSelector("#TPL_password_1"));
        passWord.sendKeys("lpyisheng920108");
        WebElement loginButton = findElement(driver, By.cssSelector("#J_SubmitStatic"));
        loginButton.click();
        try {
            Thread.sleep(10 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String html = driver.getPageSource();
        new SimpleFilepipeline(new File("test.txt")).printResult(html, false);
    }
}
