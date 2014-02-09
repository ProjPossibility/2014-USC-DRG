package com.example.httpasyncpost;

import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

public class JSONUtil {
	
	//return the String format of Json
	public static String convertToJSON(Map<String,Object> map){
		JSONObject jsonObject = new JSONObject(map);
		String jsonString = jsonObject.toString();
		if(jsonString!=null)
			return jsonString;
		return null;
		
	}
	
	public static JSONObject convertFromStrToJson(String json){
		try {
			JSONObject obj = new JSONObject(json);
			return obj;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		
	}
}
