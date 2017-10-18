package com.wedo.spider.processor;

import com.wedo.spider.Page;
import com.wedo.spider.Site;

/**
 * 页面处理器
 * 	实现自定义获取数据和增加url的业务处理方法
 * @author melody
 *
 */
public interface PageProcessor {

	/**
	 * 页面处理类
	 * @param page
	 */
	public void process(Page page);
	
	/**
	 * 返回爬虫基本设置
	 * @return
	 */
	public Site getSite();
	  
}
