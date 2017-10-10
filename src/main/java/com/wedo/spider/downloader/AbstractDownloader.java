package com.wedo.spider.downloader;

import com.wedo.spider.Page;
import com.wedo.spider.Request;
import com.wedo.spider.Site;
import com.wedo.spider.selector.Html;

/**
 * 抽象下载器，基本下载器的一些通用通用方法
 * 
 * @author melody
 *
 */
public abstract class AbstractDownloader implements Downloader {

	/**
	 * 不指定charset的普通下载方法
	 * 
	 * @param url
	 * @return
	 */
	public Html downloadHtml(String url) {
		return download(url, null);
	}

	/**
	 * 基础下载方法，获取Site页面的charset
	 * 
	 * @param url
	 * @param charset
	 * @return
	 */
	public Html download(String url, String charset) {
		Page page = download(new Request(url), Site.me().setCharset(charset).toTask());
		return page.getHtml();
	}

	protected void onSuccess(Request request) {
	}

	protected void onError(Request request) {
	}

}
