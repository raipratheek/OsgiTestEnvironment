package com.rai.mt.clientimpl;

public interface IDataListener {

	/**
	 * Notified when data from server is received by the application protocol.
	 * @param data - String
	 */
	void onDataReceived(String data);

	/**
	 * On error notification
	 * @param errorDetails
	 */
	void onError(String errorDetails);

	/**
	 * On connection open.
	 * @param msg
	 */
	void onConnectionOpen(String msg);

}
