package com.wedo.spider.test.utils;

import java.util.Properties;

import com.wedo.spider.utils.PropertiesUtil;


public class PropertiesUtilsTest {

	public static void main(String[] args) {

		// System.out.println(new File(".").getAbsolutePath());
		Properties properties = PropertiesUtil
				.getByDynamic("/home/melody/hSpace/workSpace/workPro/DangDangBookSpider/src/main/resources"
						+ "/redisPool.properties");
		System.out.println(properties);

	}
}
