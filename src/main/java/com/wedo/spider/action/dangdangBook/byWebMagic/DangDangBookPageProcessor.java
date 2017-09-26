package com.wedo.spider.action.dangdangBook.byWebMagic;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

public class DangDangBookPageProcessor implements PageProcessor {

	// private Site site = Site.me()// .setHttpProxy(new HttpHost("127.0.0.1",8888))
	// .setRetryTimes(3).setSleepTime(1000).setUseGzip(true);
	private Logger logger = LoggerFactory.getLogger(DangDangBookPageProcessor.class);
	private Site site = Site.me().setRetryTimes(3).setSleepTime(1000).setTimeOut(10000);
	private static int pageCount = 2;
	@Override
	public Site getSite() {
		return site;
	}

	@Override
	public void process(Page page) {
		Document pageDocument = page.getHtml().getDocument();
	
		/**
		 * 数据列表页面
		 */
		if(pageDocument.getElementById("component_0__0__6612") != null) {
			Element ul = pageDocument.getElementById("component_0__0__6612");
			Elements ps = ul.getElementsByClass("name");
			for (Element e : ps) {
				Elements a = e.getElementsByTag("a"																																																								);
				if(a == null) {
					continue;
				}else {
					Element firstA = a.get(0);
					page.addTargetRequest(firstA.attr("href"));
				}
			}
		}
		
		/**
		 * 数据页面
		 */
		if(pageDocument.getElementsByClass("name_info") != null && pageDocument.getElementsByClass("name_info").size() > 0) {
			String baseData = pageDocument.getElementsByClass("name_info").get(0).text();
			logger.info(baseData);
		}
		
		/**
		 * 下一页
		 */
		if(pageCount <= 100) {
			String urlRebuilder = "http://category.dangdang.com/pg"+ pageCount++ +"-cp01.54.00.00.00.00.html";
			page.addTargetRequest(urlRebuilder);
		}
																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																		
	}

	public static void main(String[] args) {

		Spider.create(new DangDangBookPageProcessor()).addUrl("http://category.dangdang.com/cp01.54.00.00.00.00.html")
				.thread(5).run();
		;
	}

}
