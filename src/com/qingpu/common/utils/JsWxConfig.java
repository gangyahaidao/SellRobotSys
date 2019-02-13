/**
* Title: JsWxConfig.java
* Description: 
* Copyright: Copyright (c) 2016
* Company: Biceng
* @date 2017-3-8
* @version 1.0
*/
package com.qingpu.common.utils;

import com.qingpu.common.utils.WeiXinConstants;

/**
 * @author wang_gang
 *
 */
public class JsWxConfig {
	public JsWxConfig(){
		
	}
	//appid，公众号标识
	private String appId = WeiXinConstants.APPID;
	//生成签名的时间戳
	private long timestamp;
	//生成签名的随机串
	private String nonceStr;
	//签名
	private String signature;
	
	public String getAppId() {
		return appId;
	}
	public void setAppId(String appId) {
		this.appId = appId;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	public String getNonceStr() {
		return nonceStr;
	}
	public void setNonceStr(String nonceStr) {
		this.nonceStr = nonceStr;
	}
	public String getSignature() {
		return signature;
	}
	public void setSignature(String signature) {
		this.signature = signature;
	}
}
