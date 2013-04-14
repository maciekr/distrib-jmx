package com.heyitworks.util.distribjmx.handlers;

import java.util.Collection;

/**
 * 
 * @author mrakowicz
 * 
 * @param <T>
 */
public interface ResultHandler<T> {
	T handle(Collection<T> remoteResults, T localResult);
}
