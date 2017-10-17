package com.wedo.spider.scheduler.component;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.wedo.spider.Request;
import com.wedo.spider.Task;

/**
 * urls 冗余删除
 * @author melody
 *
 */
public class HashSetDuplicateRemover implements DuplicateRemover {

	private Set<String> urls = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

	@Override
	public boolean isDuplicate(Request request, Task task) {
		return !urls.add(getUrl(request));
	}

	@Override
	public void resetDuplicateCheck(Task task) {
		 urls.clear();
	}

	@Override
	public int getTotalRequestsCount(Task task) {
		 return urls.size();
	}

	protected String getUrl(Request request) {
		return request.getUrl();
	}
}
