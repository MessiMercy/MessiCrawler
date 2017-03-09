package com.downloader.selenium;

import com.downloader.Request;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Set;

public abstract class SeleniumDownloader implements Selenium {
    private final static String propertyPath = "me.properties";
    private String driverPath;
    private WebDriver driver;
    private DesiredCapabilities capabilities = new DesiredCapabilities();
    private Properties properties = new Properties();

    public SeleniumDownloader() {
        init();
        if (getDriverPath().contains("chrome")) {
            System.setProperty(ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY, driverPath);
            driver = new ChromeDriver(capabilities);
        } else {
            String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.59 Safari/537.36";
            System.setProperty("phantomjs.page.settings.userAgent", userAgent);
            System.setProperty(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, driverPath);
            capabilities.setJavascriptEnabled(true);
            driver = new PhantomJSDriver(capabilities);
        }
    }

    @Override
    public void init() {
        FileInputStream in = null;
        File propertyFile = new File(propertyPath);
        try {
            if (!propertyFile.exists()) {
                propertyFile.createNewFile();
                System.out.println("请在me.properties中设置driver的路径");
                return;
            }
            in = FileUtils.openInputStream(propertyFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            this.properties.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (properties.containsKey("driverPath")) {
            setDriverPath(properties.getProperty("driverPath"));
        }
        ArrayList<String> cliArgsCap = new ArrayList<String>();
        cliArgsCap.add("--web-security=false");
        cliArgsCap.add("--ssl-protocol=any");
        cliArgsCap.add("--ignore-ssl-errors=true");
        if (getDriverPath().contains("phantom")) {
            capabilities.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, cliArgsCap);
        }

    }

    @Override
    public abstract void process();

    @Override
    public String getPage(Request request) {
        driver.get(request.getUrl());
        return driver.getPageSource();
    }

    @Override
    public Set<Cookie> getCookies() {
        return driver.manage().getCookies();
    }

    @Override
    public BufferedImage getScreenShots() {
        RemoteWebDriver remoteWebDriver = (RemoteWebDriver) driver;
        File temp = remoteWebDriver.getScreenshotAs(OutputType.FILE);
        BufferedImage image = null;
        try {
            image = ImageIO.read(temp);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    /**
     * 让当前webdriver等待by的元素加载
     */
    public void waitForLoad(By by, int timeout) {
        WebDriverWait wait = new WebDriverWait(driver, timeout);
        wait.until(ExpectedConditions.visibilityOfElementLocated(by));
    }

    /**
     * 等待元素加载之后再读取元素
     */
    public WebElement findElement(WebDriver driver, By by, int timeout) {
        try {
            waitForLoad(by, timeout);
            return driver.findElement(by);
        } catch (Exception e) {
            try {
                System.out.println("寻找元素时发生错误，请去error目录下找到错误截图");
                ImageIO.write(getScreenShots(), "png", new File("error/error.png"));
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
            System.exit(0);
            return null;
        }
    }

    /**
     * 默认等待元素加载时间为10秒
     */
    public WebElement findElement(WebDriver driver, By by) {
        return findElement(driver, by, 10);
    }

    public void alertHandle() {
        WebDriverWait wait = new WebDriverWait(getDriver(), 10);
        try {
            Alert alert = wait.until(new ExpectedCondition<Alert>() {

                @Override
                public Alert apply(WebDriver driver) {
                    try {
                        return driver.switchTo().alert();
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            });
            alert.accept();
        } catch (Exception e) {
            try {
                ImageIO.write(getScreenShots(), "jpg", new File("temp.jpg"));
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    public String getDriverPath() {
        return driverPath;
    }

    public void setDriverPath(String driverPath) {
        this.driverPath = driverPath;
    }

    public WebDriver getDriver() {
        return driver;
    }

    public void setDriver(WebDriver driver) {
        this.driver = driver;
    }

    public DesiredCapabilities getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(DesiredCapabilities capabilities) {
        this.capabilities = capabilities;
    }

    @Override
    public void close() {
        this.driver.close();
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

}
