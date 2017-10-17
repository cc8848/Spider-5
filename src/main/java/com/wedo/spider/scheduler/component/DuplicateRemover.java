package com.wedo.spider.scheduler.component;

import com.wedo.spider.Request;
import com.wedo.spider.Task;

/**
 * 删除冗余的请求
 * 
 * @author melody
 *
 */
public interface DuplicateRemover {

	/**
	 * 判断请求是否冗余
	 * 
	 * @param request
	 * @param task
	 * @return
	 */
	public boolean isDuplicate(Request request, Task task);

	/**
	 * 重设置冗余检查
	 * 
	 * @param task
	 */
	public void resetDuplicateCheck(Task task);

	/**
	 * 获得监视器的所有请求数量
	 * 
	 * @param task
	 * @return
	 */
	public int getTotalRequestsCount(Task task);
}
