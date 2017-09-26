package com.wedo.spider.action.dangdangBook.bySelenium;

/**
 * 爬虫开启入口
 * 
 * @author melody
 *
 */
public class DangDangBookCrawlerPortal {

	public static void main(String[] args) {
	
		
		// 关闭相关资源
		
		// 杀死一部分进程
		String startPage="http://category.dangdang.com/cp01.54.00.00.00.00.html";
		String dataOutPutPath = "";
		String phantomjsPath = "/home/melody/devOpt/phantomjs-2.1.1-linux-x86_64/bin/phantomjs";
		
		new Thread(new DangDangBookPageRunner(startPage,  dataOutPutPath, phantomjsPath)).start();
		
	}

}
