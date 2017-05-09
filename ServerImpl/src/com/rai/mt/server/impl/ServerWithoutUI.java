package com.rai.mt.server.impl;

import java.util.HashMap;

import org.json.JSONObject;

import com.rai.mt.data.JSONTags;
import com.rai.mt.protocol.IApprotocolServer;
import com.rai.mt.protocol.IReceiver;

/**
 * Server Instance without UI.
 * @author Prathik
 *
 */
public class ServerWithoutUI {

	private int port ;

	private String host ;

	private boolean isSecure;

	private IApprotocolServer protocol;

	private IReceiver receiver;

	private HashMap<String, RequestHandler> clientreqHandlers;

	public ServerWithoutUI(IApprotocolServer protocol, int port , String host ,boolean isSecure) {

		this.protocol = protocol;
		clientreqHandlers = new HashMap<String, RequestHandler>();
		this.host = host;
		this.port = port;
		this.isSecure = isSecure;
	}

	public void startServer() {

		try {
			protocol.init(host, port, isSecure);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (receiver == null) {
			receiver = new ServerReceiver();
			protocol.registerReceiver(receiver);
		}
		protocol.startServer();
		System.out.println(" Server started without UI.");

	}

	class ServerReceiver implements IReceiver {

		@Override
		public void onDataReceived(final String req) {

			System.out.println(req + "\n");
			JSONObject data = new JSONObject(req);
			String clientID = data.getString(JSONTags.CLIENT_ID);
			RequestHandler reqHandler = clientreqHandlers.get(clientID);

			if (reqHandler == null) {
				reqHandler = new RequestHandler(protocol);
				clientreqHandlers.put(clientID, reqHandler);
			}

			if (data.has(JSONTags.RESPONSE_STOP)) {
				reqHandler.setStopUpdater(true);
				return;
			}

			int timeToRespond = data.getInt(JSONTags.TIME_TO_RESPOND);
			if (timeToRespond > -1) {
				reqHandler.onContinuousRequest(data);
			} else {
				reqHandler.onSingleRequest(data);
			}
		}

		@Override
		public void onError(final String errorDetails) {
			System.out.println("ERROR " + errorDetails + "\n");
		}

		@Override
		public void onConnectionOpen(final String msg) {

			System.out.println("INFO " + "Application Protocol Type = " + protocol.getType() + "\n");
			System.out.println("INFO " + msg + "\n");

		}

	}

}
