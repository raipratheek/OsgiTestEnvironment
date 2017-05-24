package com.rai.mt.tcp.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.osgi.service.component.annotations.Component;

import com.rai.mt.protocol.IApprotocolServer;
import com.rai.mt.protocol.IReceiver;
import com.rai.mt.tcp.client.InStreamHandler;
import com.rai.mt.tcp.client.OutStreamHandler;

@Component
public class SimpleSocketServer implements IApprotocolServer {

	private int PORT = 8025;

	private IReceiver receiver;

	private OutStreamHandler outHandler;

	private InStreamHandler inHandler;

	@Override
	public void send(String response) {
		outHandler.addData(response);
	}

	@Override
	public void registerReceiver(IReceiver receiver) {
		this.receiver = receiver;
	}

	@Override
	public void startServer() {
		try {
			ServerSocket serverSoc = new ServerSocket(PORT);
			Socket connection = serverSoc.accept();
			OutputStream outStream = connection.getOutputStream();
			InputStream inStream = connection.getInputStream();
			outHandler = new OutStreamHandler(outStream);
			inHandler = new InStreamHandler(inStream, receiver);
			outHandler.start();
			inHandler.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void init(String address, int port, boolean secure) throws Exception {
		PORT = port;
	}

	@Override
	public PROTOCOL getType() {
		return PROTOCOL.SIMPLE_SOCKET;
	}

}
