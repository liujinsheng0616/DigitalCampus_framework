package com.cas.framework.support;

/**
 * DAO Exception
 */
public class DAOException extends RuntimeException {
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 3257284725541254961L;

	/**
	 * construction
	 * @param message
	 *            message
	 * @param cause
	 *            throwable
	 */
	public DAOException(String message, Throwable cause) {

		super(message, cause);

	}
}
