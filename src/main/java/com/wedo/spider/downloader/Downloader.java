package com.wedo.spider.downloader;

import com.wedo.spider.Page;
import com.wedo.spider.Request;
import com.wedo.spider.Task;

/**
 * 下载器。下载web页面后将存储在Page对象中。 通常，Downloader会有一个控制线程的方法setThread()，因为下载器通常是爬虫的瓶颈，
 * 这儿会有一个线程池存在与下载器中，池的大小与线程数量相关。
 * 
 * @author melody
 *
 */
public interface Downloader {

	/**
	 * 使用给定的request下载页面，并返回Page
	 * 
	 * @param request
	 * @param task
	 * @return
	 */
	public Page download(Request request, Task task);

	/**
	 * 告诉下载器，爬虫用了多少个线程
	 */
	public void setThread(int threadNum);
}
