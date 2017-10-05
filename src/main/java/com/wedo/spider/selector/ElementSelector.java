package com.wedo.spider.selector;

import java.util.List;

import org.jsoup.nodes.Element;

/**
 * 页面元素选择器（抽取器）
 * 
 * @author melody
 *
 */
public interface ElementSelector {

	/**
	 * 抽取文本的一个结果
	 * 
	 * @param element
	 * @return
	 */
	public String select(Element element);

	/**
	 * 抽取文本的所有的结果
	 * 
	 * @param element
	 * @return
	 */
	public List<String> selectList(Element element);

}
