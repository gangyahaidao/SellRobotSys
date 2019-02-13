package com.qingpu.common.service;

public abstract interface BdfsBinaryProvider {

	//保存二进制数据到文件服务器
	public abstract String upload(byte[] data, String type);
	
	
}
