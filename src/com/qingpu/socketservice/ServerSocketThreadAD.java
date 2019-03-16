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

import org.json.JSONObject;

import com.qingpu.adtemplate.dao.AdTemplateDao;
import com.qingpu.goods.service.GoodsService;
import com.qingpu.robots.dao.RobotsDao;

/**
 * 广告模板数据获取与推送Socket
 * */
public class ServerSocketThreadAD extends Thread {

	private static AdTemplateDao adTemplateDao;
	private static RobotsDao robotDao;
	private ServerSocket serverSocket;
	private GoodsService goodsService;
	private static final int SERVERPORT = 8888;
	
	public static Map<String, AdTemplateClientSocket> adTemplateMap = new HashMap<String, AdTemplateClientSocket>();
	
	public ServerSocketThreadAD(RobotsDao robotDao, AdTemplateDao adTemplateDao, GoodsService goodsService){
		try {
			if (null == serverSocket) {
				this.robotDao = robotDao;
				this.adTemplateDao = adTemplateDao;
				this.goodsService = goodsService;
				this.serverSocket = new ServerSocket(SERVERPORT);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 发送数据到广告屏幕
	 * */
	public static void sendDataToAdSocket(String machineId, JSONObject jsonObj) {
		AdTemplateClientSocket adClient = ServerSocketThreadAD.adTemplateMap.get(machineId);
		try {
			if(adClient != null && adClient.getClient().isOutputShutdown() == false) {
				OutputStream out = adClient.getClient().getOutputStream();
				String retStr = jsonObj.toString();
				out.write(retStr.getBytes("utf-8"));
				out.flush();
			} else {
				System.out.println("@@广告连接socket断开连接");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run(){
		try{
			System.out.println("@@广告数据连接ServerSocket启动，端口 = " + SERVERPORT);
			new ProcessHeartBeatClientThread().start();
			
			while(!this.isInterrupted()){
				//如果主socket没有被中断
				Socket client = serverSocket.accept();//阻塞等待客户端的连接
				client.setTcpNoDelay(true);//立即发送数据
				client.setKeepAlive(true);//当长时间未能发送数据，服务器主动断开连接
				ClientSocketThreadAdTemplate client_thread = new ClientSocketThreadAdTemplate(client, robotDao, adTemplateDao, goodsService);
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
	
	/**
	 * 创建广告连接通道心跳检测线程
	 * */
	public class ProcessHeartBeatClientThread extends Thread{
		@Override
		public void run(){
			while(true){
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				synchronized (ServerSocketThreadAD.adTemplateMap) {
					Iterator<Entry<String, AdTemplateClientSocket>> it = ServerSocketThreadAD.adTemplateMap.entrySet().iterator();
					while(it.hasNext()) {
						Map.Entry<String, AdTemplateClientSocket> entry = it.next();
						String key = entry.getKey(); // 机器编号
						AdTemplateClientSocket beat = entry.getValue();
						if((new Date().getTime() - beat.getPreDate().getTime()) >= 1000*6){ //秒
							if(!beat.isTimeout()) {
								beat.setTimeout(true);
								beat.getClientThread().closeClient();//关闭连接socket和释放线程
								// it.remove();//从在线列表中移除
								System.out.println("@@广告连接线程心跳超时，移除客户端 machineID = " + key);
							}							
						}
					}
				}
			}
		}
	}	
}
