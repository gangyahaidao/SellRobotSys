package com.qingpu.socketservice;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

import com.qingpu.adtemplate.dao.AdTemplateDao;
import com.qingpu.adtemplate.entity.AdTemplate;
import com.qingpu.adtemplate.entity.FileInfoObj;
import com.qingpu.common.entity.ReturnObject;
import com.qingpu.common.utils.DataProcessUtils;
import com.qingpu.common.utils.QingpuConstants;
import com.qingpu.goods.entity.Goods;
import com.qingpu.goods.service.GoodsService;
import com.qingpu.robots.dao.RobotsDao;
import com.qingpu.robots.entity.OneContainerFloor;
import com.qingpu.robots.entity.Robot;

/**
 * 广告显示客户端连接客户端
 * */
public class ClientSocketThreadAdTemplate extends Thread {
	private Socket client;
	private RobotsDao robotDao;
	private AdTemplateDao adTemplateDao;
	private GoodsService goodsService;
	
	public ClientSocketThreadAdTemplate(Socket client, RobotsDao robotDao, AdTemplateDao adTemplateDao, GoodsService goodsService) {
		this.client = client;
		this.robotDao = robotDao;
		this.adTemplateDao = adTemplateDao;
		this.goodsService = goodsService;
	}
	
	@Override
	public void run() {
		System.out.println("@@广告播放连接客户端信息: ip = " + client.getInetAddress() + ", port = " + client.getPort());
		try {
			InputStream in = client.getInputStream();
			byte[] result = new byte[0];
			int tmp = -1;
			boolean header = false;
			boolean tailer = false;
			
			while (!this.isInterrupted()) {			
				try {
					Thread.sleep(50);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				while ((tmp = in.read()) != -1) {
					byte b = (byte) tmp;
					if(b == QingpuConstants.HEADER_CHAR){ // '#'
						header = true; //收到消息开始字节										
					}else if(b == QingpuConstants.TAILER_CHAR){ // '@'
						tailer = true; //收到结束字节
						header = false; // 开始处理一帧数据
						tailer = false;
						String content = new String(result);
						JSONObject jsonObj = new JSONObject(content);
						String machineId = jsonObj.getString("machineId"); // 人体检测对应的编号
						String cmdStr = jsonObj.getString("cmdStr");
						if("register".equals(cmdStr)) { // 收到注册消息
							synchronized (ServerSocketThreadAD.adTemplateMap) {
								System.out.println("@@广告模板收到注册数据machineId = " + machineId);
								AdTemplateClientSocket adClient = new AdTemplateClientSocket();
								adClient.setClient(this.client);
								adClient.setClientThread(this);
								adClient.setMachineID(machineId);
								adClient.setPreDate(new Date());
								ServerSocketThreadAD.adTemplateMap.put(machineId, adClient);
							}							
						} else if("heartbeat".equals(cmdStr)) { // 心跳消息
							synchronized (ServerSocketThreadAD.adTemplateMap) {
								AdTemplateClientSocket adClient = ServerSocketThreadAD.adTemplateMap.get(machineId);
								if(adClient != null) {
									adClient.setPreDate(new Date());
									adClient.setClient(getClient());
									adClient.setClientThread(this);
								} else {
									adClient = new AdTemplateClientSocket();
									adClient.setClient(getClient());
									adClient.setClientThread(this);
									adClient.setMachineID(machineId);
									adClient.setPreDate(new Date());								
								}
								adClient.setTimeout(false);
								ServerSocketThreadAD.adTemplateMap.put(machineId, adClient);
							}							
						} else if("getRobotsGoodsJSONObj".equals(cmdStr)) { // 获取指定编号机器人上的商品列表
							ReturnObject retObj = new ReturnObject();
							Robot robot = robotDao.getRobotByMachineId(machineId);							
							if(robot != null) {
								List<OneContainerFloor> containerFloors = robot.getContainerFloors();
								containerFloors.removeAll(Collections.singleton(null));
								JSONArray retJSONArr = new JSONArray();
								for(OneContainerFloor floor : containerFloors) {
									String goodsSerialId = floor.getGoodsSerialId(); // 获取商品编号
									Goods goods = goodsService.getGoodsById(goodsSerialId);
									if(goods != null) {
										JSONObject obj = new JSONObject();
										obj.put("goodsName", goods.getName());
										obj.put("picURL", goods.getFileurl());
										obj.put("price", goods.getPrice());
										retJSONArr.put(obj);
									} else {
										retObj.setMessage("前面屏幕获取机器人商品列表查询机器人商品错误");
									}
								}
								JSONObject retJSONObj = new JSONObject();
								retJSONObj.put("code", 0);
								retJSONObj.put("message", "操作成功");
								retJSONObj.put("cmdStr", "goodsData");
								retJSONObj.put("fileJSONArr", retJSONArr);
								System.out.println("@@发送前屏商品数据 = " + retJSONObj.toString());
								ServerSocketThreadAD.sendDataToAdSocket(machineId, retJSONObj);
								result = new byte[0]; // 清空上次接收的数据
								continue;
							} else {
								System.out.println("@@获取机器人商品列表数据机器人对象不存在， machineId = " + machineId);
								retObj.setMessage("机器人ID错误，不存在");
							}
						}
						// 清空上次接收的数据
						result = new byte[0];
					}else {
						result = DataProcessUtils.appendByte(result, b);
					}
				}
			}			
		} catch (IOException e) {
			System.out.println("@@广告连接客户端异常 = " + e.getMessage());			
		}
	}
	
	// 关闭连接socket和销毁线程
	public void closeClient() {
		try {
			this.interrupt();
			if (client != null) {
				if(!client.isClosed()){
					client.getInputStream().close();
				}
				if(!client.isClosed()){
					client.getOutputStream().close();
				}				
				if(!client.isClosed()){
					client.close();
				}											
			}			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Socket getClient() {
		return client;
	}

	public void setClient(Socket client) {
		this.client = client;
	}
}
