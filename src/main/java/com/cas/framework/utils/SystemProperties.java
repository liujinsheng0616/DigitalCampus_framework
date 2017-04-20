package com.cas.framework.utils;

import java.io.*;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public final class SystemProperties {
	private SystemProperties(){}
	private static Logger logger = LoggerFactory.getLogger(SystemProperties.class);
	
	private static final String PATH = "/system.properties";

	private static final Properties properties = new Properties();
	static {
		init();
	}
	public final static void init() {
		try {
			Resource resource = new ClassPathResource(PATH);
			properties.load(resource.getInputStream());
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage(), e);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}
	public static void main(String[] args) {
		SystemProperties.setProperty("a", "b");
		SystemProperties.setProperty("studio", "test");  
	}
	public final static Boolean setProperty(String key,String value){
		try {
			properties.setProperty(key,value);  
			FileOutputStream fos = new FileOutputStream(new ClassPathResource(PATH).getFile());
			properties.store(fos, "Copyright (c) Jason");  
	        fos.close();
	        return Boolean.TRUE;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}  
        return Boolean.FALSE;
	}
	/**
	 * <p>
	 * 根据<key>得到属性</key>
	 * </p>
	 * 
	 * @param key
	 *            Key
	 * @return String
	 */
	public final static String getProperties(String key) {
		return properties.getProperty(key,StringUtils.EMPTY);
	}
	public final static String getIgnoreUrl() {
		return getProperties("ignore_url")+getProperties("ignore_url_v2");
	}
	public final static String getFilePath() {
		return getProperties("FILE_PATH");
	}
	public final static String getAPPPath() {
		return getFilePath()+File.separatorChar+getProperties("APP_UPDATE_client");
	}
}
