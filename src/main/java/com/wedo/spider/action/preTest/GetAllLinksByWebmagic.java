package com.wedo.spider.action.preTest;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

public class GetAllLinksByWebmagic implements PageProcessor {

	private final static Logger logger = LoggerFactory.getLogger(GetAllLinksByWebmagic.class);

	private Site site = Site.me().setRetryTimes(3).setSleepTime(1000).setTimeOut(10000);

	public Site getSite() {
		return site;
	}

	@Override
	public void process(Page page) {
		Document html = Jsoup.parse(page.getHtml().toString());
		Elements aTags = html.getElementsByTag("a");
		for (int i = 0; i < aTags.size(); i++) {
			String href = aTags.get(i).attr("href");
			if (!StringUtils.isBlank(href) && href.indexOf("category") > 0 && href.indexOf("cp") > 0) {
				if (!StringUtils.isBlank((aTags.get(i).attr("title"))))
					System.out.println(aTags.get(i));
			}
		}
	}

	public static void main(String[] args) {
		Spider.create(new GetAllLinksByWebmagic()).addUrl("http://book.dangdang.com/").thread(5).run();
	}

}
