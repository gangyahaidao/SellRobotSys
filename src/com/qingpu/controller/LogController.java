package com.qingpu.controller;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.qingpu.common.entity.ReturnObject;
import com.qingpu.common.service.BaseLogService;
import com.qingpu.common.utils.CommonUtils;
import com.qingpu.common.utils.EmojiUtil;
import com.qingpu.goods.entity.Goods;
import com.qingpu.goods.entity.GoodsReplenishLog;
import com.qingpu.goods.entity.OrderItem;
import com.qingpu.goods.entity.Orders;
import com.qingpu.goods.service.GoodsService;
import com.qingpu.robots.dao.RobotsDao;
import com.qingpu.robots.entity.Robot;
import com.qingpu.robots.entity.RobotReplenishLog;
import com.qingpu.user.entity.UserWeixin;
import com.qingpu.user.service.UserService;

@Controller
@RequestMapping("/log")
public class LogController {
	@Resource
	private BaseLogService<Object> baseLogService;
	@Resource
	private GoodsService goodsService;
	@Resource 
	RobotsDao robotDao;
	@Resource
	private UserService userService;
	
	/**
	 * 根据接收到的日志对象名称加载相应的日志记录，目前主要是货架日志，机器人操作修改日志，供货商修改日志
	 * */
	@RequestMapping("/getLogListAll")
	@ResponseBody
	public String getGoodsLog(@RequestBody String body) {
		ReturnObject retObj = new ReturnObject();
		
		if(body != null && body != ""){
			JSONObject jsonObject = new JSONObject(body);
			if(jsonObject.has("token")){
				String logEntityName = jsonObject.getString("logEntityName");
				List<Object> logList = baseLogService.findLogList(logEntityName);
				JSONArray jsonArr = new JSONArray(logList);
				
				JSONObject obj = new JSONObject();
				obj.put("items", jsonArr);
				obj.put("total", logList.size()); // 列表数据条数
				obj.put("code", 0);
				obj.put("message", "success");
				
				return obj.toString();
			}
		}		
		return new JSONObject(retObj).toString();
	}
	
	/**
	 * 获取用户消费订单记录
	 * */
	@RequestMapping("/getUserConsumerOrderList")
	@ResponseBody
	public String getUserConsumerOrderList(@RequestBody String body) {
		ReturnObject retObj = new ReturnObject();
		
		if(body != null && body != ""){
			JSONObject jsonObject = new JSONObject(body);
			
			int page = jsonObject.getInt("page"); // 获取要查询的页数 page值是从1开始的
			int limit = jsonObject.getInt("limit"); // 获取每一页的限制数量
			int startItem = (page-1) * limit;
			int endItem = page * limit;
			
			String machineId = "all";			
			if(jsonObject.has("machineId")) {
				machineId = jsonObject.getString("machineId"); // "all" 或者某个具体的机器人编号字符串
			}
			String outStatus = "all";
			if(jsonObject.has("outStatus")) {
				outStatus = jsonObject.getString("outStatus"); // "all"  "outOK" "outError" 三种状态查询 
			}
			
			Date startDate = null;
			Date stopDate = null;
			if(jsonObject.has("timePickerRangeArr")) { // 如果设置了时间段查询条件
				JSONArray timePickerArr = jsonObject.getJSONArray("timePickerRangeArr");
				if(timePickerArr.length() > 0) {
					String obj = timePickerArr.getString(0);
					startDate = CommonUtils.translateDatePickerStrToDate(obj);
					obj = timePickerArr.getString(1);
					stopDate = CommonUtils.translateDatePickerStrToDate(obj);
				}				
			}
			
			List<Orders> orderList = goodsService.getUserConsumerOrderListByConditions(machineId, outStatus, startDate, stopDate, startItem, endItem);
			JSONArray jsonArr = new JSONArray();
			for(int i = 0; i < orderList.size(); i++) {
				Orders order = orderList.get(i);
				if(order != null) {
					JSONObject obj = new JSONObject();
					obj.put("orderId", i+1); // 排序序号
					Robot robot = robotDao.getRobotByMachineId(order.getMachineID());
					if(robot != null) {
						obj.put("robotName", robot.getName());
					}
					UserWeixin userWeiXin = userService.getUserByOpenid(order.getOpenid());
					if(userWeiXin != null) {
						obj.put("nickname", EmojiUtil.emojiConvert(userWeiXin.getNickname()));
					}
					
					List<OrderItem> items = order.getOrderItemList();
					items.removeAll(Collections.singleton(null)); // 去除List中为空的元素
					String goodsNames = "";
					int buyCount = 0;
					for(OrderItem item : items) {
						Goods goods = goodsService.getGoodsById(item.getGoodsSerialId());
						if(goods != null) {
							goodsNames += goods.getName() + "；";													
						}
						buyCount += item.getBuyCount();
					}
					obj.put("goodsNames", goodsNames);
					obj.put("buyCount", buyCount);
					if(order.getTotalFee() > 0) { // 微信支付
						obj.put("payType", "微信支付");
						obj.put("totalMoney", order.getTotalFee()/100.0);
					}else {
						obj.put("payType", "积分支付");
						obj.put("totalScore", order.getUsedIntegral());
					}
					obj.put("payStatus", order.getPayStatus());
					obj.put("payDate", order.getDate());
					if("outOK".equals(order.getOutStatus())) {
						obj.put("outStatus", "出货正常");
					} else if("outError".equals(order.getOutStatus())) {
						obj.put("outStatus", "出货异常");
					}
					jsonArr.put(obj);
				}
			}			
			
			JSONObject obj = new JSONObject();
			obj.put("items", jsonArr);
			obj.put("total", jsonArr.length()); // 列表数据条数
			obj.put("code", 0);
			obj.put("message", "success");
			
			return obj.toString();
		}		
		return new JSONObject(retObj).toString();
	}
	
	/**
	 * 根据商品名称和时间段获取仓库的补货记录
	 * */
	@RequestMapping("/getReplenishLog")
	@ResponseBody
	public String getReplenishLog(@RequestBody String body) {
		ReturnObject retObj = new ReturnObject();
		
		if(body != null && body != ""){
			JSONObject jsonObject = new JSONObject(body);
			
			int page = jsonObject.getInt("page"); // 获取要查询的页数 page值是从1开始的
			int limit = jsonObject.getInt("limit"); // 获取每一页的限制数量
			int startItem = (page-1) * limit;
			int endItem = page * limit;
			
			String searchStr = null;			
			if(jsonObject.get("searchStr") != JSONObject.NULL) {
				searchStr = jsonObject.getString("searchStr");
			}
			
			Date startDate = null;
			Date stopDate = null;
			if(jsonObject.has("timePickerRangeArr")) { // 如果设置了时间段查询条件
				JSONArray timePickerArr = jsonObject.getJSONArray("timePickerRangeArr");
				if(timePickerArr.length() > 0) {
					if(timePickerArr.get(0) != JSONObject.NULL) {
						String obj = timePickerArr.getString(0);
						startDate = CommonUtils.translateDatePickerStrToDate(obj);
					}
					if(timePickerArr.get(1) != JSONObject.NULL) {
						String obj = timePickerArr.getString(1);
						stopDate = CommonUtils.translateDatePickerStrToDate(obj);
					}
				}				
			}			
			List<GoodsReplenishLog> replenishLogList = goodsService.findReplenishLog(searchStr, startDate, stopDate, startItem, endItem);
			int index = 1;
			for(GoodsReplenishLog log: replenishLogList) {
				log.setOrderId(index++);
			}
						
			long total = goodsService.getReplenishLogTotalCount();
			JSONObject retJSONObj = new JSONObject();
			retJSONObj.put("list", new JSONArray(replenishLogList));
			retJSONObj.put("total", total);
			retJSONObj.put("code", 0);
			retJSONObj.put("message", "查询成功");
			return retJSONObj.toString();
		}
		
		return new JSONObject(retObj).toString();
	}
	
	/**
	 * 根据补货商品名称和时间段查询机器人货柜补货记录
	 * */
	@RequestMapping("/getRobotContainerReplenishLog")
	@ResponseBody
	public String getRobotContainerReplenishLog(@RequestBody String body) {
		ReturnObject retObj = new ReturnObject();
		
		if(body != null && body != ""){
			JSONObject jsonObject = new JSONObject(body);
			
			int page = jsonObject.getInt("page"); // 获取要查询的页数 page值是从1开始的
			int limit = jsonObject.getInt("limit"); // 获取每一页的限制数量
			int startItem = (page-1) * limit;
			int endItem = page * limit;
			
			String searchStr = null;			
			if(jsonObject.get("searchStr") != JSONObject.NULL) {
				searchStr = jsonObject.getString("searchStr");
			}
			
			Date startDate = null;
			Date stopDate = null;
			if(jsonObject.has("timePickerRangeArr")) { // 如果设置了时间段查询条件
				JSONArray timePickerArr = jsonObject.getJSONArray("timePickerRangeArr");
				if(timePickerArr.length() > 0) {
					if(timePickerArr.get(0) != JSONObject.NULL) {
						String obj = timePickerArr.getString(0);
						startDate = CommonUtils.translateDatePickerStrToDate(obj);
					}
					if(timePickerArr.get(1) != JSONObject.NULL) {
						String obj = timePickerArr.getString(1);
						stopDate = CommonUtils.translateDatePickerStrToDate(obj);
					}
				}				
			}			
			List<RobotReplenishLog> replenishLogList = goodsService.findRobotReplenishLog(searchStr, startDate, stopDate, startItem, endItem);
			int index = 1;
			for(RobotReplenishLog log: replenishLogList) {
				log.setOrderId(index++);
			}
						
			long total = goodsService.getRobotReplenishLogTotalCount();
			JSONObject retJSONObj = new JSONObject();
			retJSONObj.put("list", new JSONArray(replenishLogList));
			retJSONObj.put("total", total);
			retJSONObj.put("code", 0);
			retJSONObj.put("message", "查询成功");
			return retJSONObj.toString();
		}
		
		return new JSONObject(retObj).toString();
	}
}
