package com.wedo.spider.selector;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * 基于Jsoup的链接选择器，使用全路径URL
 * 
 * @author melody
 *
 */
public class LinksSelector extends BaseElementSelector {

	@Override
	public String select(Element element) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<String> selectList(Element element) {
		Elements elements = element.select("a");
		List<String> links = new ArrayList<String>(elements.size());
		for (Element element0 : elements) {
			if (!StringUtil.isBlank(element0.baseUri())) {
				links.add(element0.attr("abs:href"));
			} else {
				links.add(element0.attr("href"));
			}
		}
		return links;
	}

	@Override
	public Element selectElement(Element element) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Element> selectElements(Element element) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean hasAttribute() {
		return true;
	}

}
