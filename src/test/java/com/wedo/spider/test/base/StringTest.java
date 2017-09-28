package com.wedo.spider.test.base;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class StringTest {

	public static void main(String[] args) {
	
		String dangdangpath = "/home/melody/dangdangBook.html";
		try {
			String readFileToString = FileUtils.readFileToString(new File(dangdangpath));
			Elements es = Jsoup.parse(readFileToString).getElementsByTag("a");
			for (Element element : es) {
				System.out.println(element.attr("href"));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
}
