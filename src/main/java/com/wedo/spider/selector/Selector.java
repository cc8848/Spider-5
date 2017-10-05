package com.wedo.spider.selector;

import java.util.List;

/**
 * 
 * 用于抽取文本的 选择器
 * 
 * @author melody
 *
 */
public interface Selector {

	/**
	 * 从文本中抽取一个结果
	 * @param text
	 * @return
	 */
	public String select(String text);

	/**
	 * 抽取文本中的所有结果
	 * @param text
	 * @return
	 */
	public  List<String> selectList(String text);


}
