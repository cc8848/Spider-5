package com.wedo.spider.selector;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.jsoup.nodes.Element;
import us.codecraft.xsoup.XPathEvaluator;
import us.codecraft.xsoup.Xsoup;

/**
 * xpath 选择器
 * @author melody
 *
 */
public class XpathSelector extends BaseElementSelector {

	private XPathEvaluator xPathEvaluator; // xPath计算

	public XpathSelector(String xpathStr) {
		this.xPathEvaluator = Xsoup.compile(xpathStr);
	}

	@Override
	public String select(Element element) {
		return xPathEvaluator.evaluate(element).get();
	}

	@Override
	public List<String> selectList(Element element) {
		return xPathEvaluator.evaluate(element).list();
	}

	@Override
	public Element selectElement(Element element) {
		List<Element> elements = selectElements(element);
		if (CollectionUtils.isNotEmpty(elements)) {
			return elements.get(0);
		}
		return null;
	}

	@Override
	public List<Element> selectElements(Element element) {
		return xPathEvaluator.evaluate(element).getElements();
	}

	@Override
	public boolean hasAttribute() {
		return xPathEvaluator.hasAttribute();
	}

}
