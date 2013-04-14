package com.heyitworks.util.distribjmx.comm.jg;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.jgroups.Channel;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.blocks.GroupRequest;
import org.jgroups.blocks.MessageDispatcher;
import org.jgroups.blocks.RequestHandler;
import org.jgroups.util.RspList;

import com.heyitworks.util.distribjmx.comm.RpcDispatcherFactory;
import com.heyitworks.util.distribjmx.core.DispatcherInitializationException;
import com.heyitworks.util.distribjmx.core.InvocationException;
import com.heyitworks.util.distribjmx.core.RpcDispatcher;
import com.heyitworks.util.distribjmx.core.RpcRequest;

/**
 * @author mrakowicz
 */
public class JGroupsRpcDispatcherFactory implements RpcDispatcherFactory {

	@Override
	public RpcDispatcher getDispatcherInstance() {
		return JGroupsRpcDispatcher.getInstance();
	}

	public static class JGroupsRpcDispatcher implements RpcDispatcher,
			RequestHandler {

		public static final JChannelManager CHANNEL_MANAGER = new JChannelManager();

		final Map<String, Object> targetMap;
		public final MessageDispatcher messageDispatcher;

		private static class LazyInstanceHolder {
			private static RpcDispatcher instance = init();

			private static final RpcDispatcher init() {
				return new JGroupsRpcDispatcher();
			}
		}

		public static RpcDispatcher getInstance() {
			return LazyInstanceHolder.instance;
		}

		private JGroupsRpcDispatcher() throws DispatcherInitializationException {
			try {
				targetMap = new HashMap<String, Object>();

				messageDispatcher = CHANNEL_MANAGER
						.createMessageDispatcher(this);

			} catch (Exception e) {
				e.printStackTrace();
				throw new DispatcherInitializationException(e);
			}
		}

		public void registerTarget(final String targetKey, final Object target) {
			targetMap.put(targetKey, target);
		}

		public Collection<?> dispatch(final RpcRequest request) {
			try {
				ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
				ObjectOutputStream objectOutputStream = new ObjectOutputStream(
						byteOutputStream);
				objectOutputStream.writeObject(request);

				Message message = new Message();
				message.setBuffer(byteOutputStream.toByteArray());
				RspList rspList = messageDispatcher.castMessage(null, message,
						GroupRequest.GET_ALL, 5000);
				return rspList.getResults();
			} catch (Exception e) {
				throw new InvocationException(e.getMessage(), e);
			}
		}

		@Override
		public Object handle(final Message message) {
			try {
				ByteArrayInputStream byteInputStream = new ByteArrayInputStream(
						message.getBuffer(), 0, message.getLength());
				ObjectInputStream objectInputStream = new ObjectInputStream(
						byteInputStream);
				RpcRequest rpcRequest = (RpcRequest) objectInputStream
						.readObject();
				Object target = targetMap.get(rpcRequest.getTargetKey());
				Class<?>[] types = rpcRequest.getArgTypes();
				Object[] values = rpcRequest.getArgValues();
				Method method = target.getClass().getMethod(
						rpcRequest.getMethodName(), types);
				return method.invoke(target, values);

			} catch (Exception e) {
				throw new InvocationException(e.getMessage(), e);
			}
		}

		public void destroy() {
			CHANNEL_MANAGER.closeChannel(this.messageDispatcher);
		}

		public static class JChannelManager {

			private JChannel channel;

			public MessageDispatcher createMessageDispatcher(
					RequestHandler handler) throws Exception {
				channel = new JChannel(Thread.currentThread()
						.getContextClassLoader().getResource(getConfigFile()));
				channel.setOpt(Channel.LOCAL, false);
				channel.setOpt(Channel.AUTO_RECONNECT, true);
				channel.connect("DistributedMBeansCluster");
				return new MessageDispatcher(channel, null, null, handler);
			}

			public void closeChannel(MessageDispatcher messageDispatcher) {
				// TODO:
			}

			private String getConfigFile() {
				String transportProtocol = System.getProperty("dmbeans.tp") == null ? "udp"
						: System.getProperty("dmbeans.tp");
				return transportProtocol + "_jgroups.xml";
			}

		}
	}

}
