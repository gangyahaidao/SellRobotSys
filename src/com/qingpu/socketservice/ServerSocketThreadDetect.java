package com.qingpu.socketservice;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.qingpu.common.utils.CommonUtils;
import com.qingpu.common.utils.HttpRequestUtils;
import com.qingpu.common.utils.WeiXinUtils;
import com.qingpu.goods.entity.Goods;
import com.qingpu.goods.service.GoodsService;
import com.qingpu.robots.dao.RobotsDao;
import com.qingpu.robots.entity.OneContainerFloor;
import com.qingpu.robots.entity.Robot;
import com.qingpu.robots.entity.RobotOtherDialog;
import com.qingpu.robots.entity.RobotPatrolOrSenseDialog;
import com.qingpu.robots.entity.TalkTemplate;

/**
 * 人体检测和语音播报连接的Socket
 * */
public class ServerSocketThreadDetect extends Thread {
	private static RobotsDao robotDao;
	private static GoodsService goodsService;
	private ServerSocket serverSocket;
	private static final int SERVERPORT = 9877;
	
	public static Map<String, DetectClientSocket> detectMachineMap = new HashMap<String, DetectClientSocket>();
	
	public ServerSocketThreadDetect(GoodsService goodsService, RobotsDao robotDao){
		try {
			if (null == serverSocket) {
				this.robotDao = robotDao;
				this.goodsService = goodsService;
				this.serverSocket = new ServerSocket(SERVERPORT);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 根据socket对象找到map中对应的封装对象
	 * */
	public static DetectClientSocket getDetectConnectObj(Socket client) {
		DetectClientSocket valueObj = null;
		
		// 遍历Map对象
		Iterator<Entry<String, DetectClientSocket>> it0 = ServerSocketThreadDetect.detectMachineMap.entrySet().iterator();
		while(it0.hasNext()) {
			Map.Entry<String, DetectClientSocket> entry = it0.next();
			valueObj = entry.getValue();
			if(valueObj.getClient() == client) {
				return valueObj;
			}
		}		
		System.out.println("@@人体检测连接socket通道不存在");
		return valueObj;
	} 
	
	/**
	 * 发送数据到人体检测连接socket对象，主要是发送需要播报的语音字符串
	 * */
	public static void sendDataToDetectSocket(String machineId, String speakMessage) {
		if(speakMessage == null) {
			System.out.println("@@对话内容不存在");
			return;
		}
		try {
			DetectClientSocket detectClient = ServerSocketThreadDetect.detectMachineMap.get(machineId);
			if(detectClient != null && detectClient.getClient().isOutputShutdown() == false) {
				OutputStream out = detectClient.getClient().getOutputStream();
				JSONObject obj = new JSONObject();
				obj.put("speakStr", speakMessage);
				out.write(obj.toString().getBytes());
				out.flush();
			}else{
				System.out.println("@@人体检测socket断开连接");
			}			
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 发送控制命令到人体检测模块
	 * */
	public static void sendControlCmdToDetectSocket(String machineId, String cmdType) {
		try {
			DetectClientSocket detectClient = ServerSocketThreadDetect.detectMachineMap.get(machineId);
			if(detectClient != null && detectClient.getClient().isOutputShutdown() == false) {
				OutputStream out = detectClient.getClient().getOutputStream();
				JSONObject obj = new JSONObject();
				obj.put("cmdType", cmdType);
				out.write(obj.toString().getBytes());
				out.flush();
				System.out.println("@@发送控制命令到人体检测模块 = " + obj.toString());
			}else{
				System.out.println("@@人体检测socket断开连接");
			}			
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * 发送回复心跳到连接客户端
	 * */
	public static void sendHeartbeatToDetectSocket(DetectClientSocket detectObj) {
		try {
			if(detectObj != null) {
				OutputStream out = detectObj.getClient().getOutputStream();
				JSONObject obj = new JSONObject();
				obj.put("Back-HB", 1);
				out.write(obj.toString().getBytes());
				out.flush();
				// System.out.println("@@回复人体检测心跳Back-HB");
			}else{
				System.out.println("@@发送心跳，人体检测socket断开连接");
			}			
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 根据当前的时间返回对话模板中时间的字符串变量
	 * */
	private static String getTemplateTimeName(){
		String retStr = "";		
		Date date = new Date();
		int hour = date.getHours();
		int minites = date.getMinutes();
		switch (hour) {
		case 8:
			retStr = "八点";
			break;
		case 9:
			retStr = "九点";
			break;
		case 10:
			retStr = "十点";
			break;
		case 11:
			retStr = "十一点";
			break;
		case 12:
			retStr = "十二点";
			break;
		case 13:
			retStr = "一点";
			break;
		case 14:
			retStr = "两点";
			break;
		case 15:
			retStr = "三点";
			break;
		case 16:
			retStr = "四点";
			break;
		case 17:
			retStr = "五点";
			break;
		case 18:
			retStr = "六点";
			break;			
		case 19:
			retStr = "七点";
			break;
		case 20:
			retStr = "八点";
			break;
		case 21:
			retStr = "九点";
			break;
		case 22:
			retStr = "十点";
			break;
		case 23:
			retStr = "十一点";
			break;
		default:
			retStr = "测试时间点";
			break;
		}
		if((minites >= 0 && minites <= 15)) {
			retStr += "了";
		}else {
			retStr += "多";
		}		
		return retStr;
	}
	/**
	 * 替换查找到的对话中的模板变量
	 * {{GeneralCall}}通用吆喝男女称呼 {{GoodsName}}商品名字 {{GenderName}}感应到人不同性别的称呼   {{TimeName}}需要换成整点 {{Weather}}替换成今天的天气
	 * */
	private static String replaceTalkTemplateStr(String templateStr, String genderStr, String machineId) {
		if(templateStr == null)
			return null;
		
		if(templateStr.contains("GeneralCall")) { // 填充通用吆喝对话
			List<TalkTemplate> list = robotDao.getRobotTalkTemplateByType("common");
			int index = CommonUtils.getRandomNum(0, list.size()-1);			
			Map<String, String> map = new HashMap<String, String>();		
			map.put("GeneralCall", list.get(index).getContent());
			templateStr = WeiXinUtils.render(templateStr, map);
		}
		if(templateStr.contains("GenderName") && genderStr != null) { // 需要查找并替换不同性别的模板变量
			List<TalkTemplate> list = null;
			int index = 0;
			if("people".equals(genderStr)) { // 只检测到人
				list = robotDao.getRobotTalkTemplateByType("nosex");
				index = CommonUtils.getRandomNum(0, list.size()-1);				
			} else if("male".equals(genderStr)) {
				list = robotDao.getRobotTalkTemplateByType("male");
				index = CommonUtils.getRandomNum(0, list.size()-1);				
			} else if("female".equals(genderStr)) {
				list = robotDao.getRobotTalkTemplateByType("female");
				index = CommonUtils.getRandomNum(0, list.size()-1);				
			}
			Map<String, String> map = new HashMap<String, String>();		
			map.put("GenderName", list.get(index).getContent());
			templateStr = WeiXinUtils.render(templateStr, map);
		}
		if(templateStr.contains("TimeName")) { // 替换时间变量
			String timeStr = getTemplateTimeName();
			Map<String, String> map = new HashMap<String, String>();
			map.put("TimeName", timeStr);
			templateStr = WeiXinUtils.render(templateStr, map);
		}
		if(templateStr.contains("Weather")) { // 替换天气变量
			String weatherStr = HttpRequestUtils.GetWeatherData("长沙");
			JSONObject weatherObj = new JSONObject(weatherStr);
			if(weatherObj.has("data")) {
				JSONObject dataObj = weatherObj.getJSONObject("data");
				JSONArray jsonArr = dataObj.getJSONArray("forecast");
				JSONObject obj = jsonArr.getJSONObject(0);
				
				String high = obj.getString("high"); // 获取高温			
				String regEx="[^0-9]";  
				Pattern p = Pattern.compile(regEx);  
				Matcher m = p.matcher(high);  
				high = m.replaceAll("").trim();
				
				String low = obj.getString("low"); // 获取低温			  
				Pattern p2 = Pattern.compile(regEx);  
				Matcher m2 = p2.matcher(low);  
				low = m2.replaceAll("").trim();
				
				String resultStr = null;
				int average = (Integer.parseInt(high)+Integer.parseInt(low))/2;
				resultStr = CommonUtils.transferDigitalToString(average+""); // 将数字转换成汉字读法
				resultStr = "长沙平均气温" + resultStr + "度，";
				if(average <= 18) {
					resultStr = resultStr + "气温有点低";
				} else if(average >= 25) {
					resultStr = resultStr + "有点热";
				} else {
					resultStr = resultStr + "凉爽舒适";
				}
				Map<String, String> map = new HashMap<String, String>();
				map.put("Weather", resultStr);
				templateStr = WeiXinUtils.render(templateStr, map);
			}			
		}
		if(templateStr.contains("GoodsName") && machineId != null) { // 替换商品名称变量
			int count = CommonUtils.getAppearCount(templateStr, "GoodsName");
			List<String> goodsNameList = new ArrayList<String>();
			// 获取当前机器人上所搭载的商品名称列表
			Robot robot = robotDao.getRobotByMachineId(machineId);
			if(robot != null) {				
				List<OneContainerFloor> containerFloor = robot.getContainerFloors();
				containerFloor.removeAll(Collections.singleton(null));
				for(OneContainerFloor item : containerFloor) {
					String goodsId = item.getGoodsSerialId();
					Goods goods = goodsService.getGoodsById(goodsId);
					goodsNameList.add(goods.getName());
				}				
			}
			int realGoodsCount = goodsNameList.size();
			int replaceCount = 0;
			while(true) {
				int beginIndex = templateStr.indexOf("{{GoodsName}}");
				int endIndex = beginIndex + "{{GoodsName}}".length();
				int index = CommonUtils.getRandomNum(0, goodsNameList.size()-1);
				templateStr = templateStr.substring(0, beginIndex) + goodsNameList.get(index) + templateStr.substring(endIndex);
				goodsNameList.remove(index); // 移除已经添加的商品名字
				replaceCount++;
				if(replaceCount >= realGoodsCount || replaceCount >= count) {// 判断进行替换的字符串次数是否大于商品的实际数目或者大于模板需要替换的次数
					break;
				}
			}
			templateStr = templateStr.replace("{{GoodsName}}", ""); // 替换多的模板变量为空，不能用replaceAll是用正则表达式的		
		}
		// System.out.println("@@模板替换输出对话 = " + templateStr);
		return templateStr;
	}
	/**
	 * 根据人体检测的结果筛选一个对话发送到人体检测模块
	 * 包括无人巡逻和到达特定目标点以及人体检测
	 * 查询感应触发对话数据表内容
	 * state: senseYes / senseTimeout
	 * genderStr: people/male/female
	 * */	
	public static String findDialogByDetectResult(String state, String genderStr, String machineId) {
		String retSpeakStr = null;
		String genderStrBack = genderStr; // 保存用来进行模板替换
		
		if("people".equals(genderStr)) { // 只检测到人
			genderStr = "感应到人";
		} else if("male".equals(genderStr)) {
			genderStr = "感应到男性";
		} else if("female".equals(genderStr)) {
			genderStr = "感应到女性";
		}
		List<RobotPatrolOrSenseDialog> list = robotDao.getSensePeopleDialogs("sensepeople", state, genderStr); //三个参数：type state peopleinfo
		if(list.size() == 1) {
			retSpeakStr = list.get(0).getMessage();
		} else if(list.size() > 1) {
			List<Integer> listProb = new ArrayList<Integer>();
			for(int i = 0; i < list.size(); i++) { // 将概率添加到list中
				listProb.add(list.get(i).getProbability());
			}
			int index = CommonUtils.findProbabilityListIndex(listProb);
			retSpeakStr = list.get(index).getMessage();
		} else {
			System.out.println("@@没有人体检测相关的对话，请添加");
		}
		retSpeakStr = replaceTalkTemplateStr(retSpeakStr, genderStrBack, machineId);
		
		return retSpeakStr;
	}
	
	private static String getCurrentTimeIntervalName(){
		String ret = null;
		Date currentDate = new Date();
		int hours = currentDate.getHours(); // 返回当前时间的小时值0-23
		if(hours >= 0 && hours < 11) { // 上午时间
			ret = "forenoon";
		} else if(hours >= 11 && hours < 14) { // 午饭时间
			ret = "noon";
		} else if(hours >= 14 && hours < 17) { // 下午时间
			ret = "afternoon";
		} else if(hours >= 17 && hours < 20) { // 晚饭时间
			ret = "dinner";
		} else if(hours >= 20 && hours <= 23) { // 晚上时间
			ret = "evening";
		}
		return ret;
	}
	/**
	 * 根据当前的时间查找一个自由巡逻的对话，或者是到达一个中间点查找对话
	 * */
	public static String findPatrolDialogByState(String state, String reachedGoal, String machineId) {
		String retSpeakStr = null;
		String timeIntervalName = getCurrentTimeIntervalName();
		List<RobotPatrolOrSenseDialog> list = robotDao.getPatrolDialogByCondition(timeIntervalName, state, reachedGoal); // 时间段  状态  到达的地点
		if(list.size() == 1) {
			retSpeakStr = list.get(0).getMessage();
		} else if(list.size() > 1) {
			List<Integer> listProb = new ArrayList<Integer>();
			for(int i = 0; i < list.size(); i++) { // 将概率添加到list中
				listProb.add(list.get(i).getProbability());
			}
			int index = CommonUtils.findProbabilityListIndex(listProb);
			retSpeakStr = list.get(index).getMessage();
		} else {
			System.out.println("@@没有找到巡逻相关的对话，请添加");
		}
		retSpeakStr = replaceTalkTemplateStr(retSpeakStr, null, machineId);
		
		return retSpeakStr;
	}
	/**
	 * 根据条件查找其他模式的对话，主要包括："userscan"用户扫码操作    "userpay"用户支付   "goodsout"商品出货   "opendoor"取货
	 * */
	public static String findOtherDialogByTypeState(int talkId, String type, String state) {
		String retSpeakStr = null;
		List<RobotOtherDialog> list = robotDao.getOtherRobotDialog(talkId, type, state);
		if(list.size() == 1) {
			retSpeakStr = list.get(0).getMessage();
		} else if(list.size() > 1) {
			List<Integer> listProb = new ArrayList<Integer>();
			for(int i = 0; i < list.size(); i++) { // 将概率添加到list中
				listProb.add(list.get(i).getProbability());
			}
			int index = CommonUtils.findProbabilityListIndex(listProb);
			retSpeakStr = list.get(index).getMessage();
		} else {
			System.out.println("@@没有找到其他模式相关的对话，请添加");
		}
		System.out.println("@@其他模式查找的对话 = " + retSpeakStr);
		return retSpeakStr;
	}
	
	@Override
	public void run(){
		try {			
			System.out.println("@@SellRobotSys人体检测服务端Socket启动, 端口 = " + SERVERPORT);
			new ProcessHeartBeatClientThread().start(); // 启动人体检测连接心跳监测线程
			
			while(!this.isInterrupted()){
				//如果主socket没有被中断
				Socket client = serverSocket.accept();//阻塞等待客户端的连接
				client.setTcpNoDelay(true);//立即发送数据
				client.setKeepAlive(true);//当长时间未能发送数据，服务器主动断开连接				
				//创建新的客户端线程处理请求，如果请求鉴权通过就加入到在线客户端列表中，如果不通过则销毁
				ClientSocketThreadDetect client_thread = new ClientSocketThreadDetect(client);
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
	
	/**
	 * 创建人体通道心跳检测线程
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
				synchronized (ServerSocketThreadDetect.detectMachineMap) {
					Iterator<Entry<String, DetectClientSocket>> it = ServerSocketThreadDetect.detectMachineMap.entrySet().iterator();
					while(it.hasNext()) {
						Map.Entry<String, DetectClientSocket> entry = it.next();
						String key = entry.getKey();//机器编号
						DetectClientSocket beat = entry.getValue();
						if((new Date().getTime() - beat.getPreDate().getTime()) >= 1000*5){ //秒
							if(!beat.isTimeout()) { // 如果还没有设置超时
								beat.setTimeout(true);
								beat.getClientThread().closeClient();//关闭连接socket和释放线程
								//it.remove();//从在线列表中移除
								System.out.println("@@人体识别控制线程心跳超时，移除客户端 machineID = " + key);
							}							
						}
					}
				}
			}
		}
	}
}
