package com.wedo.spider.action.preTest;

import redis.clients.jedis.Jedis;

public class RedisUseDemo {

	public static void main(String[] args) {

		Jedis jedis = new Jedis("192.168.209.11", 6379);
		// jedis.auth("admin");

		jedis.set("sds", "sdsdsd");
		System.out.println(jedis.get("sds"));

		jedis.close();
	}

}
