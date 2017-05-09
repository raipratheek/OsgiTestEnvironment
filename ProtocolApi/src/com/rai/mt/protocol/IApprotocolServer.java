package com.rai.mt.protocol;

public interface IApprotocolServer {
	
	enum PROTOCOL {
		
		COAP,
		
		MQTT,
		
		WEBSOCKET
		
	}
	
	
	
	void send(String response);
	
	//void init(String address , int port) throws Exception;
	
	void registerReceiver(IReceiver receiver);
	
	void startServer();

	void init(String address, int port, boolean secure) throws Exception;
	
	PROTOCOL getType();

}
