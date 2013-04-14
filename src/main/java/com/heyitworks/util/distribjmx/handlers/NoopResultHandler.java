package com.heyitworks.util.distribjmx.handlers;

import java.util.Collection;

/**
 * 
 * @author mrakowicz
 * 
 */
public class NoopResultHandler implements ResultHandler<Object> {

	@SuppressWarnings("unchecked")
	@Override
	public Object handle(final Collection remoteResults,
			final Object localResult) {
		return localResult;
	}

}
