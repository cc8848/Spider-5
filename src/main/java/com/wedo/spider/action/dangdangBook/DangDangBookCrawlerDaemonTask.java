package com.wedo.spider.action.dangdangBook;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;



/**
 * 当当爬虫
 * 
 * 每五分钟执行一次该任务，如果上次执行数据与五分钟后 的数据一致则说明，爬虫线程已经挂掉，则启动新的线程。
 * 
 * @author melody
 *
 */
public class DangDangBookCrawlerDaemonTask extends TimerTask {

	private final static Logger logger = Logger.getLogger(DangDangBookCrawlerDaemonTask.class);

	private Thread spiderThread; // 当前爬虫执行线程
	private Timer timer; // 执行这个任务的定时器对象，因为如果需要启动新的爬虫线程时，则上个线程的定时器则需要报废

	private String currentPageUrl; // 当前页面的URL
	private int currentPageCount; // 当前页面数
	private int currentItemCount; // 当前条目数

	private String lastPageUrl; // 记录的上次页面的URL
	private int lastPageCount; // 记录的上次页面数
	private int lastItemCount; // 记录的上次的条目的数

	private List<Map<String, Object>> saveData = new ArrayList<Map<String, Object>>(); // 需要保存的一份数据

	@Override
	public void run() {

	}

	public void setCurrentPageUrl(String currentPageUrl) {
		this.currentPageUrl = currentPageUrl;
	}

	public void setCurrentPageCount(int currentPageCount) {
		this.currentPageCount = currentPageCount;
	}

	public void setCurrentItemCount(int currentItemCount) {
		this.currentItemCount = currentItemCount;
	}

	public void setLastPageUrl(String lastPageUrl) {
		this.lastPageUrl = lastPageUrl;
	}

	public void setLastPageCount(int lastPageCount) {
		this.lastPageCount = lastPageCount;
	}

	public void setLastItemCount(int lastItemCount) {
		this.lastItemCount = lastItemCount;
	}

}
