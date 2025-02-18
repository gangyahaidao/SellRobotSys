package com.qingpu.socketservice;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

import com.qingpu.common.service.WeiXinTemplateService;
import com.qingpu.common.utils.CommonUtils;
import com.qingpu.common.utils.DataProcessUtils;
import com.qingpu.common.utils.QingpuConstants;
import com.qingpu.controller.WeixinSellGoodsController;
import com.qingpu.goods.entity.Goods;
import com.qingpu.goods.entity.OrderItem;
import com.qingpu.goods.entity.Orders;
import com.qingpu.goods.service.GoodsService;
import com.qingpu.robots.dao.RobotsDao;
import com.qingpu.robots.entity.OneContainerFloor;
import com.qingpu.robots.entity.Robot;

/**
 * 货柜连接Socket对象
 * */
public class ClientSocketThread extends Thread {
	private Socket client;
	private GoodsService goodsService;
	private RobotsDao robotDao;
	private boolean hasRecvCMD = false;
	private boolean hasOutOne = false;	
	private WeiXinTemplateService weiXinTemplateService;

	public ClientSocketThread(Socket client, GoodsService goodsService, RobotsDao robotDao, WeiXinTemplateService weiXinTemplateService) {
		this.client = client;
		this.goodsService = goodsService;
		this.robotDao = robotDao;
		this.weiXinTemplateService = weiXinTemplateService;
	}

	@Override
	public void run() {
		System.out.println("@@货柜连接客户端信息: ip = " + client.getInetAddress() + ", port = " + client.getPort());
		try {
			InputStream in = client.getInputStream();
			byte[] result = new byte[0];
			int tmp = -1;
			boolean header = false;
			boolean tailer = false;			
			/**
			 * 交互数据格式说明：
			 * 心跳包：#2@ 中间的数据标识当前机器人在数据库中的编号
			 * 收到消息回复：#RECV@ 
			 * 完成一个出货通知：#OK@
			 * */

			while (!this.isInterrupted()) {			
				while ((tmp = in.read()) != -1) {
					byte b = (byte) tmp;
					if(b == QingpuConstants.HEADER_CHAR){ // '#'
						header = true; //收到消息开始字节										
					}else if(b == QingpuConstants.TAILER_CHAR){ // '@'
						tailer = true; //收到结束字节
						header = false; // 开始处理一帧数据
						tailer = false;
						String content = new String(result);							
						if("RECV".equals(content)){ // 收到命令确认回复消息
							hasRecvCMD = true;
						}else if("OK".equals(content)){// 完成一个出货通知
							hasOutOne = true;
						} else if("FINISHED".equals(content)) { // 接收到柜门开关被打开，表示用户已经拿走了东西的消息
							ContainerClientSocket clientSocket = ServerSocketThread.getContainerConnectObj(this.client);
							if(clientSocket != null){
								clientSocket.setDoorOpened(true); // 门被打开拿走了东西
							}
						} else if("heartbeat".equals(content)) { // 收到心跳消息
							// System.out.println("@@收到货柜串口心跳");
							synchronized (ServerSocketThread.containerMachineMap){
								ContainerClientSocket clientSocket = ServerSocketThread.getContainerConnectObj(this.client);
								if(clientSocket != null) {
									clientSocket.setPreDate(new Date());// 更新心跳时间，防止超时被断开
									clientSocket.setClient(this.getClient());
									ServerSocketThread.containerMachineMap.put(clientSocket.getMachineID(), clientSocket);//直接进行覆盖添加
								}	
							}									
						} else if(CommonUtils.isNumber(content)){ // 货柜子连接进行注册
							synchronized (ServerSocketThread.containerMachineMap){
								ContainerClientSocket containerSocket = ServerSocketThread.containerMachineMap.get(content); 
								if(containerSocket != null) {
									System.out.println("@@收到货柜串口断开后重新注册的消息");
									System.out.println("@@上一个 processSellThread = " + containerSocket.getProcessSellThread());
									if(containerSocket.getProcessSellThread() != null) {
										System.out.println("@@货柜监控子线程还未退出，等待监控子线程关闭");
										containerSocket.setNeedStopChildThread(true); // 主动关闭售货监控子线程
										while(containerSocket.getProcessSellThread() != null){
											try {
												Thread.sleep(500);
												System.out.println("@@processSellThread = " + containerSocket.getProcessSellThread());
											} catch (InterruptedException e) {
												e.printStackTrace();
											}
										}
										System.out.println("@@关闭了货柜监控子线程");
									}
									containerSocket.getClientThread().closeClient(); // 先关闭原来的socket和线程，同时释放货柜监听线程，避免重新连接之后线程未被心跳监听程序断开，导致同时有多个货柜监听线程启动出货操作
									containerSocket.setClient(getClient());
									containerSocket.setClientThread(this);
									containerSocket.setPreDate(new Date());								
								} else {
									System.out.println("@@收到货柜串口第一次注册消息");
									containerSocket = new ContainerClientSocket();
									containerSocket.setClient(this.client);
									containerSocket.setClientThread(this);
									containerSocket.setMachineID(content);
									containerSocket.setPreDate(new Date());
								}
								containerSocket.setTimeout(false); // 设置为没有超时状态
								ServerSocketThread.containerMachineMap.put(content, containerSocket);														
								ProcessSellGoodsClientThread processSellThread = new ProcessSellGoodsClientThread();
								processSellThread.start(); // 货柜注册之后再启动监听出货线程
								containerSocket.setProcessSellThread(processSellThread);
							}														
						} else {
							System.out.println("@@收到货柜其他消息 = " + content);
						}
						// 清空上次接收的数据
						result = new byte[0];
					}else {
						result = DataProcessUtils.appendByte(result, b);
					}					
				}
			}
			System.out.println("@@退出了货柜监控主线程while(1)");
		} catch (IOException e) {			
			System.out.println("@@货柜连接socket连接断开  = " + e.getMessage());
		}
	}
	
	//创建一个线程检测当前是否要进行出货操作
	public class ProcessSellGoodsClientThread extends Thread{
		@Override
		public void run(){
			System.out.println("@@启动监听货柜出货线程");
			
			ContainerClientSocket clientSocket = ServerSocketThread.getContainerConnectObj(getClient());			
			int scanDelayCount = 0;
			while(clientSocket.isNeedStopChildThread() == false){
				try {
					Thread.sleep(20);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				
				if(clientSocket.isCustomScanQrCode()) { // 如果用户进行了扫码则停止并开始计时
					scanDelayCount++;
					if(scanDelayCount >= 50*QingpuConstants.SCANQR_OVERFLOW_TIME) { // 扫码未付款大于XXs则继续行走
						clientSocket.setCustomScanQrCode(false);
						scanDelayCount = 0;						
						// 发送继续行走命令
						ServerSocketThreadRobot.sendMoveCmdToRoobt(clientSocket.getMachineID(), false);
						System.out.println("@@用户扫码超过设置时间未付款，继续行走");
					}
				} else {
					scanDelayCount = 0; // 重置为0，避免下一次用户扫码计时的时候不准确
				}			
				
				if(clientSocket.getClient()!=null && clientSocket.isDeviceBusy()){ // 如果设备连接上且处于忙状态，说明需要进行出货操作
					hasOutOne = false;
					hasRecvCMD = false;
					clientSocket.setDoorOpened(false); // 设置门的初始状态为关闭
					scanDelayCount = 0;
					Orders order = goodsService.getOrderById(clientSocket.getCurrentOrderId());
					String machineId = order.getMachineID();
					Robot robot = robotDao.getRobotByMachineId(machineId); // 查找数据库中出货的机器人对象
					if(robot != null && order != null){ //遍历出货
						System.out.println("@@开始进行出货");
						List<OrderItem> orderItems = order.getOrderItemList();
						orderItems.removeAll(Collections.singleton(null)); // 去除List中为空的元素
						System.out.println("@@将要出货种类数量 = " + orderItems.size());
						List<OneContainerFloor> robotFloorList = robot.getContainerFloors();
						robotFloorList.removeAll(Collections.singleton(null)); // 去除List中为空的元素
						int sendDataRet = 0; // 货柜是否还处于连接状态，如果出货过程中断开则退出此线程
						boolean hasGoodsOutError = false; // 是否有订单子项出货异常
						for(OrderItem item: orderItems){
							int buyCount = item.getBuyCount();
							String buyGoodsSerialId = item.getGoodsSerialId(); // 所要购买的商品主键ID
							Goods goods = goodsService.getGoodsById(buyGoodsSerialId);
							if(buyCount <= 0) { // 过滤小程序端发送过来的无效数据
								continue;
							}
							System.out.println("@@单个商品购买数量 = " + buyCount);
							// 查找当前商品所在的货柜层数的下标
							int currentGoodsFloorIndex = -1;
							for (int i = 0; i < robotFloorList.size(); i++) {
								if(robotFloorList.get(i).getGoodsSerialId().equals(buyGoodsSerialId)) {
									currentGoodsFloorIndex = i;
									break;
								}
							}					
							weiXinTemplateService.sendTemplateMessageToUniqueUser("付款商品名称=" + goods.getName() + ",数量=" + buyCount); // 发送购买商品的数量和名称到管理员微信
							
							for(int i = 0; i < buyCount; i++){ // 对同一种商品进行出货，用户购买的数量
								String goodsFloor = item.getGoodsFloor().substring(10); // goodsFloor 1 | 2 | 3																
								int flag = QingpuConstants.HEADER_CHAR; //拼接发送的字符串
								int flag_tail = QingpuConstants.TAILER_CHAR;
								int leftCount = robotFloorList.get(currentGoodsFloorIndex).getCurrentCount(); // 获取当前货柜层剩余商品的数量
								int totalCount = robotFloorList.get(currentGoodsFloorIndex).getTotalCount(); // 获取当前层总的商品数量								
								System.out.println("@@当前层货柜上还剩余商品数量 = " + leftCount);// 先从货柜里面的商品开始出货
								String content = null;
								if(leftCount > (totalCount/3)*2 && leftCount <= totalCount) { // 如果当前层后排还有商品，则后面商品出货
									System.out.println("@@后排货架出货");
									content = goodsFloor+"B1";
								} else if(leftCount > totalCount/3 && leftCount <= (totalCount/3)*2) {
									System.out.println("@@中间排货架出货");
									content = goodsFloor+"M1";
								} else if(leftCount > 0 && leftCount <= totalCount/3){
									System.out.println("@@前排货架出货");
									content = goodsFloor+"F1";
								}
								
								byte check = (byte)1;
								byte[] dataT = DataProcessUtils.toByteArray(flag, 1);
								try {
									dataT = DataProcessUtils.mergeArray(dataT, content.getBytes("UTF8"));
								} catch (UnsupportedEncodingException e1) {
									e1.printStackTrace();
								}							
								// 暂不计算校验码，计算的校验码会跟最后一个字符冲突
//								check = dataT[1]; // 计算校验码
//								for(int j = 2; j < dataT.length; j++){
//									check = (byte)(check ^ dataT[j]);
//								}
								dataT = DataProcessUtils.appendByte(dataT, check); // 合并校验字节
								dataT = DataProcessUtils.mergeArray(dataT, DataProcessUtils.toByteArray(flag_tail, 1));
								
								JSONObject adObj = new JSONObject(); // 发送消息给广告连接线程，通知开始播放商品出货动画
								adObj.put("floorName", goodsFloor+".mp4"); // 向广告屏幕发送播放动画消息，发送当前商品所在的层数 1 、2、3
								System.out.println("@@发送命令-开始播放商品出货动画");
								ServerSocketThreadAD.sendDataToAdSocket(clientSocket.getMachineID(), adObj);								
								sendDataRet = ServerSocketThread.sendDataToContainerSocket(clientSocket.getClient(), dataT); // 发送数据给货柜
								if(sendDataRet == -1) {
									System.out.println("@@发送数据失败，出货中货柜断开连接，出货线程退出，标记该商品出货为异常");
									item.setStatus("error");									
									item.setWhichCountGoodsHasError(i+1); // 设置出货异常的产品个数
									hasGoodsOutError = true;
									continue; // 继续进行下一个出货
								} else {
									System.out.println("@@发送出货命令成功");
								}
								
								int sendDelayCount = 0;
								while(hasRecvCMD == false){ // 等待命令回复
									try {
										Thread.sleep(200);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
									sendDelayCount++;
									if(sendDelayCount > 5*8) {
										sendDelayCount = 0;
										item.setStatus("error");											
										item.setWhichCountGoodsHasError(i+1); // 设置出货异常的是第几个产品
										hasGoodsOutError = true;
										System.out.println("@@超过指定时间未收到货柜应答RECV信号，设置出货异常后继续出货");
										sendDataRet = -1;
										break; // 退出一直等待状态，继续向下
									}
								}
								if(sendDataRet == -1) {
									continue; // 继续下一个出货
								}
								
								if(hasRecvCMD) {
									System.out.println("@@货柜线程收到出货命令回复");
								} else {
									System.out.println("@@货柜线程没有收到出货命令回复");
									continue;
								}
								sendDelayCount = 0;
								hasRecvCMD = false;								
								
								while(hasOutOne == false){ //等待出货完成，接收数据线程修改此值的状态
									try {
										Thread.sleep(200);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
									sendDelayCount++;
									if(sendDelayCount >= 5*10) { // 如果等待超过一定时间
										System.out.println("@@等待很长时间没有得到货物完成出货 #OK@ 消息，标记为异常");
										item.setStatus("error");
										item.setWhichCountGoodsHasError(i+1); // 设置出货异常的是第几个产品
										hasGoodsOutError = true;
										break;
									}
								}								
								if(hasOutOne == false) { // 没有收到货物掉下的消息
									String speakMessage = ServerSocketThreadDetect.findOtherDialogByTypeState(robot.getTalkId(), "goodsout", "ERROR"); // 发送出货异常语音
									speakMessage = "!"+speakMessage;
									ServerSocketThreadDetect.sendDataToDetectSocket(clientSocket.getMachineID(), speakMessage);
									System.out.println("@@继续下一个出货");
									continue; // 继续下一个出货
								} else {
									hasOutOne = false;
									item.setStatus("success"); // 设置订单的一个子产品出货状态正常									
									
									// 通知人体检测模块货柜已经完成了一个出货
									String speakMessage = ServerSocketThreadDetect.findOtherDialogByTypeState(robot.getTalkId(), "goodsout", "OK");
									speakMessage = "!"+speakMessage;
									ServerSocketThreadDetect.sendDataToDetectSocket(clientSocket.getMachineID(), speakMessage);
									//购买一个商品成功之后：1.将当前机器人商品层剩余数量减一     2.需要将此种商品总数量减一
									robotFloorList.get(currentGoodsFloorIndex).setCurrentCount(leftCount-1); // 当前层商品数量-1
									robot.setContainerFloors(robotFloorList);
									robotDao.updateRobotInfo(robot); // 将剩余商品数量更新到数据库
																		
									goods.setGoodsSales(goods.getGoodsSales()+1); // 该商品的销售数量
									goods.setRepertory(goods.getRepertory()-1); // 商品总库存
									goodsService.updateGoodsInfo(goods);
									System.out.println("@@SellRobotSys成功完成一个出货，更新了数据库信息");
								}								
							}		
						}
						order.setOrderItemList(orderItems); // 更新订单子项列表的数据状态
						if(hasGoodsOutError) {
							order.setOutStatus("outError"); // 设置当前订单异常
							goodsService.updateOrder(order);
							System.out.println("@@设置异常订单，恢复货柜线程为初始状态等待继续购买请求");
						} else {
							order.setOutStatus("outOK"); // 设置当前订单正常完成
							goodsService.updateOrder(order);
							System.out.println("@@更新订单正常完成");
						}												
										
						if(!hasGoodsOutError) { // 如果订单出货没有失败，检测商品拿走的事件
							// 等待商品被拿走的消息上报
							int waitCount = 0;
							String speakMessage = ServerSocketThreadDetect.findOtherDialogByTypeState(robot.getTalkId(), "opendoor", "ERROR"); // 还未拿走商品的对话
							speakMessage = "!"+speakMessage;
							while(!clientSocket.isDoorOpened()) {
								try {
									Thread.sleep(200);
									waitCount++;
									if(waitCount == 5*5) {																			
										ServerSocketThreadDetect.sendDataToDetectSocket(clientSocket.getMachineID(), speakMessage); // 此处播报的语音不进行优先播报										
										System.out.println("@@等待15秒未拿走商品，播报警告信息并退出等待");										
									} else if(waitCount == 5*8) {
										System.out.println("@@退出提醒拿走商品播报");
										break;
									}
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
							speakMessage = ServerSocketThreadDetect.findOtherDialogByTypeState(robot.getTalkId(), "opendoor", "OK"); // 成功拿走商品的对话，或者是等待拿走货物超时
							speakMessage = "!"+speakMessage;
							ServerSocketThreadDetect.sendDataToDetectSocket(clientSocket.getMachineID(), speakMessage);// 最后播报期待再次光临语句							
							System.out.println("@@播报最后假定拿走商品的语句");
						}
						
						// 检查设备是否处于缺货状态
						boolean hasOutOfStore = false;
						for(OneContainerFloor floor : robotFloorList) {
							if(floor != null) {
								System.out.println("@@出货之后检查货柜当前层 = " + floor.getFloorName() + ", 剩余数量 = " + floor.getCurrentCount());
//								if("goodsFloor1".equals(floor.getFloorName())) { // 最上面一层，第一层，现在只让最里面一排的货柜进行出货
//									if(floor.getCurrentCount() <= 12) {
//										hasOutOfStore = true;
//									}
//								} 
								if("goodsFloor2".equals(floor.getFloorName())) { // 目前只售卖钥匙扣
									if(floor.getCurrentCount() <= 8) {
										hasOutOfStore = true;
									}
								} 
//								else if("goodsFloor3".equals(floor.getFloorName())) {
//									if(floor.getCurrentCount() <= 6) {
//										hasOutOfStore = true;
//									}
//								}
							}
						}
						if(hasOutOfStore) {
							ServerSocketThread.containerMachineMap.get(machineId).setRobotOutOfStore(true);
							robot.setRobotOutOfStore(true); // 设置当前数据库机器人对象处于缺货状态
							robotDao.updateRobotInfo(robot); // 更新到数据库								
							System.out.println("@@出货之后检查，发现机器人处于缺货状态，将在返回循环起始点时停止循环并提醒管理员补货");
						}
						
						clientSocket.setInBuyGoodsProcess(false); // 将货柜脱离购买模式
						clientSocket.setDoorOpened(false); // 将取货门置为关闭状态
						clientSocket.setDeviceBusy(false); // 设置设备处于闲状态
						
						// 发送一个继续运动命令, 防止买完东西之后没有收到继续运动命令停在原地
						ServerSocketThreadRobot.sendMoveCmdToRoobt(clientSocket.getMachineID(), false);		
						System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@出货完成，发送继续行走@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
					} else {
						System.out.println("@@出货时未查找到订单或者数据库机器人对象");
					}
				}				
			}
			if(clientSocket != null) {
				clientSocket.setCustomScanQrCode(false); // 复位用户扫码标识位，避免用户扫码断开之后不继续行走的问题
				clientSocket.setNeedStopChildThread(false);
				clientSocket.setProcessSellThread(null);
			}			
			System.out.println("@@@@退出while()循环，关闭货柜出货监听线程");
		}
	}

	// 关闭连接socket和销毁线程
	public void closeClient() {		
		try {			
			System.out.println("@@closeClient()货柜线程关闭主线程原来的连接和线程资源");	
			ContainerClientSocket clientSocket = ServerSocketThread.getContainerConnectObj(this.client);
			if(clientSocket != null){
				if(clientSocket.getProcessSellThread() != null) {
					clientSocket.setNeedStopChildThread(true);
					System.out.println("@@在closeClient()中，货柜监听子线程还未退出，设置setNeedStopChildThread(true)");
				}				
			}
			this.interrupt(); // 关闭当前线程
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
