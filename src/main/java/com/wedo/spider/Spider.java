package com.wedo.spider;

import com.wedo.spider.downloader.Downloader;

/**
 * 运行主类，爬虫的入口。
 * 一个爬虫包含四个部分：下载器/调度器/页面处理器/输出管道
 * @author melody
 *
 */
public class Spider implements Runnable,Task{

	protected Downloader downloader;
	
	@Override
	public void run() {
		
	}

	@Override
	public String getUUID() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Site getSite() {
		// TODO Auto-generated method stub
		return null;
	}

}
