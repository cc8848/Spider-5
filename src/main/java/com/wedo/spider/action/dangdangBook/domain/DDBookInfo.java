package com.wedo.spider.action.dangdangBook.domain;

import java.io.Serializable;

/**
 * 当当书本信息
 * 
 * @author melody
 *
 */
public class DDBookInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7796138267882444652L;

	private String bookDescInfo; // 书本描述信息
	private String bookAuthor; // 书本作者
	private String bookExpresss; // 书本出版社
	private String bookExpressTime; // 书本出版时间
	private String bookScore; // 书本评分
	private String bookCommentCount; // 书本评论数
	private String bookCurrentPrice; // 书本当前价格
	private String bookOriginalPrice; // 书本原始价格
	private String bookBriefContent; // 书本内容简介
	private String bookCategory; // 书本分类

	public String getBookCategory() {
		return bookCategory;
	}

	public void setBookCategory(String bookCategory) {
		this.bookCategory = bookCategory;
	}

	public String getBookDescInfo() {
		return bookDescInfo;
	}

	public void setBookDescInfo(String bookDescInfo) {
		this.bookDescInfo = bookDescInfo;
	}

	public String getBookAuthor() {
		return bookAuthor;
	}

	public void setBookAuthor(String bookAuthor) {
		this.bookAuthor = bookAuthor;
	}

	public String getBookExpresss() {
		return bookExpresss;
	}

	public void setBookExpresss(String bookExpresss) {
		this.bookExpresss = bookExpresss;
	}

	public String getBookExpressTime() {
		return bookExpressTime;
	}

	public void setBookExpressTime(String bookExpressTime) {
		this.bookExpressTime = bookExpressTime;
	}

	public String getBookScore() {
		return bookScore;
	}

	public void setBookScore(String bookScore) {
		this.bookScore = bookScore;
	}

	public String getBookCommentCount() {
		return bookCommentCount;
	}

	public void setBookCommentCount(String bookCommentCount) {
		this.bookCommentCount = bookCommentCount;
	}

	public String getBookCurrentPrice() {
		return bookCurrentPrice;
	}

	public void setBookCurrentPrice(String bookCurrentPrice) {
		this.bookCurrentPrice = bookCurrentPrice;
	}

	public String getBookOriginalPrice() {
		return bookOriginalPrice;
	}

	public void setBookOriginalPrice(String bookOriginalPrice) {
		this.bookOriginalPrice = bookOriginalPrice;
	}

	public String getBookBriefContent() {
		return bookBriefContent;
	}

	public void setBookBriefContent(String bookBriefContent) {
		this.bookBriefContent = bookBriefContent;
	}

}
