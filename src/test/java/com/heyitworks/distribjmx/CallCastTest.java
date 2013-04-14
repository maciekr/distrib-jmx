package com.heyitworks.distribjmx;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.Future;

import junit.framework.TestCase;

import com.heyitworks.testutils.JvmLauncher;
import com.heyitworks.util.distribjmx.comm.hc.HcRpcDispatcherFactory;
import com.heyitworks.util.distribjmx.comm.jg.JGroupsRpcDispatcherFactory;

/**
 * 
 * @author mrakowicz
 * 
 */
public class CallCastTest extends TestCase {

	public void testCallCastJGroupsUDP() throws Exception {
		testCallCast(JGroupsRpcDispatcherFactory.class.getName(),
				getIpExternalNIC().getHostAddress() + "[7800]", "udp");
	}

	public void testCallCastJGroupsTCP() throws Exception {
		testCallCast(JGroupsRpcDispatcherFactory.class.getName(),
				getIpExternalNIC().getHostAddress() + "[7800]", "tcp");
	}

	public void testCallCastHcTCP() throws Exception {
		testCallCast(HcRpcDispatcherFactory.class.getName(),
				getIpExternalNIC().getHostAddress(), "tcp");
	}

	private void testCallCast(String rpcFactoryClass, String members, String tp)
			throws Exception {

		Future<List<String>> processes = null;

		try {

			processes = spawnContexts(rpcFactoryClass, members, tp);

			Thread.sleep(10000);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			List<String> outs = processes.get();
			for (String out : outs) {
				verify(out);
			}
		}

	}

	protected Future<List<String>> spawnContexts(final String rpcFactoryClass,
			final String members, final String tp) throws Exception {

		JvmLauncher jvmLauncher = new JvmLauncher();
		jvmLauncher.setEntryPoints(new ArrayList<String[]>() {
			{
				add(new String[] { "-DappCtx=classpath:" + getAppCtxLocation(),
						MbeanServerStartup.class.getName(), rpcFactoryClass,
						members, tp });
				add(new String[] { "-DappCtx=classpath:" + getAppCtxLocation(),
						ClientStartup.class.getName(), rpcFactoryClass,
						members, tp });
			}
		});
		jvmLauncher.setPauseBetweenLaunches(1000);
		return jvmLauncher.launchProcesses();
	}

	protected String getAppCtxLocation() {
		return "appCtx.xml";
	}

	protected void verify(String out) throws Exception {
		System.out.println("------ VERIFYING OUTs ------ ");
		System.out.println(out);
		assertTrue(out.contains("--> distributedOperation_Interface <--"));
		if (out.contains(ClientStartup.class.getSimpleName())) {
			assertTrue(out.contains("--> localOperation_Interface <--"));
			assertTrue(out
					.contains("DistributedAttribute aggregate value = " + 2468));
		} else {
			assertFalse(out.contains("--> localOperation_Interface <--"));
		}
		assertTrue(out.contains("--> distributedOperation_CGLIBProxy <--"));
		assertTrue(out
				.contains("--> setDistributedAttribute_CGLIBProxy 1234 <--"));
	}

	static InetAddress getIpExternalNIC() throws Exception {
		Enumeration<NetworkInterface> nets = NetworkInterface
				.getNetworkInterfaces();
		for (NetworkInterface netint : Collections.list(nets)) {
			if (netint.isUp() && !netint.isLoopback() && !netint.isVirtual()) {
				Enumeration<InetAddress> inetAddresses = netint
						.getInetAddresses();
				while (inetAddresses.hasMoreElements()) {
					InetAddress inetAddr = inetAddresses.nextElement();
					if (inetAddr instanceof Inet6Address) {
						continue;
					}
					return inetAddr;
				}
			}
		}
		return InetAddress.getLocalHost();
	}
}
