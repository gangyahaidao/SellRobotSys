package com.qingpu.common.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qingpu.common.entity.AccessToken;
import com.qingpu.common.utils.CommonUtils;
import com.qingpu.common.utils.WeiXinConstants;
import com.qingpu.common.utils.WeiXinUtils;
import com.qingpu.user.entity.UserWeixinOriginal;
import com.qingpu.user.service.UserService;

@Service("weiXinTemplateService")
@Transactional
public class WeiXinTemplateServiceImpl implements WeiXinTemplateService {
	
	@Resource
	private UserService userService;

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
		data.put("nickname", "管理员");
		String renderStr = WeiXinUtils.render(sb.toString(), data);
		// System.out.println("@@renderStr = " + renderStr);
		
		AccessToken accessTokenObj = WeiXinUtils.getAccessToken();
		String sendResult = "";
		if(accessTokenObj != null) {
			String access_token = accessTokenObj.getAccess_token();
			// System.out.println("@@模板access_token = " + access_token);
			String sendTemplateMessage = String.format(WeiXinConstants.SEND_TEMPLATE_MESSAGE, access_token);
			sendResult = CommonUtils.httpPostJsonStr(sendTemplateMessage, renderStr);
		}		
		return sendResult;
	}

	@Override
	public void sendTemplateMessageToUniqueUser(String message) {
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
		
		UserWeixinOriginal user = userService.getOriginalUserCanRecvAdminInfo();
		if(user != null) {
			Map<String, String> data = new HashMap<String, String>();
			data.put("openid", user.getOpenid()); // 接收消息人的openid
			data.put("betinfo", message); // 发送的消息字符串内容
			data.put("nickname", "零售管理员");
			String renderStr = WeiXinUtils.render(sb.toString(), data);
			
			AccessToken accessTokenObj = WeiXinUtils.getAccessToken();
			if(accessTokenObj != null) {
				String access_token = accessTokenObj.getAccess_token();
				// System.out.println("@@模板access_token = " + access_token);
				String sendTemplateMessage = String.format(WeiXinConstants.SEND_TEMPLATE_MESSAGE, access_token);
				CommonUtils.httpPostJsonStr(sendTemplateMessage, renderStr);
			}		
		} else {
			System.out.println("@@还没有绑定接收模板通知消息的用户，请从链接'http://www.g58mall.com/SellRobotSys/weixin/sendReqForUserOpenid'绑定");
		}		
	}
}
