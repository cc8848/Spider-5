package com.wedo.spider.pipeline;

import java.util.ArrayList;
import java.util.List;

import com.wedo.spider.ResultItems;
import com.wedo.spider.Task;

/**
 * 结果集合输出类
 * 
 * @author melody
 *
 */
public class ResultItemsCollectorPipeline implements CollectorPipeline<ResultItems> {

	private List<ResultItems> collector = new ArrayList<ResultItems>();

	@Override
	public void process(ResultItems resultItems, Task task) {
		collector.add(resultItems);
	}

	@Override
	public List<ResultItems> getCollected() {
		return collector;
	}

}
