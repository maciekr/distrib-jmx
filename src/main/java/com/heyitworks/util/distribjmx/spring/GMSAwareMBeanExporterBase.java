package com.heyitworks.util.distribjmx.spring;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.management.modelmbean.ModelMBean;

import net.sf.cglib.proxy.Enhancer;

import org.springframework.jmx.export.MBeanExportException;
import org.springframework.jmx.export.MBeanExporter;

import com.heyitworks.util.distribjmx.comm.RpcDispatcherFactory;
import com.heyitworks.util.distribjmx.core.Distributed;
import com.heyitworks.util.distribjmx.core.GMSInvocationHandler;
import com.heyitworks.util.distribjmx.core.RpcDispatcher;
import com.heyitworks.util.distribjmx.core.GMSInvocationHandler.MethodHandlerTuple;

/**
 * 
 * MBeanExporter that creates AOP proxy for a target resource using
 * {@link GMSInvocationHandler} and registers it with a ModelMBean created
 * directly from the target resource. </br> That way we can still use meta-data
 * mbean assemblers from Spring.
 * 
 * @author mrakowicz
 * 
 */
public abstract class GMSAwareMBeanExporterBase extends MBeanExporter {

	private RpcDispatcher rpcDispatcher;

	@Override
	protected ModelMBean createAndConfigureMBean(final Object target,
			final String targetKey) throws MBeanExportException {

		final ModelMBean modelMBean = super.createAndConfigureMBean(target,
				targetKey);

		try {
			Object mbeanTarget = createProxy(target, targetKey);
			if (mbeanTarget == null) {
				mbeanTarget = target;
			} else {
				registerTargetWithRPC(targetKey, target);
			}
			modelMBean.setManagedResource(mbeanTarget, "ObjectReference");

		} catch (Exception e) {
			throw new MBeanExportException(e.getMessage(), e);
		}

		return modelMBean;
	}

	private Object createProxy(final Object target, final String targetKey) {

		Object proxy = null;

		final Map<String, MethodHandlerTuple> distributedMethods = getDistributedMethods(target
				.getClass());

		if (!distributedMethods.isEmpty()) {
			Class<?>[] interfaces = lookupProxyInterfaces(target);
			GMSInvocationHandler invocationHandler = new GMSInvocationHandler(
					target, targetKey, distributedMethods, rpcDispatcher);
			proxy = createCGLIBProxy(target.getClass(), interfaces,
					invocationHandler);
		}
		return proxy;
	}

	protected Map<String, MethodHandlerTuple> getDistributedMethods(
			final Class<?> target) {

		final Map<String, MethodHandlerTuple> distributedMethods = new HashMap<String, MethodHandlerTuple>();

		Method[] methods = target.getMethods();
		for (int i = 0; i < methods.length; i++) {
			Method method = methods[i];
			if (method.isAnnotationPresent(Distributed.class)) {
				Distributed distributed = method
						.getAnnotation(Distributed.class);
				distributedMethods.put(method.getName(),
						new GMSInvocationHandler.MethodHandlerTuple(method,
								distributed.handler()));
			}
		}
		return distributedMethods;
	}

	private Class<?>[] lookupProxyInterfaces(final Object target) {
		return target.getClass().getInterfaces();
	}

	private Object createCGLIBProxy(Class<?> cls, Class<?>[] interfaces,
			GMSInvocationHandler invocationHandler) {
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(cls);
		enhancer.setInterfaces(interfaces);
		enhancer.setCallback(invocationHandler);
		return enhancer.create();
	}

	private void registerTargetWithRPC(String targetKey, Object target) {
		rpcDispatcher.registerTarget(targetKey, target);
	}

	@Override
	public void destroy() {
		super.destroy();
		rpcDispatcher.destroy();
	}

	public void setRpcDispatcherFactory(
			RpcDispatcherFactory rpcDispatcherFactory) {
		this.rpcDispatcher = rpcDispatcherFactory.getDispatcherInstance();
	}

}
