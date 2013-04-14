package com.heyitworks.util.distribjmx.handlers;

import java.util.Collection;

/**
 * basic int aggregator
 * 
 * @author mrakowicz
 * 
 */
public class IntegerResultHandler implements ResultHandler<Integer> {

	@Override
	public Integer handle(final Collection<Integer> remoteResults,
			final Integer localResult) {
		int sum = 0;
		for (Integer result : remoteResults) {
			sum += result;
		}
		sum += localResult;
		return sum;
	}

}
