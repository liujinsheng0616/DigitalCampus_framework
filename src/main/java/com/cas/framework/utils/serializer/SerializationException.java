package com.cas.framework.utils.serializer;

import org.springframework.core.NestedRuntimeException;

public class SerializationException extends NestedRuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8797069278612513803L;

	/**
	 * Constructs a new <code>SerializationException</code> instance.
	 * 
	 * @param msg
	 * @param cause
	 */
	public SerializationException(String msg, Throwable cause) {
		super(msg, cause);
	}

	/**
	 * Constructs a new <code>SerializationException</code> instance.
	 * 
	 * @param msg
	 */
	public SerializationException(String msg) {
		super(msg);
	}
}
