package com.qingpu.common.service;

public abstract interface WeiXinTemplateService {
	/**
	 * 获取设置的行业信息GET
	 * */
	String getIndustryInfo();
	
	/**
	 * 获取模板列表
	 * */
	String getTemplateList();
	
	/**
	 * 发送模板消息
	 * @param recvMessageOpenid 
	 * */
	String sendTemplateMessage(String json, String recvMessageOpenid);
	
}
