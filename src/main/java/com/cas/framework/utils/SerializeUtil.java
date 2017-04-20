package com.cas.framework.utils;


import com.cas.framework.utils.serializer.JdkSerializer;
import com.cas.framework.utils.serializer.JsonSerializer;
import com.cas.framework.utils.serializer.OxmSerializer;
/**
 * 
 * @Creat 2017年04月8日
 * @Author:kingson·liu
 *  在原来基础上增加kryo
 */
public final class SerializeUtil {
	public static int BUFFER_SIZE = 1024*8;
	public static final byte[] EMPTY_ARRAY = new byte[0];

	public static boolean isEmpty(byte[] data) {
		return (data == null || data.length == 0);
	}
	
	public static byte[] serializeByJdk(Object object) {
		return JdkSerializer.serialize(object);
	}

	public static Object deserializeByJdk(byte[] bytes) {
		return JdkSerializer.deserialize(bytes);
	}
	
	public static String serializeToJson(Object object) {
		return JsonSerializer.toJson(object);
	}
	public static <T> T serializeJsonToObject(String json, Class<T> clzss){
		return JsonSerializer.toObject(json, clzss);
	}
	public static byte[] serializeByJson(Object object) {
		return JsonSerializer.serialize(object);
	}

	public static <T>T deserializeByJson(byte[] bytes,Class<T> clzss) {
		return JsonSerializer.deserialize(bytes,clzss);
	}
	
	public static byte[] serializeByKryo(Object object) {
		return JdkSerializer.serialize(object);
	}

	public static Object deserializeByKryo(byte[] bytes) {
		return JdkSerializer.deserialize(bytes);
	}
	
	public static byte[] serializeByOxm(Object object) {
		return OxmSerializer.serialize(object);
	}

	public static Object deserializeByOxm(byte[] bytes) {
		return OxmSerializer.deserialize(bytes);
	}
}
