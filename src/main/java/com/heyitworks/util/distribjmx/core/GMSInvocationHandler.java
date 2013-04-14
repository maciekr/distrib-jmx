package com.heyitworks.util.distribjmx.core;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import com.heyitworks.util.distribjmx.handlers.ResultHandler;

/**
 * 
 * @author mrakowicz
 * 
 */
@SuppressWarnings("unchecked")
public class GMSInvocationHandler implements MethodInterceptor {

	private RpcDispatcher rpcDispatcher;
	private final Map<String, MethodHandlerTuple> distributedMethods;
	private final Object target;
	private final String targetKey;

	public static class MethodHandlerTuple {

		private Method method;
		private ResultHandler resultHandler;

		public MethodHandlerTuple(Method method,
				Class<? extends ResultHandler> handlerClass) {
			this.method = method;
			try {
				this.resultHandler = (ResultHandler) handlerClass.newInstance();
			} catch (InstantiationException e) {
				throw new RequestHandlerInitializationException(e);
			} catch (IllegalAccessException e) {
				throw new RequestHandlerInitializationException(e);
			}
		}

		public ResultHandler getResultHandlerInstance() {
			return resultHandler;
		}

		public Method getMethod() {
			return method;
		}
	}

	public GMSInvocationHandler(Object target, String targetKey,
			Map<String, MethodHandlerTuple> distributedMethods,
			RpcDispatcher rpcDispatcher) {
		this.target = target;
		this.targetKey = targetKey;
		this.distributedMethods = distributedMethods;
		this.rpcDispatcher = rpcDispatcher;
	}

	/**
	 * CGLIB Proxy interceptor
	 */
	public Object intercept(Object obj, Method method, Object[] args,
			MethodProxy methodProxy) throws Throwable {
		return invoke(obj, method, args);
	}

	private Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		ResultHandler resultHandler = isMethodDistributed(method);
		Object localResult = method.invoke(target, args);
		if (resultHandler != null) {
			Collection remoteResults = castMethodCall(method, args);
			localResult = resultHandler.handle(remoteResults, localResult);
		}
		return localResult;
	}

	private ResultHandler isMethodDistributed(Method method) {
		ResultHandler resultHandler = null;
		MethodHandlerTuple annotatedMethodHandlerTuple;
		if ((annotatedMethodHandlerTuple = distributedMethods.get(method
				.getName())) != null) {
			Method annotatedMethod = annotatedMethodHandlerTuple.getMethod();
			if (isExactMatch(annotatedMethod, method)) {
				resultHandler = annotatedMethodHandlerTuple.resultHandler;
			}
		}
		return resultHandler;
	}

	private boolean isExactMatch(Method annotatedMethod, Method method) {

		boolean isExactMatch = true;

		Class<?>[] targetMethodParams = method.getParameterTypes();
		Class<?>[] distributedMethodParams = annotatedMethod
				.getParameterTypes();
		if (distributedMethodParams.length == targetMethodParams.length) {
			for (int i = 0; i < distributedMethodParams.length; i++) {
				if (distributedMethodParams[i] != targetMethodParams[i]) {
					isExactMatch = false;
					break;
				}
			}
		} else {
			isExactMatch = false;
		}
		return isExactMatch;
	}

	private Collection<?> castMethodCall(Method method, Object[] args) {
		Collection<?> results = doDispatch(method, args);
		return results;
	}

	private Collection<?> doDispatch(Method method, Object[] args) {
		RpcRequest rpcRequest = new RpcRequest();
		rpcRequest.setMethodName(method.getName());
		rpcRequest.setArgTypes(method.getParameterTypes());
		rpcRequest.setArgValues(args);
		rpcRequest.setTargetKey(targetKey);
		return rpcDispatcher.dispatch(rpcRequest);
	}
}
