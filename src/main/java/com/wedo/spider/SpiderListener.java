package com.wedo.spider;

/**
 * 处理页面时的监听器
 * 
 * @author melody
 *
 */
public interface SpiderListener {

	public void onSuccess(Request request);

	public void onError(Request request);
}
