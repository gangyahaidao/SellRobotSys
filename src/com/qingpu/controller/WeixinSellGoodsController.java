package com.qingpu.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.qingpu.common.entity.ReturnObject;
import com.qingpu.common.service.WeiXinTemplateService;
import com.qingpu.common.utils.BackToWeiXin;
import com.qingpu.common.utils.CommonUtils;
import com.qingpu.common.utils.EmojiUtil;
import com.qingpu.common.utils.GetWxOrderno;
import com.qingpu.common.utils.MessageUtil;
import com.qingpu.common.utils.PayNotifyData;
import com.qingpu.common.utils.RequestHandler;
import com.qingpu.common.utils.Sha1Util;
import com.qingpu.common.utils.TenpayUtil;
import com.qingpu.common.utils.UUIDGenerator;
import com.qingpu.common.utils.WeiXinConstants;
import com.qingpu.common.utils.WeiXinUtils;
import com.qingpu.goods.entity.Goods;
import com.qingpu.goods.entity.OrderItem;
import com.qingpu.goods.entity.Orders;
import com.qingpu.goods.service.GoodsService;
import com.qingpu.robots.dao.RobotsDao;
import com.qingpu.robots.entity.OneContainerFloor;
import com.qingpu.robots.entity.Robot;
import com.qingpu.socketservice.ContainerClientSocket;
import com.qingpu.socketservice.RobotClientSocket;
import com.qingpu.socketservice.ServerSocketThread;
import com.qingpu.socketservice.ServerSocketThreadDetect;
import com.qingpu.socketservice.ServerSocketThreadRobot;
import com.qingpu.user.entity.UserWeixin;
import com.qingpu.user.entity.UserWeixinOriginal;
import com.qingpu.user.service.UserService;

@Controller
@RequestMapping("/weixin")
public class WeixinSellGoodsController extends HandlerInterceptorAdapter {
	@Resource
	private UserService userService;
	@Resource
	private RobotsDao robotDao;
	@Resource
	private GoodsService goodsService;
	
	/**
	 * 扫码获取机器人商品列表
	 * */
	@RequestMapping("/getGoodsList")
	public void getGoodsList(HttpServletRequest request, HttpServletResponse response){
		ReturnObject retObj = new ReturnObject();
		response.setCharacterEncoding("utf-8");
		response.setContentType("application/json; charset=utf-8");
		PrintWriter writer = null;
		try {
			writer = response.getWriter();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		StringBuilder sb = new StringBuilder();
		try {
			BufferedReader reader = request.getReader();
			char[] buff = new char[2048];
			int len = 0;
			while((len = reader.read(buff)) != -1){
				sb.append(buff, 0, len);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		String body = sb.toString();		
		if(body != null && body != ""){
			JSONObject jsonObject = new JSONObject(body);
			if(jsonObject.has("code") && jsonObject.has("machineID")) {
				//使用code获取用户的openid
				String machineId = jsonObject.getString("machineID");
				String code = jsonObject.getString("code");
				String appid = WeiXinConstants.APPID;
				String appsecret = WeiXinConstants.APPSECRET;
				Map<String, String> openIdMap = WeiXinUtils.getSessionkeyAndOpenid(appid, appsecret, code);				
				String session_key = openIdMap.get("session_key");
				String openid = openIdMap.get("openid");
				if(openid == null) {
					System.out.println("@@获取用户openid失败，请检查账号密码， openIdMap = " + openIdMap);
				}else {
					//保存用户，或者更新用户信息
					UserWeixin userWX = userService.getUserByOpenid(openid);
					if(userWX != null){
						//更新用户信息
						System.out.println("@@更新用户信息 nickname = " + jsonObject.getString("nickName"));
						userWX.setCity(jsonObject.getString("city"));
						userWX.setHeadimageurl(jsonObject.getString("avatarUrl"));
						userWX.setNickname(EmojiUtil.emojiConvert(jsonObject.getString("nickName"))); // 将昵称进行转换
						userWX.setOpenid(openid);
						userWX.setProvince(jsonObject.getString("province"));
						userWX.setSex(jsonObject.getString("gender"));
						userService.updateWeixinUser(userWX);
					}else {
						System.out.println("@@新增扫码用户 nickname = " + jsonObject.getString("nickName"));
						userWX = new UserWeixin(openid,
								EmojiUtil.emojiConvert(jsonObject.getString("nickName")),
								jsonObject.getString("gender"),
								jsonObject.getString("province"),
								jsonObject.getString("city"),
								jsonObject.getString("avatarUrl"));
						userService.saveWeixinUser(userWX);
					}
					
					//使用machineUUID获取当前机器人的货柜信息
					Robot robot = robotDao.getRobotByMachineId(machineId);
					if(robot != null) {
						System.out.println("@@加载编号 = " + machineId + " 机器人商品列表");
						List<OneContainerFloor> floorList = robot.getContainerFloors();
						floorList.removeAll(Collections.singleton(null)); // 去除List中为空的元素
						JSONArray arr = new JSONArray();
						for(OneContainerFloor floor : floorList) {
							if(floor.getCurrentCount() > 0) { // 如果当前层剩余商品数量不足则不发送到客户端
								JSONArray arrTemp = new JSONArray(); // 在每一层商品列表的外面封装一层数组						
								JSONObject obj = new JSONObject();
								Goods goods = goodsService.getGoodsById(floor.getGoodsSerialId());// 使用商品的主键获取商品对象
								obj.put("goodsSerialId", goods.getId()); // 商品的主键
								obj.put("goodsName", goods.getName()); // 商品的名字
								obj.put("goodsUrl", goods.getFileurl());
								obj.put("price", goods.getPrice());
								obj.put("goodsFloor", floor.getFloorName()); // 商品所在货架的层名 goodsFloor1 | 2 | 3 | 4
								obj.put("currentCount", floor.getCurrentCount());
								arrTemp.put(obj);
								arr.put(arrTemp);
							}							
						}
						robot.setContainerFloors(null);
						
						JSONObject ret = new JSONObject();
						ret.put("items", arr);
						ret.put("total", floorList.size());
						ret.put("openid", openid);
						ret.put("code", 0);
						ret.put("message", "success");
						// System.out.println("@@发送商品列表信息 = " + ret);
						writer.write(ret.toString());					
						
						// 发送消息到货柜通知用户进行了扫码操作
						ContainerClientSocket clientSocket = ServerSocketThread.containerMachineMap.get(machineId);
						if(clientSocket != null && clientSocket.isInBuyGoodsProcess() == false) { // 如果当前没有在处理用户的付款出货，则播报用户扫码的欢迎语句
							String speakMessage = ServerSocketThreadDetect.findOtherDialogByTypeState(robot.getTalkId(), "userscan", "OK"); // 查询用户成功进行了扫码的对话
							speakMessage = "!"+speakMessage;
							ServerSocketThreadDetect.sendDataToDetectSocket(clientSocket.getMachineID(), speakMessage);							
						}
						if(clientSocket != null) {
							clientSocket.setCustomScanQrCode(true); // 设置用户进行了扫码，开始付款计时，停止响应人体检测前进命令，超过时间继续行走
						}
						return;
					}				
				}		
			}
		}
		
		writer.write(new JSONObject(retObj).toString());
		return;
	}
	
	/**
	 * 生成订单，返回小程序需要的相关参数
	 * @throws Exception 
	 * */
	@RequestMapping("/makeOrders")
	@ResponseBody
	public String makeOrders(HttpServletRequest request, HttpServletResponse response) throws Exception{
		ReturnObject retObj = new ReturnObject();
		
		StringBuilder sb = new StringBuilder();
		try {
			BufferedReader reader = request.getReader();
			char[] buff = new char[2048];
			int len = 0;
			while((len = reader.read(buff)) != -1){
				sb.append(buff, 0, len);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		//System.out.println("@@makeOrder sb.toString() = " + sb.toString());
		
		if(sb.toString().length() > 0){			
			JSONObject jsonObject = new JSONObject(sb.toString());
			String openid = jsonObject.getString("openid");
			int totalFee = jsonObject.getInt("totalFee");
			String machineID = jsonObject.getString("machineID");
			JSONArray goodsJSONArr = jsonObject.getJSONArray("goods"); // 获取购物车里的物品列表
			String clientId = jsonObject.getString("clientId"); // 传递到商场后台管理系统的用户id
			String clientIp = request.getRemoteAddr();// 客户端ip
			
			ContainerClientSocket clientSocket = ServerSocketThread.containerMachineMap.get(machineID);
			RobotClientSocket robotObj = ServerSocketThreadRobot.robotMachineMap.get(machineID); 
			if(clientSocket == null) {
				System.out.println("@@货柜socket断开连接");
				retObj.setCode(-1);
				retObj.setMessage("Disconnect");
			} else if(clientSocket.isInBuyGoodsProcess()) {
				System.out.println("@@当前有用户已经付款等待出货，不响应再次付款请求");
				retObj.setCode(-1);
				retObj.setMessage("TryLater");
			} else if(robotObj != null && robotObj.isHasRobotReachedGoal() && clientSocket.isRobotOutOfStore()) { // 是否处于停靠状态缺货状态
				System.out.println("@@编号" + machineID + "机器人处于停靠的缺货状态，请及时补货");
				retObj.setCode(-1);
				retObj.setMessage("Wait");
			} else {
				// 检查用户所购买的商品是否还有货
				Robot robot = robotDao.getRobotByMachineId(machineID);
				if(robot != null) {
					List<OneContainerFloor> listFloors = robot.getContainerFloors();
					listFloors.removeAll(Collections.singleton(null)); // 去除List中为空的元素
					for(int i = 0; i < listFloors.size(); i++) {
						OneContainerFloor floor = listFloors.get(i);						
						for(int j = 0; j < goodsJSONArr.length(); j++){ // 购物车商品列表
							JSONObject obj = (JSONObject) goodsJSONArr.get(j);
							String goodsSerialId = obj.getString("goodsSerialId");
							int buyCount = obj.getInt("buyCount"); // 购买某商品的数量
							if(floor.getGoodsSerialId().equals(goodsSerialId)) { // 如果查询到机器人货架上的一个商品编号与所需要购买的相同，则比较剩余数目和所要购买的数目
								if(buyCount > floor.getCurrentCount()) {
									System.out.println("@@商品余量不足，请减少购买数量");
									retObj.setMessage("BuyLess");
									return new JSONObject(retObj).toString();
								}
							}
							
						}
					}										
				}else {
					System.out.println("@@扫码机器人编号不存在");
					retObj.setMessage("RobotNumError");
					return new JSONObject(retObj).toString();
				}
				
				String payType = jsonObject.getString("type");
				if("coin".equals(payType)) { // 如果使用金币支付 
					Orders order = new Orders();
					String orderId = UUIDGenerator.getUUID();
					order.setOrderId(orderId); // 传递的订单编号
					order.setDate(new Date());		
					order.setMachineID(machineID);
					order.setFloor(robot.getFloor()); // 设置创建订单机器人所属的楼层
					order.setOpenid(openid);
					order.setUsedIntegral(totalFee); // 设置金币数量
					order.setOutStatus("outError"); // 默认都是出货不正常状态
					order.setPayStatus("payed"); // 设置支付状态都是已经支付
					order.setPreOrderId(UUIDGenerator.getUUID());
					order.setHasGetNotify(true);
					List<OrderItem> orderItems = new ArrayList<OrderItem>();
					for(int i = 0; i < goodsJSONArr.length(); i++){ // 购物车商品列表
						JSONObject obj = (JSONObject) goodsJSONArr.get(i);
						OrderItem item = new OrderItem();
						item.setGoodsSerialId(obj.getString("goodsSerialId"));
						item.setBuyCount(obj.getInt("buyCount"));//购买某商品的数量
						item.setGoodsFloor(obj.getString("goodsFloor"));//商品所在货架的层数
						item.setStatus("error"); // 默认都是未能正常出货状态
						item.setOrders(order);
						orderItems.add(item); // 加入list对象
					}
					order.setOrderItemList(orderItems); // 设置订单的商品列表
					goodsService.saveOrder(order);
					// 通知商场后台系统用户消费了金币购买了东西
					String serverInterfaceUrl = "http://www.g58mall.com/service/app/noLogin/user/client/buyRobotGoodsUseGold?clientId="+clientId
							+"&orderId="+orderId+"&count="+(-totalFee)+"&remark="+"零售机器人消费支出";
					String retString = CommonUtils.httpGetStr(serverInterfaceUrl);
					JSONObject serverRetJsonObj = new JSONObject(retString);
					if(serverRetJsonObj.has("code")) {
						if(serverRetJsonObj.getInt("code") == 0) {
							System.out.println("@@积分购买通知商场后台系统成功");
						} else {
							System.out.println("@@积分购买通知商场后台系统失败：" + serverRetJsonObj.getString("message"));
						}
					}					
					
					// 开启货柜出货进程
					if(clientSocket != null) {
						clientSocket.setCustomScanQrCode(false); // 已经进行了付款则将扫码标识置为false
						clientSocket.setInBuyGoodsProcess(true); // 设置当前货柜已经处于购买模式，停止响应人体检测前进命令，是为了防止用户扫码半天不付款，然后没有重新扫码就付款的场景						
						//开始控制售货机开始出货	
						clientSocket.setDeviceBusy(true); // 设置当前设备为忙，激活控制货柜线程，不响应其他付款请求
						clientSocket.setCurrentOrderId(orderId); //设置当前的订单对象id
					}					
					// 通知货柜用户已经进行了支付
					String speakMessage = ServerSocketThreadDetect.findOtherDialogByTypeState(robot.getTalkId(), "userpay", "OK"); // 查询用户成功进行了支付的对话
					speakMessage = "!"+speakMessage;
					ServerSocketThreadDetect.sendDataToDetectSocket(machineID, speakMessage);	
					
					retObj.setCode(0);
					retObj.setMessage("使用积分购买成功");
					return new JSONObject(retObj).toString();
				} else if("money".equals(payType)) {					
					// 生成支付相关的参数
					// 获取openId后调用统一支付接口https://api.mch.weixin.qq.com/pay/unifiedorder
					String currTime = TenpayUtil.getCurrTime();			
					String strTime = currTime.substring(8, currTime.length());// 8位日期			
					String strRandom = TenpayUtil.buildRandom(4) + "";// 四位随机数			
					String strReq = strTime + strRandom;// 10位序列号,可以自行调整			
					String mch_id = WeiXinConstants.PARTNER;;// 商户号			
					String nonce_str = strReq;// 随机数			
					String orderBody = "G58商场"+machineID+"号零售机器人购物付款";// 商品描述根据情况修改				
					String out_trade_no = UUIDGenerator.getUUID();// 商户订单号
					String total_fee = totalFee+"";// 总金额以分为单位，不带小数点
					String spbill_create_ip = clientIp;// 订单生成的机器 IP
					String notify_url = "http://www.g58mall.com/SellRobotSys/weixin/wxPayNotify";// 这里notify_url是 支付完成后微信发给该链接信息，可以判断会员是否支付成功
					String trade_type = "JSAPI";			
					String attach = out_trade_no+";"+machineID; // 自定义附加数据，此数据会在支付结果通知信息中原样返回，这里填充订单号和货柜编号
					// String limit_pay = "no_credit"; // 限制不可使用信用卡支付
					
					SortedMap<String, String> packageParams = new TreeMap<String, String>();
					packageParams.put("appid", WeiXinConstants.APPID);
					packageParams.put("mch_id", mch_id);
					packageParams.put("nonce_str", nonce_str);
					packageParams.put("body", orderBody);
					packageParams.put("attach", attach);
					packageParams.put("out_trade_no", out_trade_no);
					packageParams.put("total_fee", total_fee);
					packageParams.put("spbill_create_ip", spbill_create_ip);
					packageParams.put("notify_url", notify_url);
					packageParams.put("trade_type", trade_type);
					packageParams.put("openid", openid);
					
					RequestHandler reqHandler = new RequestHandler(request, response);
					reqHandler.init(WeiXinConstants.APPID, WeiXinConstants.APPSECRET, WeiXinConstants.PARTNERKEY);
					String sign = reqHandler.createSign(packageParams);
					String xml = "<xml>" 
							+ "<appid>" + WeiXinConstants.APPID + "</appid>" 
							+ "<mch_id>" + mch_id + "</mch_id>" 
							+ "<nonce_str>" + nonce_str+ "</nonce_str>" 
							+ "<sign>" + sign + "</sign>"
							+ "<body><![CDATA[" + orderBody + "]]></body>" 
							+ "<attach>" + attach + "</attach>"
							+ "<out_trade_no>" + out_trade_no + "</out_trade_no>"
							+ "<total_fee>"+ total_fee + "</total_fee>"
							+ "<spbill_create_ip>" + spbill_create_ip + "</spbill_create_ip>"
							+ "<notify_url>" + notify_url + "</notify_url>"
							+ "<trade_type>" + trade_type + "</trade_type>" + "<openid>"
							+ openid + "</openid>" 
						+"</xml>";
					String allParameters = null;			
					String createOrderURL = "https://api.mch.weixin.qq.com/pay/unifiedorder";
					String prepay_id = null;
					try {
						allParameters = reqHandler.genPackage(packageParams);
						new GetWxOrderno();
						prepay_id = GetWxOrderno.getPayNo(createOrderURL, xml);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					
					if (prepay_id.length() > 0) {
						//在数据库中创建订单，使用 out_trade_no作为主键
						Orders order = new Orders();
						order.setOrderId(out_trade_no); // 传递的订单编号
						order.setDate(new Date());		
						order.setMachineID(machineID);
						order.setFloor(robot.getFloor()); // 设置创建订单机器人所属的楼层
						order.setOpenid(openid);
						order.setTotalFee(totalFee); // 设置订单金额
						order.setOutStatus("outError"); // 默认都是出货不正常状态
						order.setPayStatus("prepay"); // 设置支付状态都是待支付
						order.setPreOrderId(prepay_id);
						order.setHasGetNotify(false);
						List<OrderItem> orderItems = new ArrayList<OrderItem>();
						for(int i = 0; i < goodsJSONArr.length(); i++){ // 购物车商品列表
							JSONObject obj = (JSONObject) goodsJSONArr.get(i);
							OrderItem item = new OrderItem();
							item.setGoodsSerialId(obj.getString("goodsSerialId"));
							item.setBuyCount(obj.getInt("buyCount"));//购买某商品的数量
							item.setGoodsFloor(obj.getString("goodsFloor"));//商品所在货架的层数
							item.setStatus("error"); // 默认都是未能正常出货状态
							item.setOrders(order);
							orderItems.add(item); // 加入list对象
						}
						order.setOrderItemList(orderItems); // 设置订单的商品列表
						goodsService.saveOrder(order);
								
						//生成hash签名值
						SortedMap<String, String> finalpackage = new TreeMap<String, String>();
						String appid2 = WeiXinConstants.APPID;
						String timestamp = Sha1Util.getTimeStamp();
						String prepay_id2 = "prepay_id=" + prepay_id;
						String packages = prepay_id2;
						finalpackage.put("appId", appid2);
						finalpackage.put("timeStamp", timestamp);
						finalpackage.put("nonceStr", nonce_str);
						finalpackage.put("package", packages);
						finalpackage.put("signType", "MD5");
						String finalsign = reqHandler.createSign(finalpackage);
						
						JSONObject retJSON = new JSONObject();
						retJSON.put("code", 0);
						retJSON.put("message", "success");
						retJSON.put("orderID", out_trade_no); // 增加订单编号，付款结果中传递过来
						retJSON.put("timeStamp", timestamp);
						retJSON.put("nonceStr", nonce_str);
						retJSON.put("package", packages);
						retJSON.put("signType", "MD5");
						retJSON.put("paySign", finalsign);
						
						return retJSON.toString();
					}else {
						System.out.println("@@生成订单失败");
					}					
				}				
			}
		}
		
		return new JSONObject(retObj).toString();
	}
	
	/**
	 * 支付结果回调
	 * */
	@RequestMapping("/wxPayNotify")
	@ResponseBody
	public synchronized void wxPayNotify(HttpServletRequest request, HttpServletResponse response){
		Map<String, String> map = null;
		System.out.println("@@收到微信的付款通知");
		
		try {
			map = MessageUtil.parseXML(request);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("@@微信返回的数据 = " + map.toString());
		PayNotifyData weChatBean = new PayNotifyData();
        weChatBean.setAppid(map.get("appid"));
        weChatBean.setAttach(map.get("attach"));
        weChatBean.setBank_type(map.get("bank_type"));
        weChatBean.setCash_fee(map.get("cash_fee"));
        weChatBean.setFee_type(map.get("fee_type"));
        weChatBean.setIs_subscribe(map.get("is_subscribe"));
        weChatBean.setMch_id(map.get("mch_id"));
        weChatBean.setNonce_str(map.get("nonce_str"));
        weChatBean.setOpenid(map.get("openid"));
        weChatBean.setOut_trade_no(map.get("out_trade_no"));
        weChatBean.setResult_code(map.get("result_code"));
        weChatBean.setReturn_code(map.get("return_code"));
        weChatBean.setSign(map.get("sign"));
        weChatBean.setTime_end(map.get("time_end"));
        weChatBean.setTotal_fee(map.get("total_fee"));
        weChatBean.setTrade_type(map.get("trade_type"));
        weChatBean.setTransaction_id(map.get("transaction_id"));
        // System.out.println("@@SellRobotSys支付结果回调 = " + new JSONObject(weChatBean).toString());
        
        String[] attachSplit = map.get("attach").split(";");
        String orderId = attachSplit[0];//订单id
        String machineId = attachSplit[1];        
		PrintWriter writer = null;
		try {
			writer = response.getWriter();
		} catch (IOException e) {
			e.printStackTrace();
		}
		BackToWeiXin backToWeiXin = new BackToWeiXin();
		System.out.println("@@微信服务器返回订单的签名 = " + weChatBean.getSign());
		String calculateSign = weChatBean.calculateSign();
		System.out.println("@@计算的数据签名 = " + calculateSign);
		//判断红包确实发送成功,同时验证sign数据，确保没有被篡改过
		if(calculateSign.equals(weChatBean.getSign())) {
			System.out.println("@@付款签名校验正确");
		} else {
			System.out.println("@@签名校验失败，请检查");
		}
		if("SUCCESS".equals(map.get("return_code")) 
				&& "SUCCESS".equals(map.get("result_code"))){
			backToWeiXin.setReturn_code("SUCCESS");
			backToWeiXin.setReturn_msg("OK");
			MessageUtil.xstream.alias("xml", backToWeiXin.getClass());		
			String backstr = MessageUtil.xstream.toXML(backToWeiXin).replaceAll("__", "_");
			//多次通知微信服务器已收到付款成功通知
			for(int i = 0; i < 6; i++){
				writer.write(backstr);
	            writer.flush();
			}
			//使用orderId查询订单
			// System.out.println("@@订单orderId = " + orderId);
			Orders order = goodsService.getOrderById(orderId);
			if(order != null) {
				if(!order.isHasGetNotify()){ //是否已经收到回调通知
					order.setHasGetNotify(true);
					order.setPayStatus("payed"); //将订单更新成已支付状态
					goodsService.updateOrder(order);									
					
					ContainerClientSocket clientSocket = ServerSocketThread.containerMachineMap.get(machineId);
					if(clientSocket != null) {
						clientSocket.setCustomScanQrCode(false); // 已经进行了付款则将扫码标识置为false
						clientSocket.setInBuyGoodsProcess(true); // 设置当前货柜已经处于购买模式，停止响应人体检测前进命令，是为了防止用户扫码半天不付款，然后没有重新扫码就付款的场景
						
						//开始控制售货机开始出货	
						clientSocket.setDeviceBusy(true); // 设置当前设备为忙，激活控制货柜线程，不响应其他付款请求
						clientSocket.setCurrentOrderId(orderId); //设置当前的订单对象id
						ServerSocketThread.containerMachineMap.put(machineId, clientSocket); // 更新当前的货柜连接对象
					}
					
					// 通知货柜用户已经进行了支付
					Robot robot = robotDao.getRobotByMachineId(machineId);
					String speakMessage = ServerSocketThreadDetect.findOtherDialogByTypeState(robot.getTalkId(), "userpay", "OK"); // 查询用户成功进行了支付的对话
					speakMessage = "!"+speakMessage;
					ServerSocketThreadDetect.sendDataToDetectSocket(machineId, speakMessage);
				}else{
					System.out.println("@@SellRobotSys已经收到回调通知了");
					return;
				}				
			}		
		}else{
			System.out.println("@@SellRobotSys收到回调支付失败");
			backToWeiXin.setReturn_code("FAIL");
			backToWeiXin.setReturn_msg("fail to pay");
			MessageUtil.xstream.alias("xml", backToWeiXin.getClass());		
			String noticeStr = MessageUtil.xstream.toXML(backToWeiXin).replaceAll("__", "_");
            writer.write(noticeStr);
            writer.flush();
		}
		return;
	}	
	
	/**
	 * 获取微信服务号用户openid和详细接口1
	 * */
	@RequestMapping("/sendReqForUserOpenid")
	@ResponseBody
	public synchronized void sendReqForUserOpenid(HttpServletRequest request, HttpServletResponse response){
		System.out.println("@@/weixin/sendReqForUserOpenid");
		String appid = WeiXinConstants.ORIGINAL_APPID;
		String backUri = "http://www.g58mall.com/SellRobotSys/weixin/getUserOpenid";
		try {
			backUri = URLEncoder.encode(backUri, "UTF-8");
			String url = "https://open.weixin.qq.com/connect/oauth2/authorize?" +
					"appid=" + appid+
					"&redirect_uri=" +
					 backUri+
					"&response_type=code&scope=snsapi_base&state=123#wechat_redirect";
			response.sendRedirect(url);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	/**
	 * 获取用户openid微信服务器调用的接口
	 * */
	@RequestMapping("/getUserOpenid")
	@ResponseBody
	public synchronized void getUserOpenid(HttpServletRequest request, HttpServletResponse response){		
		String code = request.getParameter("code");//微信返回的code
		System.out.println("@@获取Original微信openid微信服务器返回code = " + code);
		if(code == null){
			System.out.println("@@获取用户Openid失败");
			return;
		}
		String appid = WeiXinConstants.ORIGINAL_APPID;
		String appsecret = WeiXinConstants.ORIGINAL_APPSECRET;
		//使用code换取access_token、openid等，access_token+openid可以用来换取用户个人详细信息
		Map<String, String> map1 = WeiXinUtils.getAccessTokenAndOpenid(code);
		System.out.println("@@获取original_openid返回对象 = " + map1);
		String openId = map1.get("openid");
		String access_token = map1.get("access_token");
		
		//使用access_token和openid获取用户基本信息		
		String URL_getUserInfo = "https://api.weixin.qq.com/sns/userinfo?access_token="+access_token+"&openid="+openId+"&lang=zh_CN";
		Map<String, String> map = CommonUtils.httpsRequest(URL_getUserInfo, "GET", null);
		// System.out.println("@@获取original_用户基本信息 = " + map);
		
		//如果不能成功获取用户个人信息
		try {
			if(map.get("errcode") != null && map.get("errmsg") != null){
				//说明未关注公众号,使用snsapi_userinfo重新请求
				String backUrl = "http://www.g58mall.com/SellRobotSys/weixin/getUserOpenid";
				backUrl = URLEncoder.encode(backUrl, "UTF-8");
				//scope 参数视各自需求而定，这里用scope=snsapi_base    snsapi_userinfo不弹出授权页面直接授权目的只获取统一支付接口的openid
				String url = "https://open.weixin.qq.com/connect/oauth2/authorize?" +
						"appid=" + appid+
						"&redirect_uri=" +
						 backUrl+
						"&response_type=code&scope=snsapi_userinfo&state=123#wechat_redirect";
				response.sendRedirect(url);
				return;
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String openid = map.get("openid");
		UserWeixinOriginal userWX = userService.getOriginalUserByOpenid(openid);
		if(userWX != null){
			//更新用户信息
			System.out.println("@@Original微信更新用户信息 nickname = " + map.get("nickname"));
			userWX.setCity(map.get("city"));
			userWX.setHeadimageurl(map.get("headimgurl"));
			userWX.setNickname(EmojiUtil.emojiConvert(map.get("nickname"))); // 将昵称进行转换
			userWX.setOpenid(openid);
			userWX.setProvince(map.get("province"));
			userWX.setSex(map.get("gender"));
			userWX.setCanRecvAdminInfo(true);
			userWX.setDate(new Date());
			userService.updateWeixinUserOriginal(userWX);
		}else {
			System.out.println("@@Original微信新增扫码用户 nickname = " + map.get("nickname"));
			userWX = new UserWeixinOriginal(openid,
					EmojiUtil.emojiConvert(map.get("nickname")),
					map.get("gender"),
					map.get("province"),
					map.get("city"),
					map.get("headimgurl"));
			userWX.setCanRecvAdminInfo(true);
			userWX.setDate(new Date());
			userService.saveWeixinUserOriginal(userWX);
		}			
		
		try {
			request.getRequestDispatcher("/wx/bindAdminInfoOK.jsp").forward(request, response);
		} catch (ServletException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
