package com.wedo.spider;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wedo.spider.downloader.Downloader;
import com.wedo.spider.downloader.HttpClientDownloader;
import com.wedo.spider.pipeline.Pipeline;
import com.wedo.spider.processor.PageProcessor;
import com.wedo.spider.scheduler.QueueScheduler;
import com.wedo.spider.scheduler.Scheduler;
import com.wedo.spider.thread.CountableThreadPool;
import com.wedo.spider.utils.UrlUtils;

import us.codecraft.webmagic.pipeline.ConsolePipeline;


/**
 * 运行主类，爬虫的入口。 一个爬虫包含四个部分：下载器/调度器/页面处理器/输出管道
 * 
 * @author melody
 *
 */
public class Spider implements Runnable, Task {

	// 设置下载器
	protected Downloader downloader;
	// 设置输出 通道
	protected List<Pipeline> pipelines = new ArrayList<Pipeline>();
	// 设置页面处理器
	protected PageProcessor pageProcessor;
	// 设置起始请求
	protected List<Request> startRequests;
	// 设置爬虫站点配置信息
	protected Site site;
	//
	protected String uuid;
	// 调度器
	protected Scheduler scheduler = new QueueScheduler();
	//
	protected Logger logger = LoggerFactory.getLogger(getClass());
	// 线程池 执行请求监听等任务
	protected CountableThreadPool threadPool;
	// 初始化线程池服务 可以使用默认executorService
	protected ExecutorService executorService;
	// m默认线程池大小为1
	protected int threadNum = 1;
	// 运行状态
	protected AtomicInteger stat = new AtomicInteger(STAT_INIT);
	//
	protected boolean exitWhenComplete = true;
	//
	protected final static int STAT_INIT = 0;
	//
	protected final static int STAT_RUNNING = 1;
	//
	protected final static int STAT_STOPPED = 2;

	protected boolean spawnUrl = true;
	//
	protected boolean destroyWhenExit = true;
	// 新生URL锁
	private ReentrantLock newUrlLock = new ReentrantLock();
	//
	private Condition newUrlCondition = newUrlLock.newCondition();
	//
	private List<SpiderListener> spiderListeners;
	// 页面计数
	private final AtomicLong pageCount = new AtomicLong(0);
	//
	private Date startTime;
	//
	private int emptySleepTime = 30000;

	/**
	 * 使用页面处理器生成新的Spider
	 * 
	 * @param pageProcessor
	 * @return
	 */
	public static Spider create(PageProcessor pageProcessor) {
		return new Spider(pageProcessor);
	}

	public Spider(PageProcessor pageProcessor) {
		this.pageProcessor = pageProcessor;
		this.site = pageProcessor.getSite();
	}

	/**
	 * 设置爬虫的起始URL
	 * 
	 * @param startUrls
	 * @return
	 */
	public Spider startUrls(List<String> startUrls) {
		checkIfRunning();
		this.startRequests = UrlUtils.convertToRequests(startUrls);
		return this;
	}

	public Spider startRequest(List<Request> startRequests) {
		checkIfRunning();
		this.startRequests = startRequests;
		return this;
	}

	/**
	 * 设置爬虫的UUID
	 * 
	 * @param uuid
	 * @return
	 */
	public Spider setUUID(String uuid) {
		this.uuid = uuid;
		return this;
	}

	/**
	 * 设置 Spider的调度器
	 * 
	 * @param scheduler
	 * @return
	 */
	@Deprecated
	public Spider scheduler(Scheduler scheduler) {
		return setScheduler(scheduler);
	}

	public Spider setScheduler(Scheduler scheduler) {
		checkIfRunning();
		Scheduler oldScheduler = this.scheduler;
		this.scheduler = scheduler;
		if (oldScheduler != null) {
			Request request;
			// 推出旧调度器中的任务，并将其推入到新的schedule
			while ((request = oldScheduler.poll(this)) != null) {
				this.scheduler.push(request, this);
			}
		}
		return this;
	}

	/**
	 * 为爬虫添加pipelne
	 *
	 * @param pipeline
	 *            pipeline
	 * @return this
	 * @see #addPipeline(us.codecraft.webmagic.pipeline.Pipeline)
	 * @deprecated
	 */
	public Spider pipeline(Pipeline pipeline) {
		return addPipeline(pipeline);
	}

	// 添加pipeline
	public Spider addPipeline(Pipeline pipeline) {
		checkIfRunning();
		this.pipelines.add(pipeline);
		return this;
	}

	/**
	 * 设置pipelines
	 *
	 * @param pipelines
	 *            pipelines
	 * @return this
	 * @see Pipeline
	 * @since 0.4.1
	 */
	public Spider setPipelines(List<Pipeline> pipelines) {
		checkIfRunning();
		this.pipelines = pipelines;
		return this;
	}

	/**
	 * 清除所有的pipeline
	 * 
	 * @return
	 */
	public Spider clearPipeline() {
		pipelines = new ArrayList<Pipeline>();
		return this;
	}

	/**
	 * 设置Spider的下载器
	 *
	 * @param downloader
	 *            downloader
	 * @return this
	 * @see #setDownloader(us.codecraft.webmagic.downloader.Downloader)
	 * @deprecated
	 */
	public Spider downloader(Downloader downloader) {
		return setDownloader(downloader);
	}

	public Spider setDownloader(Downloader downloader) {
		checkIfRunning();
		this.downloader = downloader;
		return this;
	}
	
	/**
	 * 初始化爬虫相关的组件
	 */
	protected void initComponent() {
		if(downloader == null) {
			this.downloader = new HttpClientDownloader();
		}
		if(pipelines.isEmpty()) {
			pipelines.add(new ConsolePipeline())
		}
	}
	
	

	@Override
	public String getUUID() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void run() {

	}

	@Override
	public Site getSite() {
		return site;
	}

	/**
	 * 检查爬虫是否运行
	 */
	protected void checkIfRunning() {
		if (stat.get() == STAT_RUNNING) {
			throw new IllegalStateException("Spider is already running!");
		}
	}

}
