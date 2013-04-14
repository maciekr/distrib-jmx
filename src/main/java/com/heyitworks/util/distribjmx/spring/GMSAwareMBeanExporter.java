package com.heyitworks.util.distribjmx.spring;

import java.util.HashMap;
import java.util.Map;

import org.springframework.jmx.support.JmxUtils;

import com.heyitworks.util.distribjmx.core.GMSInvocationHandler.MethodHandlerTuple;

/**
 * GMS exporter that works with both jdk standard mbeans (by convention) and
 * annotation-based spring stuff
 * 
 * @author mrakowicz
 * 
 */
public class GMSAwareMBeanExporter extends GMSAwareMBeanExporterBase {

	private boolean proxyJDKMBeans = false;

	@SuppressWarnings("unchecked")
	@Override
	protected boolean isMBean(Class beanClass) {
		return proxyJDKMBeans ? false : super.isMBean(beanClass);
	}

	@Override
	protected Map<String, MethodHandlerTuple> getDistributedMethods(
			Class<?> target) {

		Map<String, MethodHandlerTuple> methods = new HashMap<String, MethodHandlerTuple>();

		if (JmxUtils.isMBean(target)) {
			// try mbean interface
			methods = super.getDistributedMethods(JmxUtils
					.getMBeanInterface(target));		
			// try class level annotations
			methods.putAll(super.getDistributedMethods(target));
		} else {
			methods = super.getDistributedMethods(target);
		}
		return methods;
	}

	public void setProxyJDKMBeans(boolean proxyJDKMBeans) {
		this.proxyJDKMBeans = proxyJDKMBeans;
	}

	public boolean isProxyJDKMBeans() {
		return proxyJDKMBeans;
	}

}
