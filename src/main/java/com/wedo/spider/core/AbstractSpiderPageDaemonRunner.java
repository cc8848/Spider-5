package com.wedo.spider.core;

/**
 * 页面 守护线程 抽象类
 * @author melody
 *
 */
public abstract class AbstractSpiderPageDaemonRunner implements SpiderPageDaemonRunnable{

	// 备份页面URL 也是恢复爬虫线程启动的登录URL
	protected String bakPageUrl;

	
	public String getBakPageUrl() {
		return bakPageUrl;
	}

	public void setBakPageUrl(String bakPageUrl) {
		this.bakPageUrl = bakPageUrl;
	}
	
	
	
	
}
