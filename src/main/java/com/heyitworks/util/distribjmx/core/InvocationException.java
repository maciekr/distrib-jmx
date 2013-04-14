package com.heyitworks.util.distribjmx.core;

/**
 * 
 * @author mrakowicz
 * 
 */
public class InvocationException extends RuntimeException {

	private static final long serialVersionUID = -1605583322099363401L;

	public InvocationException(String message, Throwable throwable) {
		super(message, throwable);
	}

}
