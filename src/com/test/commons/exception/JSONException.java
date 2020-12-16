package com.test.commons.exception;

import com.test.commons.util.JSONArray;
import com.test.commons.util.JSONObject;

/**
 * for JSONObject, JSONArray.
 * @see JSONObject
 * @see JSONArray
 */
public class JSONException extends RuntimeException {
	public JSONException() {
		super();
	}
	
	public JSONException(String message) {
		super(message);
	}
	
	public JSONException(Throwable t) {
		super(t);
	}
	
	public JSONException(String message, Throwable t) {
		super(message, t);
	}
}
