package com.wedo.spider;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.wedo.spider.utils.HttpConstant;

/**
 * 爬虫配置信息
 * 
 * @author melody
 *
 */
public class Site {

	private String domain;

	private String userAgent;

	// 默认cookie
	private Map<String, String> defaultCookies = new LinkedHashMap<String, String>();

	private Map<String, Map<String, String>> cookies = new HashMap<String, Map<String, String>>();

	private String charset;

	private int sleepTime = 5000;

	private int retryTimes = 0; // 请求重试次数

	private int cycleRetryTimes = 0; // 循环请求次数

	private int retrySleepTime = 1000;

	private int timeOut = 5000;

	private static final Set<Integer> DEFAULT_STATUS_CODE_SET = new HashSet<Integer>();

	private Set<Integer> acceptStatCode = DEFAULT_STATUS_CODE_SET;

	private Map<String, String> headers = new HashMap<String, String>();

	private boolean useGzip = true;

	private boolean disableCookieManagement = false;

	static {
		DEFAULT_STATUS_CODE_SET.add(HttpConstant.StatusCode.CODE_200);
	}

	/**
	 * 新建站点对象
	 * 
	 * @return
	 */
	public static Site me() {
		return new Site();
	}

	/**
	 * 为站点添加Cookie，使用当前的domain(默认站点)
	 * 
	 * @param name
	 * @param value
	 * @return
	 */
	public Site addCookie(String name, String value) {
		defaultCookies.put(name, value);
		return this;
	}

	/**
	 * 给制定的站点添加cookie
	 * 
	 * @param domain
	 * @param name
	 * @param value
	 * @return
	 */
	public Site addCookie(String domain, String name, String value) {
		if (!cookies.containsKey(domain)) {
			cookies.put(domain, new HashMap<String, String>());
		}
		cookies.get(domain).put(name, value);
		return this;
	}

	/**
	 * 设置用户代理
	 * 
	 * @param userAgent
	 * @return
	 */
	public Site setUserAgent(String userAgent) {
		this.userAgent = userAgent;
		return this;
	}

	/**
	 * 获得默认站点的cookie
	 * 
	 * @return
	 */
	public Map<String, String> getCookies() {
		return defaultCookies;
	}

	/**
	 * 获得所有站点的Cookie
	 * 
	 * @return
	 */
	public Map<String, Map<String, String>> getAllCookies() {
		return cookies;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public String getDomain() {
		return domain;
	}

	/**
	 * 设置当前站点的domain
	 * 
	 * @param domain
	 * @return
	 */
	public Site setDomain(String domain) {
		this.domain = domain;
		return this;
	}

	/**
	 * 手动设置page的charset
	 * 
	 * @param charset
	 * @return
	 */
	public Site setCharset(String charset) {
		this.charset = charset;
		return this;
	}

	public String getCharset() {
		return charset;
	}

	public int getTimeOut() {
		return timeOut;
	}

	/**
	 * 设置downloader下载器的超时(ms)
	 * 
	 * @param timeOut
	 * @return
	 */
	public Site setTimeOut(int timeOut) {
		this.timeOut = timeOut;
		return this;
	}

	/**
	 * 
	 * @param acceptStatCode
	 * @return
	 */
	public Site setAcceptStatCode(Set<Integer> acceptStatCode) {
		this.acceptStatCode = acceptStatCode;
		return this;
	}

	public Set<Integer> getAcceptStatCode() {
		return acceptStatCode;
	}

	/**
	 * 设置处理两个页面之间的休息时间，一般用来防止爬取速度过快
	 * 
	 * @param sleepTime
	 * @return
	 */
	public Site setSleepTime(int sleepTime) {
		this.sleepTime = sleepTime;
		return this;
	}

	/**
	 * 两个页面之间的处理间隔时间
	 * 
	 * @return
	 */
	public int getSleepTime() {
		return sleepTime;
	}

	/**
	 * 设置下载失败后的重试次数
	 * 
	 * @param retryTimes
	 * @return
	 */
	public Site setRetryTimes(int retryTimes) {
		this.retryTimes = retryTimes;
		return this;
	}

	/**
	 * 获得重试次数，当下载失败时
	 * 
	 * @return
	 */
	public int getRetryTimes() {
		return retryTimes;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	/**
	 * 添加下载时的Http header
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public Site addHeader(String key, String value) {
		headers.put(key, value);
		return this;
	}

	/**
	 * 获得循环重试次数 假如这个数值大于0，这个URL将被添加到调度器，并且再次下载 这个有别与 retryTime
	 * 
	 * @return
	 */
	public int getCycleRetryTimes() {
		return cycleRetryTimes;
	}

	/**
	 * 设置循环重试次数 假如这个数值大于0，这个URL将被添加到调度器，并且再次下载
	 * 
	 * @param cycleRetryTimes
	 * @return
	 */
	public Site setCycleRetryTimes(int cycleRetryTimes) {
		this.cycleRetryTimes = cycleRetryTimes;
		return this;
	}

	public boolean isUseGzip() {
		return useGzip;
	}

	public int getRetrySleepTime() {
		return retrySleepTime;
	}

	/**
	 * 设置下载失败后的重试的间隔时间
	 * 
	 * @param retrySleepTime
	 * @return
	 */
	public Site setRetrySleepTime(int retrySleepTime) {
		this.retrySleepTime = retrySleepTime;
		return this;
	}

	/**
	 * 是否使用gzip 默认为true
	 * 
	 * @param useGzip
	 * @return
	 */
	public Site setUseGzip(boolean useGzip) {
		this.useGzip = useGzip;
		return this;
	}

	public boolean isDisableCookieManagement() {
		return disableCookieManagement;
	}

	/**
	 * 设置downloader是否支持存储cookie。 关闭他将导致忽视所有的cookie字段。 注意：如果设置为真将任然无法工作
	 * 
	 * @param disableCookieManagement
	 * @return
	 */
	public Site setDisableCookieManagement(boolean disableCookieManagement) {
		this.disableCookieManagement = disableCookieManagement;
		return this;
	}

	/**
	 * 设置任务 表示该不同站点时的不同任务
	 * 
	 * @return
	 */
	public Task toTask() {
		return new Task() {
			@Override
			public String getUUID() {
				String uuid = Site.this.domain;
				if (uuid == null) {
					uuid = UUID.randomUUID().toString();
				}
				return uuid;
			}

			@Override
			public Site getSite() {
				return Site.this;
			}

		};
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		Site site = (Site) o;

		if (cycleRetryTimes != site.cycleRetryTimes)
			return false;
		if (retryTimes != site.retryTimes)
			return false;
		if (sleepTime != site.sleepTime)
			return false;
		if (timeOut != site.timeOut)
			return false;
		if (acceptStatCode != null ? !acceptStatCode.equals(site.acceptStatCode) : site.acceptStatCode != null)
			return false;
		if (charset != null ? !charset.equals(site.charset) : site.charset != null)
			return false;
		if (defaultCookies != null ? !defaultCookies.equals(site.defaultCookies) : site.defaultCookies != null)
			return false;
		if (domain != null ? !domain.equals(site.domain) : site.domain != null)
			return false;
		if (headers != null ? !headers.equals(site.headers) : site.headers != null)
			return false;
		if (userAgent != null ? !userAgent.equals(site.userAgent) : site.userAgent != null)
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = domain != null ? domain.hashCode() : 0;
		result = 31 * result + (userAgent != null ? userAgent.hashCode() : 0);
		result = 31 * result + (defaultCookies != null ? defaultCookies.hashCode() : 0);
		result = 31 * result + (charset != null ? charset.hashCode() : 0);
		result = 31 * result + sleepTime;
		result = 31 * result + retryTimes;
		result = 31 * result + cycleRetryTimes;
		result = 31 * result + timeOut;
		result = 31 * result + (acceptStatCode != null ? acceptStatCode.hashCode() : 0);
		result = 31 * result + (headers != null ? headers.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "Site{" + "domain='" + domain + '\'' + ", userAgent='" + userAgent + '\'' + ", cookies=" + defaultCookies
				+ ", charset='" + charset + '\'' + ", sleepTime=" + sleepTime + ", retryTimes=" + retryTimes
				+ ", cycleRetryTimes=" + cycleRetryTimes + ", timeOut=" + timeOut + ", acceptStatCode=" + acceptStatCode
				+ ", headers=" + headers + '}';
	}

}
