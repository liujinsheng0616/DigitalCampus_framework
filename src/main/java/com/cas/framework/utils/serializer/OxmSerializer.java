package com.cas.framework.utils.serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.springframework.oxm.xstream.XStreamMarshaller;

import com.cas.framework.utils.SerializeUtil;

/**
 * 其实要一个就够，担心有共享的问题
 * @Creat 2017年04月8日
 * @Author:kingson·liu
 *
 */
public class OxmSerializer{

	private static final XStreamMarshaller marshaller=new XStreamMarshaller();
	private static final XStreamMarshaller unmarshaller=new XStreamMarshaller();

	static {
		marshaller.afterPropertiesSet();
		unmarshaller.afterPropertiesSet();
	}
	public static Object deserialize(byte[] bytes) throws SerializationException {
		if (SerializeUtil.isEmpty(bytes)) {
			return null;
		}
		try {
			return unmarshaller.unmarshal(new StreamSource(new ByteArrayInputStream(bytes)));
		} catch (Exception ex) {
			throw new SerializationException("Cannot deserialize bytes", ex);
		}
	}

	public static byte[] serialize(Object t) throws SerializationException {
		if (t == null) {
			return SerializeUtil.EMPTY_ARRAY;
		}

		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		StreamResult result = new StreamResult(stream);

		try {
			marshaller.marshal(t, result);
		} catch (Exception ex) {
			throw new SerializationException("Cannot serialize object", ex);
		}
		return stream.toByteArray();
	}
}
