package com.wedo.spider.test.dao;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

public class MongoDBTest {

	public static void main(String[] args) {
		insert();
	}

	/**
	 * 返回数据库中数据的集合
	 * 
	 * @param dbName
	 * @param collectionName
	 * @return
	 */
	// MongoDB无需预定义数据库和集合,在使用的时候会自动创建
	public static MongoCollection<Document> getCollection(String dbName, String collectionName) {
		// 实例化一个mongo客户端，服务地址127.0.0.1 端口： 270117
		MongoClient mongoClient = new MongoClient("localhost", 27017);
		// 实例化一个mongo数据库
		MongoDatabase dateBase = mongoClient.getDatabase(dbName);
		// 获取数据库中某个集合
		MongoCollection<Document> collection = dateBase.getCollection(collectionName);
		return collection;
	}

	/**
	 * 插入数据
	 */
	public static void insert() {
		// 获得collection
		MongoCollection<Document> collection = getCollection("test", "haha");
		// 实例化一个文档 档内容为{sname:'Mary',sage:25}，如果还有其他字段，可以继续追加append
		Document doc1 = new Document("sname", "mary").append("sage", 25);
		Document doc2 = new Document("sname", "Bob").append("sage", 22);
		List<Document> docs = new ArrayList<Document>();
		docs.add(doc1);
		docs.add(doc2);

		// 插入到数据集合
		collection.insertMany(docs);
		System.out.println("插入成功");
	}

	/**
	 * 更新数据
	 */
	public static void update() {
		try {
			MongoCollection<Document> collection = getCollection("test", "student");
			// 更新文档 将文档中sname='Mary'的文档修改为sage=22
			collection.updateMany(Filters.eq("sname", "Mary"), new Document("$set", new Document("sage", 22)));
			System.out.println("更新成功！");
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
		}
	}

	/**
	 * 删除数据
	 */
	public static void delete() {
		try {
			MongoCollection<Document> collection = getCollection("test", "student");
			// 删除符合条件的第一个文档
			collection.deleteOne(Filters.eq("sname", "Bob"));
			// 删除所有符合条件的文档
			// collection.deleteMany (Filters.eq("sname", "Bob"));
			System.out.println("删除成功！");
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
		}
	}

}
