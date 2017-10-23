package com.wedo.spider.scheduler.distributedcomponent.queue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 分布式队列： 同步队列实现
 * 
 * @author melody
 *
 */
public class DistributedQueue<T> {

	private static final Logger logger = LoggerFactory.getLogger(DistributedQueue.class);

	// zookeeper 连接
	protected final ZooKeeper zookeeper;
	// 根节点
	protected final String root;
	// 队列长度
	private int queueSize;
	// zk 表示同步队列 可运行标识
	private static String startPath = "/queue/start";

	// 顺序节点名称
	protected static final String NODE_NAME = "n_";

	public DistributedQueue(ZooKeeper zookeeper, String root, int queueSize) {
		this.zookeeper = zookeeper;
		this.root = root;
		this.queueSize = queueSize;
		init();
	}

	/**
	 * 初始化根目录
	 */
	private void init() {
		try {
			Stat stat = zookeeper.exists(root, false);
			if (stat == null) {
				// 公开持久的根节点
				zookeeper.create(root, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			}
			// 删除startPath节点所有版本的
			zookeeper.delete(startPath, -1);
		} catch (KeeperException | InterruptedException e) {
			logger.error("create rootPath error", e);
		}
	}

	/**
	 * 获取队列的大小
	 * 
	 * @return
	 * @throws KeeperException
	 * @throws InterruptedException
	 */
	public int size() throws KeeperException, InterruptedException {
		return zookeeper.getChildren(root, false).size();
	}

	/**
	 * 判断队列是否为空
	 * 
	 * @return
	 * @throws InterruptedException
	 * @throws KeeperException
	 */
	public boolean isEmpty() throws KeeperException, InterruptedException {
		return zookeeper.getChildren(root, false).size() == 0;
	}

	/**
	 * byte转object
	 * 
	 * @param bytes
	 * @return
	 */
	private Object byteToObject(byte[] bytes) {
		Object obj = null;
		try {
			ByteArrayInputStream bi = new ByteArrayInputStream(bytes);
			ObjectInputStream oi = new ObjectInputStream(bi);
			obj = oi.readObject();
			bi.close();
			oi.close();
		} catch (ClassNotFoundException | IOException e) {
			logger.error("translation: " + e.getMessage());
			e.printStackTrace();
		}
		return obj;
	}

	/**
	 * 对象转化成byte数组
	 * 
	 * @param obj
	 * @return
	 */
	private byte[] ObjectToByte(java.lang.Object obj) {
		byte[] bytes = null;
		try {
			ByteArrayOutputStream bo = new ByteArrayOutputStream();
			ObjectOutputStream oo = new ObjectOutputStream(bo);
			oo.writeObject(obj);
			bytes = bo.toByteArray();

			bo.close();
			oo.close();
		} catch (IOException e) {
			logger.error("translation: " + e.getMessage());
			e.printStackTrace();
		}
		return bytes;
	}

	/**
	 * 向队列提供数据，队列满的话会阻塞等待直到start标志位清除
	 * 
	 * @param element
	 * @return
	 */
	public boolean offer(T element) {
		// 构建数据节点的完整路径
		String nodeFullPath = root.concat("/").concat(NODE_NAME);
		try {
			if (queueSize > size()) {
				// 创建持久的节点，写入数据
				zookeeper.create(nodeFullPath, ObjectToByte(element), ZooDefs.Ids.OPEN_ACL_UNSAFE,
						CreateMode.PERSISTENT);
				// 再判断一下队列是否满
				if (queueSize > size()) {
					// 确认当前队列未满 该标志不存在
					zookeeper.delete(startPath, -1);
				} else {
					zookeeper.create(startPath, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
				}
			} else {
				// 创建队列满的标记
				if (zookeeper.exists(startPath, false) != null) {
					zookeeper.create(startPath, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
				}
				final CountDownLatch latch = new CountDownLatch(1);
				// 删除节点 监听器
				final Watcher previousListener = new Watcher() {
					@Override
					public void process(WatchedEvent event) {
						if (event.getType() == EventType.NodeDeleted) {
							latch.countDown();
						}
					}
				};
				// 如果节点不存在会出现异常
				zookeeper.exists(startPath, previousListener);
				latch.await();
				// 继续添加该元素
				offer(element);
			}
		} catch (KeeperException | InterruptedException e) {
			logger.error("", e);
			e.printStackTrace();
		}
		return true;
	}

	/**
	 * 从队列取数据，当有start标志位时，开始取全部数据，全部取完数据后才删除start标识
	 * 
	 * @return
	 */
	public T poll() {
		try {
			// 队列未满时
			if (zookeeper.exists(startPath, false) == null) {
				final CountDownLatch latch = new CountDownLatch(1);
				// 监听 可运行标志位节点是否创建
				final Watcher previousListener = new Watcher() {
					@Override
					public void process(WatchedEvent event) {
						if (event.getType() == EventType.NodeCreated) {
							latch.countDown();
						}
					}
				};
				// 如果节点不存在 则会抛异常
				zookeeper.exists(startPath, previousListener);
				//
				latch.await();
			}

			List<String> list = zookeeper.getChildren(root, false);
			if (list.size() == 0) {
				return null;
			}

			// 将队列按照由小到大的顺序排序
			Collections.sort(list, new Comparator<String>() {
				@Override
				public int compare(String o1, String o2) {
					return getNodeNumber(o1, NODE_NAME).compareTo(getNodeNumber(o2, NODE_NAME));
				}
			});

			/**
			 * 将队列中的元素循环，然后构建完整的路径，再通过这个路径去读取数据
			 */
			for (String nodeName : list) {
				String nodeFullPath = root.concat("/").concat(nodeName);
				T node = (T) byteToObject(zookeeper.getData(nodeFullPath, false, null));
				zookeeper.delete(nodeFullPath, -1);
				return node;
			}
		} catch (KeeperException | InterruptedException e) {
			logger.error("", e);
		}
		return null;
	}

	/**
	 * 截取节点的数字
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
