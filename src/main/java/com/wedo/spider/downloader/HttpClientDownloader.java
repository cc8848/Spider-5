package com.wedo.spider.downloader;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wedo.spider.Page;
import com.wedo.spider.Request;
import com.wedo.spider.Task;


/**
 * 基于HttpClient 的Http 下载器
 * @author melody
 *
 */
// @ThreadSafe	HttpCore 4.4
public class HttpClientDownloader extends AbstractDownloader{

	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private final Map<String,CloseableHttpClient> httpClients = new HashMap<String, CloseableHttpClient>();
	
	private HttpClientGenerator httpClientGenerator = new HttpClientGenerator();
	
	
	
	@Override
	public Page download(Request request, Task task) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setThread(int threadNum) {
		// TODO Auto-generated method stub
		
	}

	
}
