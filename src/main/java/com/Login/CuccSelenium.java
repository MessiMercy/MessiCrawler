package com.Login;

import com.downloader.CrawlerLib;
import com.downloader.selenium.SeleniumDownloader;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Created by Administrator on 2016/10/24.
 */
public class CuccSelenium extends SeleniumDownloader {
    private String phoneNum;
    private String pw;

    CuccSelenium(String phoneNum, String pw) {
        super();
        this.phoneNum = phoneNum;
        this.pw = pw;
    }

    public static void main(String[] args) {
        CuccSelenium cucc = new CuccSelenium("18615711454", "549527");
        cucc.process();
    }


    @Override
    public void process() {
        WebDriver driver = this.getDriver();
        driver.get("http://iservice.10010.com/e3/query/call_dan.html");
        WebElement element = findElement(driver, By.cssSelector("#login_iframe"));
        driver.switchTo().frame(element);
        findElement(driver, By.xpath("//*[@id=\"userName\"]")).sendKeys(this.phoneNum);
        findElement(driver, By.xpath("//*[@id=\"userPwd\"]")).sendKeys(this.pw);
        findElement(driver, By.xpath("//*[@id=\"login1\"]")).click();
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        CrawlerLib.printResult(driver.getPageSource(), false);
    }
}
