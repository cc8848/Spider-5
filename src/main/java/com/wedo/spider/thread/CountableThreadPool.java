package com.wedo.spider.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 工作线程池 使用ExecutorService 作为内部实现 新特征： 当线程池满时，将会阻塞从而避免推出没有处理的大量url 监听器计算活线程的数量
 * 
 * @author melody
 *
 */
public class CountableThreadPool {

	// 线程数量
	private int threadNum;
	// 活动线程数量
	private AtomicInteger threadAlive = new AtomicInteger();
	//
	private ReentrantLock reentrantLock = new ReentrantLock();
	//
	private Condition condition = reentrantLock.newCondition();
	//
	private ExecutorService executorService;

	public CountableThreadPool(int threadNum) {
		this.threadNum = threadNum;
		this.executorService = Executors.newFixedThreadPool(threadNum);
	}

	public CountableThreadPool(int threadNum, ExecutorService executorService) {
		this.threadNum = threadNum;
		this.executorService = executorService;
	}

	public void setExecutorService(ExecutorService executorService) {
		this.executorService = executorService;
	}

	public int getThreadAlive() {
		return threadAlive.get();
	}

	public int getThreadNum() {
		return threadNum;
	}
	
	/**
	 * 执行程序
	 * @param runnable
	 */
	public void execute(final Runnable runnable) {
		if (threadAlive.get() >= threadNum) {
			try {
				reentrantLock.lock();
				// 如果线程池已满
				while (threadAlive.get() >= threadNum) {
					try {
						condition.await();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			} finally {
				reentrantLock.unlock();
			}
		}
		// 如果还有空闲链接，表示活的线程数增一
		threadAlive.incrementAndGet();
		// 线程池 执行新的任务
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				try {
					runnable.run();
				} finally {
					try {
						reentrantLock.lock();
						threadAlive.decrementAndGet();
						condition.signal();
					} finally {
						reentrantLock.unlock();
					}
				}
			}
		});
	}

	public boolean isShutdown() {
		return executorService.isShutdown();
	}

	public void shutdown() {
		executorService.shutdown();
	}

}
