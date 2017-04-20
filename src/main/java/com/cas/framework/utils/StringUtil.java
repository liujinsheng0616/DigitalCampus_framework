package com.cas.framework.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

/**
 * @Creat 2017年04月8日
 * @Author:kingson·liu
 */
public final class StringUtil extends StringUtils {
	private StringUtil() {
	}

	public static void main(String[] args) {
		for (int i = 0; i < 3; ++i) {
			System.out.println(StringUtil.random(29));
		}
	}
	public static List<Long> changeStringToLong(String str) {
		if (StringUtil.isBlank(str))
			return null;
		List<Long> temp = new ArrayList<Long>();
		for (String s : str.split(","))
			temp.add(Long.valueOf(s));
		return temp;
	}
	 /**
     * 判断一组字符串是否有空值
     *
     * @param strs 需要判断的一组字符串
     * @return 判断结果，只要其中一个字符串为null或者为空，就返回true
     */
    public static boolean hasBlank(String... strs) {
        if (null == strs || 0 == strs.length) {
            return true;
        } else {
            //这种代码如果用java8就会很优雅了
            for (String str : strs) {
                if (isBlank(str)) {
                    return true;
                }
            }
        }
        return false;
    }
	public static boolean isNotEmpty(String str) {
        return str != null && !("null").equalsIgnoreCase(str) && str.length() > 0;
    } 
	/**
	 * @param str
	 * @param maxSize
	 * @return String
	 */
	public static String addZeroByMaxSize(String str, int maxSize) {
		String emptyStr = "";
		if (str == null)
			return str;
		for (int i = 0; i < maxSize - str.length(); i++) {
			emptyStr += "0";
		}
		return emptyStr + str;
	}

	public static String random(int size) {
		Random r = new Random();
		String code = "";
		for (int i = 0; i < size; ++i) {
			if (r.nextInt(size) % 2 == 0) { // 偶数位生产随机整数
				code = code + r.nextInt(10);
			} else { // 奇数产生随机字母包括大小写
				int temp = r.nextInt(52);
				char x = (char) (temp < 26 ? temp + 97 : (temp % 26) + 65);
				code += x;
			}
		}
		return code;
	}

	public static String randomNum(int size) {
		Random r = new Random();
		String code = "";
		for (int i = 0; i < size; ++i) {
			code = code + r.nextInt(10);
		}
		return code;
	}

	//正则判断是否为数字
	public static boolean isNumber(String str) {
		Pattern pattern = Pattern.compile("[0-9]*");
		Matcher match = pattern.matcher(str);
		if (match.matches() == false) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * json 字符串转义
	 * @param s
	 * @return
	 */
	public static String string2Json(String s) {
		if (s == null || s.length() <= 0)
			return "";
		String newstr = "";
		for (int i = 0, len = s.length(); i < len; i++) {
			char c = s.charAt(i);
			switch (c) {
			case '\"': {
				newstr += "\\\"";
				break;
			}
			case '\\': {
				newstr += "\\\\";
				break;
			}
			case '/': {
				newstr += "\\/";
				break;
			}
			case '\b': {
				newstr += "\\b";
				break;
			}
			case '\f': {
				newstr += "\\f";
				break;
			}
			case '\n': {
				newstr += "\\n";
				break;
			}
			case '\r': {
				newstr += "\\r";
				break;
			}
			case '\t': {
				newstr += "\\t";
				break;
			}
			case '\'': {
				newstr += "&#39;";
				break;
			}
			default: {
				newstr += c;
			}
			}
		}
		return newstr;
	}
}
