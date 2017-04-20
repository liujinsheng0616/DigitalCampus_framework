package com.cas.framework.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * @Creat 2017年04月8日
 * @Author:kingson·liu
 * 该类用于http远程调用
 */
public class HttpClientUtil {
    private static final Logger  LOG= LoggerFactory.getLogger(HttpClientUtil.class);
    private static final Charset UTF_8 = Charset.forName("UTF-8");
    public static final  int     CONNECT_TIMEOUT = 600 * 1000;
    //public static CloseableHttpClient client;
	/**
	 * 发起HTTP POST同步请求
	 * @param url
	 *            请求对应的URL地址
	 * @param paramData
	 *            请求所带参数，目前支持JSON格式的参数
	 * @param callback
	 *            请求收到响应后回调函数.
	 */
	public static void post(String url, Map<String, Object> paramData,Map<String, String> header,ResponseCallback callback) {
		post(url, paramData,header, null, callback);
	}
	public static void post(String url, String jsonData,Map<String, String> header,ResponseCallback callback) {
		doRequest(RequestMethod.POST, url, jsonData, header,null, callback);
	}
	public static void post(String url, Map<String, Object> paramMap, Map<String, String> header,List<File> fileList,ResponseCallback callback) {
		doRequest(RequestMethod.POST, url, wapperParamMap(paramMap), header,fileList, callback);
	}
	public static String postData(String url, Map<String, Object> paramData,Map<String, String> header) {
		final String json[] = {""}; 
		post(url, paramData, header ,new ResponseCallback(){
			@Override
			public void onResponse(int resultCode, String resultJson) {
				json[0]= resultJson;
			}
		});
		return json[0];
	}
	/**
     * 发起HTTP GET同步请求
     * @param url      请求对应的URL地址
     * @param paramMap GET请求所带参数Map
     * @param callback 请求收到响应后回调函数，
     */
    public static void get(String url, Map<String, Object> paramMap, Map<String, String> header,ResponseCallback callback) {
        doRequest(RequestMethod.GET, url, wapperParamMap(paramMap),header ,null, callback);
    }

	public static String getData(String url,Map<String, String> header) {
		return getData(url, null, header);
	}
	public static String getData(String url, Map<String, Object> paramData,Map<String, String> header) {
		final String json[] = {""}; 
		get(url, paramData,header, new ResponseCallback() {
			@Override
			public void onResponse(int resultCode, String resultJson) {
				json[0]= resultJson;
			}
		}) ;
		return json[0];
	}
	
	private static String wapperParamMap(Map<String, Object> paramMap){
		String paramData=null;
		if (null != paramMap && !paramMap.isEmpty()) {
	        StringBuilder buffer = new StringBuilder();
	        for (Map.Entry<String, Object> param : paramMap.entrySet()) {
	        	Object value=param.getValue();
	        	if(null!=value){
	        		if(value instanceof Map){
	        			buffer.append(param.getKey()).append("=").append(SerializeUtil.serializeToJson(value)).append("&");
	        		}else{
	        			buffer.append(param.getKey()).append("=").append(param.getValue()).append("&");
	        		}
	        	}
	        }
	        paramData = buffer.substring(0, buffer.length() - 1);
        }
		return paramData;
	}
	private static void doRequest(final RequestMethod method, final String url,
			final String paramData, final Map<String, String> header,final List<File> fileList,
			final ResponseCallback callback) {
		if (null == url || url.isEmpty()) {
			LOG.warn("The url is null or empty!!You must give it to me!OK?");
			return;
		}
		boolean haveCallback = true;
		if (null == callback) {
			LOG.warn("------no callback block!------");
			haveCallback = false;
		}
		LOG.debug("------请求地址:{}------", url);
		
		String getUrl = url;
		HttpUriRequest request = null;
		
		switch (method) {
		case GET:
			if (StringUtil.isNotEmpty(paramData)) {
				getUrl += "?" + paramData;
			}
			LOG.debug("getUrl:"+getUrl);
			request = new HttpGet(getUrl);
			break;
		case POST:
			LOG.debug("请求入参:");
			LOG.debug(paramData);
			request = new HttpPost(getUrl);
			if (null != fileList && !fileList.isEmpty()) {
				LOG.debug("上传文件...");
				MultipartEntityBuilder builder = MultipartEntityBuilder.create();
				for (File file : fileList) {
					// 只能上传文件哦 ^_^
					if (file.isFile()) {
						FileBody fb = new FileBody(file);
						builder.addPart("files", fb);
					} else { //如果上传内容有不是文件的，则不发起本次请求
						LOG.warn(
								"The target '{}' not a file,please check and try again!",
								file.getPath());
						return;
					}
				}
				if (StringUtil.isNotEmpty(paramData)) {
					builder.addPart("data", new StringBody(paramData,ContentType.APPLICATION_JSON));
				}
				((HttpPost) request).setEntity(builder.build());
			} else {
				if (StringUtil.isNotEmpty(paramData)) {
					 StringEntity jsonEntity = new StringEntity(paramData,ContentType.create(ContentType.APPLICATION_FORM_URLENCODED.getMimeType(), "UTF-8") );
					 ((HttpPost) request).setEntity(jsonEntity);
				}
			}
			break;
		case PUT:
		case DELETE:
		default:
			LOG.warn("------请求类型:{} 暂不支持------",
					method.toString());
			break;
		}
		if (null != header && !header.isEmpty()) {
			for (Map.Entry<String, String> head : header.entrySet()) {
	        	if(StringUtil.isNotEmpty(head.getValue())){
	        		request.addHeader(head.getKey(),head.getValue());
	        	}
	        }
		}
		CloseableHttpResponse response = null;
		try {
			long start = System.currentTimeMillis();
			// 发起请求
			RequestConfig config = RequestConfig.custom()
					.setConnectionRequestTimeout(CONNECT_TIMEOUT)
					.setConnectTimeout(CONNECT_TIMEOUT)
					.setSocketTimeout(CONNECT_TIMEOUT).build();
			CloseableHttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
			response = client.execute(request);
			long time = System.currentTimeMillis() - start;
			LOG.debug("本次请求'{}'耗时:{}ms",url.substring(url.lastIndexOf("/") + 1, url.length()), time);
			int resultCode = response.getStatusLine().getStatusCode();
			HttpEntity entity = response.getEntity();
			String resultJson = EntityUtils.toString(entity, UTF_8);
			if (HttpStatus.SC_OK == resultCode) {
				LOG.debug("------请求成功------");
				LOG.debug("响应结果:");
				LOG.debug(resultJson);
				if (haveCallback) {
					callback.onResponse(resultCode, resultJson);
				}
			} else {
				if (haveCallback) {
					LOG.warn("------请求出现错误，错误码:{}------",
							resultCode);
					callback.onResponse(resultCode, resultJson);
				}
			}
		} catch (ClientProtocolException e) {
			LOG.error("ClientProtocolException:", e);
			LOG.warn("------请求出现异常:{}------",
					e.toString());
			if (haveCallback) {
				callback.onResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR,
						e.toString());
			}
		} catch (IOException e) {
			LOG.error("IOException:", e);
			LOG.warn("------请求出现IO异常:{}------",
					e.toString());
			if (haveCallback) {
				callback.onResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR,
						e.toString());
			}
		} catch (Exception e) {
			LOG.error("Exception:", e);
			LOG.warn("------请求出现其他异常:{}------",e.toString());
			if (haveCallback) {
				callback.onResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR,e.toString());
			}
		} finally {
			if (null != request && !request.isAborted()) {
				request.abort();
			}
			HttpClientUtils.closeQuietly(response);
		}
	}
	/**
	 * 自定义HTTP响应回调接口
	 */
	public interface ResponseCallback {
		/**
		 * 响应后回调方法
		 * 
		 * @param resultCode
		 *            响应结果码，比如200成功，404不存在，500服务器异常等
		 * @param resultJson
		 *            响应内容，目前支持JSON字符串
		 */
		void onResponse(int resultCode, String resultJson);
	}

	/**
	 * 标识HTTP请求类型枚举
	 */
	static enum RequestMethod {
		GET,
		POST,
		PUT,
		DELETE
	}
	
	public static InputStream String2Inputstream(String str) {
		return new ByteArrayInputStream(str.getBytes());
	}
	public static void main(String[] args) {
		Map<String, String> header=new HashMap<String, String>();
		header.put("jike-client-from", "WEB");
		get("http://data.fclassroom.com/login.json?loginName=meicun001&password=19A2854144B63A8F7617A6F225019B12", null,header, new HttpClientUtil.ResponseCallback() {
			@Override
			public void onResponse(int resultCode, String resultJson) {
				System.out.println("resultJson:"+resultJson);
			}
		}) ;
	}
}