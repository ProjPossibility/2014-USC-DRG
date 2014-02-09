package com.example.httpasyncpost;

import com.example.bluetooth_faster_connection.MainActivity;
import com.loopj.android.http.AsyncHttpResponseHandler;

public class AsyncResponseHandler extends AsyncHttpResponseHandler{
	
	MainActivity activity;
	
	public AsyncResponseHandler(MainActivity mainActivity){
		this.activity = mainActivity;
	}

	@Override
	public void onSuccess(String response) {
		activity.getResponseMessage(response);
		System.out.println(response);
	};
	
	@Override
	public void onFailure(Throwable e, String errorResponse) {
		String msg = "String *" + e.toString() + "*"
				+ errorResponse;
		System.out.println(msg);
	}
}
