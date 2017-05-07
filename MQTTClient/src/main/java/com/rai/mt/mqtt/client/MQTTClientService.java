package com.rai.mt.mqtt.client;

import java.io.InputStream;
import java.net.URI;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.osgi.service.component.annotations.Component;

import com.rai.mt.protocol.IAppProtocolClient;
import com.rai.mt.protocol.IReceiver;

@Component
public class MQTTClientService implements IAppProtocolClient{

	private static MqttClient mqtt;
	
	private IReceiver receiver;
	
	
	public static SSLSocketFactory configureSSLSocketFactory() throws Exception {
	    KeyStore ks = KeyStore.getInstance("JKS");
	    InputStream jksInputStream = MQTTClientService.class.getClassLoader().getResourceAsStream("clientkeystore.jks");
	    ks.load(jksInputStream, "password1".toCharArray());

	    KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
	    kmf.init(ks, "password1".toCharArray());

	    TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
	    tmf.init(ks);

	    SSLContext sc = SSLContext.getInstance("TLS");
	    TrustManager[] trustManagers = tmf.getTrustManagers();
	    sc.init(kmf.getKeyManagers(), trustManagers, null);

	    SSLSocketFactory ssf = sc.getSocketFactory();
	    return ssf;
	}
	
	@Override
	public void send(String request) {
		try {
			mqtt.publish("Request",
					new MqttMessage((request).getBytes()));
		} catch (MqttPersistenceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public void connect(URI url, IReceiver receiver1) throws Exception {
		mqtt = new MqttClient(url.toString(), "MqttJavaClient", new MemoryPersistence());
		
		this.receiver = receiver1;
		
		SSLSocketFactory ssf = configureSSLSocketFactory();
		MqttConnectOptions options = new MqttConnectOptions();
		
		options.setCleanSession(true);
		options.setKeepAliveInterval(30);
		
		
		
		
		
		options.setUserName("testuser");
		options.setPassword("passwd".toCharArray());
		
		if(url.toString().contains("ssl")){
			options.setSocketFactory(ssf);
		}
		
		//mqtt = new MqttClient("ssl://127.0.0.1:443", "MQTT_TestClient", new MqttDefaultFilePersistence());
		mqtt.setCallback(new MqttCallback() {

			@Override
			public void messageArrived(String arg0, MqttMessage arg1) throws Exception {
				System.out.println(" " + new String(arg1.getPayload()) + " "
						+ System.currentTimeMillis());
				receiver.onDataReceived(new String(arg1.getPayload()));

			}

			@Override
			public void deliveryComplete(IMqttDeliveryToken arg0) {
			//	System.out.println(" Delivery complete .");

			}

			@Override
			public void connectionLost(Throwable arg0) {
				System.out.println(" Connection Lost .");
				receiver.onError(" Connection Lost .");

			}
		});

		
		

		// if ( optionsComp.isLWTTopicSet() ) {
		// opts.setWill(mqtt.getTopic(optionsComp.getLWTTopic()),
		// optionsComp.getLWTData().getBytes(),
		// optionsComp.getLWTQoS(),
		// optionsComp.isLWTRetainSelected());
		// }
		mqtt.connect(options);
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		mqtt.subscribe("Response", 1);

	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

//	public static void main(String[] args) {
//
//		try {
//			
//			mqtt = new MqttClient("ssl://localhost:443", "SSLClientTest", new MqttDefaultFilePersistence());
//			SSLSocketFactory ssf = configureSSLSocketFactory();
//			MqttConnectOptions options = new MqttConnectOptions();
//			options.setSocketFactory(ssf);
//			options.setCleanSession(true);
//			options.setKeepAliveInterval(30);
//			
//			
//			options.setUserName("testuser");
//			options.setPassword("passwd".toCharArray());
//			
//			
//			
//			//mqtt = new MqttClient("ssl://127.0.0.1:443", "MQTT_TestClient", new MqttDefaultFilePersistence());
//			mqtt.setCallback(new MqttCallback() {
//
//				@Override
//				public void messageArrived(String arg0, MqttMessage arg1) throws Exception {
//					System.out.println(" " + new String(arg1.getPayload()) + " "
//							+ System.currentTimeMillis());
//
//				}
//
//				@Override
//				public void deliveryComplete(IMqttDeliveryToken arg0) {
//				//	System.out.println(" Delivery complete .");
//
//				}
//
//				@Override
//				public void connectionLost(Throwable arg0) {
//					System.out.println(" Connection Lost .");
//
//				}
//			});
//
//			
//			
//
//			// if ( optionsComp.isLWTTopicSet() ) {
//			// opts.setWill(mqtt.getTopic(optionsComp.getLWTTopic()),
//			// optionsComp.getLWTData().getBytes(),
//			// optionsComp.getLWTQoS(),
//			// optionsComp.isLWTRetainSelected());
//			// }
//			mqtt.connect(options);
//			try {
//				Thread.sleep(2000);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//
//			mqtt.subscribe("Response");
//
//			try {
//				Thread.sleep(2000);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//
//			while (true) {
//				try {
//					Thread.sleep(500);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				mqtt.publish("Request",
//						new MqttMessage((" Hello from Client " + System.currentTimeMillis() + " ").getBytes()));
//			}
//
//		} catch (MqttException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (Exception e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//
//	}

	
}
