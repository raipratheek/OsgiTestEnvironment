package org.java_websocket.osgi;

import java.net.URI;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import com.rai.mt.protocol.IAppProtocolClient;
import com.rai.mt.protocol.IReceiver;

import org.java_websocket.osgi.ClientWebsocketImpl;
import org.osgi.service.component.annotations.Component;

@Component
public class ClientService implements IAppProtocolClient {

	private ClientWebsocketImpl client;

	@Override
	public void send(String request) {
		client.send(request);

	}

	@Override
	public void connect(URI url, IReceiver receiver) throws Exception {

		client = new ClientWebsocketImpl(
				url/* new URI("wss://localhost:8025") */);
		client.registerReceiver(receiver);

		if (url.toString().contains("wss")) {
			// load up the key store
			String STORETYPE = "JKS";
			String KEYSTORE = "keystore.jks";
			String STOREPASSWORD = "keystorepass";
			String KEYPASSWORD = "keypass";

			KeyStore ks = KeyStore.getInstance(STORETYPE);
			// File kf = new File( KEYSTORE );
			ks.load(this.getClass().getClassLoader().getResourceAsStream(KEYSTORE), STOREPASSWORD.toCharArray());

			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
			kmf.init(ks, KEYPASSWORD.toCharArray());
			TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
			tmf.init(ks);

			SSLContext sslContext = null;
			sslContext = SSLContext.getInstance("TLS");
			sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
			// sslContext.init( null, null, null ); // will use java's default
			// key and trust store which is sufficient unless you deal with
			// self-signed certificates

			SSLSocketFactory factory = sslContext.getSocketFactory();// (SSLSocketFactory)
																		// SSLSocketFactory.getDefault();

			client.setSocket(factory.createSocket());
		}

		client.connect();

	}

}
