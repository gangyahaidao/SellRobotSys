package com.qingpu.socketservice;

import java.net.Socket;
import java.util.Date;

public class AdTemplateClientSocket {
	private Socket client; // 标识当前的socket连接通道
	private ClientSocketThreadAdTemplate clientThread; // 连接子线程
	private String machineID; // 机器人编号
	private Date preDate; // 接收上一次心跳的时间
	private boolean isTimeout;
	
	public boolean isTimeout() {
		return isTimeout;
	}
	public void setTimeout(boolean isTimeout) {
		this.isTimeout = isTimeout;
	}
	public Socket getClient() {
		return client;
	}
	public void setClient(Socket client) {
		this.client = client;
	}	
	public ClientSocketThreadAdTemplate getClientThread() {
		return clientThread;
	}
	public void setClientThread(ClientSocketThreadAdTemplate clientThread) {
		this.clientThread = clientThread;
	}
	public String getMachineID() {
		return machineID;
	}
	public void setMachineID(String machineID) {
		this.machineID = machineID;
	}
	public Date getPreDate() {
		return preDate;
	}
	public void setPreDate(Date preDate) {
		this.preDate = preDate;
	}
}
