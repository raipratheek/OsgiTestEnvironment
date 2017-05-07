package com.rai.mt.mqtt.server;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import io.moquette.BrokerConstants;
import io.moquette.interception.AbstractInterceptHandler;
import io.moquette.interception.InterceptHandler;
import io.moquette.server.config.IConfig;
import io.moquette.server.netty.MessageBuilder;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttQoS;

public class MQTTEmbeddedBroker {

	private static final boolean isTcpConnection = false;

	private io.moquette.server.Server mqttBroker;

	private IConfig classPathConfig;

	public void initServer(String address, int port, boolean secure) {

		classPathConfig = new MQTTConfigProperties();

		classPathConfig.setProperty(BrokerConstants.HOST_PROPERTY_NAME, address);
		// setProperty(BrokerConstants.WEB_SOCKET_PORT_PROPERTY_NAME,
		// Integer.toString(BrokerConstants.WEBSOCKET_PORT));
		classPathConfig.setProperty(BrokerConstants.PASSWORD_FILE_PROPERTY_NAME, "password_file.conf");
		classPathConfig.setProperty(BrokerConstants.ALLOW_ANONYMOUS_PROPERTY_NAME, Boolean.FALSE.toString());
		classPathConfig.setProperty(BrokerConstants.ALLOW_ZERO_BYTE_CLIENT_ID_PROPERTY_NAME, Boolean.FALSE.toString());

		if (secure) {
			// setProperty(BrokerConstants.PERSISTENT_STORE_PROPERTY_NAME,
			// BrokerConstants.DEFAULT_PERSISTENT_PATH);

			classPathConfig.setProperty(BrokerConstants.AUTHENTICATOR_CLASS_NAME, "");
			classPathConfig.setProperty(BrokerConstants.AUTHORIZATOR_CLASS_NAME, "");
			classPathConfig.setProperty(BrokerConstants.JKS_PATH_PROPERTY_NAME, "serverkeystore.jks");
			classPathConfig.setProperty(BrokerConstants.SSL_PORT_PROPERTY_NAME, Integer.toString(port));
			classPathConfig.setProperty(BrokerConstants.KEY_STORE_PASSWORD_PROPERTY_NAME, "password");
			classPathConfig.setProperty(BrokerConstants.KEY_MANAGER_PASSWORD_PROPERTY_NAME, "password");
		} else {
			// classPathConfig = new ResourceLoaderConfig(classpathLoader,
			// "moquetteunsecure.conf");
			if (isTcpConnection) {
				// for TCP connection.
				classPathConfig.setProperty(BrokerConstants.PORT_PROPERTY_NAME, Integer.toString(port));
			} else {
				classPathConfig.setProperty(BrokerConstants.PORT_PROPERTY_NAME, BrokerConstants.DISABLED_PORT_BIND);
				// for websocket connectionBrokerConstants.DISABLED_PORT_BIND
				classPathConfig.setProperty(BrokerConstants.WEB_SOCKET_PORT_PROPERTY_NAME, Integer.toString(port));
			}
		}
	}

	public void startMqttServer(AbstractInterceptHandler pubListener, boolean secure) {
		mqttBroker = new io.moquette.server.Server();
		List<? extends InterceptHandler> userHandlers = Arrays.asList(pubListener);

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				System.out.println("Stopping broker");
				mqttBroker.stopServer();
				System.out.println("Broker stopped");
			}
		});
		try {
			mqttBroker.startServer(classPathConfig, userHandlers);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void sendResponse(String msg) {

		MqttPublishMessage message = MessageBuilder.publish().topicName("Response").retained(true)
				// qos(MqttQoS.AT_MOST_ONCE);
				.qos(MqttQoS.AT_LEAST_ONCE)
				// .qos(MqttQoS.EXACTLY_ONCE)
				.payload(msg.getBytes(Charset.forName("UTF-8"))).build();
		mqttBroker.internalPublish(message, "INTRLPUB");
	}

}
