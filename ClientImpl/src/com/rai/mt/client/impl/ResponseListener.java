package com.rai.mt.client.impl;

import java.util.HashMap;

import org.json.JSONObject;

import com.rai.mt.client.api.IDataListener;
import com.rai.mt.data.JSONTags;
import com.rai.mt.protocol.IReceiver;

/**
 * Data listerner class for the I AppProtocolClient.
 * @author Prathik
 *
 */
public class ResponseListener implements IReceiver {

	private HashMap<String, IDataListener> dataListeners = new HashMap<String, IDataListener>();

	/**
	 * Add IDataListeners.
	 * @param clientID
	 * @param listener
	 */
	public void addDataListener(String clientID, IDataListener listener) {
		dataListeners.put(clientID, listener);
	}

	
	@Override
	public void onDataReceived(final String data) {
		if ("".equals(data)) {
			return;
		}
		JSONObject jobj = new JSONObject(data);
//		String response = jsonHandler.unwrapResponse(jobj);
//		long responseTime = jsonHandler.unwrapResponseTime(jobj);

		String clientID = jobj.getString(JSONTags.CLIENT_ID);

		IDataListener listener = dataListeners.get(clientID);

		if (listener != null) {
			listener.onDataReceived(data);
		}
	}

	@Override
	public void onError(String errorDetails) {
		System.err.println(errorDetails);
		
		for(IDataListener listener : dataListeners.values()){
			listener.onError(errorDetails);
		}
		

	}

	@Override
	public void onConnectionOpen(String msg) {
		System.err.println(msg);
		for(IDataListener listener : dataListeners.values()){
			listener.onConnectionOpen(msg);
		}
		
	}

}
