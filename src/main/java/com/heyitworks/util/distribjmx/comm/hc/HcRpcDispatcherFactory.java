package com.heyitworks.util.distribjmx.comm.hc;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import org.apache.commons.lang.StringUtils;

import com.hazelcast.config.Config;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.config.Interfaces;
import com.hazelcast.config.Join;
import com.hazelcast.config.MulticastConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.config.TcpIpConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.Member;
import com.hazelcast.core.MultiTask;
import com.heyitworks.util.distribjmx.comm.RpcDispatcherFactory;
import com.heyitworks.util.distribjmx.core.InvocationException;
import com.heyitworks.util.distribjmx.core.RpcDispatcher;
import com.heyitworks.util.distribjmx.core.RpcRequest;

/**
 * 
 * @author mrakowicz
 * 
 */
public class HcRpcDispatcherFactory implements RpcDispatcherFactory {

	@Override
	public RpcDispatcher getDispatcherInstance() {
		return new HcRpcDispatcher();
	}

	public static class HcRpcDispatcher implements RpcDispatcher,
			Serializable {

		private static final long serialVersionUID = -8143284701385097159L;

		private static Map<String, Object> targetMap;

		private ExecutorService executorService;
		private Member localMember;

		public HcRpcDispatcher() {
			targetMap = new HashMap<String, Object>();
			initHC();
		}

		private void initHC() {
			TcpIpConfig tcpConfig = new TcpIpConfig();
			tcpConfig.setEnabled(true);
			Interfaces interfaces = null;
			String[] members = getMembers();
			for (String member : members) {
				tcpConfig.addMember(member);
				if (interfaces == null) {
					interfaces = new Interfaces();
					interfaces.setEnabled(true);
					interfaces.addInterface(member);
				}
			}
			Hazelcast
					.init(new Config()
							.setGroupConfig(
									new GroupConfig("DistributedMBeansCluster"))
							.setNetworkConfig(
									new NetworkConfig().setInterfaces(
											interfaces).setJoin(
											new Join().setMulticastConfig(
													new MulticastConfig()
															.setEnabled(false))
													.setTcpIpConfig(tcpConfig))));

			localMember = Hazelcast.getCluster().getLocalMember();
			executorService = Hazelcast
					.getExecutorService("DistributedMBeansCluster");
		}
		
		private String[] getMembers() {
			String memberIps = System.getProperty("dmbeans.tcp.members");
			if (StringUtils.isBlank(memberIps)) {
				throw new RuntimeException(
						"No Cluster Member IPs (TCP) specified. Set dmbeans.tcp.members system variable (,-separated list)!!");
			}
			return memberIps.split(";");
		}

		@Override
		public Collection<?> dispatch(RpcRequest request) {
			try {
				Set<Member> members = Hazelcast.getCluster().getMembers();
				members.remove(localMember);
				MultiTask<?> task = new MultiTask<Object>(
						new RemoteTask<Object>(request), members);
				executorService.execute(task);

				return task.get();

			} catch (Exception e) {
				throw new InvocationException(e.getMessage(), e);
			}
		}

		@Override
		public void destroy() {
			Hazelcast.shutdownAll();
		}

		@Override
		public void registerTarget(String targetKey, Object target) {
			targetMap.put(targetKey, target);
		}
	}

	static class RemoteTask<T> implements Callable<Object>, Serializable {

		private static final long serialVersionUID = -7754159466577253917L;

		private RpcRequest request;

		public RemoteTask(RpcRequest request) {
			this.request = request;
		}

		@Override
		public Object call() throws Exception {
			try {
				Object target = HcRpcDispatcher.targetMap.get(request
						.getTargetKey());
				Class<?>[] types = request.getArgTypes();
				Object[] values = request.getArgValues();
				Method method = target.getClass().getMethod(
						request.getMethodName(), types);
				return method.invoke(target, values);
			} catch (Exception e) {
				throw new InvocationException(e.getMessage(), e);
			}
		}

	}
}
