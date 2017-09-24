package com.wedo.spider.action.dangdangBook;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Timer;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
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

import com.alibaba.fastjson.JSON;

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
	private String dataOutPutPath; // 数据输出页面
	private String phantomjsPath; // phantomjs路径

	private int recoverStartPage = -1; // 恢复线程起始页
	private int recoverStartItem = -1; // 恢复线程起始条目
	private String revoerStartPageUrl = ""; // 恢复页面的URL，用作标记为该线程是否为恢复线程，同时作为该线程的起始页
	// 当前存储所有数据的对象
	private List<String> listData = new ArrayList<String>();
	static {
		// firfoxDriver 驱动支持
		System.setProperty("webdriver.gecko.driver", "/home/melody/devOpt/geckodriver");
		// ssl证书支持
		dcaps.setCapability("acceptSslCerts", true);
		// 截屏支持
		dcaps.setCapability("takesScreenshot", true);
		// css搜索支持
		dcaps.setCapability("cssSelectorsEnabled", true);
		// 关闭图片加载 慎关闭 加载变快 容易被禁
		dcaps.setCapability("phantomjs.page.settings.loadImages", false);
		// dcaps.setCapability("phantomjs.page.settings.userAgent", "Mozilla/5.0
		// (Windows NT 10.0; Win64; x64; rv:54.0) Gecko/20100101 Firefox/54.0");
		// dcaps.setCapability("phantomjs.page.customHeaders.User-Agent",
		// "Mozilla/5.0
		// (Windows NT 10.0; Win64; x64; rv:54.0) Gecko/20100101 Firefox/54.0");
		// js支持
		dcaps.setJavascriptEnabled(true);

	}

	public DangDangBookPageRunner(String startPage, String dataOutPutPath, String phantomjsPath) {
		this.startPage = startPage;
		this.dataOutPutPath = dataOutPutPath;
		this.phantomjsPath = phantomjsPath;
		// 驱动支持
		dcaps.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, phantomjsPath);
		// 驱动支持
		// dcaps.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY,"/home/melody/devOpt/phantomjs-2.1.1-linux-x86_64/bin/phantomjs");
	}

	@Override
	public void run() {
		// 判断当前线程是否为恢复线程
		if (!StringUtils.isBlank(revoerStartPageUrl)) {
			logger.info("开启恢复线程,恢复页面URL: " + revoerStartPageUrl);
		} else {
			logger.info("开启一个新的爬取线程，爬取URL ： " + startPage);
		}

		// 开启守护线程
		DangDangBookCrawlerDaemonTask task = new DangDangBookCrawlerDaemonTask(dataOutPutPath, phantomjsPath);
		Timer timer = new Timer();
		task.setTimer(timer); // 开启新线程后 就线程中的定时任务需要关闭
		timer.schedule(task, 1000 * 10 * 60, 1000 * 60 * 5); // 线程开启后10分钟启动任务，每隔五分钟执行一次
		logger.info("生成守护线程：" + revoerStartPageUrl);

		PhantomJSDriver driver = new PhantomJSDriver(dcaps);
		// 生成Driver
		if (!StringUtils.isBlank(revoerStartPageUrl)) {
			driver.get(revoerStartPageUrl);
			task.getSaveData().addAll(listData);
		} else {
			driver.get(startPage);
		}
		// 设置恢复页面
		task.setRecoverPageUrl(startPage);
		task.setDriver(driver);
		// 设置恢复项
		task.setCurrentItemCount(recoverStartItem);		// 防止断网后无法确定爬取数据项
		task.setCurrentPageCount(recoverStartPage);		// 防止断网后无法确定爬取数据页面
		
		WebElement ul = driver.findElement(By.id("component_0__0__6612"));
		List<WebElement> findElements = ul.findElements(By.tagName("li"));
		boolean next = true;
		boolean recoverFlag = true; // 线程恢复标志 表示线程恢复后 进入到下一页后 不需要再判断recoverItem
		int pageNum = 1; // 无论是恢复线程还是非恢复线程，初始爬取页面编号都为1
		int item = 1;
		
		while (next) {
			item = 1;
			// 只有当开启pageNum大于恢复页面Num时才进入爬取列表
			for (int i = 0; pageNum >= recoverStartPage && i < findElements.size(); i++) {
				// 判断是否为恢复线程，并找到需要抓取的那一条数据
				if (item < recoverStartItem && recoverFlag) {
					item++;
					continue;
				} else {
					recoverFlag = false;
				}
				// 设置守护线程判断参数
				task.setCurrentItemCount(item);
				task.setCurrentPageCount(pageNum);

				Map<String, Object> dataMap = new HashMap<String, Object>();
				WebElement webElement = (WebElement) findElements.get(i);
				WebElement aTag = webElement.findElement(By.className("name")).findElement(By.tagName("a"));

				// 进入数据页面
				// System.out.println(aTag.getAttribute("title"));
				// WebDriver webdriver2 = new FirefoxDriver();
				PhantomJSDriver webdriver2 = new PhantomJSDriver(dcaps);
				webdriver2.get(aTag.getAttribute("href"));
				// 等待breadcrumb元素出现，如果没有则返回
//				boolean isExited = waitForElement(By.id("breadcrumb"), webdriver2);
				// 如果元素不存在 则返回
//				if (!isExited) {
//					continue;
//				}
				// System.out.println("第 " + page + "页 --> " + item + " 条 " + " " + item + "/" +
				// findElements.size());
				WebElement breadcrumb = webdriver2.findElement(By.id("breadcrumb"));
				if(breadcrumb == null) {			// 如果分类数据为空，则舍弃这条数据
					item++;
					continue;
				}
				logger.info("抓取第 " + pageNum + "页 --> " + item + " 条 " + "  " + item + "/" + findElements.size());
				Document breadcrumbEle = Jsoup.parse(breadcrumb.getText());
				String rawText = breadcrumbEle.text();
				// System.out.println(rawText);
				logger.info(webdriver2.getCurrentUrl());
				logger.info(rawText);
				dataMap.put("tagLine", rawText);

				WebElement name_info = webdriver2.findElement(By.className("name_info"));
				Document name_infoEle = Jsoup.parse(name_info.getText());
				String name_info_Txt = name_infoEle.text();
				// System.out.println(name_info_Txt);
				logger.info(name_info_Txt);
				dataMap.put("nameInfo", name_info_Txt);

				WebElement messbox_info = webdriver2.findElement(By.className("messbox_info"));
				Document messbox_infoEle = Jsoup.parse(messbox_info.getText());
				String messbox_info_Txt = messbox_infoEle.text();
				// System.out.println(messbox_info_Txt);
				logger.info(messbox_info_Txt);
				dataMap.put("messbox_info", messbox_info_Txt);

				WebElement price_info = webdriver2.findElement(By.className("price_info"));
				Document price_infoEle = Jsoup.parse(price_info.getText());
				String price_infoEle_Txt = price_infoEle.text();
				// System.out.println(price_infoEle_Txt);
				logger.info(price_infoEle_Txt);
				dataMap.put("price_info", price_infoEle_Txt);
				try {
					webdriver2.quit();
				} catch (Exception e) {
				}
				String jsonData = JSON.toJSONString(dataMap);
				listData.add(jsonData);
				task.getSaveData().add(jsonData);	// 保存一份数据
				// 条目数增加
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
				// 测试输出数据
//				try {
//					FileUtils.writeLines(new File("/home/melody/hhhzzz.txt"), listData);
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
				logger.info("进入到下一页");
				ul = driver.findElement(By.id("component_0__0__6612"));
				findElements = ul.findElements(By.tagName("li"));
			} else {
				// 最后一页 运行终止
				try {
					timer.cancel();
					driver.quit();
				} catch (Exception e) {
				}
				break;
			}
			// 进入下一个页面 编号加1
			pageNum++;
		}

		try {
			FileUtils.writeLines(new File("/home/melody/hhhzzz.txt"), listData);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// 关闭相关资源

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

	public void setRecoverStartPage(int recoverStartPage) {
		this.recoverStartPage = recoverStartPage;
	}

	public void setRecoverStartItem(int recoverStartItem) {
		this.recoverStartItem = recoverStartItem;
	}

	public void setRevoerStartPageUrl(String revoerStartPageUrl) {
		this.revoerStartPageUrl = revoerStartPageUrl;
	}

	public List<String> getListData() {
		return listData;
	}

}
