package com.wedo.spider.selector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.jayway.jsonpath.JsonPath;

/**
 * json Path 选择器
 * 
 * @author melody
 *
 */
public class JsonPathSelector implements Selector {

	private String jsonPathStr;

	private JsonPath jsonPath;

	public JsonPathSelector(String jsonPathStr) {
		this.jsonPathStr = jsonPathStr;
		this.jsonPath = JsonPath.compile(this.jsonPathStr);
	}

	@Override
	public String select(String text) {
		Object object = jsonPath.read(text);
		if (object == null) {
			return null;
		}
		if (object instanceof List) {
			List list = (List) object;
			if (list != null && list.size() > 0) {
				return toString(list.iterator().next());
			}
		}
		return object.toString();
	}

	private String toString(Object object) {
		if (object instanceof Map) {
			return JSON.toJSONString(object);
		} else {
			return String.valueOf(object);
		}
	}

	@Override
	public List<String> selectList(String text) {
		List<String> list = new ArrayList<String>();
		Object object = jsonPath.read(text);
		if (object == null) {
			return list;
		}
		if (object instanceof List) {
			List<Object> items = (List<Object>) object;
			for (Object item : items) {
				list.add(toString(item));
			}
		} else {
			list.add(toString(object));
		}
		return list;
	}
}