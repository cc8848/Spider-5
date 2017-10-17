package com.wedo.spider.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wedo.spider.Request;
import com.wedo.spider.Task;
import com.wedo.spider.scheduler.component.DuplicateRemover;
import com.wedo.spider.scheduler.component.HashSetDuplicateRemover;
import com.wedo.spider.utils.HttpConstant;

/**
 * 删除重复的URL并添加非冗余的URL
 * 
 * @author melody
 *
 */
public abstract class DuplicateRemovedScheduler implements Scheduler {

	protected Logger logger = LoggerFactory.getLogger(getClass());

	private DuplicateRemover duplicatedRemover = new HashSetDuplicateRemover();

	public DuplicateRemover getDuplicateRemover() {
		return duplicatedRemover;
	}

	public DuplicateRemovedScheduler setDuplicateRemover(DuplicateRemover duplicateRemover) {
		this.duplicatedRemover = duplicateRemover;
		return this;
	}

	@Override
	public void push(Request request, Task task) {
		logger.trace("get a candidate url {}", request.getUrl());
		if (shouldReserved(request) || noNeedToRemoveDuplicate(request)
				|| !duplicatedRemover.isDuplicate(request, task)) {
			logger.debug("push to queue {}", request.getUrl());
			pushWhenNoDuplicate(request, task);
		}
	}

	protected boolean shouldReserved(Request request) {
		return request.getExtra(Request.CYCLE_TRIED_TIMES) != null;
	}

	protected boolean noNeedToRemoveDuplicate(Request request) {
		return HttpConstant.Method.POST.equalsIgnoreCase(request.getMethod());
	}

	protected void pushWhenNoDuplicate(Request request, Task task) {

	}
}
