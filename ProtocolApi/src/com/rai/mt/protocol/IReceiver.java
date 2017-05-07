package com.rai.mt.protocol;

public interface IReceiver {
	
	void onConnectionOpen(String msg);
	
	void onDataReceived (String data);
	
	void onError (String errorDetails);

}
