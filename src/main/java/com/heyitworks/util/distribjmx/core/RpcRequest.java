package com.heyitworks.util.distribjmx.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * @author mrakowicz
 * 
 */
public class RpcRequest implements Serializable {

	private static final long serialVersionUID = -8254307550142021653L;

	private static final Pattern argPattern = Pattern.compile("\\((.+)\\)");

	private String targetKey;

	private String methodName;

	private Class<?>[] argTypes;

	private Object[] argValues;

	public String getTargetKey() {
		return targetKey;
	}

	public void setTargetKey(String targetKey) {
		this.targetKey = targetKey;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public void setSignature(String signature) throws ClassNotFoundException {
		this.argTypes = unboxArgTypes(signature);
	}

	public void setArgValues(Object[] values) {
		this.argValues = values;
	}

	public void setArgTypes(Class<?>[] argTypes) {
		this.argTypes = argTypes;
	}

	public Class<?>[] getArgTypes() {
		return argTypes;
	}

	public Object[] getArgValues() {
		return argValues;
	}

	private Class<?>[] unboxArgTypes(String signature)
			throws ClassNotFoundException {
		Matcher matcher = argPattern.matcher(signature);
		List<Class<?>> argTypes = new ArrayList<Class<?>>();
		if (matcher.find()) {
			String[] classStr = matcher.group(1).split(",");
			for (int i = 0; i < classStr.length; i++) {
				argTypes.add(forName(classStr[i]));
			}
		}
		return argTypes.toArray(new Class[argTypes.size()]);
	}

	// there are utils (Spring, commons) doing that, but don't want any
	// dependency, no short supported due to its weird jvm impl
	private static final Map<String, Class<?>> PRIMITIVE_CLASSES = new HashMap<String, Class<?>>();

	static {
		Class<?>[] types = new Class[] { void.class, boolean.class, byte.class,
				char.class, int.class, float.class, double.class, long.class };
		for (Class<?> type : types) {
			PRIMITIVE_CLASSES.put(type.getName(), type);
		}
	}

	public static Class<?> forName(String name) throws ClassNotFoundException {
		Class<?> type = PRIMITIVE_CLASSES.get(name);
		if (type == null) {
			PRIMITIVE_CLASSES.put(name, type = Class.forName(name));
		}
		return type;
	}

}
