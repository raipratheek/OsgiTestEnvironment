package com.rai.mt.client.util;

import org.json.JSONObject;
import com.rai.mt.data.JSONTags;

public class JSONHandler {

	public JSONObject wrapRequest(String request, String clientID , int i ) {
		JSONObject jObj = new JSONObject();
		jObj.put(JSONTags.REQUEST, request);
		jObj.put(JSONTags.CLIENT_ID, clientID);
		jObj.put(JSONTags.REQUEST_TIME, System.currentTimeMillis());
		jObj.put(JSONTags.TIME_TO_RESPOND, new Integer(i));
		return jObj;

	}

	public String unwrapResponse(JSONObject jsonObj) {
		String response = jsonObj.getString(JSONTags.RESPONSE);
		return response;
	}
	
	public long unwrapResponseTime(JSONObject jsonObj) {
		long response = jsonObj.getLong(JSONTags.RESPONSE_TIME);
		return response;
	}
}
