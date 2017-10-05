package com.wedo.spider;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 请求类 包含需要抓取的URL以及请求头的其他信息 还包含一些额外的信息
 * 
 * @author melody
 *
 */
public class Request implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3993743680684586796L;

	private String url; // 抓取url

	private String method; // 方法

	private Map<String, Object> extras; // 额外的一些信息

	/**
	 * 设置当前url的 cookie 如果没有设置，默认使用 Site 的
	 */
	private Map<String, String> cookies = new HashMap<String, String>();
	/**
	 * 设置header
	 */
	private Map<String, String> headers = new HashMap<String, String>();

	/**
	 * 设置 URL的优先级 优先级越高将越快被处理
	 */
	private long priority;

	/**
	 * 当设置为真 下载器不会试图将响应主体解析为文本
	 */
	private boolean binaryContent = false;

	private String charset;

	public Request() {
	}

	public Request(String url) {
		this.url = url;
	}

	public long getPriority() {
		return priority;
	}

	/**
	 * 
	 * 设置request的优先级，需要调度器去支持。
	 * 
	 * @param priority
	 *            priority
	 * @return this
	 */
	// @Experimental
	public Request setPriority(long priority) {
		this.priority = priority;
		return this;
	}

	/**
	 * 获得额外内容
	 * 
	 * @param key
	 * @return
	 */
	public Object getExtra(String key) {
		if (extras == null) {
			return null;
		}
		return extras.get(key);
	}

	public Request putExtra(String key, Object value) {
		if (extras == null) {
			extras = new HashMap<String, Object>();
		}
		extras.put(key, value);
		return this;
	}

	public String getUrl() {
		return url;
	}

	public Map<String, Object> getExtras() {
		return extras;
	}

	public Request setExtras(Map<String, Object> extras) {
		this.extras = extras;
		return this;
	}

	public Request setUrl(String url) {
		this.url = url;
		return this;
	}

	/**
	 * request的默认请求方法，默认为GET请求
	 * 
	 * @return
	 */
	public String getMethod() {
		return method;
	}

	public Request setMethod(String method) {
		this.method = method;
		return this;
	}

	@Override
	public int hashCode() {
		int result = url != null ? url.hashCode() : 0;
		result = 31 * result + (method != null ? method.hashCode() : 0);
		return result;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		Request request = (Request) o;

		if (url != null ? !url.equals(request.url) : request.url != null)
			return false;
		return method != null ? method.equals(request.method) : request.method == null;
	}

	public Request addCookie(String name, String value) {
		cookies.put(name, value);
		return this;
	}

	public Request addHeader(String name, String value) {
		headers.put(name, value);
		return this;
	}

	public Map<String, String> getCookies() {
		return cookies;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public boolean isBinaryContent() {
		return binaryContent;
	}

	public Request setBinaryContent(boolean binaryContent) {
		this.binaryContent = binaryContent;
		return this;
	}

	public String getCharset() {
		return charset;
	}

	public Request setCharset(String charset) {
		this.charset = charset;
		return this;
	}

	@Override
	public String toString() {
		return "Request{" + "url='" + url + '\'' + ", method='" + method + '\'' + ", extras=" + extras + ", priority="
				+ priority + ", headers=" + headers + ", cookies=" + cookies + '}';
	}

}
