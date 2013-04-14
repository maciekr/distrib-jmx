package com.heyitworks.distribjmx;

import java.io.Serializable;

import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.heyitworks.util.distribjmx.core.Distributed;

/**
 * 
 * @author mrakowicz
 * 
 */
@ManagedResource(description = "i'm dummy")
public class Dummy implements DummyInterface, Serializable {

	@Distributed
	@ManagedOperation
	public void distributedOperation() {
		System.out.println("--> distributedOperation_Interface <-- " + this);
	}

	@ManagedOperation
	public void localOperation() {
		System.out.println("--> localOperation_Interface <-- " + this);
	}

	@Override
	public void totallyNoJmxInterfaceMethod() {
	}

}
