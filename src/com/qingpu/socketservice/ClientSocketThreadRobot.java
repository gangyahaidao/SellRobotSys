package com.qingpu.socketservice;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.qingpu.common.service.WeiXinTemplateService;
import com.qingpu.common.utils.DataProcessUtils;
import com.qingpu.common.utils.QingpuConstants;

public class ClientSocketThreadRobot extends Thread {
	private Socket client;
	private WeiXinTemplateService weiXinTemplateService;

	public ClientSocketThreadRobot(Socket client, WeiXinTemplateService weiXinTemplateService) {
		this.client = client;
		this.weiXinTemplateService = weiXinTemplateService;
	}

	@Override
	public void run() {
		System.out.println("@@连接零售机器人底盘客户端信息: ip = " + client.getInetAddress() + ", port = " + client.getPort() 
				+ ", time = " + new Date() + ", thread = " + this.getName());
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
					if(b == QingpuConstants.HEADER_BYTE){
						if(!header){//收到消息开始字节
							header = true;
						}else{//收到结束字节
							tailer = true;
						}											
					}
					result = DataProcessUtils.appendByte(result, b);
					if(header &&  tailer){//如果既收到头又收到尾，则收到一条完整信息，开始处理
						header = false;
						tailer = false;
						handleReceivedData(result);						
						result = new byte[0];// 清空上次接收的数据
					}					
				}
			}			
		} catch (IOException e) {
			e.printStackTrace();			
		}
	}	

	/**
	 * 从接受的数据中解析出消息体，并根据加密方式进行解密，返回解密之后的数据
	 * */
	private byte[] getDecryptedContent(byte[] result){
		// 获取消息体长度
		int bodyLength = result[5] & 0xff;
		bodyLength = (bodyLength << 8) | (result[4] & 0xff);
		if(bodyLength > 0){
			// 根据消息体长度获取消息体内容
			byte[] content = new byte[bodyLength];
			// 去除消息头数据，将消息体数据复制到content数组中
			System.arraycopy(result, 6, content, 0, bodyLength);
			// 如果消息体不为空再判断消息体的加密方式
			if (result[3] == (byte) QingpuConstants.ENCRYPT_BY_NONE) {
				// 不加密
			} else if (result[3] == (byte) QingpuConstants.ENCRYPT_BY_RSA) {
				// 使用非对称RSA加密
			} else if (result[3] == (byte) QingpuConstants.ENCRYPT_BY_AES) {
				// 使用对称AES加密
			} else {
				// 未知加密方式
				System.out.println("@@error, Unknown Encrypt Type");
			}
			return content;
		}
		
		return null;
	}
	private void handleReceivedData(byte[] result) {
		// TODO Auto-generated method stub
		byte flag = result[0];
		int cmd = -1;
		if (flag == (byte) QingpuConstants.HEADER_BYTE) {
			// 消息首字节正确
			// 1.转义还原
			byte[] data = DataProcessUtils.replaceData(result);
			// 2.校验验证码
			boolean check = DataProcessUtils.checkMessage(data);
			if (check) {
				cmd = result[2] & 0xff;
				cmd = (cmd << 8) | (result[1] & 0xff);
				byte[] content = getDecryptedContent(result);
				if(content != null){ 
					if(cmd == QingpuConstants.RECV_HEART_BEAT) { // 接收到底盘的心跳
						JSONObject jsonobj = new JSONObject(new String(content));
						String machineId = jsonobj.getString("machineId");
						RobotClientSocket clientObj = ServerSocketThreadRobot.robotMachineMap.get(machineId);
						if(clientObj != null) {
							clientObj.setPreDate(new Date());
							clientObj.setClient(this.getClient());
							ServerSocketThreadRobot.robotMachineMap.put(machineId, clientObj);
							// 返回一个心跳包给客户端
							JSONObject retJsonObj = new JSONObject();
							retJsonObj.put("machineId", "3");
							ResponseSocketUtils.sendJsonDataToClient(
									retJsonObj, 
									this.getClient(),
									QingpuConstants.SEND_BACK_HEART_BEAT,
									QingpuConstants.ENCRYPT_BY_NONE,
									QingpuConstants.DATA_TYPE_JSON);
						} else {
							System.out.println("@@收到心跳，但是底盘连接通道被定时器断开");
						}						
					} else if (cmd == QingpuConstants.RECV_ROBOT_REGISTER_CODE) { // 接收到机器人的初始注册信息
						System.out.println("@@收到底盘机器人注册消息 = " + new String(content));
						JSONObject jsonobj = new JSONObject(new String(content));
						String registerCode = jsonobj.getString("registerCode");
						RobotClientSocket clientObj = ServerSocketThreadRobot.robotMachineMap.get(registerCode);
						if(clientObj != null) { // 如果前面连接的线程还没有释放，则先释放原来的线程
							clientObj.getClientThread().closeClient();
							System.out.println("@@收到断开重新注册的消息，则先释放原来的Socket资源");
							clientObj.setClient(this.client);
							clientObj.setClientThread(this);
							clientObj.setPreDate(new Date());
						} else {
							System.out.println("@@收到底盘第一次注册消息");
							clientObj = new RobotClientSocket();
							clientObj.setClient(this.client);
							clientObj.setClientThread(this);
							clientObj.setMachineID(registerCode);
							clientObj.setPreDate(new Date());							
							clientObj.setHasRobotReachedGoal(true); // 设置机器人初始连接处于空闲状态
						}						
						ServerSocketThreadRobot.robotMachineMap.put(registerCode, clientObj);
					} else if(cmd == QingpuConstants.RECV_ROBOT_POS_SPEED){ // 接收机器人发送的位置和速度消息
						RobotClientSocket clientObj = ServerSocketThreadRobot.getRobotConnectObj(this.client);
						if(clientObj != null) {
							System.out.println("@@RECV_ROBOT_POS_SPEED = " + new String(content));
							JSONObject jsonObj = new JSONObject(new String(content));
							clientObj.setRecvRobotPosAndSpeedObj(jsonObj); // 设置机器人底盘上报的速度和路段信息
							int carOnePosPercent = jsonObj.getInt("carOnePosPercent");
							if(carOnePosPercent == 100) { // 如果到达了某点
								String carOneEndPosName = jsonObj.getString("carOneEndPosName");
								System.out.println("@@到达中途地点 = " + carOneEndPosName);	
								weiXinTemplateService.sendTemplateMessageToUniqueUser("到达中途点：" + carOneEndPosName);
								clientObj.setCurrentPosName(carOneEndPosName); // 设置当前到达的中间点
								// 向底盘发送停止命令，同时设置底盘的标志位到达某点为true，此段时间不响应人体检测的move命令								
								DetectClientSocket detectObj = ServerSocketThreadDetect.detectMachineMap.get(clientObj.getMachineID()); 
								if(detectObj != null) {// 设置标志位不响应行走命令
									detectObj.setReachedGoalNeedStop(true);
								}									
								ServerSocketThreadRobot.sendMoveCmdToRoobt(clientObj.getMachineID(), true); // 发送停止命令
								
								// 获取设置的到达此中途点需要停留的秒数，目前的实现是路径中不能有重复的点，如果有重复的点就需要再考虑
								int staySec = 0; 
								JSONArray pathPosArr = clientObj.getPosStayTimeJSONArr();
								for(int i = 0; i < pathPosArr.length(); i++) {
									JSONObject obj = pathPosArr.getJSONObject(i);
									String name = obj.getString("posName");
									if(name.equals(carOneEndPosName)) {
										staySec = obj.getInt("staySec");
										break;
									}
								}
								int delayCount = 0;
								boolean needSendSpeakMessage = false;
								while(true) { // 休眠一段时间，如果没有扫码就继续行走，中间会重复说话
									if(staySec > 0) { // 需要停留指定的时间
										if(delayCount >= staySec) { // 如果超过设置的时间
											// 设置标志位开始继续响应行走命令
											if(detectObj != null) {
												detectObj.setReachedGoalNeedStop(false); // 离开此点，继续响应人体检测运动命令
											}											
											ServerSocketThreadRobot.sendMoveCmdToRoobt(clientObj.getMachineID(), false); // 发送继续行走命令
											System.out.println("@@底盘继续前往下一个目标点");
											break;											
										}
										if (delayCount % 15 == 0) {
											needSendSpeakMessage = true;
										}										
									} else { // 按默认时间停留
										if(delayCount >= 30) {
											// 设置标志位开始继续响应行走命令
											if(detectObj != null) {
												detectObj.setReachedGoalNeedStop(false); // 离开此点，继续响应人体检测运动命令
											}											
											ServerSocketThreadRobot.sendMoveCmdToRoobt(clientObj.getMachineID(), false); // 发送继续行走命令
											System.out.println("@@底盘继续前往下一个目标点");
											break;											
										}
										if (delayCount % 15 == 0) {
											needSendSpeakMessage = true;
										}										
									}																										
									if(needSendSpeakMessage) {
										String speakMessage = ServerSocketThreadDetect.findPatrolDialogByState("reachedgoal", carOneEndPosName, clientObj.getMachineID());
										if(speakMessage != null) {
											DetectClientSocket detectClient = ServerSocketThreadDetect.detectMachineMap.get(clientObj.getMachineID());
											if(detectClient != null) { // 发送语音数据到人体检测模块
												ServerSocketThreadDetect.sendDataToDetectSocket(detectClient.getMachineID(), speakMessage);
											}
										} else {
											if(staySec <= 0) { // 如果此中途点不需要停留
												System.out.println("@@到达中途点没有要说的话，退出，继续行走");
												if(detectObj != null) {
													detectObj.setReachedGoalNeedStop(false); // 离开此点，继续响应人体检测运动命令
												}											
												ServerSocketThreadRobot.sendMoveCmdToRoobt(clientObj.getMachineID(), false); // 发送继续行走命令
												break;
											}else {
												System.out.println("@@到达中途点没有要说的话，继续停留等待指定的时间");
											}										
										}
										needSendSpeakMessage = false;
									}
									// 如果用户进行了扫码操作
									ContainerClientSocket containerClient = ServerSocketThread.containerMachineMap.get(clientObj.getMachineID()); 
									if(containerClient != null && containerClient.isCustomScanQrCode()) {
										if(detectObj != null) {
											detectObj.setReachedGoalNeedStop(false); // 离开此点，继续响应人体检测运动命令
										}
										System.out.println("@@到达中途地点停下时用户进行了扫码，退出此等待状态");
										break;
									}
									delayCount++;
									try {
										Thread.sleep(1000); // 休眠一段时间
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
								}															
							}							
						}
					}else if(cmd == QingpuConstants.RECV_ROBOT_REACHED_GOAL){ // 机器人到达目标点消息
						JSONObject jsonObject = new JSONObject(new String(content));
						RobotClientSocket clientObj = ServerSocketThreadRobot.getRobotConnectObj(this.client);
						if(clientObj != null) {
							String currentPosName = jsonObject.getString("reachedPosName");
							System.out.println("@@到达最终点 = " + currentPosName);
							clientObj.setCurrentPosName(currentPosName);// 设置机器人当前的路径点名称
							weiXinTemplateService.sendTemplateMessageToUniqueUser("到达当前行走路径终点：" + currentPosName);

							// 判断机器人是否需要补货
							String machineId = clientObj.getMachineID();
							ContainerClientSocket containerClient = ServerSocketThread.containerMachineMap.get(machineId);
							if(containerClient != null && containerClient.isRobotOutOfStore()) { // 机器人处于缺货状态
								if(currentPosName.equals(clientObj.getStartPosName())) { // 如果当前的终点与设置的路径起始点一样，则通知管理员开始补货
									clientObj.setHasRobotReachedGoal(false); // 缺货时设置机器人处于忙状态，不响应路径控制命令 
									int delayCount = 0;
									weiXinTemplateService.sendTemplateMessageToUniqueUser("零售商品不足，请及时补货");
									ServerSocketThreadDetect.sendDataToDetectSocket(machineId, "呼叫，呼叫，货柜商品不足，管理员同志快来给我补货吧"); // 进行语音播报
									while(containerClient.isRobotOutOfStore()){
										if(delayCount >= 2*30) {
											ServerSocketThreadDetect.sendDataToDetectSocket(machineId, "呼叫，呼叫，货柜商品不足，管理员同志快来给我补货吧"); // 进行语音播报
											System.out.println("@@机器人处于缺货状态, 货柜商品不足，请管理员及时补货");
											delayCount = 0;
										}								
										try {
											Thread.sleep(500);
											delayCount++;
										} catch (InterruptedException e) {
											e.printStackTrace();
										}
									}
									ServerSocketThreadDetect.sendDataToDetectSocket(machineId, "补货完成继续行走");
									System.out.println("@@补货完成继续检测循环行走");
								}											
							} else {
								System.out.println("@@到达终点货柜商品正常，不需要补货");
							}
							
							// 判断是否需要循环行走，根据当前到达的终点判断继续下发路径点
							JSONArray pathJSONArr = clientObj.getPosStayTimeJSONArr(); // 获取机器人绑定的路线jsonArr数据，在启动机器人进行循环行走时已经进行了设置
							JSONArray sendPathJSONArr = new JSONArray();
							System.out.println("@@机器人绑定的路线 = " + pathJSONArr);
							if(pathJSONArr.length() > 0) { // 当机器人绑定的路线中有路径点
								boolean needMove = false;
								clientObj.setHasRobotReachedGoal(false); // 因为设置了循环路径，且没有设置停止运行，则标记机器人处于忙状态
								if(currentPosName.equals(clientObj.getStartPosName())) { // 如果当前的终点是路径的起始点，也就是充点电附近的地点
									System.out.println("@@机器人到达充电点附近的一个终点");
									if(clientObj.isNeedStopLoopMove()) { // 如果需要停止，向底盘发送进行充电命令
										System.out.println("@@机器人结束循环状态，处于停靠状态，清空原来的路径相关信息，发送进行自动充电命令");
										System.out.println("@@充电发送坐标信息 = " + pathJSONArr.getJSONObject(0));
										ResponseSocketUtils.sendJsonDataToClient(
												pathJSONArr.getJSONObject(0),
												clientObj.getClient(),
												QingpuConstants.SEND_START_CHARGE,
												QingpuConstants.ENCRYPT_BY_NONE,
												QingpuConstants.DATA_TYPE_JSON);										
										clientObj.setHasRobotReachedGoal(true); // 设置机器人处于停靠空闲状态
										
										// 清空原来设置的路径信息，不然被定时器停止后，网页上单击运行到某点又会开始循环
										clientObj.setStartLoopMiliTime(0);
										clientObj.setStopLoopMiliTime(0);
										clientObj.setStartPosName("");
										clientObj.setPosStayTimeJSONArr(null);
									} else {
										System.out.println("@@机器人到达充电点起始点，继续运动，注意：不合理，请检测");																				
									}
								} else if(currentPosName.equals(pathJSONArr.getJSONObject(1).getString("posName"))) { // 如果是到达充电点的下一个点
									System.out.println("@@到达充电点的下一个终点");
									if(clientObj.isNeedStopLoopMove()) { // 如果需要停止，则导航到充电点附近的一个点
										System.out.println("@@停止循环，发送到达充电点的路径信息");
										sendPathJSONArr.put(0, pathJSONArr.get(1));
										sendPathJSONArr.put(1, pathJSONArr.get(0));																				
										needMove = true;										
									} else {
										System.out.println("@@继续循环");
										for(int i = 1; i < pathJSONArr.length(); i++) { // 复制原来的路径信息，但是去除第一个点充电点
											sendPathJSONArr.put(i-1, pathJSONArr.get(i));
										}										
										needMove = true;
									}									
								} else if(currentPosName.equals(pathJSONArr.getJSONObject(pathJSONArr.length()-1).getString("posName"))) { // 如果是到了路径点的最后一个点终点
									System.out.println("@@到达路径最后一个点终点");
									if(clientObj.isNeedStopLoopMove()) { // 如果需要停止循环
										System.out.println("@@停止循环，翻转整条路径继续行走");
										for(int i = pathJSONArr.length()-1, j = 0; i >= 0; i--, j++) { // 翻转原来的路径信息
											sendPathJSONArr.put(j, pathJSONArr.get(i));
										}
										System.out.println("@@翻转之后的路径 = " + sendPathJSONArr);
										needMove = true;										
									} else {
										System.out.println("@@往回走，继续循环");
										for(int i = pathJSONArr.length()-1, j = 0; i >= 1; i--, j++) { // 翻转原来的路径信息，往回走的路径不包含充电点
											sendPathJSONArr.put(j, pathJSONArr.get(i));
										}
										System.out.println("@@翻转之后的路径 = " + sendPathJSONArr);
										needMove = true;
									}									
								}
								
								if(needMove) { // 如果需要继续运动，则运动之前先检测一下在这两个终点是否需要进行停留
									DetectClientSocket detectObj = ServerSocketThreadDetect.detectMachineMap.get(clientObj.getMachineID()); 
									if(detectObj != null) {// 设置标志位不响应行走命令
										detectObj.setReachedGoalNeedStop(true);
									}									
									ServerSocketThreadRobot.sendMoveCmdToRoobt(clientObj.getMachineID(), true); // 发送停止命令
									
									int staySec = clientObj.getLoopStartPosStaySec(); // 获取机器人在循环起始点需要暂停的时间									
									int delayCount = 0;
									boolean needSendSpeakMessage = false;
									while(true) { // 休眠一段时间，如果没有扫码就继续行走，中间会重复说话
										if(staySec > 0) { // 需要停留指定的时间
											if(delayCount >= staySec) { // 如果超过设置的时间
												// 设置标志位开始继续响应行走命令
												if(detectObj != null) {
													detectObj.setReachedGoalNeedStop(false); // 离开此点，继续响应人体检测运动命令
												}											
												ServerSocketThreadRobot.sendMoveCmdToRoobt(clientObj.getMachineID(), false); // 发送继续行走命令
												System.out.println("@@底盘继续在终点开始循环");
												break;											
											}
											if (delayCount % 15 == 0) {
												needSendSpeakMessage = true;
											}										
										} else { // 按默认时间停留
											if(delayCount >= 30) {
												// 设置标志位开始继续响应行走命令
												if(detectObj != null) {
													detectObj.setReachedGoalNeedStop(false); // 离开此点，继续响应人体检测运动命令
												}											
												ServerSocketThreadRobot.sendMoveCmdToRoobt(clientObj.getMachineID(), false); // 发送继续行走命令
												System.out.println("@@底盘继续在终点开始循环");
												break;											
											}
											if (delayCount % 15 == 0) {
												needSendSpeakMessage = true;
											}										
										}																										
										if(needSendSpeakMessage) {
											String speakMessage = ServerSocketThreadDetect.findPatrolDialogByState("reachedgoal", currentPosName, clientObj.getMachineID());
											if(speakMessage != null) {
												DetectClientSocket detectClient = ServerSocketThreadDetect.detectMachineMap.get(clientObj.getMachineID());
												if(detectClient != null) { // 发送语音数据到人体检测模块
													ServerSocketThreadDetect.sendDataToDetectSocket(detectClient.getMachineID(), speakMessage);
												}
											} else {
												if(staySec <= 0) { // 如果此终点不需要停留
													System.out.println("@@到达终点没有要说的话，退出，继续行走");
													if(detectObj != null) {
														detectObj.setReachedGoalNeedStop(false); // 离开此点，继续响应人体检测运动命令
													}											
													ServerSocketThreadRobot.sendMoveCmdToRoobt(clientObj.getMachineID(), false); // 发送继续行走命令
													break;
												}else {
													System.out.println("@@到达终点没有要说的话，继续停留等待指定的时间");
												}										
											}
											needSendSpeakMessage = false;
										}
										// 如果用户进行了扫码操作 
										if(containerClient != null && containerClient.isCustomScanQrCode()) {
											if(detectObj != null) {
												detectObj.setReachedGoalNeedStop(false); // 离开此点，继续响应人体检测运动命令
											}
											System.out.println("@@到达终点停下时用户进行了扫码，退出此等待状态");
											break;
										}
										delayCount++;
										try {
											Thread.sleep(1000); // 休眠一段时间
										} catch (InterruptedException e) {
											e.printStackTrace();
										}
									}										
									
									JSONObject jsonObj = new JSONObject();
									
									jsonObj.put("carOneGoalPosName", sendPathJSONArr);
									System.out.println("@@任务时间中继续运行循环路径 = " + jsonObj.toString());
									ResponseSocketUtils.sendJsonDataToClient(
											jsonObj, 
											clientObj.getClient(),
											QingpuConstants.SEND_ROBOT_GOAL,
											QingpuConstants.ENCRYPT_BY_NONE,
											QingpuConstants.DATA_TYPE_JSON);
								}
							} else {
								System.out.println("@@机器人当前没有路径点可行走，请检查");
							}
						} else {
							System.out.println("@@接收到达终点位置时底盘连接断开");
						}
					} else if(cmd == QingpuConstants.RECV_CURRENT_POS) { // 获取当前位置的坐标XYZ
						System.out.println("@@收到坐标点  = " + new String(content));
						JSONObject jsonObj = new JSONObject(new String(content));						
						RobotClientSocket clientObj = ServerSocketThreadRobot.getRobotConnectObj(this.getClient());
						clientObj.setCurrentPosObj(jsonObj);						
					}		
				}				
			}
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
