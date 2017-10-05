package com.wedo.spider.processor;

import com.wedo.spider.Page;

/**
 * 页面处理器
 * 	实现自定义获取数据和增加url的业务处理方法
 * @author melody
 *
 */
public interface PageProcessor {

	public void process(Page page);
}
