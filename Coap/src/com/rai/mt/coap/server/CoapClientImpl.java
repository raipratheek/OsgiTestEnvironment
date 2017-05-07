package com.rai.mt.coap.server;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.config.NetworkConfig;
import org.eclipse.californium.elements.UDPConnector;
import org.eclipse.californium.elements.tcp.TcpClientConnector;
import org.eclipse.californium.scandium.DTLSConnector;
import org.eclipse.californium.scandium.config.DtlsConnectorConfig;
import org.eclipse.californium.scandium.dtls.pskstore.StaticPskStore;
import org.osgi.service.component.annotations.Component;

import com.rai.mt.protocol.IAppProtocolClient;
import com.rai.mt.protocol.IReceiver;

@Component
public class CoapClientImpl implements IAppProtocolClient {
	
	private static final boolean UDPConnection = true;

	private static volatile boolean flag = false;

	private static final String TRUST_STORE_PASSWORD = "rootPass";
	private static final String KEY_STORE_PASSWORD = "endPass";
	private static final String KEY_STORE_LOCATION = "keyStore.jks";
	private static final String TRUST_STORE_LOCATION = "trustStore.jks";
	

	private DTLSConnector dtlsConnector;

	private UDPConnector udpConnector;

	private TcpClientConnector tcpConnector;

	private IReceiver receiver;

	private CoapClient coapClient;

	private CoapHandler respHandler;

	public void createSecureClientConnector() {
		try {
			// load key store
			KeyStore keyStore = KeyStore.getInstance("JKS");
			InputStream in = CoapClientImpl.class.getClassLoader().getResourceAsStream(KEY_STORE_LOCATION);
			keyStore.load(in, KEY_STORE_PASSWORD.toCharArray());
			in.close();

			// load trust store
			KeyStore trustStore = KeyStore.getInstance("JKS");
			in = getClass().getClassLoader().getResourceAsStream(TRUST_STORE_LOCATION);
			trustStore.load(in, TRUST_STORE_PASSWORD.toCharArray());
			in.close();

			// You can load multiple certificates if needed
			Certificate[] trustedCertificates = new Certificate[1];
			trustedCertificates[0] = trustStore.getCertificate("root");

			DtlsConnectorConfig.Builder builder = new DtlsConnectorConfig.Builder(new InetSocketAddress(0));
			builder.setPskStore(new StaticPskStore("Client_identity", "secretPSK".getBytes()));
			builder.setIdentity((PrivateKey) keyStore.getKey("client", KEY_STORE_PASSWORD.toCharArray()),
					keyStore.getCertificateChain("client"), true);
			builder.setTrustStore(trustedCertificates);
			dtlsConnector = new DTLSConnector(builder.build());

		} catch (GeneralSecurityException | IOException e) {
			System.err.println("Could not load the keystore");
			e.printStackTrace();
		}
	}

	public void connect(URI url, boolean secure, boolean isUDP) {

		CoapResponse response = null;
		URI uri = url;

		coapClient = new CoapClient(uri);
		if (secure) {
			coapClient.setEndpoint(new CoapEndpoint(dtlsConnector, NetworkConfig.getStandard()));
		} else {
			if (isUDP) {
				coapClient.setEndpoint(new CoapEndpoint(udpConnector, NetworkConfig.getStandard()));
			} else {
				coapClient.setEndpoint(new CoapEndpoint(tcpConnector, NetworkConfig.getStandard()));
			}
		}
		respHandler = new CoapHandler() {

			@Override
			public void onLoad(CoapResponse response) {
				if (response != null) {

					// System.out.println(response.getCode());
					// System.out.println(response.getOptions());
					System.out.println(response.getResponseText());
					receiver.onDataReceived(response.getResponseText());

					// System.out.println("\nADVANCED\n");
					// // access advanced API with access to more details
					// // through .advanced()
					// System.out.println(Utils.prettyPrint(response));
					// flag = true;

				} else {
					System.out.println("No response received.");
				}

			}

			@Override
			public void onError() {
				System.out.println("Error occured.");

			}
		};

		CoapObserveRelation relation = coapClient.observe(respHandler);

		// if (response != null) {
		//
		// System.out.println(response.getCode());
		// System.out.println(response.getOptions());
		// System.out.println(response.getResponseText());
		//
		// System.out.println("\nADVANCED\n");
		// System.out.println(Utils.prettyPrint(response));
		//
		// } else {
		// System.out.println("No response received.");
		// }
	}

	// public static void main(String[] args) throws InterruptedException {
	//
	//
	//
	// synchronized (CoapClientImpl.class) {
	// CoapClientImpl.class.wait();
	// }
	// }

	@Override
	public void send(String request) {
		coapClient.post(respHandler, request, 0);

	}

	@Override
	public void connect(URI url, IReceiver receiver) throws Exception {
		this.receiver = receiver;
		if (url.toString().contains("coaps")) {
			createSecureClientConnector();
			connect(url, true , UDPConnection);
		} else {
			udpConnector = new UDPConnector();
			tcpConnector = new TcpClientConnector(1, 100000, 30000);
			connect(url, false, UDPConnection);
		}
	}
}
