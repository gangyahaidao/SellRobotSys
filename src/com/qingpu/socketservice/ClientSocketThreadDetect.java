package com.qingpu.socketservice;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import com.qingpu.common.utils.DataProcessUtils;
import com.qingpu.common.utils.QingpuConstants;

public class ClientSocketThreadDetect extends Thread {

	private Socket client;
	
	public ClientSocketThreadDetect(Socket client) {
		this.client = client;
	}
	
	@Override
	public void run() {
		System.out.println("@@人体检测连接客户端信息: ip = " + client.getInetAddress() + ", port = " + client.getPort());
		Date preMoveDate = new Date(); // 用于进行行走计时
		Date preStopDate = new Date(); // 用于停止的时间
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
						String detectStr = jsonObj.getString("detectStr"); // 人体检测内容
												
						if("register".equals(detectStr)) { // 收到人体检测的注册消息  register
							System.out.println("@@人体检测收到注册数据 = " + content);							
							DetectClientSocket detectClient = ServerSocketThreadDetect.detectMachineMap.get(machineId);
							if(detectClient == null) {
								System.out.println("@@第一次收到人体检测的注册消息");
								detectClient = new DetectClientSocket();
								detectClient.setClient(this.client);
								detectClient.setClientThread(this);
								detectClient.setMachineID(machineId);
								detectClient.setPreDate(new Date());								
							} else {
								System.out.println("@@收到人体检测断开重新注册的消息");
								detectClient.getClientThread().closeClient();
								detectClient.setClient(this.client);
								detectClient.setPreDate(new Date());
								detectClient.setClientThread(this);
							}
							detectClient.setTimeout(false);
							ServerSocketThreadDetect.detectMachineMap.put(machineId, detectClient);
						} else if("heartbeat".equals(detectStr)) {
							System.out.println("@@收到人体检测控制心跳");
							DetectClientSocket detectObj = ServerSocketThreadDetect.detectMachineMap.get(machineId);
							if(detectObj != null) {
								detectObj.setPreDate(new Date());
								detectObj.setClient(this.client);
								ServerSocketThreadDetect.detectMachineMap.put(machineId, detectObj); // 更新连接信息
							}
						} else if("stop".equals(detectStr)) { // 接收到人体检测的停止命令，感应状态没有切换的正常情况下两秒钟一次，如果有切换立即发送
							preStopDate = new Date(); // 更新时间用于继续运动的时间检测
							Date currentDate = new Date();
							long deltaMilliSeconds = currentDate.getTime() - preMoveDate.getTime(); // 获取当前距离上一次收到前进命令的时间间隔
							ContainerClientSocket containerClient = ServerSocketThread.containerMachineMap.get(machineId);							
							if((containerClient!=null && containerClient.isCustomScanQrCode()) || (containerClient!=null && containerClient.isInBuyGoodsProcess())) {
								System.out.println("@@机器人处于被扫码或者出货状态，收到停止命令，发送停止命令到底盘");
								ServerSocketThreadRobot.sendMoveCmdToRoobt(machineId, true); // 发送停止命令
							} else {
								// System.out.println("@@机器人没有处于扫码和出货状态，收到停止命令");
								if(deltaMilliSeconds >= QingpuConstants.RECV_STOP_DELAY_TIME) { // 停留时间超过指定秒数									
									if(jsonObj.has("genderStr")) { // 播报指定的超时语句
										String genderStr = jsonObj.getString("genderStr"); // people / male / female
										String speakMessage = ServerSocketThreadDetect.findDialogByDetectResult("senseTimeout", genderStr, machineId); // 获取感应超时状态下的概率语音
										System.out.println("@@停止超时发送指定的播报语句 = " + speakMessage);
										if(speakMessage != null) {
											ServerSocketThreadDetect.sendDataToDetectSocket(machineId, speakMessage); // 发送语音数据到人体检测模块进行播报
										}									
									}
									System.out.println("@@停止超过指定时间，发送继续运动命令");
									ServerSocketThreadRobot.sendMoveCmdToRoobt(machineId, false); // 发送继续运动命令
									if(deltaMilliSeconds >= QingpuConstants.RECV_STOP_DELAY_TIME+QingpuConstants.RECV_STOP_DELAY_GOON) { // 当继续运动了几秒钟，则停止
										System.out.println("@@前方障碍物情况下继续运动了指定时间，发送停止命令");
										preMoveDate = new Date();
										ServerSocketThreadRobot.sendMoveCmdToRoobt(machineId, true); // 发送停止命令
									}
								} else { // 正常停止消息
									ServerSocketThreadRobot.sendMoveCmdToRoobt(machineId, true); // 发送停止命令									
									// 停止命令中包含有人体性别检测信息，检测信息客户端每隔一定时间才上传一次，只有停止和继续运动命令是每隔两秒上传一次
									if(jsonObj.has("genderStr")) { // 非忙状态下才响应人体检测结果发送对话
										String genderStr = jsonObj.getString("genderStr"); // people / male / female
										System.out.println("@@自由巡逻状态收到停止命令，检测到人genderStr = " + genderStr);
										String speakMessage = ServerSocketThreadDetect.findDialogByDetectResult("senseYes", genderStr, machineId); // 获取感应触发状态下的概率语音
										System.out.println("@@播报语句 = " + speakMessage);
										if(speakMessage != null) {
											ServerSocketThreadDetect.sendDataToDetectSocket(machineId, speakMessage); // 发送语音数据到人体检测模块进行播报
										}									
									}
								}								
							}							
						} else if("move".equals(detectStr)){ // 接收到人体检测的继续运动命令，正常情况下人体检测模块两秒钟发送一次
							preMoveDate = new Date();
							ContainerClientSocket containerClient = ServerSocketThread.containerMachineMap.get(machineId);
							if(ServerSocketThreadDetect.detectMachineMap.get(machineId).isReachedGoalNeedStop() // 扫码超时检测是在货柜ClientSocketThread中进行检查的
									|| (containerClient!=null && containerClient.isCustomScanQrCode())
									|| (containerClient!=null && containerClient.isInBuyGoodsProcess())) { // 如果机器人到达了终点或者用户进行了扫码或者用户已经付款等待出货，则不响应继续运动命令
								System.out.println("@@处于终点或者用户扫码或者买东西阶段，忽略行走检测");
								preStopDate = new Date(); // 更新停止的计时，为的是继续运动的时候不会马上播报自由巡逻状态下的对话
							} else {
								Date currentDate = new Date();
								long deltaMilliSeconds = currentDate.getTime() - preStopDate.getTime();
								if(deltaMilliSeconds >= QingpuConstants.RECV_MOVE_CONTINUE_TIME) { // 如果自由行走超过指定的时间，则根据当前时间挑选一个随机的对话进行播报
									String speakMessage = ServerSocketThreadDetect.findPatrolDialogByState("freegoing", null, machineId); // 根据当前时间查找一个对话
									System.out.println("@@一直继续运动超时发送巡逻状态下的随机对话 = " + speakMessage);
									if(speakMessage != null) {
										ServerSocketThreadDetect.sendDataToDetectSocket(machineId, speakMessage); // 发送语音数据到人体检测模块进行播报
									}
									preStopDate = new Date(); // 更新延迟时间
								}
								// System.out.println("@@发送巡逻运动命令");
								ServerSocketThreadRobot.sendMoveCmdToRoobt(machineId, false); // 继续运动
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
			System.out.println("@@人体检测控制socket连接断开  = " + e.getMessage());			
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
}
