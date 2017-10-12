package com.wedo.spider.proxy;

import com.wedo.spider.Page;
import com.wedo.spider.Task;

import us.codecraft.webmagic.proxy.Proxy;

/**
 * 代理 提供
 * 
 * @author melody
 *
 */
public interface ProxyProvider {

	/**
	 * 下载时 提供一个代理
	 * 
	 * @param proxy
	 * @param page
	 * @param task
	 */
	void returnProxy(Proxy proxy, Page page, Task task);

	/**
	 * 通过一些策略获得任务代理
	 * @param task
	 * @return
	 */
	Proxy getProxy(Task task);

}
