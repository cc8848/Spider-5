package com.wedo.spider.action.dangdangBook;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 当当书籍爬虫
 * 
 * 每五分钟执行一次该任务，如果上次执行数据与五分钟后 的数据一致则说明，爬虫线程已经挂掉，则启动新的线程。
 * 
 * @author melody
 *
 */
public class DangDangBookCrawlerDaemonTask extends TimerTask {

	private final static Logger logger = LoggerFactory.getLogger(DangDangBookCrawlerDaemonTask.class);

	// private Thread spiderThread; // 当前爬虫执行线程

	private Timer timer; // 执行这个任务的定时器对象，因为如果需要启动新的爬虫线程时，则上个线程的定时器则需要报废
	private String recoverPageUrl; // 恢复页面 也是线程的开启页面

	private int currentPageCount; // 当前页面数
	private int currentItemCount; // 当前条目数

	private int lastPageCount; // 记录的上次页面数
	private int lastItemCount; // 记录的上次的条目的数

	private String dataOutPutPath; // 数据输出路径
	private String phantomjsPath; // phantomjs路径

	private PhantomJSDriver driver; // 当前线程的driver

	private List<Map<String, Object>> saveData = new ArrayList<Map<String, Object>>(); // 需要保存的一份数据

	public DangDangBookCrawlerDaemonTask(String dataOutPutPath, String phantomjsPath) {
		this.dataOutPutPath = dataOutPutPath;
		this.phantomjsPath = phantomjsPath;
	}

	@Override
	public void run() {
		// 如果当前数据还与指定的时间段前数据一样则重启该线程
		if (currentItemCount == lastItemCount && currentPageCount == lastPageCount) {
			// 关闭当前浏览器
			if (driver != null) {
				try {
					driver.close();
					logger.info("关闭报废的爬虫线程,URL: " + driver.getCurrentUrl());
				} catch (Exception e) {
				}
			}
			// 开启新线程
			DangDangBookPageRunner runner = new DangDangBookPageRunner(recoverPageUrl, dataOutPutPath, phantomjsPath);
			runner.setRevoerStartPageUrl(recoverPageUrl);
			runner.setRecoverStartPage(currentPageCount);
			runner.setRecoverStartItem(currentItemCount);
			logger.info("关闭报废线程后，生成新的爬虫线程。");
			logger.info("将恢复线程中数据导入到生成的新线程中，大小为" + saveData.size() + "条");
			runner.getListData().addAll(saveData);
			new Thread(runner).start();
			// 关闭 timer
			try {
				if (timer != null)
					timer.cancel();
			} catch (Exception e) {
			}
		} else {
			// 保存当前数据
			lastPageCount = currentPageCount;
			lastItemCount = currentItemCount;
		}
	}

	public List<Map<String, Object>> getSaveData() {
		return saveData;
	}

	public void setDriver(PhantomJSDriver driver) {
		this.driver = driver;
	}

	public void setRecoverPageUrl(String recoverPageUrl) {
		this.recoverPageUrl = recoverPageUrl;
	}

	public void setTimer(Timer timer) {
		this.timer = timer;
	}

	public void setCurrentPageCount(int currentPageCount) {
		this.currentPageCount = currentPageCount;
	}

	public void setCurrentItemCount(int currentItemCount) {
		this.currentItemCount = currentItemCount;
	}

}
