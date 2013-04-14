package com.heyitworks.distribjmx.jdk;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import com.heyitworks.testutils.JvmLauncher;
import com.heyitworks.distribjmx.CallCastTest;
import com.heyitworks.distribjmx.MbeanServerStartup;

/**
 * 
 * @author mrakowicz
 * 
 */
public class JdkCallCastTest extends CallCastTest {

	protected Future<List<String>> spawnContexts(final String rpcFactoryClass,
			final String members, final String tp) throws Exception {

		JvmLauncher jvmLauncher = new JvmLauncher();
		jvmLauncher.setEntryPoints(new ArrayList<String[]>() {
			{
				add(new String[] { "-DappCtx=classpath:" + getAppCtxLocation(),
						MbeanServerStartup.class.getName(), rpcFactoryClass,
						members, tp });
				add(new String[] { "-DappCtx=classpath:" + getAppCtxLocation(),
						JdkMBeanClientStartup.class.getName(), rpcFactoryClass,
						members, tp });
			}
		});
		jvmLauncher.setPauseBetweenLaunches(1000);
		return jvmLauncher.launchProcesses();
	}

	protected void verify(String out) throws Exception {
		// super.verify(out);
		
		System.out.println("-------> OUT = "+out);
		
		// verify 3rd bean stuff here
		assertTrue(out
				.contains("--> distributedOperation_StandardMBean_InterfaceLevelAnnotation <-- "));

		assertTrue(out
				.contains("--> distributedOperation_StandardMBean_ClassLevelAnnotation <-- "));
	}

}
