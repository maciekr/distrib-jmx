package com.heyitworks.distribjmx;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 
 * @author mrakowicz
 * 
 */
public class MbeanServerStartup {

	private static ClassPathXmlApplicationContext applicationContext;

	public static void main(String[] args) {
		try {

			// check it's legal factory name, nothing else
			String rpcDispatcherFactoryName = args[0];
			Class.forName(rpcDispatcherFactoryName);
			String members = args[1];
			String tp = args[2];

			startup(rpcDispatcherFactoryName, members, tp);

			Thread.sleep(1000000);

			tearDown();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void startup(String rpcFactoryName, String members, String tp)
			throws Exception {
		System.setProperty("dmbeans.tp", tp);
		System.setProperty("dmbeans.tcp.members", members);
		System.setProperty("rpcDispatcherFactoryClass", rpcFactoryName);
//		System.setProperty("dmbeans.bind.address", members);
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public synchronized void start() {
				if (applicationContext != null)
					applicationContext.close();
			}
		});
		applicationContext = new ClassPathXmlApplicationContext(System
				.getProperty("appCtx"));
	}

	static void tearDown() throws Exception {
		applicationContext.close();
	}

}
