package com.heyitworks.distribjmx.jdk;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import com.heyitworks.distribjmx.MbeanServerStartup;

public class JdkMBeanClientStartup {

	public static void main(String[] args) throws Exception {

		System.out.println(JdkMBeanClientStartup.class.getName());

		initContext(args[0], args[1], args[2]);

		Thread.sleep(10000);

		// look it up via jmx
		MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();

		ObjectName dummy = new ObjectName(
				"com.heyitworks.util.distribjmx:name=dummy,type=Dummy");
		ObjectName noInterfaceDummy = new ObjectName(
				"com.heyitworks.util.distribjmx:name=dummyNoInterface,type=DummyNoInterface");

		ObjectName jdkDummy = new ObjectName(
				"com.heyitworks.util.distribjmx.jdk:name=jdkDummy,type=JdkDummy");
		System.out.println("JDK DUMMY = "+jdkDummy);
		// invoke on dyna proxy
//		mBeanServer.invoke(dummy, "distributedOperation", null, null);
//		mBeanServer.invoke(dummy, "localOperation", null, null);

		// invoke on cglib proxy Integer i, String a, long b
//		mBeanServer.invoke(noInterfaceDummy, "distributedOperation", new Object[] { 1,
//				"a", 1L }, new String[] { "java.lang.Integer",
//				"java.lang.String", "java.lang.Long" });
//		mBeanServer.setAttribute(noInterfaceDummy, new Attribute("DistributedAttribute",
//				1234));
//
//		System.out.println("DistributedAttribute aggregate value = "
//				+ mBeanServer.getAttribute(noInterfaceDummy, "DistributedAttribute"));

		mBeanServer.invoke(jdkDummy,
				"distributedOperation_InterfaceLevelAnnotation", null, null);
		mBeanServer.invoke(jdkDummy, "distributedOperation_ClassLevelAnnotation",
				null, null);

	}

	private static void initContext(String rpcFactoryClass, String members, String tp)
			throws Exception {
		MbeanServerStartup.startup(rpcFactoryClass, members, tp);
	}

}
