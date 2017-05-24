package com.rai.mt.coap.server;

import java.io.IOException;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
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
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.Endpoint;
import org.eclipse.californium.core.network.EndpointManager;
import org.eclipse.californium.core.network.config.NetworkConfig;
import org.eclipse.californium.core.network.interceptors.MessageTracer;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.elements.tcp.TcpServerConnector;
import org.eclipse.californium.elements.tcp.TlsServerConnector;
import org.eclipse.californium.scandium.DTLSConnector;
import org.eclipse.californium.scandium.ScandiumLogger;
import org.eclipse.californium.scandium.config.DtlsConnectorConfig;
import org.eclipse.californium.scandium.dtls.cipher.CipherSuite;
import org.eclipse.californium.scandium.dtls.pskstore.InMemoryPskStore;
import org.osgi.service.component.annotations.Component;

import com.rai.mt.protocol.IApprotocolServer;
import com.rai.mt.protocol.IReceiver;

@Component
public class CoapServerImpl implements IApprotocolServer {

	private static final boolean UDPConnector = false;

	private CoapServer coapServer;

	private TestResource resource;

	private IReceiver receiver;

	private String response;

	private boolean isSecureServer = true;

	static {
		CaliforniumLogger.initialize();
		CaliforniumLogger.setLevel(Level.CONFIG);
		ScandiumLogger.initialize();
		ScandiumLogger.setLevel(Level.FINER);
	}

	// allows configuration via Californium.properties
	public static final int DTLS_PORT = NetworkConfig.getStandard().getInt(NetworkConfig.Keys.COAP_SECURE_PORT);

	private static final String TRUST_STORE_PASSWORD = "rootPass";
	private final static String KEY_STORE_PASSWORD = "endPass";
	private static final String KEY_STORE_LOCATION = "keyStore.jks";
	private static final String TRUST_STORE_LOCATION = "trustStore.jks";

	private static final int COAP_PORT = NetworkConfig.getStandard().getInt(NetworkConfig.Keys.COAP_PORT);

	public void startServer() {
		coapServer = new CoapServer();
		resource = new TestResource("coap");
		coapServer.add(resource);
		if (isSecureServer) {
			addSecureEndPoints();
		} else {
			addEndpoints();
		}
		coapServer.start();
		System.out.println(" Coap Server Started.");

	}

	/**
	 * Add individual endpoints listening on default CoAP port on all IPv4
	 * addresses of all network interfaces.
	 */
	private void addEndpoints() {
		if (UDPConnector) {
			for (InetAddress addr : EndpointManager.getEndpointManager().getNetworkInterfaces()) {
				// only binds to IPv4 addresses and localhost
				if (addr instanceof Inet4Address || addr.isLoopbackAddress()) {
					InetSocketAddress bindToAddress = new InetSocketAddress(addr, COAP_PORT);
					coapServer.addEndpoint(new CoapEndpoint(bindToAddress));
				}
			}

		} else {
			for (InetAddress addr : EndpointManager.getEndpointManager().getNetworkInterfaces()) {
				if (addr instanceof Inet4Address || addr.isLoopbackAddress()) {

					InetSocketAddress bindToAddress = new InetSocketAddress(addr, COAP_PORT);
					int threadCount = NetworkConfig.getStandard().getInt(NetworkConfig.Keys.TCP_WORKER_THREADS);
					int connTimeout = NetworkConfig.getStandard()
							.getInt(NetworkConfig.Keys.TCP_CONNECTION_IDLE_TIMEOUT);
					TcpServerConnector tcpconnector = new TcpServerConnector(bindToAddress, threadCount, connTimeout);
					coapServer.addEndpoint(new CoapEndpoint(tcpconnector, NetworkConfig.getStandard()));
				}
			}
		}
	}

	private void addSecureEndPoints() {
		if (UDPConnector) {
			try {
				// Pre-shared secrets
				InMemoryPskStore pskStore = new InMemoryPskStore();
				pskStore.setKey("password", "sesame".getBytes()); // from ETSI
																	// Plugtest
																	// test
																	// spec

				// load the trust store
				KeyStore trustStore = KeyStore.getInstance("JKS");
				InputStream inTrust = CoapServerImpl.class.getClassLoader().getResourceAsStream(TRUST_STORE_LOCATION);
				trustStore.load(inTrust, TRUST_STORE_PASSWORD.toCharArray());

				// You can load multiple certificates if needed
				Certificate[] trustedCertificates = new Certificate[1];
				trustedCertificates[0] = trustStore.getCertificate("root");

				// load the key store
				KeyStore keyStore = KeyStore.getInstance("JKS");
				InputStream in = CoapServerImpl.class.getClassLoader().getResourceAsStream(KEY_STORE_LOCATION);
				keyStore.load(in, KEY_STORE_PASSWORD.toCharArray());

				DtlsConnectorConfig.Builder config = new DtlsConnectorConfig.Builder(new InetSocketAddress(DTLS_PORT));
				config.setSupportedCipherSuites(new CipherSuite[] { CipherSuite.TLS_PSK_WITH_AES_128_CCM_8,
						CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CCM_8 });
				config.setPskStore(pskStore);
				config.setIdentity((PrivateKey) keyStore.getKey("server", KEY_STORE_PASSWORD.toCharArray()),
						keyStore.getCertificateChain("server"), true);
				config.setTrustStore(trustedCertificates);

				DTLSConnector connector = new DTLSConnector(config.build());

				coapServer.addEndpoint(new CoapEndpoint(connector, NetworkConfig.getStandard()));

				// add special interceptor for message traces
				for (Endpoint ep : coapServer.getEndpoints()) {
					ep.addInterceptor(new MessageTracer());
				}
			} catch (GeneralSecurityException | IOException e) {
				System.err.println("Could not load the keystore");
				e.printStackTrace();
			}
		} else {
			try {
				SSLContext serverContext = SSLContext.getInstance("TLS");

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
				serverContext.init(kmf.getKeyManagers(), trustManagers, null);

				TlsServerConnector connector = new TlsServerConnector(serverContext, new InetSocketAddress(DTLS_PORT),
						1, 10000);

				coapServer.addEndpoint(new CoapEndpoint(connector, NetworkConfig.getStandard()));

				// add special interceptor for message traces
				for (Endpoint ep : coapServer.getEndpoints()) {
					ep.addInterceptor(new MessageTracer());
				}
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

	class TestResource extends CoapResource {

		public TestResource(String name) {
			super(name);
			this.setObservable(true);

		}

		@Override
		public void handleGET(CoapExchange exchange) {
			// respond to the request
			exchange.respond(response);
		}

		public void handlePOST(CoapExchange exchange) {
			receiver.onDataReceived(exchange.getRequestText());
		}
	}

	@Override
	public void send(String response) {
		this.response = response;
		resource.changed();
	}

	@Override
	public void init(String address, int port, boolean secure) throws Exception {
		isSecureServer = secure;

	}

	@Override
	public void registerReceiver(IReceiver receiver) {
		this.receiver = receiver;

	}

	@Override
	public PROTOCOL getType() {
		return PROTOCOL.COAP;
	}
}
