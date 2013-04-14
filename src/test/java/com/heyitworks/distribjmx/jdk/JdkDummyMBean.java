package com.heyitworks.distribjmx.jdk;

import com.heyitworks.util.distribjmx.core.Distributed;

public interface JdkDummyMBean {
	
	@Distributed
	void distributedOperation_InterfaceLevelAnnotation();
	
	void distributedOperation_ClassLevelAnnotation();

}
