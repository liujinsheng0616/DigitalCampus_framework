package com.cas.framework.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @Creat 2017年04月8日
 * @Author:kingson·liu
 * 验证工具类
 */
public final class ValidatorUtils {
	private ValidatorUtils(){}
	/**
	 * 邮件的正则表达式
	 */
	private static String EMAIL_REGEX = ".+[@]{1}.+[.]{1}[\\w]+";
	/**中文的正则表达式*/
	private static String CHINESE_REGEX="[\u4E00-\u9FA5]|[\uFE30-\uFFA0]/gi";
	/**字符的的正则表达式*/
	private static String NUMBERCHAR_REGEX = "^[a-zA-Z0-9]+$";
	
	/**
	 * isNumberChar
	 * @param input
	 * @return boolean
	 */
	public static boolean isNumberChar(String input){
		Pattern p = Pattern.compile(NUMBERCHAR_REGEX);
		Matcher m = p.matcher(input);
		return m.matches();
	}
	/**
	 * isChinese
	 * @param input
	 * @return boolean
	 */
	public static boolean isChinese(String input){
		Pattern p = Pattern.compile(CHINESE_REGEX);
		Matcher m = p.matcher(input);
		return m.matches();
	}
	/**
	 * isEmailAddress
	 * 
	 * @param input input string
	 * @return boolean
	 * 
	 */
	public static boolean isEmailAddress(String input) {
		Pattern p = Pattern.compile(EMAIL_REGEX);
		Matcher m = p.matcher(input);
		return m.matches();

	}
	/**
	 * isAllLetterOrDigit
	 * 
	 * @param input input string
	 * @return boolean
	 * 
	 */
	public static boolean isAllLetterOrDigit(String input) {

		if (input == null || input.length() < 1)
			return false;
		char[] c = input.toCharArray();
		for (int i = 0, m = c.length; i < m; i++) {
			if (!Character.isLetterOrDigit(c[i]))
				return false;
		}
		return true;

	}
	/**
	 * isDigit
	 * 
	 * @param input input String
	 * @return boolean
	 * 
	 */
	public static boolean isDigit(String input) {
		try {
			Integer.parseInt(input);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}
	/**
	 * isFilePathValid
	 * 
	 * @param input input string
	 * @return boolean
	 * 
	 */
	public static boolean isFilePathValid(String input) {

		if (input == null || input.length() < 1)
			return false;
		char[] c = input.toCharArray();
		for (int i = 0, m = c.length; i < m; i++) {
			if (!Character.isLetterOrDigit(c[i]) && c[i] != '/' && c[i] != '-' && c[i] != '_')
				return false;
		}
		return true;
	}
	public static boolean isEmpty(String str) {
		if (str != null && str.trim().length() > 0)
			return false;
		return true;
	}
}
