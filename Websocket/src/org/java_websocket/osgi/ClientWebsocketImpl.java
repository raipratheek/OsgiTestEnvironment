package org.java_websocket.osgi;

import java.net.URI;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import com.rai.mt.protocol.IReceiver;

public class ClientWebsocketImpl extends WebSocketClient {

	IReceiver receiver;

	public ClientWebsocketImpl(URI serverURI) {
		super(serverURI);
	}

	@Override
	public void onOpen(ServerHandshake handshakedata) {
		receiver.onConnectionOpen("connection opened to server with HTTP status = " + handshakedata.getHttpStatus());
	}

	@Override
	public void onMessage(String message) {
		receiver.onDataReceived(message);

	}

	@Override
	public void onClose(int code, String reason, boolean remote) {
		receiver.onError("connection closed " + " with exit code " + code + " additional info: " + reason);

	}

	@Override
	public void onError(Exception ex) {
		receiver.onError("an error occured on connection " + ":" + ex);

	}

	public void registerReceiver(IReceiver receiver) {
		this.receiver = receiver;
	}

}
