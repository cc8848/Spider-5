package com.wedo.spider.scheduler.distributedcomponent.lock;

import java.util.concurrent.TimeUnit;

/**
 * 
 * 分布式锁
 * @author melody
 *
 */
public interface DistributedLock {

	/**
	 * 尝试获取锁，不进行等待。得到返回true
	 * 
	 * @return
	 */
	public boolean tryLock() throws Exception;

	/**
	 * 阻塞等待获取
	 * 
	 * @throws Exception
	 */
	public void lock() throws Exception;

	/**
	 * 在规定时间内等待获取
	 * 
	 * @param time
	 * @param unit
	 * @return
	 * @throws Exception
	 */
	public boolean lock(long time, TimeUnit unit) throws Exception;

	/**
	 * 释放锁
	 * 
	 * @throws Exception
	 */
	public void unLock() throws Exception;

}
