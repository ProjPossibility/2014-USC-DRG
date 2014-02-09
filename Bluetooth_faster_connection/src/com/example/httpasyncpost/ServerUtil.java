package com.example.httpasyncpost;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONObject;

public class ServerUtil {
	public void sendData(String url, String jsonData){
		HttpURLConnection conn = null;
		OutputStream os = null;
		byte[] data = jsonData.getBytes();
		
		//Get the out stream
		try{
			URL openUrl = new URL(url);
			conn = (HttpURLConnection) openUrl.openConnection();
			conn.setDoOutput(true);
			conn.setFixedLengthStreamingMode(data.length);
			os = conn.getOutputStream();
		}catch(MalformedURLException e){
			//If the URL is not a valid url, do something
		}catch(IOException e){
			//If error when opening or writing to the stream
		}
		
		try{
			os.write(data);
		}catch(IOException e){
			//If error occurred, prevent writing
		}finally{
			if(conn!=null)
				conn.disconnect();
		}
	}
}
