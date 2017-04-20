package com.cas.framework.base;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.cas.framework.support.Page;
import com.cas.framework.utils.SerializeUtil;

/**
 * 
 * @Creat 2017年04月8日
 * @Author:kingson·liu
 * controller的基类。
 */
public class BaseController {
	public static final Long EXPIRES_IN = 1000 * 3600 * 24 * 1L;// 1天
	protected static final String ENCODING_DEFAULT = "UTF-8";
	protected static final String ContentType_json = "application/json;charset=" + ENCODING_DEFAULT;
	protected static final String ContentType_html ="text/html;charset=" + ENCODING_DEFAULT;
	
	public static final Logger logger = LogManager.getLogger(BaseController.class);
	
	@Autowired
	public MessageSource messageSource;

	/***
	 * 获取当前的website路径 String
	 *  like http://192.168.1.1:8441/UUBean/
	 */
	public static String getWebSite(HttpServletRequest request) {
		String returnUrl = request.getScheme() + "://" + request.getServerName();
		if (request.getServerPort() != 80) {
			returnUrl += ":" + request.getServerPort();
		}
		returnUrl += request.getContextPath();
		return returnUrl;
	}
	/***
	 * 获取当前的website路径 String
	 */
	public static String getWebSiteNoPort(HttpServletRequest request) {
		return request.getScheme() + "://" + request.getServerName() + request.getContextPath();
	}
	/**
	 * like "/action!method.action"
	 */
	public String getRequestUrl(HttpServletRequest request)
	{
		String url =request.getRequestURI();
		String path = request.getContextPath();
		if (StringUtils.isNotEmpty(path))
		{
			return url.substring(path.length());
		}
		return url;
	}
	/**
	 * Render data to json for i18n
	 * @param errorCode - error code
	 * @return json
	 */
	public ResponseEntity<String> renderData(String errorCode) {
		return renderData(errorCode, null);
	}
	/**
	 * Render data to json for i18n
	 * @param errorCode - error code
	 * @param obj - object
	 * @return json
	 */
	public ResponseEntity<String> renderData(String errorCode, Object obj) {
		String errorMsg = messageSource.getMessage(errorCode, null, errorCode, LocaleContextHolder.getLocale());
		return renderData(errorCode, errorMsg, obj);
	}
	/**
	 * Render data to json
	 * @param errorCode - error code
	 * @param error - error message
	 * @param obj - object
	 * @return json
	 */
	public ResponseEntity<String> renderData(String errorCode, String error, Object obj) {
		StringBuffer sb = new StringBuffer();
		sb.append("{");
		sb.append("\"error_code\":" + errorCode + ",");
		sb.append("\"error_msg\":\"" + error + "\",");
		if (obj != null) {
			sb.append("\"data\":" + SerializeUtil.serializeToJson(obj) + "");
		} else {
			sb.append("\"data\":" + null + "");
		}
		sb.append("}");
		ResponseEntity<String> responseEntity = new ResponseEntity<String>(sb.toString(), initHttpHeadersJson(),HttpStatus.OK);
		return responseEntity;
	}
	/**
	 * 初始化HTTP头.
	 * @return HttpHeaders
	 */
	protected HttpHeaders initHttpHeadersJson() {
		HttpHeaders headers = new HttpHeaders();
		MediaType mediaType = new MediaType("application", "json", Charset.forName("utf-8"));
		headers.setContentType(mediaType);
		return headers;
	}

	/**
	 * 初始化HTTP头.
	 * @return HttpHeaders
	 */
	public HttpHeaders initHttpHeaders() {
		HttpHeaders headers = new HttpHeaders();
		MediaType mediaType = new MediaType("text", "html", Charset.forName("utf-8"));
		headers.setContentType(mediaType);
		return headers;
	}

	public ResponseEntity<String> renderMsg(Boolean status, String msg) {
		if (StringUtils.isEmpty(msg)) {
			msg = "";
		}
		String str = "{\"status\":" + status + ",\"msg\":\"" + msg + "\"}";
		ResponseEntity<String> responseEntity = new ResponseEntity<String>(str, initHttpHeaders(), HttpStatus.OK);
		return responseEntity;
	}

	/**
	 * 直接输出.
	 * 
	 * @param contentType 内容的类型.html,text,xml的值见后，json为"text/x-json;charset=UTF-8"
	 */
	protected void render(HttpServletResponse response, String text, String contentType) {
		try {
			response.setContentType(contentType);
			response.getWriter().write(text);
		} catch (IOException e) {
		}
	}

	/**
	 * 直接输出json格式.
	 */
	protected void renderJson(HttpServletResponse response, Object obj) {
		render(response, SerializeUtil.serializeToJson(obj), "text/x-json;charset=UTF-8");
	}

	/**
	 * 直接输出纯字符串.
	 */
	protected void renderText(HttpServletResponse response, String text) {
		render(response, text, "text/plain;charset=UTF-8");
	}

	/**
	 * 直接输出纯HTML.
	 */
	protected void renderHtml(HttpServletResponse response, String text) {
		render(response, text, "text/html;charset=UTF-8");
	}

	/**
	 * 直接输出纯XML.
	 */
	protected void renderXML(HttpServletResponse response, String text) {
		render(response, text, "text/xml;charset=UTF-8");
	}

	public ResponseEntity<String> renderData(Boolean status, String msg, Object obj) {
		if (StringUtils.isEmpty(msg)) {
			msg = "";
		}
		StringBuffer sb = new StringBuffer();
		sb.append("{");
		sb.append("\"status\":" + status + ",\"msg\":\"" + msg + "\",");
		sb.append("\"data\":" + SerializeUtil.serializeToJson(obj) + "");
		sb.append("}");

		ResponseEntity<String> responseEntity = new ResponseEntity<String>(sb.toString(), initHttpHeaders(),
				HttpStatus.OK);
		return responseEntity;
	}

	@SuppressWarnings("rawtypes")
	public ResponseEntity<String> renderPageData(Boolean status, String msg, Page page) {
		if (StringUtils.isEmpty(msg)) {
			msg = "";
		}
		if (page == null) {//|| page.getRows().isEmpty()
			return renderMsg(false, "No data found");
		}
		StringBuffer sb = new StringBuffer();
		sb.append("{");
		sb.append("\"status\":" + status + ",\"msg\":\"" + msg + "\",");
		sb.append("\"total\":\"" + page.getTotal() + "\",\"pageSize\":\"" + page.getPageSize() + "\",");
		sb.append("\"prePage\":\"" + page.getPrePage() + "\",\"nextPage\":\"" + page.getNextPage() + "\",");
		sb.append("\"currentPage\":\"" + page.getCurrentPage() + "\",\"totalPage\":\"" + page.getTotalPage() + "\",");
		sb.append("\"rows\":" + SerializeUtil.serializeToJson(page.getRows()) + "");
		sb.append("}");

		ResponseEntity<String> responseEntity = new ResponseEntity<String>(sb.toString(), initHttpHeaders(),
				HttpStatus.OK);
		return responseEntity;
	}

	/**
	 * 设置禁止客户端缓存的Header.
	 */
	public static void setDisableCacheHeader(HttpServletResponse response) {
		//Http 1.0 header
		response.setDateHeader("Expires", 1L);
		response.addHeader("Pragma", "no-cache");
		//Http 1.1 header
		response.setHeader("Cache-Control", "no-cache, no-store, max-age=0");
	}

	/**
	 * 设置让浏览器弹出下载对话框的Header.
	 * 
	 * @param fileName 下载后的文件名.
	 */
	public static void setFileDownloadHeader(HttpServletResponse response, String fileName) {
		try {
			//中文文件名支持
			String encodedfileName = new String(fileName.getBytes(), "ISO8859-1");
			response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedfileName + "\"");
		} catch (UnsupportedEncodingException e) {
		}
	}
	/***
	 * 获取IP（如果是多级代理，则得到的是一串IP值）
	 */
	public static String getIpAddr(HttpServletRequest request) {
		String ip = request.getHeader("x-forwarded-for");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		if (ip != null && ip.length() > 0) {
			String[] ips = ip.split(",");
			for (int i = 0; i < ips.length; i++) {
				if (!"unknown".equalsIgnoreCase(ips[i])) {
					ip = ips[i];
					break;
				}
			}
		}
		return ip;
	}
}
