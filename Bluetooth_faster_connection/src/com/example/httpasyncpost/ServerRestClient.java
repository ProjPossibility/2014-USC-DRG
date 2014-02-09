package com.example.httpasyncpost;

import com.loopj.android.http.*;


public class ServerRestClient {
	private static final String BASE_URL = "http://zhoutsby.com/";

	private static AsyncHttpClient client = new AsyncHttpClient();
	
	public static void get(String url, RequestParams params,
			AsyncHttpResponseHandler responseHandler) {
		System.out.println(getAbsoluteUrl(url));
		client.get(getAbsoluteUrl(url), params, responseHandler);
	}
	
	public static void post(String url, RequestParams params,
			AsyncHttpResponseHandler responseHandler) {
		client.post(getAbsoluteUrl(url), params, responseHandler);
	}
	
	private static String getAbsoluteUrl(String relativeUrl) {
		return BASE_URL + relativeUrl;
	}
}
