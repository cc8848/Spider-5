package com.wedo.spider;

/**
 * 用来标志不同任务的接口
 * 
 * @author melody
 *
 */
public interface Task {

	/**
	 * 获得该任务的唯一ID
	 *
	 * @return uuid
	 */
	public String getUUID();

	/**
	 * 该任务的站点
	 *
	 * @return site
	 */
	public Site getSite();

}