package com.qingpu.test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

import com.qingpu.common.utils.HttpRequestUtils;

public class TestHttpWeather {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String weatherStr = HttpRequestUtils.GetWeatherData("长沙");
		JSONObject weatherObj = new JSONObject(weatherStr);
		JSONObject dataObj = weatherObj.getJSONObject("data");
		JSONArray jsonArr = dataObj.getJSONArray("forecast");
		JSONObject obj = jsonArr.getJSONObject(0);
		System.out.println("--str = " + obj.toString());
		String high = obj.getString("high");
		
		String regEx="[^0-9]";  
		Pattern p = Pattern.compile(regEx);  
		Matcher m = p.matcher(high);  
		System.out.println( m.replaceAll("").trim());
		high = m.replaceAll("").trim();
		
		String low = obj.getString("low"); // 获取高温			  
		Pattern p2 = Pattern.compile(regEx);  
		Matcher m2 = p2.matcher(low);  
		low = m2.replaceAll("").trim();
		System.out.println("low = " + low);
		
		int average = (Integer.parseInt(high)+Integer.parseInt(low))/2;
		System.out.println("average = " + average);
	}

}
