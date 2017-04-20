package com.cas.framework.utils.serializer;

import java.io.IOException;
import java.text.SimpleDateFormat;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.codehaus.jackson.type.JavaType;

import com.cas.framework.utils.SerializeUtil;

/**
 * 
 * @Creat 2017年04月8日
 * @Author:kingson·liu
 *  json 转换
 */
public class JsonSerializer {
	private static final Logger log = LogManager.getLogger(JsonSerializer.class);
	private static final ObjectMapper mapper = new ObjectMapper();

	static {
		mapper.getSerializerProvider().setNullValueSerializer(new org.codehaus.jackson.map.JsonSerializer<Object>() {
			@Override
			public void serialize(Object value, JsonGenerator jgen, SerializerProvider provider)
					throws IOException, JsonProcessingException {
				jgen.writeString("");
				
			}
		});
		mapper.configure(org.codehaus.jackson.map.DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.setSerializationInclusion(Inclusion.NON_NULL);
		mapper.setSerializationInclusion(Inclusion.NON_EMPTY);
		mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
	}
	
	public static <T>T deserialize(byte[] bytes,Class<T> clzss) throws SerializationException {
		if (SerializeUtil.isEmpty(bytes)) {
			return null;
		}
		try {
			return (T) mapper.readValue(bytes, 0, bytes.length, clzss);
		} catch (Exception ex) {
			throw new SerializationException("Could not read JSON: " + ex.getMessage(), ex);
		}
	}
	public static byte[] serialize(Object t) throws SerializationException {
		if (t == null) {
			return SerializeUtil.EMPTY_ARRAY;
		}
		try {
			return mapper.writeValueAsBytes(t);
		} catch (Exception ex) {
			throw new SerializationException("Could not write JSON: " + ex.getMessage(), ex);
		}
	}
	
	public static String toJson(Object object) {
		try {
			return mapper.writeValueAsString(object);
		} catch (JsonGenerationException e) {
			log.warn(e.getMessage());
		} catch (JsonMappingException e) {
			log.warn(e.getMessage());
		} catch (IOException e) {
			log.warn(e.getMessage());
		}
		log.warn("toJson error...");
		return "";
	}

	/**
	 * json字符串转换成java对象
	 * 
	 * @param json
	 * @param clzss
	 */
	public static <T> T toObject(String json, Class<T> clzss) {
		try {
			return mapper.readValue(json,clzss);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * json字符串转换成java对象
	 * @param json
	 * @param valueType
	 */
	public static <T> T toObject(String json, JavaType valueType) {
		try {
			return mapper.readValue(json,valueType);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	 /**  
     * 获取泛型的Collection Type 
     * @param collectionClass 泛型的Collection  
     * @return JavaType Java类型  
     * @since 1.0  
     */  

	public static JavaType getCollectionType(Class<?> collectionClass, Class<?>... elementClasses) {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.getTypeFactory().constructParametricType(collectionClass, elementClasses);
	}
}
