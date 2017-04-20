package com.cas.framework.utils;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * 数字相关辅助类
 */
public class NumberUtil {
	/**
	 * 普通浮点或双精度数字格式化器
	 */
	private static NumberFormat nf = NumberFormat.getNumberInstance();

	/**
	 * 货币数字格式化器
	 */
	private static NumberFormat cur = NumberFormat.getCurrencyInstance();

	/**
	 * 数字格式化器：三位分隔，小数点后两位
	 */
	private static NumberFormat nf2 = new DecimalFormat("#,###.##");

	/**
	 * 简单数字格式化器（默认小数点后两位）
	 */
	private static NumberFormat snf = new DecimalFormat("#.##");

	public static String FormatNumber(double num) {
		return nf.format(num);
	}

	public static String FormatCurrency(double num) {
		return cur.format(num);
	}

	/**
	 * 格式化给定的数字：小数点前每三位分隔，小数点后两位
	 * @param num
	 * @return String
	 */
	public static String format(double num) {
		return nf2.format(num);
	}

	public static String format(Double number) {
		return nf2.format(number.doubleValue());
	}

	public static String format(Float number) {
		return nf2.format(number.floatValue());
	}

	/**
	 * 格式化给定的数字：小数点前无间隔符，小数点后两位
	 * @param num
	 * @return String
	 */
	public static String simpleFormat(double num) {
		return snf.format(num);
	}

	public static float tofNPrecision(float val, int n) {
		double temp = Math.pow(10, n);
		long l = (long) (val * temp);
		return (float) (l / temp);
	}

	public static double todNPrecision(double val, int n) {
		double temp = Math.pow(10, n);
		long l = (long) (val * temp);
		return (l / temp);
	}

	public static boolean isZero(double value) {
		return Math.abs(value) < 10E-6;
	}

	public static boolean isZero(float value) {
		return Math.abs(value) < 10E-5;
	}

	public static boolean equals(double first, double second) {
		return isZero(first - second);
	}

	public static boolean equals(float first, float second) {
		return isZero(first - second);
	}

	/**
	 * 判定给定的字符串为非十进制数字 <br>
	 * 字符串小数点前可以有逗号分隔
	 * @param s
	 * @param comma 当且仅当comma为逗号时有效
	 * @return boolean
	 */
	public static boolean isNaN(String s, char comma) {
		if (s == null || s.length() == 0) {
			return true;
		}
		boolean commaValid = comma == ',';
		int pointCounter = 0;//小数点计数器
		for (int i = s.length() - 1; i > -1; i--) {
			char c = s.charAt(i);
			switch (c) {
				case '0':
				case '1':
				case '2':
				case '3':
				case '4':
				case '5':
				case '6':
				case '7':
				case '8':
				case '9':
					break;
				case '.':
					pointCounter++;
					if (pointCounter > 1) {//小数点不能多于1个
						return true;
					}
					break;
				case ',':
					if (commaValid) {
						if (pointCounter > 0) {//小数点后不允许出现逗号
							return true;
						}
						break;
					}
					return true;
				default:
					return true;
			}
		}
		return false;
	}

	/**
	 * 判定给定的字符串为非纯十进制数字 <br>
	 * @param s
	 * @return boolean
	 */
	public static boolean isNaN(String s) {
		if (s == null || s.length() == 0) {
			return true;
		}
		int pointCounter = 0;//小数点计数器
		for (int i = s.length() - 1; i > -1; i--) {
			char c = s.charAt(i);
			switch (c) {
				case '0':
				case '1':
				case '2':
				case '3':
				case '4':
				case '5':
				case '6':
				case '7':
				case '8':
				case '9':
					break;
				case '.':
					pointCounter++;
					if (pointCounter > 1) {//小数点不能多于1个
						return true;
					}
					break;
				default:
					return true;
			}
		}
		return false;
	}
}