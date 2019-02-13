/**
* Title: AccessToken.java
* Description: 
* Copyright: Copyright (c) 2016
* Company: Biceng
* @date 2017-3-8
* @version 1.0
*/
package com.qingpu.common.entity;

/**
 * @author wang_gang
 *
 */
public class AccessToken {
	//获取到的凭证
	private String access_token;
	//凭证的有效时间，单位：秒
	private int expires_in;

	public String getAccess_token() {
		return access_token;
	}
	public void setAccess_token(String access_token) {
		this.access_token = access_token;
	}
	public int getExpires_in() {
		return expires_in;
	}
	public void setExpires_in(int i) {
		this.expires_in = i;
	}
}
