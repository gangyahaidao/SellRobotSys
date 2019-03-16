package com.qingpu.socketservice;

import java.net.Socket;
import java.util.Date;

import com.qingpu.socketservice.ClientSocketThreadDetect;
import com.qingpu.socketservice.ClientSocketThreadRobot;

/**
 * 机器人人体检测和货柜连接线程
 * */
public class DetectClientSocket {
	private Socket client; // 标识当前的socket连接通道
	private ClientSocketThreadDetect clientThread; // 连接子线程
	private String machineID; // 机器人编号
	private Date preDate; // 接收上一次心跳的时间
	private boolean isTimeout; // 心跳是否超时了
	private boolean reachedGoalNeedStop; // 到达了某点是否需要不响应继续行走命令
	
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
	public ClientSocketThreadDetect getClientThread() {
		return clientThread;
	}
	public void setClientThread(ClientSocketThreadDetect clientThread) {
		this.clientThread = clientThread;
	}
	public boolean isReachedGoalNeedStop() {
		return reachedGoalNeedStop;
	}
	public void setReachedGoalNeedStop(boolean reachedGoalNeedStop) {
		this.reachedGoalNeedStop = reachedGoalNeedStop;
	}
	
}
