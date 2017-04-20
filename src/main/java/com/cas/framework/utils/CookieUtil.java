package com.cas.framework.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 
 * @Creat 2017年04月8日
 * @Author:kingson·liu
 * 操作cookie的类
 */
public class CookieUtil {
	/**
	 * 设置cookie
	 * @param response
	 * @param name  cookie名字
	 * @param value cookie值
	 * @param maxAge cookie生命周期  以天为单位
	 */
	public static void addCookie(HttpServletResponse response,String name,String value,int maxAge){
		Cookie cookie = new Cookie(name,encode(value));
		cookie.setPath("/");
		try{
			cookie.setMaxAge(maxAge*12*60*60);
		}catch (Exception e) {
			cookie.setMaxAge(Integer.MAX_VALUE);
		}
		response.addCookie(cookie);
	}
	/**
	 * 根据名字获取cookie
	 * @param request
	 * @param name cookie名字
	 * @return Cookie
	 */
	public static String getCookie(HttpServletRequest request,String name){
		Map<String,Cookie> cookieMap = ReadCookieMap(request);
		if(cookieMap.containsKey(name)){
			Cookie cookie = cookieMap.get(name);
			return decode(cookie.getValue());
		}else{
			return null;
		}	
	}
	/**
	 * 将cookie封装到Map里面
	 * @param request
	 * @return
	 */
	private static Map<String,Cookie> ReadCookieMap(HttpServletRequest request){	
		Map<String,Cookie> cookieMap = new HashMap<String,Cookie>();
		Cookie[] cookies = request.getCookies();
		if(null!=cookies){
			for(Cookie cookie : cookies){
				cookieMap.put(cookie.getName(), cookie);
			}
		}
		return cookieMap;
	}
	private static String decode(String value) {
		String result = "";
		if (StringUtil.isNotEmpty(value))
			try {
				result = URLDecoder.decode(value, "utf-8");
			} catch (UnsupportedEncodingException localUnsupportedEncodingException) {
			}
		return result;
	}
	private static String encode(String value) {
		String result = "";
		if (StringUtil.isNotEmpty(value))
			try {
				result = URLEncoder.encode(value, "utf-8");
			} catch (UnsupportedEncodingException localUnsupportedEncodingException) {
			}
		return result;
	}
}
