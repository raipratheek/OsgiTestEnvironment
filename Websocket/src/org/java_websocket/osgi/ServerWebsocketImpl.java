package org.java_websocket.osgi;

import java.net.InetSocketAddress;
import java.util.Collection;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import com.rai.mt.protocol.IReceiver;


public class ServerWebsocketImpl extends WebSocketServer {

	WebSocketServer server;
	
	IReceiver receiver;
	
	
	public ServerWebsocketImpl(InetSocketAddress address) {
		super(address);
	}

	
	@Override
	public void onOpen(WebSocket conn, ClientHandshake handshake) {
		System.out.println("new connection to " + conn.getRemoteSocketAddress());
		receiver.onConnectionOpen("new connection to " + conn.getRemoteSocketAddress());
	}

	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote) {
		System.out.println("closed " + conn.getRemoteSocketAddress() + " with exit code " + code + " additional info: " + reason);
	    receiver.onError("closed " + conn.getRemoteSocketAddress() + " with exit code " + code + " additional info: " + reason);
	}

	@Override
	public void onMessage(WebSocket conn, String message) {
		System.out.println("received message from "	+ conn.getRemoteSocketAddress() + ": " + message);
		receiver.onDataReceived(message);
	}

	@Override
	public void onError(WebSocket conn, Exception ex) {
		System.err.println("an error occured on connection " + conn.getRemoteSocketAddress()  + ":" + ex);
		receiver.onError("an error occured on connection " + conn.getRemoteSocketAddress()  + ":" + ex);
	}

	public void registerReceiver(IReceiver receiver){
		this.receiver = receiver;
	}
	
	public void sendToAll( String text ) {
		Collection<WebSocket> con = connections();
		synchronized ( con ) {
			for( WebSocket c : con ) {
				c.send( text );
			}
		}
	}
	
}
