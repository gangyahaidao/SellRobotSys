/**
* Title: JsapiTicket.java
* Description: 
* Copyright: Copyright (c) 2016
* Company: Biceng
* @date 2017-3-8
* @version 1.0
*/
package com.qingpu.common.utils;

/**
 * @author wang_gang
 *
 */
public class JsapiTicket {
	//错误码
	private int errcode;
	//错误信息
	private String errmsg;
	//获取的凭证
	private String ticket;
	//有效时间
	private int expires_in;
	
	public JsapiTicket(){
		
	}
	public int getErrcode() {
		return errcode;
	}
	public void setErrcode(int i) {
		this.errcode = i;
	}
	public String getErrmsg() {
		return errmsg;
	}
	public void setErrmsg(String errmsg) {
		this.errmsg = errmsg;
	}
	public String getTicket() {
		return ticket;
	}
	public void setTicket(String ticket) {
		this.ticket = ticket;
	}
	public int getExpires_in() {
		return expires_in;
	}
	public void setExpires_in(int i) {
		this.expires_in = i;
	}
}
