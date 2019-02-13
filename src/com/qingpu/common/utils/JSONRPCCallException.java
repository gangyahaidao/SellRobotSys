package com.qingpu.common.utils;

public class JSONRPCCallException extends RuntimeException{
	
	private static final long serialVersionUID = -5557055869273904716L;

	private int code;
	
	private String message;
	
	//定义各种构造函数
	public JSONRPCCallException(){
		super();
	}
	
	public JSONRPCCallException(String message)	{
		super(message);
		this.message = message;
	}
	
	public JSONRPCCallException(int code, String message){
		super();
		this.code = code;
		this.message = message;
	}
	
	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	@Override
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	@Override
	public String toString(){
		return super.toString() + ":errorCode = " + this.code;
	}
	
}
