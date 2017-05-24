package com.rai.mt.server.impl;

import org.json.JSONObject;
import com.rai.mt.data.JSONTags;
import com.rai.mt.protocol.IApprotocolServer;

public class RequestHandler {

	private IApprotocolServer protocolHandler;

	private Thread updater = null;

	private volatile boolean stopUpdater;

	public boolean isStopUpdater() {
		return stopUpdater;
	}

	public void setStopUpdater(boolean stopUpdater) {
		this.stopUpdater = stopUpdater;
	}

	public RequestHandler(IApprotocolServer protHandler) {
		protocolHandler = protHandler;
	}

	public void onContinuousRequest(final JSONObject jsonObj) {
		if (updater == null) {
			stopUpdater = false;
			updater = new Thread(new Runnable() {

				@Override
				public void run() {
					int seqnumber = 1;
					long requestTime = jsonObj.getLong(JSONTags.REQUEST_TIME);
					long startTime = System.currentTimeMillis();
					while (!stopUpdater) {
						try {
							JSONObject jobj = new JSONObject();
							jobj.put(JSONTags.RESPONSE, jsonObj.get(JSONTags.REQUEST));
							jobj.put(JSONTags.RESPONSE_TIME, requestTime + (System.currentTimeMillis() - startTime));
							jobj.put(JSONTags.SEQUENCE_NUM, seqnumber++);
							jobj.put(JSONTags.CLIENT_ID, jsonObj.get(JSONTags.CLIENT_ID));
							jobj.put(JSONTags.REQUEST_TIME, jsonObj.get(JSONTags.REQUEST_TIME));
							int timeInterval = jsonObj.getInt(JSONTags.TIME_TO_RESPOND);
							
							protocolHandler.send(jobj.toString());
							Thread.sleep(timeInterval);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					updater = null;
				}
			});
			updater.start();
		}
	}

	public void onSingleRequest(JSONObject jsonObj) {
		JSONObject jsonResponse = new JSONObject();
		jsonResponse.put(JSONTags.CLIENT_ID, jsonObj.get(JSONTags.CLIENT_ID));
		jsonResponse.put(JSONTags.RESPONSE, jsonObj.get(JSONTags.REQUEST));
		jsonResponse.put(JSONTags.RESPONSE_TIME, jsonObj.get(JSONTags.REQUEST_TIME));
		jsonResponse.put(JSONTags.SEQUENCE_NUM, 1);
		jsonResponse.put(JSONTags.REQUEST_TIME, jsonObj.get(JSONTags.REQUEST_TIME));
		protocolHandler.send(jsonResponse.toString());
	}
}
