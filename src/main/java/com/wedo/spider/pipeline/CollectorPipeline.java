package com.wedo.spider.pipeline;

import java.util.List;

/**
 * 集合输出Pipeline  表示返回的结果是存储在集合中并返回
 * @author melody
 *
 * @param <T>
 */
public interface CollectorPipeline<T> extends Pipeline{

	/**
	 * 返回集合中的所有结果
	 * @return
	 */
	public List<T> getCollected();
}
