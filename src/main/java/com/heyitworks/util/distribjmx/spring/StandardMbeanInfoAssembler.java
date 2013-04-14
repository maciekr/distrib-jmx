package com.heyitworks.util.distribjmx.spring;

import java.lang.reflect.Method;

import javax.management.JMException;
import javax.management.MBeanParameterInfo;
import javax.management.modelmbean.ModelMBeanInfo;
import javax.management.modelmbean.ModelMBeanInfoSupport;
import javax.management.modelmbean.ModelMBeanOperationInfo;

import org.springframework.jmx.export.annotation.AnnotationJmxAttributeSource;
import org.springframework.jmx.export.assembler.MetadataMBeanInfoAssembler;
import org.springframework.jmx.support.JmxUtils;

/**
 * 
 * try mbean interface first, if not present try annotations
 * 
 * @author mrakowicz
 * 
 */
public class StandardMbeanInfoAssembler extends MetadataMBeanInfoAssembler {

	public StandardMbeanInfoAssembler(
			AnnotationJmxAttributeSource annotationJmxAttributeSource) {
		super(annotationJmxAttributeSource);
	}

	@Override
	public ModelMBeanInfo getMBeanInfo(Object managedBean, String beanKey)
			throws JMException {

		ModelMBeanInfo beanInfo;

		if (JmxUtils.isMBean(managedBean.getClass())) {
			Class<?> mbeanInterface = JmxUtils.getMBeanInterface(managedBean
					.getClass());

			beanInfo = getMBeanInfo(managedBean, mbeanInterface);
		} else {
			beanInfo = super.getMBeanInfo(managedBean, beanKey);
		}

		return beanInfo;
	}

	public ModelMBeanInfo getMBeanInfo(Object object, Class<?> interfaceClass) {
		return new ModelMBeanInfoSupport(object.getClass().getName(), "", null,
				null, getMethods(interfaceClass), null);
	}

	private ModelMBeanOperationInfo[] getMethods(Class<?> interfaceClass) {
		Method[] methods = interfaceClass.getMethods();
		ModelMBeanOperationInfo[] result = new ModelMBeanOperationInfo[methods.length];
		for (int i = 0; i < methods.length; i++) {
			Method method = methods[i];
			ModelMBeanOperationInfo metaDataOperationInfo = new ModelMBeanOperationInfo(
					method.getName(), method.getName(), getParameters(method),
					String.class.toString(), ModelMBeanOperationInfo.INFO);
			result[i] = metaDataOperationInfo;
		}
		return result;
	}

	private MBeanParameterInfo[] getParameters(Method method) {
		Class<?>[] parameterTypes = method.getParameterTypes();
		MBeanParameterInfo[] result = new MBeanParameterInfo[parameterTypes.length];
		for (int i = 0; i < parameterTypes.length; i++) {
			Class<?> parameterType = parameterTypes[i];
			MBeanParameterInfo parameterInfo = new MBeanParameterInfo(
					"arg" + 1, parameterType.getName(), "");
			result[i] = parameterInfo;
		}
		return result;
	}

}
