package com.qingpu.socketservice;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.qingpu.socketservice.ClientSocketThreadRobot;
import com.qingpu.socketservice.ClientSocketThreadRobot.ProcessRobotCmdThread;

/**
 * 机器人底盘连接的socket对象
 * */
public class RobotClientSocket {

	private Socket client; // 标识当前的socket连接通道
	private ClientSocketThreadRobot clientThread; // 连接子线程
	private String machineID; // 机器人编号
	private Date preDate; // 接收上一次心跳的时间
	private boolean isTimeout;
	private ProcessRobotCmdThread procesCmdThread = null;
	private boolean needStopChindThread = false;

	private JSONObject recvRobotPosAndSpeedObj; // 存储底盘上传的位置速度信息
	private boolean hasRobotReachedGoal; // 初始处于空闲状态
	private String currentPosName;// 当前机器人的位置点名称
	private boolean needStopLoopMove; // 是否需要停止循环，在机器人返回起始点的时候才进行检查
	
	private String startPosName; // 循环的起始点
	private long startLoopMiliTime; // 循环的起始时间毫秒数
	private long stopLoopMiliTime; // 循环停止的毫秒数
	private JSONArray posStayTimeJSONArr; // 存储的是机器人绑定的路线json数组
	private boolean hasTimerSendStartMove = false; // 定时器是否已经发送了开始运动命令，命令只发送一次
	private boolean hasTimerSendStopMove = false; // 是否已经发送了停止命令，如果指定的运动时间到则马上返回起始点
	private JSONObject currentPosObj = null; // 存储当前底盘上传的坐标点

	private int loopStartPosStaySec; // 循环一圈之后在起始点停留的时间	
	
	public boolean isNeedStopChindThread() {
		return needStopChindThread;
	}

	public void setNeedStopChindThread(boolean needStopChindThread) {
		this.needStopChindThread = needStopChindThread;
	}

	public JSONObject getCurrentPosObj() {
		return currentPosObj;
	}

	public void setCurrentPosObj(JSONObject currentPosObj) {
		this.currentPosObj = currentPosObj;
	}
	
	public ProcessRobotCmdThread getProcesCmdThread() {
		return procesCmdThread;
	}

	public void setProcesCmdThread(ProcessRobotCmdThread procesCmdThread) {
		this.procesCmdThread = procesCmdThread;
	}

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
	public Date getPreDate() {
		return preDate;
	}
	public void setPreDate(Date preDate) {
		this.preDate = preDate;
	}
	public String getMachineID() {
		return machineID;
	}
	public void setMachineID(String machineID) {
		this.machineID = machineID;
	}
	public ClientSocketThreadRobot getClientThread() {
		return clientThread;
	}
	public void setClientThread(ClientSocketThreadRobot clientThread) {
		this.clientThread = clientThread;
	}

	public JSONObject getRecvRobotPosAndSpeedObj() {
		return recvRobotPosAndSpeedObj;
	}

	public void setRecvRobotPosAndSpeedObj(JSONObject recvRobotPosAndSpeedObj) {
		this.recvRobotPosAndSpeedObj = recvRobotPosAndSpeedObj;
	}

	public boolean isHasRobotReachedGoal() {
		return hasRobotReachedGoal;
	}

	public void setHasRobotReachedGoal(boolean hasRobotReachedGoal) {
		this.hasRobotReachedGoal = hasRobotReachedGoal;
	}

	public String getCurrentPosName() {
		return currentPosName;
	}

	public void setCurrentPosName(String currentPosName) {
		this.currentPosName = currentPosName;
	}

	public long getStartLoopMiliTime() {
		return startLoopMiliTime;
	}

	public void setStartLoopMiliTime(long startLoopMiliTime) {
		this.startLoopMiliTime = startLoopMiliTime;
	}

	public long getStopLoopMiliTime() {
		return stopLoopMiliTime;
	}

	public void setStopLoopMiliTime(long stopLoopMiliTime) {
		this.stopLoopMiliTime = stopLoopMiliTime;
	}

	public JSONArray getPosStayTimeJSONArr() {
		return posStayTimeJSONArr;
	}

	public void setPosStayTimeJSONArr(JSONArray posStayTimeJSONArr) {
		this.posStayTimeJSONArr = posStayTimeJSONArr;
	}

	public boolean isHasTimerSendStartMove() {
		return hasTimerSendStartMove;
	}

	public void setHasTimerSendStartMove(boolean hasTimerSendStartMove) {
		this.hasTimerSendStartMove = hasTimerSendStartMove;
	}

	public boolean isHasTimerSendStopMove() {
		return hasTimerSendStopMove;
	}

	public void setHasTimerSendStopMove(boolean hasTimerSendStopMove) {
		this.hasTimerSendStopMove = hasTimerSendStopMove;
	}

	public boolean isNeedStopLoopMove() {
		return needStopLoopMove;
	}

	public void setNeedStopLoopMove(boolean needStopLoopMove) {
		this.needStopLoopMove = needStopLoopMove;
	}

	public String getStartPosName() {
		return startPosName;
	}

	public void setStartPosName(String startPosName) {
		this.startPosName = startPosName;
	}

	public int getLoopStartPosStaySec() {
		return loopStartPosStaySec;
	}

	public void setLoopStartPosStaySec(int loopStartPosStaySec) {
		this.loopStartPosStaySec = loopStartPosStaySec;
	}
		
}
