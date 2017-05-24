package com.rai.mt.coap.server;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.logging.Level;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.eclipse.californium.core.CaliforniumLogger;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.config.NetworkConfig;
import org.eclipse.californium.elements.UDPConnector;
import org.eclipse.californium.elements.tcp.TcpClientConnector;
import org.eclipse.californium.elements.tcp.TlsClientConnector;
import org.eclipse.californium.scandium.DTLSConnector;
import org.eclipse.californium.scandium.ScandiumLogger;
import org.eclipse.californium.scandium.config.DtlsConnectorConfig;
import org.eclipse.californium.scandium.dtls.pskstore.StaticPskStore;
import org.osgi.service.component.annotations.Component;

import com.rai.mt.protocol.IAppProtocolClient;
import com.rai.mt.protocol.IReceiver;

@Component
public class CoapClientImpl implements IAppProtocolClient {

	private static final boolean UDPConnection = false;

	private static final String TRUST_STORE_PASSWORD = "rootPass";
	private static final String KEY_STORE_PASSWORD = "endPass";
	private static final String KEY_STORE_LOCATION = "keyStore.jks";
	private static final String TRUST_STORE_LOCATION = "trustStore.jks";

	static {
		CaliforniumLogger.initialize();
		CaliforniumLogger.setLevel(Level.FINER);
		ScandiumLogger.initialize();
		ScandiumLogger.setLevel(Level.FINER);
	}

	private DTLSConnector dtlsConnector;

	private UDPConnector udpConnector;

	private TcpClientConnector tcpConnector;

	private TlsClientConnector tlsconnector;

	private IReceiver receiver;

	private CoapClient coapClient;

	private CoapHandler respHandler;

	public void createSecureClientConnector() {
		if (UDPConnection) {
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
		} else {
			try {
				SSLContext clientContext = SSLContext.getInstance("TLS");
				// load the trust store
				KeyStore trustStore = KeyStore.getInstance("JKS");
				InputStream inTrust = CoapServerImpl.class.getClassLoader().getResourceAsStream(TRUST_STORE_LOCATION);
				trustStore.load(inTrust, TRUST_STORE_PASSWORD.toCharArray());

				// load the key store
				KeyStore keyStore = KeyStore.getInstance("JKS");
				InputStream in = CoapServerImpl.class.getClassLoader().getResourceAsStream(KEY_STORE_LOCATION);
				keyStore.load(in, KEY_STORE_PASSWORD.toCharArray());

				KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
				kmf.init(keyStore, KEY_STORE_PASSWORD.toCharArray());

				TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
				tmf.init(trustStore);
				TrustManager[] trustManagers = tmf.getTrustManagers();
				clientContext.init(kmf.getKeyManagers(), trustManagers, null);

				tlsconnector = new TlsClientConnector(clientContext, 1, 10000, 30000);
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (KeyStoreException e) {	
				e.printStackTrace();
			} catch (CertificateException e) {	
				e.printStackTrace();
			} catch (IOException e) {	
				e.printStackTrace();
			} catch (UnrecoverableKeyException e) {	
				e.printStackTrace();
			} catch (KeyManagementException e) {
				e.printStackTrace();
			}

		}
	}

	public void connect(URI url, boolean secure, boolean isUDP) {

		URI uri = url;

		coapClient = new CoapClient(uri);

		if (secure) {
			if (isUDP) {
				coapClient.setEndpoint(new CoapEndpoint(dtlsConnector, NetworkConfig.getStandard()));
			} else {
				coapClient.setEndpoint(new CoapEndpoint(tlsconnector, NetworkConfig.getStandard()));
			}

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
					System.out.println(response.getResponseText());
					receiver.onDataReceived(response.getResponseText());
				} else {
					System.out.println("No response received.");
				}
			}

			@Override
			public void onError() {
				System.out.println("Error occured.");
			}
		};
		coapClient.observe(respHandler);		
	}


	@Override
	public void send(String request) {
		coapClient.post(respHandler, request, 0);
	}

	@Override
	public void connect(URI url, IReceiver receiver) throws Exception {
		this.receiver = receiver;
		if (url.toString().contains("coaps")) {
			createSecureClientConnector();
			connect(url, true, UDPConnection);
		} else {
			udpConnector = new UDPConnector();
			tcpConnector = new TcpClientConnector(1, 10000, 30000);
			connect(url, false, UDPConnection);
		}
	}
}
