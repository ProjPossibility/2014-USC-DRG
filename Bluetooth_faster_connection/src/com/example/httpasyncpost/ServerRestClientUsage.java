package com.example.httpasyncpost;

import java.util.HashMap;

import com.example.bluetooth_faster_connection.MainActivity;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class ServerRestClientUsage {
	
	public void getPairedPhone(HashMap<String, String> map, MainActivity mainActivity) {
		RequestParams params = new RequestParams(map);
		
		ServerRestClient.get("deal-data/", params,
				new AsyncResponseHandler(mainActivity));
		
	}
	
	public void getPairedPhone() {
		RequestParams params = new RequestParams();
		params.put("mac_address", "1111");
		params.put("shake_date", "0000");

		ServerRestClient.get("deal-data/", params,
				new AsyncHttpResponseHandler() {
			
					@Override
					public void onSuccess(String response) {
						System.out.println(response);
					};
					
					@Override
					public void onFailure(Throwable e, String errorResponse) {
						String msg = "String *" + e.toString() + "*"
								+ errorResponse;
						System.out.println(msg);
					}
				});
	}
}
