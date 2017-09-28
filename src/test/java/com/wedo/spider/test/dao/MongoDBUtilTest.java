package com.wedo.spider.test.dao;

import com.wedo.spider.utils.MongoDBUtil;

public class MongoDBUtilTest {

	public static void main(String[] args) {
		// 删除数据
		MongoDBUtil.instance.dropDB("xxx");
	}
}
