package com.wedo.spider.action.dangdangBook.byWebMagic;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.client.MongoCollection;
import com.wedo.spider.utils.MongoDBUtil;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

public class DangDangBookPageProcessor implements PageProcessor {

	// private Site site = Site.me()// .setHttpProxy(new HttpHost("127.0.0.1",8888))
	// .setRetryTimes(3).setSleepTime(1000).setUseGzip(true);
	private Logger logger = LoggerFactory.getLogger(DangDangBookPageProcessor.class);
	private Site site = Site.me().setRetryTimes(3).setSleepTime(1000).setTimeOut(10000);
	private final static MongoCollection<org.bson.Document> collection = MongoDBUtil.instance.getCollection("ddbook",
			"it_type");
	private static AtomicInteger count = new AtomicInteger(1); // 记录条数
	private static List<String> dataLines = new ArrayList<String>(8192);
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
		if (pageDocument.getElementById("component_0__0__6612") != null) {
			Element ul = pageDocument.getElementById("component_0__0__6612");
			Elements ps = ul.getElementsByClass("name");
			for (Element e : ps) {
				Elements a = e.getElementsByTag("a");
				if (a == null) {
					continue;
				} else {
					Element firstA = a.get(0);
					page.addTargetRequest(firstA.attr("href"));
				}
			}

			/**
			 * 下一页
			 */
			if (pageDocument.getElementsByClass("paging") != null) {
				Element next = getElementByClass(pageDocument, "next");
				if (next != null && next.child(0) != null) {
					String nextUrl = next.child(0).attr("href");
					logger.info("获得下一页URL: " + nextUrl);
					if (StringUtils.isNotBlank(nextUrl)) {
						page.addTargetRequest("http://category.dangdang.com/" + nextUrl);
					}
				}
			}
		}

		/**
		 * 数据页面
		 */
		if (pageDocument.getElementsByClass("name_info") != null
				&& pageDocument.getElementsByClass("name_info").size() > 0) {

			String bookDescInfo; // 书本描述信息
			String bookAuthor; // 书本作者
			String bookExpresss = ""; // 书本出版社
			String bookExpressTime = ""; // 书本出版时间
			String bookScore; // 书本评分
			String bookCommentCount; // 书本评论数
			String bookCurrentPrice = ""; // 书本当前价格
			String bookOriginalPrice = ""; // 书本原始价格
			String bookBriefContent = ""; // 书本内容简介
			String bookCategory; // 书本分类

			// 书本分类
			Element breadcrumb = getElementByClass(pageDocument, "breadcrumb");
			// 如果书本目录为空 则舍弃这条记录
			if (breadcrumb == null || StringUtils.isBlank((bookCategory = breadcrumb.text()))) {
				return;
			}

			// 书本描述信息
			Element name_info = getElementByClass(pageDocument, "name_info");
			bookDescInfo = name_info != null ? name_info.text().trim() : "";

			// 书本作者 书本出版社 书本出版时间 书本评分 书本评论数
			Element messbox_info = getElementByClass(pageDocument, "messbox_info");
			// 书本作者
			Element author = messbox_info.getElementById("author");
			bookAuthor = author != null ? author.text().replace("作者:", "").trim() : "";
			Elements t1s = messbox_info.getElementsByClass("t1");
			if(t1s != null) {
				// 书本出版社
				bookExpresss = t1s.get(1) != null ? t1s.get(1).text().replace("出版社:", "").trim() : "";
				// 书本出版时间
				if(t1s.size() > 2) {
					bookExpressTime = t1s.get(2) != null ? t1s.get(2).text().replace("出版时间:", "").trim() : "";
				}
			}
			// 书本评分
			Element star = getElementByClass(messbox_info, "star");
			bookScore = star != null ? star.attr("style").replace("width:", "").trim() : "";
			// 书本评论数
			Element comm_num_down = messbox_info.getElementById("comm_num_down");
			bookCommentCount = comm_num_down != null ? comm_num_down.text().trim() : "";
			// 当前价格 原始价格
			Element pc_price = pageDocument.getElementById("pc-price");
			if (pc_price != null) {
				// 当前价格
				Element dd_price = pc_price.getElementById("dd-price");
				bookCurrentPrice = dd_price != null ? dd_price.text().replace("¥", "").trim() : "";
				// 原始价格
				Element original_price = pc_price.getElementById("original-price");
				bookOriginalPrice = original_price != null ? original_price.text().replace("¥", "").trim() : "";
			}
			// 书本内容简介
			Element content = pageDocument.getElementById("content");
			if (content != null) {
				Element descrip = getElementByClass(content, "descrip");
				bookBriefContent = descrip != null ? descrip.text() : "";
			}

			// 新建文档
			org.bson.Document doc = new org.bson.Document();

			doc.put("bookDescInfo", bookDescInfo); // 书本描述信息
			doc.put("bookAuthor", bookAuthor); // 书本作者
			doc.put("bookExpresss", bookExpresss); // 书本出版社
			doc.put("bookExpressTime", bookExpressTime); // 书本出版时间
			doc.put("bookScore", bookScore); // 书本评分
			doc.put("bookCommentCount", bookCommentCount); // 书本评论数
			doc.put("bookCurrentPrice", bookCurrentPrice); // 书本当前价格
			doc.put("bookOriginalPrice", bookOriginalPrice); // 书本原始价格
			doc.put("bookBriefContent", bookBriefContent); // 书本内容简介
			doc.put("bookCategory", bookCategory); // 书本分类

			collection.insertOne(doc);
			logger.info("第 " + count.intValue() + " 条数据: " + bookDescInfo);
			dataLines.add(doc.toJson());
			if(dataLines.size() > 4096) {
				writeToTxt(dataLines);
			}
			count.incrementAndGet();
		}

	}

	private  static void writeToTxt(List<String> lines) {
		String dataPath = "/home/melody/ddbook.txt";
		BufferedWriter writer = null;
		try {
			File file = new File(dataPath);
			writer = new BufferedWriter(new FileWriter(file,true));
			for(String line : lines) {
				writer.write(line);
				writer.append("\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			if(writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			// 清除数据
			lines.clear();
		}
	}

	/**
	 * 获得Class元素中的第一个
	 * 
	 * @param document
	 * @return
	 */
	private static Element getElementByClass(Document document, String className) {
		return document.getElementsByClass(className).first();
	}

	private static Element getElementByClass(Element element, String className) {
		return element.getElementsByClass(className).first();
	}

	/**
	 * 获得tag Name 的第一个元素
	 * 
	 * @param document
	 * @param className
	 * @return
	 */
	private static Element getElementsByTagName(Document document, String tagName) {
		return document.getElementsByTag(tagName).first();
	}

	private static Element getElementsByTagName(Element element, String tagName) {
		return element.getElementsByTag(tagName).first();
	}

	public static void main(String[] args) {

		// 获得所有书本的分类网页URL
		String dangdangpath = "/home/melody/dangdangBook.html";
		List<String> urls = new ArrayList<String>();
//		try {
//			String readFileToString = FileUtils.readFileToString(new File(dangdangpath));
//			Elements es = Jsoup.parse(readFileToString).getElementsByTag("a");
//			for (Element element : es) {
//				String url = null;
//				if (StringUtils.isNotBlank((url = element.attr("href")))) {
//					urls.add(url);
//				}
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		}

		// Spider.create(new
		// DangDangBookPageProcessor()).addUrl("http://category.dangdang.com/cp01.54.00.00.00.00.html")
		
		
		urls.add("http://category.dangdang.com/cp01.54.06.00.00.00.html");
		urls.add("http://category.dangdang.com/cp01.54.00.00.00.00.html");
		urls.add("http://category.dangdang.com/cp01.54.12.00.00.00.html");
		
		// .thread(64).run();

		Spider.create(new DangDangBookPageProcessor()).startUrls(urls).thread(64).run();
		

	}

}
