package com.wedo.spider.scheduler;

import com.wedo.spider.Task;

/**
 * 调度器的请求能够被监听器计数
 * 
 * @author melody
 *
 */
public interface MonitorableScheduler extends Scheduler {

	/**
	 * 获得剩余请求数量
	 * @param task
	 * @return
	 */
	public int getLeftRequestsCount(Task task);

	/**
	 * 获得所有请求数量
	 * @param task
	 * @return
	 */
	public int getTotalRequestsCount(Task task);

}