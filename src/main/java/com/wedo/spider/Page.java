package com.wedo.spider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.wedo.spider.selector.Html;
import com.wedo.spider.selector.Json;
import com.wedo.spider.selector.Selectable;
import com.wedo.spider.utils.HttpConstant;
import com.wedo.spider.utils.UrlUtils;


/**
 * 存储结果以及需要抓取的URL对象 非线程安全
 * 
 * @author melody
 *
 */
public class Page {

	private Request request;

	private ResultItems resultItems = new ResultItems();

	private Html html;

	private Json json;

	private String rawText;

	private Selectable url;

	private Map<String, List<String>> headers;

	private int statusCode = HttpConstant.StatusCode.CODE_200;

	private boolean downloadSuccess = true;

	private byte[] bytes;

	private List<Request> targetRequests = new ArrayList<Request>();

	private String charset;

	public Page() {
	}

	/**
	 * 生成新的页面对象，设置为下载为成功
	 * 
	 * @return
	 */
	public static Page fail() {
		Page page = new Page();
		page.setDownloadSuccess(false);
		return page;
	}

	/**
	 * 结果项是否舍弃
	 * 
	 * @param skip
	 * @return
	 */
	public Page setSkip(boolean skip) {
		resultItems.setSkip(skip);
		return this;
	}

	/**
	 * 设置输出字段
	 * 
	 * @param key
	 * @param field
	 */
	public void putField(String key, Object field) {
		resultItems.put(key, field);
	}

	/**
	 * 获取页面的html内容
	 * 
	 * @return
	 */
	public Html getHtml() {
		if (html == null) {
			html = new Html(rawText, request.getUrl());
		}
		return html;
	}

	/**
	 * 获得原始页面内容的json数据
	 * 
	 * @return
	 */
	public Json getJson() {
		if (json == null) {
			json = new Json(rawText);
		}
		return json;
	}

	public void setHtml(Html html) {
		this.html = html;
	}

	public List<Request> getTargetRequests() {
		return targetRequests;
	}

	/**
	 * 添加需要抓取的URL
	 * 
	 * @param requests
	 */
	public void addTargetRequests(List<String> requests) {
		for (String s : requests) {
			if (StringUtils.isBlank(s) || s.equals("#") || s.startsWith("javascript:")) {
				continue;
			}
			s = UrlUtils.canonicalizeUrl(s, url.toString());
			targetRequests.add(new Request(s));
		}
	}

	public void addTargetRequests(List<String> requests, long priority) {
		for (String s : requests) {
			if (StringUtils.isBlank(s) || s.equals("#") || s.startsWith("javascript:")) {
				continue;
			}
			s = UrlUtils.canonicalizeUrl(s, url.toString());
			targetRequests.add(new Request(s).setPriority(priority));
		}
	}

	public void addTargetRequest(String requestString) {
		if (StringUtils.isBlank(requestString) || requestString.equals("#")) {
			return;
		}
		requestString = UrlUtils.canonicalizeUrl(requestString, url.toString());
		targetRequests.add(new Request(requestString));
	}

	public void addTargetRequest(Request request) {
		targetRequests.add(request);
	}

	/**
	 * 获得当前页面的URL
	 * 
	 * @return
	 */
	public Selectable getUrl() {
		return url;
	}

	public void setUrl(Selectable url) {
		this.url = url;
	}

	/**
	 * 获得当前页面的请求
	 * 
	 * @return
	 */
	public Request getRequest() {
		return request;
	}

	public void setRequest(Request request) {
		this.request = request;
		this.resultItems.setRequest(request);
	}

	public ResultItems getResultItems() {
		return resultItems;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	public String getRawText() {
		return rawText;
	}

	public Page setRawText(String rawText) {
		this.rawText = rawText;
		return this;
	}

	public Map<String, List<String>> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, List<String>> headers) {
		this.headers = headers;
	}

	public boolean isDownloadSuccess() {
		return downloadSuccess;
	}

	public void setDownloadSuccess(boolean downloadSuccess) {
		this.downloadSuccess = downloadSuccess;
	}

	public byte[] getBytes() {
		return bytes;
	}

	public void setBytes(byte[] bytes) {
		this.bytes = bytes;
	}

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	@Override
	public String toString() {
		return "Page{" + "request=" + request + ", resultItems=" + resultItems + ", html=" + html + ", json=" + json
				+ ", rawText='" + rawText + '\'' + ", url=" + url + ", headers=" + headers + ", statusCode="
				+ statusCode + ", downloadSuccess=" + downloadSuccess + ", targetRequests=" + targetRequests
				+ ", charset='" + charset + '\'' + ", bytes=" + Arrays.toString(bytes) + '}';
	}
}
