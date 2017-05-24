package com.rai.mt.client.impl;

import org.json.JSONObject;

import com.rai.mt.client.data.ResponseAnalysisData;
import com.rai.mt.client.util.JSONHandler;
import com.rai.mt.data.JSONTags;

/**
 * A simple class which analyses the responses from the server and checks the
 * validity.
 * 
 * @author Prathik
 *
 */
public class ResponseAnalyser {

	/**
	 * Keeps track of the responses received.
	 */
	private int responseNum = 0;

	/**
	 * Accumulates the time for average time calculation.
	 */
	private long timeAccumulator = 0;

	/**
	 * Keeps the total response received.
	 */
	private int totalResponse = 0;

	/**
	 * Time of the last received response.
	 */
	private long prevResTime = 0;

	/**
	 * Sequence number of the data received.
	 */
	private int seqNumber = 0;

	/**
	 * The data that is sent from the client .Test Payload.
	 */
	private String testPayload;

	private JSONHandler jsonhandler = new JSONHandler();

	/**
	 * Set the current test payload data.
	 * 
	 * @param testData
	 *            - Test payload.
	 */
	public void setTestPayload(String testData) {
		this.testPayload = testData;
	}

	public int getResponseNum() {
		return responseNum;
	}

	public ResponseAnalysisData analyze(JSONObject response) {
		long responseTime = jsonhandler.unwrapResponseTime(response);
		long flightTime = System.currentTimeMillis() - responseTime;
		ResponseAnalysisData analysisData = new ResponseAnalysisData();
		String responseString = jsonhandler.unwrapResponse(response);
		
		
		totalResponse++;
		int responseNumber = response.getInt(JSONTags.SEQUENCE_NUM);
		analysisData.setSequenceValid(totalResponse == responseNumber);
		
		analysisData.setResponseValid(isResponseValid(responseString));
		analysisData.setResponseNumber(responseNumber);

		if (prevResTime == 0) {
			// first response.
			prevResTime = responseTime;
		} else {
			timeAccumulator = (timeAccumulator + (responseTime - prevResTime));
			prevResTime = responseTime;
			analysisData.setAverageTimeInterval(timeAccumulator / (totalResponse - 1));
		}
		// do this after avg time calculation.
		analysisData.setFlightTime(flightTime);

		return analysisData;
	}

	// TODO- make this a single method.

	private boolean isResponseValid(String response) {
		boolean res = false;
		res = testPayload.equals(response);
		return res;
	}

	public void setResponseTime(long time) {
		if (prevResTime == 0) {
			prevResTime = time;
			return;
		}
		totalResponse++;
		timeAccumulator = (timeAccumulator + (time - prevResTime));
		prevResTime = time;
	}

	public void reset() {
		timeAccumulator = 0;
		prevResTime = 0;
		seqNumber = 0;
		responseNum = 0;
		totalResponse = 0;
		

	}

	public boolean isSequenceValid(int seq) {
		boolean res = false;
		res = (seqNumber + 1 == seq);
		seqNumber++;
		return res;
	}

}
