package com.wedo.spider;

import java.util.ArrayList;
import java.util.List;

import com.wedo.spider.downloader.Downloader;
import com.wedo.spider.pipeline.Pipeline;
import com.wedo.spider.processor.PageProcessor;

/**
 * 运行主类，爬虫的入口。
 * 一个爬虫包含四个部分：下载器/调度器/页面处理器/输出管道
 * @author melody
 *
 */
public class Spider implements Runnable,Task{
	
	// 设置下载器
	protected Downloader downloader;
	// 设置输出 通道
	protected List<Pipeline> pipelines = new ArrayList<Pipeline>();
	// 设置页面处理器
	protected PageProcessor pageProcessor;
	// 设置起始请求
	protected List<Request> startRequests;
	// 设置爬虫站点配置信息
	protected Site site;
	// 
	protected String uuid;
	
	
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
