package com.rai.mt.tcp.client;

import java.net.Socket;
import java.net.URI;

import org.osgi.service.component.annotations.Component;

import com.rai.mt.protocol.IAppProtocolClient;
import com.rai.mt.protocol.IReceiver;

@Component
public class SimpleSocketClient implements IAppProtocolClient {
	
	private OutStreamHandler outHandler;

	private InStreamHandler inHandler;
	

	@Override
	public void send(String request) {
		outHandler.addData(request);

	}

	@Override
	public void connect(URI url, IReceiver receiver) throws Exception {	
		Socket socConnection = new Socket(url.getHost(),url.getPort());
		outHandler = new OutStreamHandler(socConnection.getOutputStream());
		inHandler = new InStreamHandler(socConnection.getInputStream(), receiver);
		outHandler.start();
		inHandler.start();

	}

}
