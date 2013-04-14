package com.heyitworks.util.distribjmx.core;

import java.util.Collection;

public interface RpcDispatcher {

	public void registerTarget(final String targetKey, final Object target);

	public Collection<?> dispatch(final RpcRequest request);

	public void destroy();

}
