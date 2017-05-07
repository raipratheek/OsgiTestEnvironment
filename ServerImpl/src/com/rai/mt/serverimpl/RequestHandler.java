package com.rai.mt.serverimpl;

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

					while (!stopUpdater) {
						try {
							jsonObj.put(JSONTags.RESPONSE, jsonObj.get(JSONTags.REQUEST));
							jsonObj.put(JSONTags.RESPONSE_TIME, System.currentTimeMillis());
							jsonObj.put(JSONTags.SEQUENCE_NUM, seqnumber++);
							int timeInterval = jsonObj.getInt(JSONTags.TIME_TO_RESPOND);
							protocolHandler.send(jsonObj.toString());
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
		
		jsonObj.put(JSONTags.RESPONSE, jsonObj.get(JSONTags.REQUEST));
		jsonObj.put(JSONTags.RESPONSE_TIME, System.currentTimeMillis());
		jsonObj.put(JSONTags.SEQUENCE_NUM, 1);
		protocolHandler.send(jsonObj.toString());
	}
}
