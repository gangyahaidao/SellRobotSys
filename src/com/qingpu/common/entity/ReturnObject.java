package com.qingpu.common.entity;

public class ReturnObject {
	
	//-1标识失败，0标识成功
	private int code;
	private String message;
	
	public ReturnObject(){
		this.code = -1;
		this.message = "Error";
	}
	
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	
}
