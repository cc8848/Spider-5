package com.wedo.spider.scheduler.distributedcomponent.queue;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.I0Itec.zkclient.ExceptionUtil;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkNoNodeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 分布式队列 生产者 消费者 实现
 * 
 * @author melody
 *
 * @param <T>
 */
public class DistributedSimpleQueue<T> {

	private static Logger logger = LoggerFactory.getLogger(DistributedSimpleQueue.class);

	protected final ZkClient zkClient; // 操作zookeeper客户端
	protected final String root; // 代表根节点

	protected static final String NODE_NAME = "n_"; // 顺序节点的名称

	public DistributedSimpleQueue(ZkClient zkClient, String root) {
		this.zkClient = zkClient;
		this.root = root;
	}

	/**
	 * 获取队列的大小
	 * 
	 * @return
	 */
	public int size() {
		return zkClient.getChildren(root).size();
	}

	/**
	 * 判断队列是否为空
	 * 
	 * @return
	 */
	public boolean isEmpty() {
		return zkClient.getChildren(root).size() == 0;
	}

	/**
	 * 
	 * @param element
	 * @return
	 * @throws Exception
	 */
	public boolean offer(T element) {
		// 构建数据节点的完整路径
		String nodeFullPath = root.concat("/").concat(NODE_NAME);
		try {
			zkClient.createPersistentSequential(nodeFullPath, element);
		} catch (ZkNoNodeException e) {
			zkClient.createPersistent(root);
			offer(element);
		} catch (Exception e) {
			throw ExceptionUtil.convertToRuntimeException(e);
		}
		return true;
	}

	/**
	 * 取出队列中的数据
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public T poll() {
		try {
			List<String> list = zkClient.getChildren(root);
			if (list.size() == 0) {
				return null;
			}
			// 将队列从由小按照大的顺序进行排序
			Collections.sort(list, new Comparator<String>() {
				@Override
				public int compare(String o1, String o2) {
					return getNodeNumber(o1, NODE_NAME).compareTo(getNodeNumber(o2, NODE_NAME));
				}
			});

			/**
			 * 将队列中的元素做循环，然后构建完整的路径，再通过这个路径去读取数据
			 */
			for (String nodeName : list) {
				String nodeFullPath = root.concat("/").concat(nodeName);
				try {
					T node = (T) zkClient.readData(nodeFullPath);
					zkClient.delete(nodeFullPath);
					return node;
				} catch (ZkNoNodeException e) {
					logger.error("", e);
				}
			}
			return null;
		} catch (Exception e) {
			throw ExceptionUtil.convertToRuntimeException(e);
		}
	}

	/**
	 * 获得节点编号
	 * 
	 * @param str
	 * @param nodeName
	 * @return
	 */
	private String getNodeNumber(String str, String nodeName) {
		int index = str.lastIndexOf(nodeName);
		if (index >= 0) {
			index += NODE_NAME.length();
			return index <= str.length() ? str.substring(index) : "";
		}
		return str;

	}

}
