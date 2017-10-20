package com.wedo.spider;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wedo.spider.downloader.Downloader;
import com.wedo.spider.downloader.HttpClientDownloader;
import com.wedo.spider.pipeline.CollectorPipeline;
import com.wedo.spider.pipeline.ConsolePipeline;
import com.wedo.spider.pipeline.Pipeline;
import com.wedo.spider.pipeline.ResultItemsCollectorPipeline;
import com.wedo.spider.processor.PageProcessor;
import com.wedo.spider.scheduler.QueueScheduler;
import com.wedo.spider.scheduler.Scheduler;
import com.wedo.spider.thread.CountableThreadPool;
import com.wedo.spider.utils.UrlUtils;
import com.wedo.spider.utils.WMCollections;

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
	// 爬虫起始时间
	private Date startTime;
	// 等待新URL添加时间
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
		if (downloader == null) {
			this.downloader = new HttpClientDownloader();
		}
		if (pipelines.isEmpty()) {
			pipelines.add(new ConsolePipeline());
		}
		downloader.setThread(threadNum);
		if (threadPool == null || threadPool.isShutdown()) {
			if (executorService != null && !executorService.isShutdown()) {
				threadPool = new CountableThreadPool(threadNum, executorService);
			} else {
				threadPool = new CountableThreadPool(threadNum);
			}
		}
		if (startRequests != null) {
			for (Request request : startRequests) {
				addRequest(request);
			}
			startRequests.clear();
		}
		// 设置爬虫开启时间
		startTime = new Date();
	}

	/**
	 * 正式运行
	 */
	@Override
	public void run() {
		checkRunningStat();
		initComponent();
		logger.info("Spider {} started!.", getUUID());
		while (!Thread.currentThread().interrupted() && stat.get() == STAT_RUNNING) {
			final Request request = scheduler.poll(this);
			if (request == null) {
				if (threadPool.getThreadAlive() == 0 && exitWhenComplete) {
					break;
				}
				// 在新的url被添加进之前将会阻塞
				waitNewUrl();
			} else {
				threadPool.execute(new Runnable() {
					@Override
					public void run() {
						try {
							processRequest(request);
							onSuccess(request);
						} catch (Exception e) {
							onError(request);
							logger.error("process request " + request + " error", e);
						} finally {
							pageCount.incrementAndGet();
							signalNewUrl();
						}
					}
				});
			}
		}
		// 设置爬虫运行状态： 停止
		stat.set(STAT_STOPPED);
		// 释放所有资源
		if (destroyWhenExit) {
			close();
		}
		logger.info("Spider {} closed! {} pages downloaded.", getUUID(), pageCount.get());
	}

	/**
	 * 关闭线程占用的所有资源
	 */
	public void close() {
		destroyEach(downloader);
		destroyEach(pageProcessor);
		destroyEach(scheduler);
		for (Pipeline pipeline : pipelines) {
			destroyEach(pipeline);
		}
		threadPool.shutdown();
	}

	/**
	 * 处理一些特定的URL
	 * 
	 * @param urls
	 */
	public void test(String... urls) {
		initComponent();
		if (urls.length > 0) {
			for (String url : urls) {
				processRequest(new Request(url));
			}
		}
	}

	/**
	 * 关闭资源
	 * 
	 * @param object
	 */
	private void destroyEach(Object object) {
		if (object instanceof Closeable) {
			try {
				((Closeable) object).close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 下载失败后触发监听事件
	 * 
	 * @param request
	 */
	protected void onError(Request request) {
		if (CollectionUtils.isNotEmpty(spiderListeners)) {
			for (SpiderListener spiderListener : spiderListeners) {
				spiderListener.onError(request);
			}
		}
	}

	/**
	 * 下载成功后 触发监听事件
	 * 
	 * @param request
	 */
	private void onSuccess(Request request) {
		if (CollectionUtils.isNotEmpty(spiderListeners)) {
			for (SpiderListener spiderListener : spiderListeners) {
				spiderListener.onSuccess(request);
			}
		}
	}

	private void processRequest(Request request) {
		Page page = downloader.download(request, this);
		if (page.isDownloadSuccess()) {
			onDownloadSuccess(request, page);
		} else {
			onDownloaderFail(request);
		}
	}

	private void onDownloaderFail(Request request) {
		// 如果下载失败 判断是否设置循环次数
		if (site.getCycleRetryTimes() == 0) {
			sleep(site.getSleepTime());
		} else {
			// 重试
			doCycleRetry(request);
		}
	}

	/**
	 * 循环重试请求
	 */
	private void doCycleRetry(Request request) {
		Object cycleTriedTimesObject = request.getExtra(Request.CYCLE_TRIED_TIMES);
		if (cycleTriedTimesObject == null) {
			addRequest(SerializationUtils.clone(request).setPriority(0).putExtra(Request.CYCLE_TRIED_TIMES, 1));
		} else {
			int cycleTriedTimes = (Integer) cycleTriedTimesObject;
			cycleTriedTimes++;
			if (cycleTriedTimes < site.getCycleRetryTimes()) {
				addRequest(SerializationUtils.clone(request).setPriority(0).putExtra(Request.CYCLE_TRIED_TIMES,
						cycleTriedTimes));
			}
		}
		sleep(site.getRetrySleepTime());
	}

	/**
	 * 下载页面成功后 处理页面
	 * 
	 * @param request
	 * @param page
	 */
	private void onDownloadSuccess(Request request, Page page) {
		// 如果请求状态码 在状态码集合中 则认为成功
		if (site.getAcceptStatCode().contains(page.getStatusCode())) {
			// 使用自定义逻辑处理页面
			pageProcessor.process(page);
			extractAndAddRequests(page, spawnUrl);
			// 如果页面中的结果没有被设置成跳过
			if (!page.getResultItems().isSkip()) {
				for (Pipeline pipeline : pipelines) {
					pipeline.process(page.getResultItems(), this);
				}
			} else {
				logger.info("page status code error,page {},code: {}", request.getUrl(), page.getStatusCode());
			}
			// 页面处理完成后 需要暂停的时间
			sleep(site.getSleepTime());
		}
	}

	/**
	 * 异步调用
	 */
	public void runAsync() {
		Thread thread = new Thread(this);
		thread.setDaemon(false);
		thread.start();
	}

	/**
	 * 停止程序
	 */
	public void stop() {
		if (stat.compareAndSet(STAT_RUNNING, STAT_STOPPED)) {
			logger.info("Spider " + getUUID() + " stop success!");
		} else {
			logger.info("Spider " + getUUID() + " stop fail!");
		}
	}

	/**
	 * 添加新的URL
	 * 
	 * @param urls
	 * @return
	 */
	public Spider addUrl(String... urls) {
		for (String url : urls) {
			addRequest(new Request(url));
		}
		// 唤醒 程序
		signalNewUrl();
		return this;
	}

	/**
	 * 直接下载传入的urls集合 并返回结果
	 * 
	 * @param urls
	 * @return
	 */
	public <T> List<T> getAll(Collection<String> urls) {
		destroyWhenExit = false;
		spawnUrl = false;
		if (startRequests != null) {
			startRequests.clear();
		}
		for (Request request : UrlUtils.convertToRequests(urls)) {
			addRequest(request);
		}
		CollectorPipeline collectorPipeline = getCollectorPipeline();
		pipelines.add(collectorPipeline);
		run();
		spawnUrl = true;
		destroyWhenExit = true;
		return collectorPipeline.getCollected();
	}

	protected CollectorPipeline getCollectorPipeline() {
		return new ResultItemsCollectorPipeline();
	}

	/**
	 * 直接返回这个url的抓取的内容
	 * 
	 * @param url
	 * @return
	 */
	public <T> T get(String url) {
		List<String> urls = WMCollections.newArrayList(url);
		List<T> resultItemses = getAll(urls);
		if (resultItemses != null && resultItemses.size() > 0) {
			return resultItemses.get(0);
		} else {
			return null;
		}
	}

	/**
	 * 设置线程舒朗
	 * 
	 * @param threadNum
	 * @return
	 */
	public Spider thread(int threadNum) {
		checkIfRunning();
		this.threadNum = threadNum;
		if (threadNum <= 0) {
			throw new IllegalArgumentException("threadNum should be more than one!");
		}
		return this;
	}

	public Spider thread(ExecutorService executorService, int threadNum) {
		checkIfRunning();
		this.threadNum = threadNum;
		if (threadNum <= 0) {
			throw new IllegalArgumentException("threadNum should be more than one!");
		}
		this.executorService = executorService;
		return this;
	}

	public boolean isExitWhenComplete() {
		return exitWhenComplete;
	}

	/**
	 * 设置完成时是否推出程序
	 * 
	 * @param exitWhenComplete
	 * @return
	 */
	public Spider setExitWhenComplete(boolean exitWhenComplete) {
		this.exitWhenComplete = exitWhenComplete;
		return this;
	}

	/**
	 * 添加
	 * 
	 * @param requests
	 * @return
	 */
	public Spider addRequest(Request... requests) {
		for (Request request : requests) {
			addRequest(request);
		}
		signalNewUrl();
		return this;
	}

	/**
	 * 设置是否将url作为种子url
	 * 
	 * @return
	 */
	public boolean isSpawnUrl() {
		return spawnUrl;
	}

	/**
	 * 获得爬虫下载的数量
	 * 
	 * @return
	 */
	public long getPageCount() {
		return pageCount.get();
	}

	/**
	 * 返回爬虫的运行状态
	 * 
	 * @return
	 */
	public Status getStatus() {
		return Status.fromValue(stat.get());
	}

	public enum Status {
		Init(0), Running(1), Stopped(2);

		private Status(int value) {
			this.value = value;
		}

		private int value;

		int getValue() {
			return value;
		}

		public static Status fromValue(int value) {
			for (Status status : Status.values()) {
				if (status.getValue() == value) {
					return status;
				}
			}
			// default value
			return Init;
		}
	}

	protected void sleep(int time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			logger.error("Thread interrupted when sleep", e);
		}
	}

	/**
	 * 获得活着的线程数量
	 * 
	 * @return
	 */
	public int getThreadAlive() {
		if (threadPool == null) {
			return 0;
		}
		return threadPool.getThreadAlive();
	}

	/**
	 * 是否添加url进行下载。 为true，添加url进行下载，如果为false时，则直接将起始URL下载完毕后就结束下载
	 * 
	 * @param spawnUrl
	 * @return
	 */
	public Spider setSpawnUrl(boolean spawnUrl) {
		this.spawnUrl = spawnUrl;
		return this;
	}

	public Spider setExecutorService(ExecutorService executorService) {
		checkIfRunning();
		this.executorService = executorService;
		return this;
	}

	/**
	 * 抽取解析到page中的url 并将其添加到新的url 作为种子URL
	 * 
	 * @param page
	 * @param spawnUrl
	 */
	protected void extractAndAddRequests(Page page, boolean spawnUrl) {
		if (spawnUrl && CollectionUtils.isNotEmpty(page.getTargetRequests())) {
			for (Request request : page.getTargetRequests()) {
				addRequest(request);
			}
		}
	}

	private void waitNewUrl() {
		newUrlLock.lock();
		try {
			// 二次判断 是达到退出程序的条件
			if (threadPool.getThreadAlive() == 0 && exitWhenComplete) {
				return;
			}
			newUrlCondition.await(emptySleepTime, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			logger.warn("waitNewUrl - interrupted,error {}", e);
		} finally {
			newUrlLock.unlock();
		}
	}

	private void signalNewUrl() {
		try {
			newUrlLock.lock();
			newUrlCondition.signalAll();
		} finally {
			newUrlLock.unlock();
		}
	}

	/**
	 * 检测并设置 运行状态
	 */
	private void checkRunningStat() {
		while (true) {
			int statNow = stat.get();
			if (statNow == STAT_RUNNING) {
				throw new IllegalStateException("Spider is aready running!");
			}
			if (stat.compareAndSet(statNow, STAT_RUNNING)) {
				break;
			}
		}
	}

	@Override
	public String getUUID() {
		if (uuid != null) {
			return uuid;
		}
		if (site != null) {
			return site.getDomain();
		}
		uuid = UUID.randomUUID().toString();
		return uuid;
	}

	private void addRequest(Request request) {
		if (site.getDomain() == null && request != null && request.getUrl() != null) {
			site.setDomain(UrlUtils.getDomain(request.getUrl()));
		}
		scheduler.push(request, this);
	}

	public void start() {
		runAsync();
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

	public List<SpiderListener> getSpiderListeners() {
		return spiderListeners;
	}

	public Spider setSpiderListeners(List<SpiderListener> spiderListeners) {
		this.spiderListeners = spiderListeners;
		return this;
	}

	public Date getStartTime() {
		return startTime;
	}

	public Scheduler getScheduler() {
		return scheduler;
	}

	/**
	 * Set wait time when no url is polled.<br>
	 * <br>
	 *
	 * @param emptySleepTime
	 *            In MILLISECONDS.
	 */
	public void setEmptySleepTime(int emptySleepTime) {
		this.emptySleepTime = emptySleepTime;
	}

}
