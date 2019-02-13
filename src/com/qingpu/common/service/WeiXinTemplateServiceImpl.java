package com.qingpu.common.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qingpu.common.utils.CommonUtils;
import com.qingpu.common.utils.WeiXinConstants;
import com.qingpu.common.utils.WeiXinUtils;

@Service("weiXinTemplateService")
@Transactional
public class WeiXinTemplateServiceImpl implements WeiXinTemplateService {

	@Override
	public String getIndustryInfo() {		
		String access_token = WeiXinUtils.getAccessToken().getAccess_token();
		String getIndustryUrl = String.format(WeiXinConstants.GET_INDUSTRY_TEMPLATE, access_token);
		String result = CommonUtils.httpGetStr(getIndustryUrl);
		
		return result;
	}

	@Override
	public String getTemplateList() {
		String access_token = WeiXinUtils.getAccessToken().getAccess_token();
		String getTemplateListUrl = String.format(WeiXinConstants.GET_TEMPLATE_LIST, access_token);
		String result = CommonUtils.httpGetStr(getTemplateListUrl);
		
		return result;
	}

	@Override
	public String sendTemplateMessage(String message, String openid) {
		if(openid == null) {
			openid = "oPr4242BNciQoIXriB-y_8UswpEM";
		}
		//读取模板文件
		StringBuffer sb = new StringBuffer();
		String filePath = WeiXinTemplateServiceImpl.class.getClassLoader().getResource("config/properties/betResultInfo.txt").getPath();
		try {
			FileInputStream fip = new FileInputStream(new File(filePath));
			InputStreamReader reader = new InputStreamReader(fip, "UTF-8");
			while (reader.ready()) {				
				sb.append((char) reader.read()); // 转成char加到StringBuffer对象中			  
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Map<String, String> data = new HashMap<String, String>();
		data.put("openid", openid); // 接收消息人的openid
		data.put("betinfo", message); // 发送的消息字符串
		data.put("nickname", "XXXXXX");
		String renderStr = WeiXinUtils.render(sb.toString(), data);
		System.out.println("@@renderStr = " + renderStr);
		
		String access_token = WeiXinUtils.getAccessToken().getAccess_token();
		System.out.println("@@access_token = " + access_token);
		String sendTemplateMessage = String.format(WeiXinConstants.SEND_TEMPLATE_MESSAGE, access_token);
		String result = CommonUtils.httpPostJsonStr(sendTemplateMessage, renderStr);
		
		return result;
	}

}
