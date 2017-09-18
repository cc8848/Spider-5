package com.wedo.spider.action.dangdangBook;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 当当书本爬虫 页面爬去类
 * 
 * @author melody
 *
 */
public class DangDangBookPageRunner implements Runnable {

	private final static Logger logger = LoggerFactory.getLogger(DangDangBookPageRunner.class);
	// phantomjs 参数设置
	private final static DesiredCapabilities dcaps = new DesiredCapabilities();

	private String startPage; // 爬取开始页面

	static {
		System.setProperty("webdriver.gecko.driver", "/home/melody/devOpt/geckodriver");
		// ssl证书支持
		dcaps.setCapability("acceptSslCerts", true);
		// 截屏支持
		dcaps.setCapability("takesScreenshot", true);
		// css搜索支持
		dcaps.setCapability("cssSelectorsEnabled", true);
		// 关闭图片加载 慎关闭 加载变快 容易被禁
		dcaps.setCapability("phantomjs.page.settings.loadImages", true);
		// dcaps.setCapability("phantomjs.page.settings.userAgent", "Mozilla/5.0
		// (Windows NT 10.0; Win64; x64; rv:54.0) Gecko/20100101 Firefox/54.0");
		// dcaps.setCapability("phantomjs.page.customHeaders.User-Agent",
		// "Mozilla/5.0
		// (Windows NT 10.0; Win64; x64; rv:54.0) Gecko/20100101 Firefox/54.0");

		// js支持
		dcaps.setJavascriptEnabled(true);
		// 驱动支持
		dcaps.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY,
				"/home/melody/devOpt/phantomjs-2.1.1-linux-x86_64/bin/phantomjs");
	}

	@Override
	public void run() {
		// 判断是否为恢复页面

		PhantomJSDriver driver = new PhantomJSDriver(dcaps);
		driver.get(startPage);

		WebElement ul = driver.findElement(By.id("component_0__0__6612"));
		List<WebElement> findElements = ul.findElements(By.tagName("li"));
		List<Map<String, Object>> liData = new ArrayList<Map<String, Object>>();
		boolean next = true;
		int page = 0;
		while (next) {
			page++;
			int item = 1;
			for (Iterator iterator = findElements.iterator(); iterator.hasNext();) {
				Map<String, Object> dataMap = new HashMap<String, Object>();

				WebElement webElement = (WebElement) iterator.next();
				WebElement aTag = webElement.findElement(By.className("name")).findElement(By.tagName("a"));

				// 进入数据页面
				// System.out.println(aTag.getAttribute("title"));
				// WebDriver webdriver2 = new FirefoxDriver();
				PhantomJSDriver webdriver2 = new PhantomJSDriver(dcaps);
				webdriver2.get(aTag.getAttribute("href"));
				// 等待breadcrumb元素出现，如果没有则返回
				boolean isExited = waitForElement(By.id("breadcrumb"), webdriver2);
				// 如果元素不存在 则返回
				if (!isExited) {
					continue;
				}
				System.out.println("第 " + page + "页 --> " + item + " 条 " + "  " + item + "/" + findElements.size());
				WebElement breadcrumb = webdriver2.findElement(By.id("breadcrumb"));
				Document breadcrumbEle = Jsoup.parse(breadcrumb.getText());
				String rawText = breadcrumbEle.text();
				System.out.println(rawText);
				dataMap.put("tagLine", rawText);

				WebElement name_info = webdriver2.findElement(By.className("name_info"));
				Document name_infoEle = Jsoup.parse(name_info.getText());
				String name_info_Txt = name_infoEle.text();
				System.out.println(name_info_Txt);
				dataMap.put("nameInfo", name_info_Txt);

				WebElement messbox_info = webdriver2.findElement(By.className("messbox_info"));
				Document messbox_infoEle = Jsoup.parse(messbox_info.getText());
				String messbox_info_Txt = messbox_infoEle.text();
				System.out.println(messbox_info_Txt);
				dataMap.put("messbox_info", messbox_info_Txt);

				WebElement price_info = webdriver2.findElement(By.className("price_info"));
				Document price_infoEle = Jsoup.parse(price_info.getText());
				String price_infoEle_Txt = price_infoEle.text();
				System.out.println(price_infoEle_Txt);
				dataMap.put("price_info", price_infoEle_Txt);
				try {
					webdriver2.quit();
				} catch (Exception e) {
				}
				liData.add(dataMap);
				item++;
			}
			WebElement nextEle = driver.findElement(By.className("paging")).findElement(By.className("next"));
			if (nextEle != null) {
				next = true;
				// 点击进入下一页
				nextEle.findElement(By.tagName("a")).click();
				// 等待下一页面出现
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				ul = driver.findElement(By.id("component_0__0__6612"));
				findElements = ul.findElements(By.tagName("li"));
			} else {
				break;
			}
		}
		try {
			FileUtils.writeLines(new File("/home/melody/hhhzzz.txt"), liData);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 判断该元素在30秒内是否出现，是：返回true， 否：返回 false
	 * 
	 * @param elementLocator
	 * @param driver
	 * @return
	 */
	private boolean waitForElement(final By elementLocator, WebDriver driver) {
		try {
			WebDriverWait driverWait = (WebDriverWait) new WebDriverWait(driver, 30, 500)
					.ignoring(StaleElementReferenceException.class).withMessage("元素在30秒内没有出现!");
			return driverWait.until(new ExpectedCondition<Boolean>() {
				public Boolean apply(WebDriver driver) {
					try {
						if (driver.findElement(elementLocator).isDisplayed()) {
							return false;
						}
					} catch (IndexOutOfBoundsException e) {
					} catch (NoSuchElementException e) {
					}
					return true;
				}
			});
		} catch (Exception e) {
			return false;
		}
	}

}
