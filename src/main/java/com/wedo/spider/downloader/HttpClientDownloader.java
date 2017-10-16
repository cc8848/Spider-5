package com.wedo.spider.downloader;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wedo.spider.Page;
import com.wedo.spider.Request;
import com.wedo.spider.Site;
import com.wedo.spider.Task;
import com.wedo.spider.proxy.Proxy;
import com.wedo.spider.proxy.ProxyProvider;
import com.wedo.spider.selector.PlainText;
import com.wedo.spider.utils.CharsetUtils;
import com.wedo.spider.utils.HttpClientUtils;

/**
 * 基于HttpClient 的Http 下载器
 * 
 * @author melody
 *
 */
// @ThreadSafe HttpCore 4.4
public class HttpClientDownloader extends AbstractDownloader {

	private Logger logger = LoggerFactory.getLogger(getClass());

	private final Map<String, CloseableHttpClient> httpClients = new HashMap<String, CloseableHttpClient>();

	private HttpClientGenerator httpClientGenerator = new HttpClientGenerator();

	private HttpUriRequestConverter httpUriRequestConverter = new HttpUriRequestConverter();

	private ProxyProvider proxyProvider;

	private boolean responseHeader = true;

	public void setHttpUriRequestConverter(HttpUriRequestConverter httpUriRequestConverter) {
		this.httpUriRequestConverter = httpUriRequestConverter;
	}

	public void setProxyProvider(ProxyProvider proxyProvider) {
		this.proxyProvider = proxyProvider;
	}

	private CloseableHttpClient getHttpClient(Site site) {
		if (site == null) {
			return httpClientGenerator.getClient(null);
		}
		String domain = site.getDomain();
		CloseableHttpClient httpClient = httpClients.get(domain);
		if (httpClient == null) {
			synchronized (this) {
				httpClient = httpClients.get(domain);
				if (httpClient == null) {
					httpClient = httpClientGenerator.getClient(site);
					httpClients.put(domain, httpClient);
				}
			}
		}
		return httpClient;
	}

	/**
	 * 下载 页面主要方法
	 */
	@Override
	public Page download(Request request, Task task) {
		if (task == null || task.getSite() == null) {
			throw new NullPointerException("task 与 request 都不能为空。");
		}
		CloseableHttpResponse httpResponse = null;
		// 链接客户端
		CloseableHttpClient httpClient = getHttpClient(task.getSite());
		// 代理提供
		Proxy proxy = proxyProvider != null ? proxyProvider.getProxy(task) : null;
		HttpClientRequestContext requestContext = httpUriRequestConverter.convert(request, task.getSite(), proxy);
		Page page = Page.fail(); // 初始化
		try {
			httpResponse = httpClient.execute(requestContext.getHttpUriRequest(),
					requestContext.getHttpClientContext());
			// 处理请求 返回处理后的页面
			page = handleResponse(request,
					request.getCharset() != null ? request.getCharset() : task.getSite().getCharset(), httpResponse,
					task);
			onSuccess(request); // 暴露处理接口 可以自定义实现 也可不实现
			logger.info("downloading page success {}", request.getUrl());
			return page;
		} catch (IOException e) {
			logger.warn("download page {} error", request.getUrl(), e);
			onError(request);
			return page;
		}finally {
			if(httpResponse != null) {
				//ensure the connection is released back to pool
                EntityUtils.consumeQuietly(httpResponse.getEntity());
			}
			if (proxyProvider != null && proxy != null) {
                proxyProvider.returnProxy(proxy, page, task);
            }
		}
	}

	@Override
	public void setThread(int threadNum) {
		httpClientGenerator.setPoolSize(threadNum);
	}

	/**
	 * 处理请求
	 * 
	 * @param request
	 * @param charset
	 * @param httpResponse
	 * @param task
	 * @return
	 * @throws IOException
	 * @throws UnsupportedOperationException
	 */
	protected Page handleResponse(Request request, String charset, HttpResponse httpResponse, Task task)
			throws UnsupportedOperationException, IOException {
		byte[] bytes = IOUtils.toByteArray(httpResponse.getEntity().getContent());
		String contentType = httpResponse.getEntity().getContent() == null ? ""
				: httpResponse.getEntity().getContentType().getValue();
		Page page = new Page();
		page.setBytes(bytes);
		if (!request.isBinaryContent()) {
			if (charset == null) {
				charset = getHtmlCharset(contentType, bytes);
			}
			page.setCharset(charset);
			page.setRawText(new String(bytes, charset));
		}
		page.setUrl(new PlainText(request.getUrl()));
		page.setRequest(request);
		page.setStatusCode(httpResponse.getStatusLine().getStatusCode());
		page.setDownloadSuccess(true);
		if (responseHeader) {
			page.setHeaders(HttpClientUtils.convertHeaders(httpResponse.getAllHeaders()));
		}
		return page;
	}

	/**
	 * g获得html编码
	 * 
	 * @param contentType
	 * @param contentBytes
	 * @return
	 * @throws IOException
	 */
	private String getHtmlCharset(String contentType, byte[] contentBytes) throws IOException {
		String charset = CharsetUtils.detectCharset(contentType, contentBytes);
		if (charset == null) {
			charset = Charset.defaultCharset().name();
			logger.warn("Charset autodetect failed, use {} as charset. Please specify charset in Site.setCharset()",
					Charset.defaultCharset());
		}
		return charset;
	}

}
