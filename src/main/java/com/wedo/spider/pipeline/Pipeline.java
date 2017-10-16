package com.wedo.spider.pipeline;

import com.wedo.spider.ResultItems;
import com.wedo.spider.Task;

/**
 * 结果处理 
 * 实现自定义的数据导出，比如导出到mysql noSql 数据库等
 * @author melody
 *
 */
public interface Pipeline {

    /**
     * 结果处理 ，如格式化输出
     *
     * @param resultItems 
     * @param task task
     */
    public void process(ResultItems resultItems, Task task);
    
}