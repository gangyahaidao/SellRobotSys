package com.qingpu.socketservice;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONException;
import org.json.JSONObject;

import com.qingpu.goods.service.GoodsService;
import com.qingpu.robots.dao.RobotsDao;

/**
 * socket服务类，接收客户端的请求并处理连接，用于和货柜相连接
 * */
public class ServerSocketThread extends Thread{
	private ServerSocket serverSocket;
	private static final int SERVERPORT = 19999;
	private GoodsService goodsService;
	private RobotsDao robotDao;
	
	public static Map<String, ContainerClientSocket> containerMachineMap = new HashMap<String, ContainerClientSocket>(); // 用于存储售货机器人的socket连接，key值为机器上传的心跳中包含的编号值
		
	public ServerSocketThread(GoodsService goodsService, RobotsDao robotDao){
		try {
			if (null == serverSocket) {
				this.serverSocket = new ServerSocket(SERVERPORT);  // "120.24.175.156", 9877
			}
			this.goodsService = goodsService;
			this.robotDao = robotDao;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 根据连接的socket通道对象获取注册时封装的RobotClientSocket对象
	 * 未找到返回null
	 * */
	public static ContainerClientSocket getContainerConnectObj(Socket client){
		ContainerClientSocket valueObj = null;
		
		// 遍历Map对象
		Iterator<Entry<String, ContainerClientSocket>> it0 = ServerSocketThread.containerMachineMap.entrySet().iterator();
		while(it0.hasNext()) {
			Map.Entry<String, ContainerClientSocket> entry = it0.next();
			valueObj = entry.getValue();
			if(valueObj.getClient() == client) {
				return valueObj;
			}
		}		
		return valueObj;
	}
	
	/**
	 * 发送数据到指定的Socket对象
	 * */
	public static int sendDataToContainerSocket(Socket client, byte[] dataT) {
		try {
			if(client != null && client.isConnected()){
				System.out.println("@@发送货柜出货命令 = " + new String(dataT));
				OutputStream out = client.getOutputStream();
				out.write(dataT);
				out.flush();
				return 0;
			}else{
				System.out.println("@@货柜连接Socket通道断开");				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public void run(){
		try {			
			System.out.println("@@SellRobotSys服务端Socket启动, 端口 = " + SERVERPORT);
			new ProcessHeartBeatClientThread().start(); // 启动货柜连接心跳监测线程
			while(!this.isInterrupted()){
				//如果主socket没有被中断
				Socket client = serverSocket.accept();//阻塞等待客户端的连接
				client.setTcpNoDelay(true);//立即发送数据
				client.setKeepAlive(true);//当长时间未能发送数据，服务器主动断开连接
				//创建新的客户端线程处理请求，如果请求鉴权通过就加入到在线客户端列表中，如果不通过则销毁
				ClientSocketThread client_thread = new ClientSocketThread(client, goodsService, robotDao);
				//启动子线程
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
				synchronized (ServerSocketThread.containerMachineMap) {
					Iterator<Entry<String, ContainerClientSocket>> it = ServerSocketThread.containerMachineMap.entrySet().iterator();
					while(it.hasNext()){
						Map.Entry<String, ContainerClientSocket> entry = it.next();
						String key = entry.getKey();//机器编号
						ContainerClientSocket beat = entry.getValue();//消息回复对象
						//如果当前时间 - 上一次收到心跳的时间 >= 3000ms
						if((new Date().getTime() - beat.getPreDate().getTime()) >= 1000*3){ //秒
							beat.getClientThread().closeClient();//关闭连接socket和释放线程							
							it.remove();//从在线列表中移除
							System.out.println("@@货柜线程心跳超时，移除客户端 machineID = " + key);																			
						}
					}
				}	
			}
		}
	}
}
