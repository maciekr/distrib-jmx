package com.heyitworks.distribjmx.jdk;

import com.heyitworks.util.distribjmx.core.Distributed;

public class JdkDummy implements JdkDummyMBean {

	public void distributedOperation_InterfaceLevelAnnotation() {
		System.out
				.println("--> distributedOperation_StandardMBean_InterfaceLevelAnnotation <-- "
						+ this);
	}

	@Distributed
	public void distributedOperation_ClassLevelAnnotation() {
		System.out
				.println("--> distributedOperation_StandardMBean_ClassLevelAnnotation <-- "
						+ this);
	}

}
