package com.heyitworks.distribjmx;

import java.lang.management.ManagementFactory;

import javax.management.Attribute;
import javax.management.MBeanServer;
import javax.management.ObjectName;

public class ClientStartup {

	public static void main(String[] args) throws Exception {

		System.out.println(ClientStartup.class.getName());

		initContext(args[0],args[1],args[2]);

		Thread.sleep(10000);

		// look it up via jmx
		MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
		ObjectName dummy = new ObjectName(
				"com.heyitworks.util.distribjmx:name=dummy,type=Dummy");
		ObjectName dummy2 = new ObjectName(
				"com.heyitworks.util.distribjmx:name=dummyNoInterface,type=DummyNoInterface");

		// invoke on dyna proxy
		mBeanServer.invoke(dummy, "distributedOperation", null, null);
		mBeanServer.invoke(dummy, "localOperation", null, null);

		// invoke on cglib proxy Integer i, String a, long b
		mBeanServer.invoke(dummy2, "distributedOperation", new Object[] { 1,
				"a", 1L }, new String[] { "java.lang.Integer",
				"java.lang.String", "java.lang.Long" });
		mBeanServer.setAttribute(dummy2, new Attribute("DistributedAttribute",
				1234));

		System.out.println("DistributedAttribute aggregate value = "
				+ mBeanServer.getAttribute(dummy2, "DistributedAttribute"));

	}

	private static void initContext(String rpcFactoryName, String members, String tp) throws Exception {
		MbeanServerStartup.startup(rpcFactoryName, members, tp);
	}

}
