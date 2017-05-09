package com.rai.mt.mqtt.server;

import org.osgi.service.component.annotations.Component;

import com.rai.mt.protocol.IApprotocolServer;
import com.rai.mt.protocol.IReceiver;

import io.moquette.interception.AbstractInterceptHandler;
import io.moquette.interception.messages.InterceptAcknowledgedMessage;
import io.moquette.interception.messages.InterceptConnectMessage;
import io.moquette.interception.messages.InterceptConnectionLostMessage;
import io.moquette.interception.messages.InterceptDisconnectMessage;
import io.moquette.interception.messages.InterceptPublishMessage;
import io.moquette.interception.messages.InterceptSubscribeMessage;
import io.moquette.interception.messages.InterceptUnsubscribeMessage;
import io.netty.buffer.ByteBuf;

@Component
public class MQTTServerService implements IApprotocolServer {

	private MQTTEmbeddedBroker mqttServer;

	private PublisherListener pubListener;

	private IReceiver receiver;

	private boolean isSecureServer = true;

	class PublisherListener extends AbstractInterceptHandler {

		@Override
		public void onConnect(InterceptConnectMessage msg) {
			receiver.onConnectionOpen(" Connection opened with clinet = " + msg.getClientID() + " with keep alive ping every"
					+ msg.getKeepAlive() + "sec protocol " + msg.getProtocolName() );
		}

		@Override
		public void onDisconnect(InterceptDisconnectMessage msg) {
			receiver.onError(" Disconnected.. "+ msg.getClientID());
		}

		@Override
		public void onConnectionLost(InterceptConnectionLostMessage msg) {
			receiver.onError(" Connection Lost.. "+ msg.getClientID());
		}

		@Override
		public void onSubscribe(InterceptSubscribeMessage msg) {
			
		}

		@Override
		public void onUnsubscribe(InterceptUnsubscribeMessage msg) {
		}

		@Override
		public void onMessageAcknowledged(InterceptAcknowledgedMessage msg) {
		}

		@Override
		public String getID() {
			return "EmbeddedLauncherPublishListener";
		}

		@Override
		public void onPublish(InterceptPublishMessage msg) {

			ByteBuf byteBuffer = msg.getPayload();
			byte[] byteArray = new byte[byteBuffer.readableBytes()];
			byteBuffer.readBytes(byteArray);
			String request = new String(byteArray);

			receiver.onDataReceived(request);
		}
	}

	@Override
	public void send(String response) {
		mqttServer.sendResponse(response);

	}

	@Override
	public void init(String address, int port, boolean secure) throws Exception {
		isSecureServer = secure;
		if (mqttServer == null) {
			mqttServer = new MQTTEmbeddedBroker();
			if ("localhost".equals(address)) {
				mqttServer.initServer("127.0.0.1", port, secure);
			} else {
				mqttServer.initServer(address, port, secure);
			}
		} else {
			System.out.println(" The MQTT server is already initialized. ");
		}

	}

	@Override
	public void registerReceiver(IReceiver receiver) {
		this.receiver = receiver;
	}

	@Override
	public void startServer() {

		pubListener = new PublisherListener();
		mqttServer.startMqttServer(pubListener, isSecureServer);
		// } else {
		// System.out.println(" The MQTT server is already running. ");
		// }
	}

	@Override
	public PROTOCOL getType() {
		return PROTOCOL.MQTT;
	}

}
