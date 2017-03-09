package com.downloader.selenium;

import com.downloader.Request;
import org.openqa.selenium.Cookie;

import java.awt.image.BufferedImage;
import java.util.Set;

public interface Selenium {
	void init();

	void process();

	String getPage(Request request);

	Set<Cookie> getCookies();

	BufferedImage getScreenShots();

	void close();

}
