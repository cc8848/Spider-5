package com.wedo.spider.action.music163;

import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;

public class BySeleniumDemo {

	public static void main(String[] args) {

		String urlTmp = "http://music.163.com/#/song?id=274686";

		System.setProperty("webdriver.gecko.driver", "/home/melody/devOpt/geckodriver");

		DesiredCapabilities dcaps = new DesiredCapabilities();
		// ssl证书支持
		dcaps.setCapability("acceptSslCerts", true);
		// 截屏支持
		dcaps.setCapability("takesScreenshot", true);
		// css搜索支持
		dcaps.setCapability("cssSelectorsEnabled", true);
		// 关闭图片加载 慎关闭 加载变快 容易被禁
		dcaps.setCapability("phantomjs.page.settings.loadImages", false);
		dcaps.setCapability("phantomjs.page.settings.userAgent",
				"Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:54.0) Gecko/20100101 Firefox/54.0");
		dcaps.setCapability("phantomjs.page.customHeaders.User-Agent",
				"Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:54.0) Gecko/20100101 Firefox/54.0");
		// js支持
		// dcaps.setJavascriptEnabled(true);
		// 驱动支持
		dcaps.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY,
				"/home/melody/devOpt/phantomjs-2.1.1-linux-x86_64/bin/phantomjs");
		// PhantomJSDriver driver = new PhantomJSDriver(dcaps);
		FirefoxDriver driver = new FirefoxDriver();

		driver.get(urlTmp);

		String profile = driver.PROFILE;
	}
}
