package com.qingpu.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.qingpu.common.entity.ReturnObject;
import com.qingpu.common.utils.CommonUtils;
import com.qingpu.common.utils.EmojiUtil;
import com.qingpu.goods.entity.Goods;
import com.qingpu.goods.entity.OrderItem;
import com.qingpu.goods.entity.Orders;
import com.qingpu.goods.service.GoodsService;
import com.qingpu.robots.dao.RobotsDao;
import com.qingpu.robots.entity.Robot;
import com.qingpu.user.entity.UserWeixin;
import com.qingpu.user.service.UserService;

/**
 * 获取排行榜数据
 * */
@Controller
@RequestMapping("/rankList")
public class RankListController extends HandlerInterceptorAdapter {
	
	@Resource
	GoodsService goodsService;
	@Resource
	UserService userService;
	@Resource
	RobotsDao robotDao;

	/**
	 * 根据星期数，获取指定的微信用户消费排行榜
	 * */
	@RequestMapping(value="/getWXUserRankList")
	@ResponseBody
	public String getWXUserRankList(@RequestBody String body){
		ReturnObject retObj = new ReturnObject();
		
		if(body != null && body != ""){
			JSONObject jsonObj = new JSONObject(body);
			int listWeekNum = jsonObj.getInt("listWeekNum"); // 获取指定的星期数
			List<Orders> listOrders = goodsService.getOrderListByWeekNum(listWeekNum); // 按照id递增的顺序的生成list
			
			Map<String, Integer> userTotalFeeMap = new HashMap<String, Integer>(); // 用于存储用户的消费金额
			Map<String, String> userNicknameMap = new HashMap<String, String>(); // 用于存储用户的昵称			
			for(int i = 0; i < listOrders.size(); i++){
				Orders order = listOrders.get(i);
				String openid = order.getOpenid();
				if(userTotalFeeMap.get(openid) != null) {
					userTotalFeeMap.put(openid, order.getTotalFee()+userTotalFeeMap.get(openid)); // 累加前面的金额
				}else {
					userTotalFeeMap.put(openid, order.getTotalFee());
				}
				
				if(userNicknameMap.get(openid) == null) { // 每个相关用户只设置一次
					UserWeixin userWX = userService.getUserByOpenid(openid);
					if(userWX != null) {
						userNicknameMap.put(openid, EmojiUtil.emojiRecovery(userWX.getNickname())); // 获取用户的昵称
					}
				}
			}
			// 对用户消费金额进行排序
			userTotalFeeMap = CommonUtils.sortMapValueByDescending(userTotalFeeMap);
			
			// 拼接结果输出
			JSONArray jsonArr = new JSONArray();
			int rankCount = 1;
			for(Map.Entry<String,Integer> mapping : userTotalFeeMap.entrySet()){
				JSONObject obj = new JSONObject();
				String openid = mapping.getKey();
				int totalFee = mapping.getValue();
				if(totalFee > 0) { // 只显示大于0的用户
					obj.put("name", userNicknameMap.get(openid));
					obj.put("score", totalFee/100.0+"");
					obj.put("rank", rankCount);
					jsonArr.put(obj);
					rankCount++;
					if(rankCount > 10) { //只获取前指定时间内的最多前十名用户 
						break;
					}
				}else{ // 说明已经统计完毕，剩下的全是0不需要统计了
					break;
				}						
			}
			JSONObject retJSONObj = new JSONObject();
			retJSONObj.put("luckyUserForeTen", jsonArr);
			retJSONObj.put("code", 0);
			retJSONObj.put("message", "查询成功");
			return retJSONObj.toString();			
		}
		
		return new JSONObject(retObj).toString();
	}
	
	/**
	 * 获取最畅销的最多前十名商品
	 * */
	@RequestMapping(value="/getSellBestGoodsList")
	@ResponseBody
	public String getSellBestGoodsList(@RequestBody String body){
		ReturnObject retObj = new ReturnObject();
		
		if(body != null && body != ""){
			JSONObject jsonObj = new JSONObject(body);
			int listWeekNum = jsonObj.getInt("listWeekNum"); // 获取指定的星期数
			List<Orders> listOrders = goodsService.getOrderListByWeekNum(listWeekNum); // 按照id递增的顺序的生成list			
			Map<String, Integer> goodsTotalSellMap = new HashMap<String, Integer>(); // 用来存储各种商品的销量
			Map<String, String> goodsNameMap = new HashMap<String, String>(); // 存储商品的名字
			
			for(Orders order : listOrders) {
				List<OrderItem> item = order.getOrderItemList();
				item.removeAll(Collections.singleton(null));
				for(OrderItem obj : item) {
					String goodsSerialId = obj.getGoodsSerialId();
					int buyCount = obj.getBuyCount();
					if(goodsTotalSellMap.get(goodsSerialId) != null) {
						goodsTotalSellMap.put(goodsSerialId, goodsTotalSellMap.get(goodsSerialId) + buyCount); // 累加
					}else{
						goodsTotalSellMap.put(goodsSerialId, buyCount);
					}
					
					if(goodsNameMap.get(goodsSerialId) == null) { // 获取商品的名称
						Goods goods = goodsService.getGoodsById(goodsSerialId);
						if(goods != null) {
							goodsNameMap.put(goodsSerialId, goods.getName());
						}						
					}
				}
			}
			
			// 对商品销量进行排序
			goodsTotalSellMap = CommonUtils.sortMapValueByDescending(goodsTotalSellMap);
			
			// 拼接结果输出
			JSONArray jsonArr = new JSONArray();
			int rankCount = 1;
			for(Map.Entry<String,Integer> mapping:goodsTotalSellMap.entrySet()){
				JSONObject obj = new JSONObject();
				String goodsSerialId = mapping.getKey();
				int sellCount = mapping.getValue();
				obj.put("name", goodsNameMap.get(goodsSerialId));
				obj.put("score", sellCount+""); // 转换成字符串
				obj.put("rank", rankCount);
				jsonArr.put(obj);
				
				rankCount++;
				if(rankCount > 10) { //只获取前指定时间内的最多前十名用户 
					break;
				}
			}
			JSONObject retJSONObj = new JSONObject();
			retJSONObj.put("saleGoodsForeTen", jsonArr);
			retJSONObj.put("code", 0);
			retJSONObj.put("message", "查询成功");
			return retJSONObj.toString();						
		}
		
		return new JSONObject(retObj).toString();
	}
	
	/**
	 * 获取销售金额最高的最多前十名机器人
	 * */
	@RequestMapping(value="/getRobotSellRankList")
	@ResponseBody
	public String getRobotSellRankList(@RequestBody String body){
		ReturnObject retObj = new ReturnObject();
		
		if(body != null && body != ""){
			JSONObject jsonObj = new JSONObject(body);
			int listWeekNum = jsonObj.getInt("listWeekNum"); // 获取指定的星期数
			List<Orders> listOrders = goodsService.getOrderListByWeekNum(listWeekNum); // 按照id递增的顺序的生成list
			
			Map<String, Integer> robotTotalFeeMap = new HashMap<String, Integer>(); // 用于存储用户的消费金额
			Map<String, String> robotNameMap = new HashMap<String, String>(); // 用于存储用户的昵称			
			for(int i = 0; i < listOrders.size(); i++){
				Orders order = listOrders.get(i);
				String machineId = order.getMachineID();
				
				if(robotTotalFeeMap.get(machineId) != null) {
					robotTotalFeeMap.put(machineId, order.getTotalFee()+robotTotalFeeMap.get(machineId)); // 累加前面的金额
				}else {
					robotTotalFeeMap.put(machineId, order.getTotalFee());
				}
				
				if(robotNameMap.get(machineId) == null) { // 每个相关机器人只设置一次
					Robot robot = robotDao.getRobotByMachineId(machineId);
					if(robot != null) {
						robotNameMap.put(machineId, robot.getName()); // 获取机器人名称
					}
				}
			}
			// 对用户消费金额进行排序
			robotTotalFeeMap = CommonUtils.sortMapValueByDescending(robotTotalFeeMap);
			
			// 拼接结果输出
			JSONArray jsonArr = new JSONArray();
			int rankCount = 1;
			for(Map.Entry<String,Integer> mapping : robotTotalFeeMap.entrySet()){
				JSONObject obj = new JSONObject();
				String machineId = mapping.getKey();
				int totalSellFee = mapping.getValue();
				if(totalSellFee > 0) { // 只显示大于0的用户
					obj.put("name", robotNameMap.get(machineId));
					obj.put("score", totalSellFee/100.0+"");
					obj.put("rank", rankCount);
					jsonArr.put(obj);
					rankCount++;
					if(rankCount > 10) { //只获取前指定时间内的最多前十名用户 
						break;
					}
				}else{ // 说明已经统计完毕，剩下的全是0不需要统计了
					break;
				}						
			}
			JSONObject retJSONObj = new JSONObject();
			retJSONObj.put("bestRobotRankList", jsonArr);
			retJSONObj.put("code", 0);
			retJSONObj.put("message", "查询成功");
			return retJSONObj.toString();			
		}
		
		return new JSONObject(retObj).toString();
	}
}
