package com.rai.mt.client.data;

public class ResponseAnalysisData {

	/**
	 * Average time interval between 2 successive responses.
	 */
	private float averageTimeInterval = -1;

	private boolean isSequenceValid = false;

	private boolean isResponseValid = false;

	private int responseNumber = 0;
	
	private long responseTime = 0;

	public long getResponseTime() {
		return responseTime;
	}

	public void setResponseTime(long responseTime) {
		this.responseTime = responseTime;
	}

	public boolean isSequenceValid() {
		return isSequenceValid;
	}

	public void setSequenceValid(boolean isSequenceValid) {
		this.isSequenceValid = isSequenceValid;
	}

	public boolean isResponseValid() {
		return isResponseValid;
	}

	public void setResponseValid(boolean isResponseValid) {
		this.isResponseValid = isResponseValid;
	}

	public int getResponseNumber() {
		return responseNumber;
	}

	public void setResponseNumber(int responseNumber) {
		this.responseNumber = responseNumber;
	}

	public void setAverageTimeInterval(float averageTimeInterval) {
		this.averageTimeInterval = averageTimeInterval;
	}

	public float getAverageTimeInterval() {
		return averageTimeInterval;
	}

}
