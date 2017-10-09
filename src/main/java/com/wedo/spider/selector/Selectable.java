package com.wedo.spider.selector;

import java.util.List;


/**
 * 可用于解析的，用于选择的文本
 * 
 * @author melody
 *
 */
public interface Selectable {

	/**
	 * 使用xpath 选择数据
	 * 
	 * @param xpath
	 * @return
	 */
	public Selectable xpath(String xpath);

	/**
	 * css 选择器
	 * 
	 * @param selector
	 * @return
	 */
	public Selectable $(String selector);

	/**
	 * css 选择器
	 * 
	 * @param selector
	 * @return
	 */
	public Selectable $(String selector, String attrName);

	/**
	 * css 选择器
	 * 
	 * @param selector
	 * @return
	 */
	public Selectable css(String selector);

	public Selectable css(String selector, String attrName);

	/**
	 * 智能选择内容
	 * 
	 * @return
	 */
	public Selectable smartContent();

	/**
	 * 返回所有的 链接 <a>
	 * 
	 * @return
	 */
	public Selectable links();

	/**
	 * 正则 匹配 返回Group
	 * 
	 * @param regex
	 * @return
	 */
	public Selectable regex(String regex);

	public Selectable regex(String regex, int group);

	
	public Selectable replace(String regex, String replacement);

	/**
	 * 返回 选择器结果
	 * 
	 * @return
	 */
	public String toString();

	public String get();

	/**
	 * 是否存在结果
	 * 
	 * @return
	 */
	public boolean match();

	/**
	 * 多个string结果
	 * 
	 * @return
	 */
	public List<String> all();

	/**
	 * 通过json Path抽取
	 * 
	 * @param jsonPath
	 * @return
	 */
	public Selectable jsonPath(String jsonPath);

	/**
	 * 通过自定义选择器
	 * 
	 * @param selector
	 * @return
	 */
	public Selectable select(Selector selector);

	public Selectable selectList(Selector selector);

	/**
	 * 获得所有的节点
	 * 
	 * @return
	 */
	public List<Selectable> nodes();

}
