package com.heyitworks.distribjmx;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.heyitworks.util.distribjmx.core.Distributed;
import com.heyitworks.util.distribjmx.handlers.IntegerResultHandler;

/**
 * 
 * @author mrakowicz
 * 
 */
@ManagedResource
public class DummyNoInterface {

	private Integer attr = -1;

	@ManagedOperation
	@Distributed
	public void distributedOperation(Integer i, String a, Long b) {
		System.out.println("--> distributedOperation_CGLIBProxy <--");
	}

	@ManagedAttribute
	@Distributed
	public void setDistributedAttribute(Integer attr) {
		System.out.println("--> setDistributedAttribute_CGLIBProxy " + attr
				+ " <--");
		this.attr = attr;
	}

	@Distributed(handler = IntegerResultHandler.class)
	@ManagedAttribute
	public Integer getDistributedAttribute() {
		System.out.println("--> getDistributedAttribute_CGLIBProxy " + attr + " <---");
		return this.attr;
	}

}
