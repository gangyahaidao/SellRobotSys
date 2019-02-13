package com.qingpu.test;

import java.util.HashMap;
import java.util.Map;

import com.qingpu.common.utils.WeiXinUtils;

public class TestStrTemplate {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String srcStr = "fsfksdfsdfkjdsfjsdj{{replace}}fjkls{{replace}}fkl--{{llllll}}++{{llllll}}++dsssfls";
		Map<String, String> map = new HashMap<String, String>();		
		map.put("replace", "副教授积分开始");
		String renderStr = WeiXinUtils.render(srcStr, map);
		renderStr = renderStr.replace("{{llllll}}", "");
		
		System.out.println(renderStr);
	}

}
