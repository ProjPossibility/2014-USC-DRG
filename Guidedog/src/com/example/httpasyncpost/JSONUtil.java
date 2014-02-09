package com.example.httpasyncpost;

import java.util.Map;

import org.json.JSONObject;

public class JSONUtil {
	
	//return the String format of Json
	public String convertToJSON(Map<String,Object> map){
		JSONObject jsonObject = new JSONObject(map);
		String jsonString = jsonObject.toString();
		if(jsonString!=null)
			return jsonString;
		return null;
		
	}
}
