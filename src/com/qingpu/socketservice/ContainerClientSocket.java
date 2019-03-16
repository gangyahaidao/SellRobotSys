package com.qingpu.socketservice;

import java.net.Socket;
import java.util.Date;

import com.qingpu.socketservice.ClientSocketThread;

public class ContainerClientSocket {
	private Socket client; // 标识当前的socket连接通道
	private ClientSocketThread clientThread; // 连接子线程
	private String machineID; // 机器人编号
	private Date preDate; // 接收上一次心跳的时间
	private boolean isTimeout; // 当前socket连接是否超时标识，如果超时置位此值同时关闭对应的socket和线程，下次超时检测时不再进行关闭
		
	private boolean isDoorOpened = false; // 出货的货柜门是否处于开启状态
	private boolean isDeviceBusy = false;//售卖货柜是否处于忙状态
	private String currentOrderId = null; // 当前售货柜正在处理的订单对象
	private boolean isCustomScanQrCode = false; // 标识用户是否已经扫码，如果扫码则停止，如果超过一定时间未付款则继续行走
	private boolean isInBuyGoodsProcess = false; // 标识是否用户已经进行了付款，如果有人在购买则不响应人体检测的行走move命令
	private boolean isRobotOutOfStore = false; // 机器人是否处于缺货状态，用于每次在出完货之后进行检查，与数据库中的状态同步
	
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
	public ClientSocketThread getClientThread() {
		return clientThread;
	}
	public void setClientThread(ClientSocketThread clientThread) {
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
	public boolean isDeviceBusy() {
		return isDeviceBusy;
	}
	public void setDeviceBusy(boolean isDeviceBusy) {
		this.isDeviceBusy = isDeviceBusy;
	}
	public String getCurrentOrderId() {
		return currentOrderId;
	}
	public void setCurrentOrderId(String currentOrderId) {
		this.currentOrderId = currentOrderId;
	}
	public boolean isDoorOpened() {
		return isDoorOpened;
	}
	public void setDoorOpened(boolean isDoorOpened) {
		this.isDoorOpened = isDoorOpened;
	}
	public boolean isCustomScanQrCode() {
		return isCustomScanQrCode;
	}
	public void setCustomScanQrCode(boolean isCustomScanQrCode) {
		this.isCustomScanQrCode = isCustomScanQrCode;
	}
	public boolean isInBuyGoodsProcess() {
		return isInBuyGoodsProcess;
	}
	public void setInBuyGoodsProcess(boolean isInBuyGoodsProcess) {
		this.isInBuyGoodsProcess = isInBuyGoodsProcess;
	}
	public boolean isRobotOutOfStore() {
		return isRobotOutOfStore;
	}
	public void setRobotOutOfStore(boolean isRobotOutOfStore) {
		this.isRobotOutOfStore = isRobotOutOfStore;
	}	
	
}
