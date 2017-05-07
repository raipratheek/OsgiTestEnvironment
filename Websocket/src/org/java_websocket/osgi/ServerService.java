package org.java_websocket.osgi;

import java.net.InetSocketAddress;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.java_websocket.server.DefaultSSLWebSocketServerFactory;
import org.osgi.service.component.annotations.Component;

import com.rai.mt.protocol.IApprotocolServer;
import com.rai.mt.protocol.IReceiver;

@Component
public class ServerService implements IApprotocolServer {

	private ServerWebsocketImpl server;

	private boolean isSecureServer;

	@Override
	public void send(String response) {
		server.sendToAll(response);
	}

	@Override
	public void init(String address, int portNum, boolean secure) throws Exception {
		String host = address;
		int port = portNum;
		isSecureServer = secure;
		server = new ServerWebsocketImpl(new InetSocketAddress(host, port));

		if (secure) {
			String STORETYPE = "JKS";
			String KEYSTORE = "keystore.jks";
			String STOREPASSWORD = "keystorepass";
			String KEYPASSWORD = "keypass";

			KeyStore ks = KeyStore.getInstance(STORETYPE);
			// File kf = new File(t );
			ks.load(this.getClass().getClassLoader().getResourceAsStream(KEYSTORE), STOREPASSWORD.toCharArray());

			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
			kmf.init(ks, KEYPASSWORD.toCharArray());
			TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
			tmf.init(ks);

			SSLContext sslContext = null;
			sslContext = SSLContext.getInstance("TLS");
			sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

			server.setWebSocketFactory(new DefaultSSLWebSocketServerFactory(sslContext));
		}

		System.out.println(" Initialized server.");

	}

	@Override
	public void registerReceiver(IReceiver receiver) {
		server.registerReceiver(receiver);

	}

	@Override
	public void startServer() {
		server.start();
		System.out.println(" Server Started.");
	}

}
