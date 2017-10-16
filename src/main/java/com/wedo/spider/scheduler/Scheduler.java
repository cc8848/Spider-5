package com.wedo.spider.scheduler;

import com.wedo.spider.Request;
import com.wedo.spider.Task;

/**
 * 
 * scheduler 是url的管理部分
 * 可以自定义实现 接口管理需要抓取的URL或者删除重复的URL
 * @author melody
 *
 */
public interface Scheduler {

    /**
     * 添加需要抓取的URL
     *
     * @param request request
     * @param task task
     */
    public void push(Request request, Task task);

    /**
     * 获取需要抓取的URL
     *
     * @param task the task of spider
     * @return the url to crawl
     */
    public Request poll(Task task);

}