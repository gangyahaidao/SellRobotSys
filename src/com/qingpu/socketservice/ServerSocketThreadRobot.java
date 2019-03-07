package com.qingpu.socketservice;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONObject;

import com.qingpu.common.utils.QingpuConstants;

/**
 * socket服务类，接收客户端的请求并处理连接，用于和底盘相连接
 * */
public class ServerSocketThreadRobot extends Thread{
	private ServerSocket serverSocket;
	private static final int SERVERPORT = 8089;
	
	public static Map<String, RobotClientSocket> robotMachineMap = new HashMap<String, RobotClientSocket>(); // 用于存储售货机器人底盘的socket连接，key值为底盘初次连接上传测注册字符串（楼层+机器人编号）
	
	public ServerSocketThreadRobot(){
		try {
			if (null == serverSocket) {
				this.serverSocket = new ServerSocket(SERVERPORT); // "120.24.175.156", 8089
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 根据连接的socket通道对象获取注册时封装的RobotClientSocket对象
	 * 未找到返回null
	 * */
	public static RobotClientSocket getRobotConnectObj(Socket client){
		RobotClientSocket valueObj = null;
		
		// 遍历Map对象
		Iterator<Entry<String, RobotClientSocket>> it0 = ServerSocketThreadRobot.robotMachineMap.entrySet().iterator();
		while(it0.hasNext()) {
			Map.Entry<String, RobotClientSocket> entry = it0.next();
			valueObj = entry.getValue();
			if(valueObj.getClient() == client) {
				return valueObj;
			}
		}		
		return valueObj;
	}
	/**
	 * 向指定编号的底盘发送行走控制命令
	 * machineId: 机器人底盘编号
	 * 参数isStop：
	 * 		true:表示停止
	 * 		false:表示继续行走
	 * */
	public static void sendMoveCmdToRoobt(String machineId, boolean isStop) {
		RobotClientSocket robotObj = ServerSocketThreadRobot.robotMachineMap.get(machineId);
		if(robotObj != null && robotObj.getClient() != null && robotObj.getClient().isConnected()) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("stopMove", isStop);
			ResponseSocketUtils.sendJsonDataToClient(
					jsonObject, 
					robotObj.getClient(), 
					QingpuConstants.SEND_ROBOT_RUN_CONTROL,
					QingpuConstants.ENCRYPT_BY_NONE,
					QingpuConstants.DATA_TYPE_JSON);
		}
	}
	
	/**
	 * 根据当前的位置和目标点名称查询路径列表，得到一条路径名称列表 --- 弃用
	 * */
	@Deprecated
	public static List<String> getPathListByStartAndEndPosName_back(String currentPosName, String goalPosName) {
		String[] posNameArr = {"实验室","商会","会议室","公司前台","小蔡总办公室","研发部办公区","庄总办公室","蔡总办公室","财务室",
				"蔡总办公室","庄总办公室","研发部办公区", "小蔡总办公室","公司前台","数据中心","工程部","商管部","其他公司办公区域","实验室"};
		
		if(currentPosName.equals(goalPosName)) { // 如果目标点与当前点名称一样则返回Null
			return null;
		}
		List<String> pathArr = new ArrayList<String>();// 存储路径点名称
		List<Integer> currentIndexArr = new ArrayList<Integer>();
		List<Integer> goalIndexArr = new ArrayList<Integer>();
		
		for(int i = 0; i < posNameArr.length; i++) {
			if(currentPosName.equals(posNameArr[i])) { // 获取当前位置点在前进数组中的位置
				currentIndexArr.add(i);
			}
			if(goalPosName.equals(posNameArr[i])) { // 获取目标点在前进数组中的位置
				goalIndexArr.add(i);
			}
		}
		System.out.println("@@当前位置点在路径数组中的位置列表数组 = " + currentIndexArr);
		System.out.println("@@目标位置在路径数组中的位置列表数组 = " + goalIndexArr);
		
		// 从两个list中寻找最短的两点 
		int currentIndex = 0;
		int goalIndex = 0;
		int min = 100;
		for(int i = 0; i < currentIndexArr.size(); i++) {
			for(int j = 0; j < goalIndexArr.size(); j++) {
				if(Math.abs(currentIndexArr.get(i)-goalIndexArr.get(j)) < min) {
					min = Math.abs(currentIndexArr.get(i)-goalIndexArr.get(j));
					currentIndex = currentIndexArr.get(i);
					goalIndex = goalIndexArr.get(j);
				}
			}
		}
		System.out.println("@@最短下标距离值min = " + min + ", currentIndex = " + currentIndex + ", goalIndex = " + goalIndex);
		if(currentIndex < goalIndex) {
			for(int k = currentIndex; k <= goalIndex; k++) {
				pathArr.add(posNameArr[k]);
			}
		}else{
			for(int k = currentIndex; k >= goalIndex; k--) {
				pathArr.add(posNameArr[k]);
			}
		}
		
		return pathArr;
	}

	@Override
	public void run(){
		try {			
			System.out.println("@@底盘服务端机器人连接Socket启动, 端口 = " + SERVERPORT);
			new ProcessHeartBeatClientThread().start(); // 启动底盘连接心跳
			while(!this.isInterrupted()){//如果主socket没有被中断				
				Socket client = serverSocket.accept();//阻塞等待客户端的连接
				client.setTcpNoDelay(true);//立即发送数据
				client.setKeepAlive(true);//当长时间未能发送数据，服务器主动断开连接
				ClientSocketThreadRobot client_thread = new ClientSocketThreadRobot(client);
				client_thread.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void closeSocketService() {
		// 关闭socket
		try {
			this.interrupt();
			if(null != serverSocket && !serverSocket.isClosed()){
				serverSocket.close();
			}			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//A.创建一个线程处理心跳超时的客户端
	public class ProcessHeartBeatClientThread extends Thread{
		@Override
		public void run(){			
			while(true){				
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				synchronized (ServerSocketThreadRobot.robotMachineMap) {
					Iterator<Entry<String, RobotClientSocket>> it = ServerSocketThreadRobot.robotMachineMap.entrySet().iterator();
					while(it.hasNext()){
						Map.Entry<String, RobotClientSocket> entry = it.next();
						String key = entry.getKey();//机器编号
						RobotClientSocket beat = entry.getValue();//消息回复对象
						Date preTime = beat.getPreDate();
						//如果当前时间 - 上一次收到心跳的时间 >= 3000ms
						if((new Date().getTime() - preTime.getTime()) >= 1000*5){ //ms							
							beat.getClientThread().closeClient();//关闭连接socket和释放线程
							// it.remove();//从在线列表中移除
							// System.out.println("@@底盘心跳超时，移除客户端 machineID = " + key);																			
						}
					}
				}	
			}
		}
	}
}
