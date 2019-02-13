package com.qingpu.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;

import org.apache.poi.ss.formula.functions.WeekdayFunc;
import org.jgroups.demos.TotalOrder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.qingpu.common.entity.ReturnObject;
import com.qingpu.common.utils.CommonUtils;
import com.qingpu.goods.entity.Goods;
import com.qingpu.goods.entity.GoodsReplenishLog;
import com.qingpu.goods.entity.OrderItem;
import com.qingpu.goods.entity.Orders;
import com.qingpu.goods.service.GoodsService;
import com.qingpu.robots.dao.RobotsDao;
import com.qingpu.robots.entity.OneContainerFloor;
import com.qingpu.robots.entity.Robot;
/**
 * 用于进行金额以及销量统计，机器人销售数据的统计
 * */
@Controller
@RequestMapping("/datareport")
public class DataReportController extends HandlerInterceptorAdapter {
	
	@Resource
	GoodsService goodsService;
	@Resource 
	RobotsDao robotDao;

	/**
	 * 根据楼层与日期查找销售金额及销售数量等信息
	 * */
	@RequestMapping(value="/getPanelStatisticsData") // ", method = RequestMethod.GET" 指定改请求为Get方式，默认get或者post方式都可以请求
	@ResponseBody
	public String getPanelStatisticsData(@RequestBody String body){
		ReturnObject retObj = new ReturnObject();
		
		if(body != null && body != ""){
			JSONObject jsonObj = new JSONObject(body);
			String floorName = jsonObj.getString("floorName"); // allFloors floor1 floor2 floor3
			String saleDuration = jsonObj.getString("saleDuration"); // currentDay foreWeek foreMonth
			
			// 根据时间查找对应的订单，获取总金额以及售卖商品数量
			float saleMoney = 0;
			int saleCount = 0, scanPeopleCount = 0, payPeopleCount = 0;
			List<Orders> listOrder = goodsService.getOrderListByDate(saleDuration);
			payPeopleCount = listOrder.size(); // 付款人数，现在不去除那些重复的人，准确的说是付款次数
			for (int i = 0; i < listOrder.size(); i++) {
				Orders order = listOrder.get(i);
				if(order != null) {
					if("allFloors".equals(floorName) || floorName.equals(order.getFloor())) { // 订单信息符合查询楼层的条件
						saleMoney += order.getTotalFee(); // 计算金额
						List<OrderItem> list = order.getOrderItemList();
						list.removeAll(Collections.singleton(null)); // 去除List中为空的元素
						for(int j = 0; j < list.size(); j++) {
							OrderItem item = list.get(j);
							if("success".equals(item.getStatus())) { // 只计算成功出货的商品个数
								saleCount += item.getBuyCount(); // 计算商品个数
							}						
						}
					}					
				}
			}
			JSONObject retJsonObj = new JSONObject();
			retJsonObj.put("saleMoney", saleMoney/100.0); // 换算成元
			retJsonObj.put("saleCount", saleCount);
			retJsonObj.put("scanPeopleCount", scanPeopleCount);
			retJsonObj.put("payPeopleCount", payPeopleCount);
			retJsonObj.put("code", 0);
			retJsonObj.put("message", "查询成功");

			return retJsonObj.toString();
		}
		
		return new JSONObject(retObj).toString();
	}
	
	/**
	 * 获取指定星期数的金额与积分与销售数量的变化百分比数组
	 * */
	@RequestMapping(value="/getSaleChartList") // ", method = RequestMethod.GET" 指定改请求为Get方式，默认get或者post方式都可以请求
	@ResponseBody
	public String getSaleChartList(@RequestBody String body){
		ReturnObject retObj = new ReturnObject();
		
		if(body != null && body != ""){
			JSONObject jsonObj = new JSONObject(body);
			int listWeekNum = jsonObj.getInt("listWeekNum"); // 获取指定的星期数
			List<Orders> listOrders = goodsService.getOrderListByWeekNum(listWeekNum); // 按照id递增的顺序的生成list
			List<String> xAxisArrayData = new ArrayList<String>(); // x轴显示的日期字符数组
			List<Integer> yAxisFeeRatio = new ArrayList<Integer>(); // y轴显示的金额百分比
			List<Integer> yAxisIntegralRatio = new ArrayList<Integer>(); // y轴显示的积分百分比
			List<Integer> yAxisNumPeopleRatio = new ArrayList<Integer>(); // y轴显示的购买人数百分比
			
			int preDay = 0;
			int dayFeeCount = 0, dayIntegralCount = 0, dayNumPeopleCount = 0; // 用于存储y轴一天的累加数据量
			for(int i = 0; i < listOrders.size(); i++) { // 以查询结果中最早的一天数据为100%
				Orders order = listOrders.get(i);
				if(order != null) {
					int day = order.getDate().getDate(); // 获取这个月的第几号
					
					if(i == 0) {
						preDay = day;
						dayFeeCount += order.getTotalFee();
						dayIntegralCount += order.getUsedIntegral();
						dayNumPeopleCount += 1;
					}else{
						if(preDay == day) { // 如果第二条记录是同一天则继续累加当天的数值							
							dayFeeCount += order.getTotalFee();
							dayIntegralCount += order.getUsedIntegral();
							dayNumPeopleCount += 1;
						}else{ // 是新的一天的数据，将前面累加的数据添加到list中
							String dateStr = preDay+"号";
							xAxisArrayData.add(dateStr);
							yAxisFeeRatio.add(dayFeeCount/100);
							yAxisIntegralRatio.add(dayIntegralCount);
							yAxisNumPeopleRatio.add(dayNumPeopleCount);
							
							dayFeeCount = 0; dayIntegralCount = 0; dayNumPeopleCount = 0; // 清空累加
							dayFeeCount += order.getTotalFee();
							dayIntegralCount += order.getUsedIntegral();
							dayNumPeopleCount += 1;
							preDay = day;
						}
					}
				}				
			}
			JSONArray xAxisArr = new JSONArray(xAxisArrayData);
			JSONArray yAxisFeeArr = new JSONArray(yAxisFeeRatio);
			JSONArray yAxisIntegralArr = new JSONArray(yAxisIntegralRatio);
			JSONArray yAxisNumPeopleArr = new JSONArray(yAxisNumPeopleRatio);
			
			JSONObject retJSONObj = new JSONObject();
			retJSONObj.put("xAxisArrayData", xAxisArr);
			retJSONObj.put("yAxisFeeRatio", yAxisFeeArr);
			retJSONObj.put("yAxisIntegralRatio", yAxisIntegralArr);
			retJSONObj.put("yAxisNumPeopleRatio", yAxisNumPeopleArr);
			retJSONObj.put("code", 0);
			retJSONObj.put("message", "查询成功");
			// System.out.println(retJSONObj.toString());
			return retJSONObj.toString();
		}
		
		return new JSONObject(retObj).toString();
	}
	
	/**
	 * 查询某楼层的某个机器人指定星期中的销售数据，包括所售卖的每种商品的销售数量以及总的销售数量
	 * */	
	@RequestMapping(value="/getRobotSaleData")
	@ResponseBody
	public String getRobotSaleData(@RequestBody String body){
		ReturnObject retObj = new ReturnObject();
		
		if(body != null && body != ""){
			JSONObject jsonObj = new JSONObject(body);
			String floorName = jsonObj.getString("floorName");
			String machineId = jsonObj.getString("robotMachineId");
			int weekNum = jsonObj.getInt("weekNum");
			List<Orders> listOrders = goodsService.getOrderListByMachineId(machineId, weekNum); // 获取指定机器人的订单数据
			Robot robot = robotDao.getRobotByMachineId(machineId); // 获取查询的机器人对象
			
			if(robot == null) {
				retObj.setMessage("要查询的机器人编号错误");
				return new JSONObject(retObj).toString();
			}
			List<OneContainerFloor> listFloor = robot.getContainerFloors();
			listFloor.removeAll(Collections.singleton(null));
			Map<String, Integer> dayGoodsCountMap = new HashMap<String, Integer>(); // 用来存储一天货柜各层商品的编号和售卖数量
			for(OneContainerFloor goodsFloor : listFloor) {
				dayGoodsCountMap.put(goodsFloor.getGoodsSerialId(), 0); //  初始化为0
			}
			Map<String, List<Integer>> goodsSaleCountArr = new HashMap<String, List<Integer>>(); // 用于存储各种商品销量的数组，key值为商品的id号
			int preDay = 0, day = 0;
			List<String> xAxisArrayData = new ArrayList<String>(); // x轴显示的日期字符数组
			
			for(int i = 0; i < listOrders.size(); i++) {
				Orders order = listOrders.get(i);
				if(order != null) {
					day = order.getDate().getDate();
					if(i == 0) {
						preDay = day; 						
						List<OrderItem> orderItem = order.getOrderItemList();
						orderItem.removeAll(Collections.singleton(null));
						for(OrderItem item : orderItem) {
							dayGoodsCountMap.put(item.getGoodsSerialId(), item.getBuyCount());
						}
					} else {
						if(preDay == day) { // 是同一天的订单数据
							List<OrderItem> orderItem = order.getOrderItemList();
							orderItem.removeAll(Collections.singleton(null));
							for(OrderItem item : orderItem) {
								dayGoodsCountMap.put(item.getGoodsSerialId(), dayGoodsCountMap.get(item.getGoodsSerialId())+item.getBuyCount()); // 累加其中某一种商品的销售数量
							}
						} else { // 新的一天的订单数据
							String dateStr = preDay+"号"; // 新增日期
							xAxisArrayData.add(dateStr);
							
							// 遍历每天商品销量累加器，将前一天的数据填充到最终的数组
							int dayTotalGoodsCount = 0; // 前一天所有商品的总销售量
							Iterator<Entry<String, Integer>> it = dayGoodsCountMap.entrySet().iterator();
							while(it.hasNext()){
								Map.Entry<String, Integer> entry = it.next();
								String key = entry.getKey(); // 商品编号
								int value = entry.getValue(); // 商品一天的累计销量
								
								List<Integer> list = goodsSaleCountArr.get(key);
								if(list == null) {
									list = new ArrayList<Integer>();
									goodsSaleCountArr.put(key, list); // 将新创建的list对象重新填充到Map中，不然最终结果中不会保存此数据
								}
								list.add(value);
								dayTotalGoodsCount += value;
							}
							List<Integer> dayTotalCountArr = goodsSaleCountArr.get("dayTotalCountArr");
							if(dayTotalCountArr == null) {
								dayTotalCountArr = new ArrayList<Integer>();
								goodsSaleCountArr.put("dayTotalCountArr", dayTotalCountArr);
							}
							dayTotalCountArr.add(dayTotalGoodsCount);
							
							for(OneContainerFloor goodsFloor : listFloor) {
								dayGoodsCountMap.put(goodsFloor.getGoodsSerialId(), 0); //  初始化为0
							}
							List<OrderItem> orderItem = order.getOrderItemList(); // 添加新的一天的数据
							orderItem.removeAll(Collections.singleton(null));
							for(OrderItem item : orderItem) {
								dayGoodsCountMap.put(item.getGoodsSerialId(), item.getBuyCount());
							}
							
							preDay = day;
						}
					}
				}
			}
			// 将数据发送到客户端
			JSONObject retJSONObj = new JSONObject();
			
			JSONArray jsonArr = new JSONArray();
			List<String> goodsNameArr = new ArrayList<String>();			
			Iterator<Entry<String, List<Integer>>> it = goodsSaleCountArr.entrySet().iterator();
			while(it.hasNext()){
				Map.Entry<String, List<Integer>> entry = it.next();
				String key = entry.getKey();
				if(!"dayTotalCountArr".equals(key)) { // 滤除每天总的商品销量数组
					List<Integer> value = entry.getValue();
					
					Goods goods = goodsService.getGoodsById(key); // 获取商品的名称数组
					if(goods != null) {
						goodsNameArr.add(goods.getName());
					}
					
					jsonArr.put(new JSONArray(value)); // 添加商品的销售数组到一个统一的jsonArray对象中
				}				
			}
			goodsNameArr.add("总数");
			
			retJSONObj.put("goodsNameArr", new JSONArray(goodsNameArr)); // 商品的名字数组
			retJSONObj.put("dayTotalCountArr", new JSONArray(goodsSaleCountArr.get("dayTotalCountArr"))); // 商品的总销售量数组
			retJSONObj.put("xAxisArrayData", new JSONArray(xAxisArrayData));
			retJSONObj.put("everyGoodsCountArr", jsonArr);
			retJSONObj.put("code", 0);
			retJSONObj.put("message", "查询成功");
			
			// System.out.println("--retJSONObj.toString() = " + retJSONObj.toString());
			return retJSONObj.toString();				
		}
		return new JSONObject(retObj).toString();
	}
	
	/**
	 * 获取本月全部商品的销量排行榜
	 * 参数为获取商品的数量
	 * */
	@RequestMapping(value="/getGoodsMonthSellRanklist")
	@ResponseBody
	public String getGoodsMonthSellRanklist(@RequestBody String body) {
		ReturnObject retObj = new ReturnObject();
		
		if(body != null && body != "") {
			JSONObject jsonObj = new JSONObject(body);
			
			int goodsCategoryCount = jsonObj.getInt("goodsCategoryCount"); // 所要查询的商品种类			
			
			List<Goods> goodsList = goodsService.getAllGoods(); // 获取所有的商品
			Map<String, Integer> goodsSellCountMap = new HashMap<String, Integer>();
			for(Goods goods : goodsList) {
				goodsSellCountMap.put(goods.getId(), 0); // 初始化为0
			}
			
			List<Orders> orderList = goodsService.getOrdersByCurrentMonth();
			for(Orders order : orderList) {
				List<OrderItem> item = order.getOrderItemList();
				item.removeAll(Collections.singleton(null));
				for(OrderItem obj : item) {
					goodsSellCountMap.put(obj.getGoodsSerialId(), goodsSellCountMap.get(obj.getGoodsSerialId())+obj.getBuyCount()); // 累加每个订单所买商品的数量
				}
			}
			
			// 对商品销售数量进行Map排序
			goodsSellCountMap = CommonUtils.sortMapValueByDescending(goodsSellCountMap);

			// 拼接结果输出
			JSONArray jsonArr = new JSONArray();
			int rankCount = 1;
			for(Map.Entry<String,Integer> mapping : goodsSellCountMap.entrySet()){				
				String goodsId = mapping.getKey();
				Goods goods = goodsService.getGoodsById(goodsId);
				if(goods != null) {
					JSONObject obj = new JSONObject();
					obj.put("goodsName", goods.getName());
					obj.put("totalCount", mapping.getValue());
					jsonArr.put(obj);
					rankCount++;
					if(rankCount > goodsCategoryCount) { //只获取前指定时间内的最多前十名用户 
						break;
					}	
				}							
			}
			JSONObject retJSONObj = new JSONObject();
			retJSONObj.put("goodsSellCountRanklist", jsonArr);
			retJSONObj.put("code", 0);
			retJSONObj.put("message", "查询成功");
			return retJSONObj.toString();		
		}		
		return new JSONObject(retObj).toString();
	}
	
	/****************************************************************************************************************/
	/****************************************************主页上使用的查询接口**********************************************/
	/****************************************************************************************************************/
	/**
	 * 获取一周内按照楼层每个机器人一周每天的销售总数
	 * */
	@RequestMapping(value="/getRobotsTotalSaleWeekCountByFloorName")
	@ResponseBody
	public String getRobotsTotalSaleCountOneWeek(@RequestBody String body){
		ReturnObject retObj = new ReturnObject();
		
		if(body != null && body != ""){
			JSONObject jsonObj = new JSONObject(body);
			String floorName = jsonObj.getString("floorName");
			
			// 获取机器人列表
			List<Robot> robotList = robotDao.getRobotsList();
			Map<String, Map<String, Integer>> totalSaleCountMap = new HashMap<String, Map<String,Integer>>(); // 第一个key使用机器人编号，第二个key使用订单星期的名字
			for(Robot robot : robotList) {				
				Map<String, Integer> map = new LinkedHashMap<String, Integer>();				
				totalSaleCountMap.put(robot.getMachineId(), map);
			}
					
			// 查询本周的几天订单数据
			String dayStr = "", preDayStr = "";
			List<String> xAxisDataList = new ArrayList<String>(); // 存储x轴的数据
			List<Orders> orderList = goodsService.getOrderListByWeekNum(1); // 获取前一周的数据
			for(int i = 0; i < orderList.size(); i++) {
				Orders order = orderList.get(i);
				if(order != null) {
					if(!"all".equals(floorName)) { // 如果是查询某一个楼层的数据
						if(!floorName.equals(order.getFloor())) { // 是要查询的楼层才继续向下执行
							continue;
						}
					}
					dayStr = CommonUtils.dateToWeekDayStr(order.getDate()); // 获取订单日期的星期字符串
					if(i == 0) {
						preDayStr = dayStr;
						xAxisDataList.add(dayStr);
					} else{						
						if(!preDayStr.equals(dayStr)) { // 如果不是同一天的订单
							xAxisDataList.add(dayStr);
							preDayStr = dayStr;
						}
					}
					
					List<OrderItem> orderItem = order.getOrderItemList();
					orderItem.removeAll(Collections.singleton(null));
					int orderTotalCount = 0; 
					for(OrderItem item : orderItem) { //计算一个订单的商品数量	
						orderTotalCount += item.getBuyCount();
					}
					String machineId = order.getMachineID(); // 机器人编号					
					Map<String, Integer> weekDataMap = totalSaleCountMap.get(machineId);
					if(weekDataMap.get(dayStr) != null) {
						weekDataMap.put(dayStr, weekDataMap.get(dayStr)+orderTotalCount);
					} else {
						weekDataMap.put(dayStr, orderTotalCount);
					}
					
					totalSaleCountMap.put(machineId, weekDataMap); // 将修改之后的数组对象更新到map中
				}
			}
			
			
			// 遍历map拼接数据
			List<String> robotNameList = new ArrayList<String>();
			JSONArray robotWeekListData = new JSONArray();
			
			Iterator<Entry<String, Map<String, Integer>>> it = totalSaleCountMap.entrySet().iterator();
			while(it.hasNext()){
				Entry<String, Map<String, Integer>> entry = it.next();
				String machineId = entry.getKey();
				Robot robot = robotDao.getRobotByMachineId(machineId);
				if(robot != null) {
					robotNameList.add(robot.getName()); // 填充机器人名称到数组中
				}
				
				List<Integer> weekDataList = new ArrayList<Integer>();
				Map<String, Integer> mapValue = entry.getValue();
				Iterator<Entry<String, Integer>> it0 = mapValue.entrySet().iterator();
				while(it0.hasNext()) {
					Entry<String, Integer> entry0 = it0.next();
					int value = entry0.getValue();
					weekDataList.add(value);
				}
				
				
				JSONArray robotDataList = new JSONArray(weekDataList);
				JSONObject obj = new JSONObject();
				obj.put("robotName", robot.getName());				
				obj.put("robotWeekData", robotDataList);
				robotWeekListData.put(obj); // 填充曲线数组数据
			}
			
			JSONObject retJSONObj = new JSONObject();
			retJSONObj.put("robotNameList", new JSONArray(robotNameList));
			retJSONObj.put("xAxisDataList", new JSONArray(xAxisDataList));
			retJSONObj.put("robotWeekListData", robotWeekListData);
			retJSONObj.put("code", 0);
			retJSONObj.put("message", "查询成功"); // x轴星期几显示直接在客户端进行配置
			return retJSONObj.toString();
		}
		
		return new JSONObject(retObj).toString();
	}
	
	/**
	 * 主页上查询机器人的当前详细状态列表
	 * */
	@RequestMapping(value="/getRobotsHomePageDetailInfo")
	@ResponseBody
	public String getRobotsHomePageDetailInfo(@RequestBody String body){
		ReturnObject retObj = new ReturnObject();
		
		if(body != null && body != ""){			
			List<Robot> robotList = robotDao.getRobotsList();
			
			//将机器人列表中的商品id转化成id+名字
			int robotId = 1;
			for(Robot robot : robotList){
				robot.setOrderId(robotId++); // 机器人对象的序号，客户端显示
				List<OneContainerFloor> floorList = robot.getContainerFloors();					
				floorList.removeAll(Collections.singleton(null)); // 去除List中为空的元素
				String goodsListStr = "";
				for(OneContainerFloor floor : floorList) {
					Goods goods = goodsService.getGoodsById(floor.getGoodsSerialId());// 使用商品的主键获取商品对象
					if(goods != null) {						
						goodsListStr += goods.getName() + "；";
					}							
				}
				robot.setGoodsListStr(goodsListStr); // 设置机器人商品名称字符串列表
				robot.setContainerFloors(null);
			}			
			JSONArray jsonArray = new JSONArray(robotList);
			
			JSONObject ret = new JSONObject();
			ret.put("items", jsonArray);
			ret.put("code", 0);
			ret.put("message", "操作成功");
			return ret.toString();			
		}
		
		return new JSONObject(retObj).toString();
	}
	
	/**
	 * 获取主页上机器人销售数量等统计数据
	 * */
	@RequestMapping(value="/getSaleMoneyByFloorName")
	@ResponseBody
	public String getSaleMoneyByFloorName(@RequestBody String body){
		ReturnObject retObj = new ReturnObject();
		
		if(body != null && body != ""){
			JSONObject jsonObj = new JSONObject(body);
			String floorName = jsonObj.getString("floorName");
			JSONObject retJSONObj = new JSONObject();
			List<Orders> totalOrderList = null; // 总订单，都是按照楼层来进行查询的
			List<Orders> todayOrderList = null; // 今天的订单
			List<Orders> currWeekOrderList = null; // 本周的订单
			List<Orders> currMonthOrderList = null; // 本月的订单
			
			if("all".equals(floorName)) { // 如果是查询所有楼层的数据
				totalOrderList = goodsService.getAllOrderList();
				todayOrderList = goodsService.getOrderListByDate("currentDay");
				currWeekOrderList = goodsService.getCurrentWeekList();
				currMonthOrderList = goodsService.getOrdersByCurrentMonth();
			} else { // 按照楼层进行查询
				totalOrderList = goodsService.getTotolOrdersByFloorName(floorName);
				todayOrderList = goodsService.getCurrentDayOrderByFloorName(floorName);
				currWeekOrderList = goodsService.getCurrentWeekListByFloorName(floorName);
				currMonthOrderList = goodsService.getOrdersByCurrentMonthByFloorName(floorName);				
			}
			
			int totalSaleMoney = 0, totalSaleCoin = 0, totalSaleCount = 0, totalServeCount = 0;
			totalServeCount = totalOrderList.size();
			for(Orders order : totalOrderList) {
				totalSaleMoney += order.getTotalFee(); // 统计所有的订单金额，以分为单位
				totalSaleCoin += order.getUsedIntegral(); //统计所有的金币数
				List<OrderItem> orderItem = order.getOrderItemList();
				orderItem.removeAll(Collections.singleton(null));
				for(OrderItem item : orderItem) {
					totalSaleCount += item.getBuyCount(); // 统计总的销售数量
				}
			}
			
			int todaySaleMoney = 0, todaySaleCoin = 0, todaySaleCount = 0, todayServeCount = 0;
			todayServeCount = todayOrderList.size();
			for(Orders order : todayOrderList) {
				todaySaleMoney += order.getTotalFee();
				todaySaleCoin += order.getUsedIntegral();
				List<OrderItem> orderItem = order.getOrderItemList();
				orderItem.removeAll(Collections.singleton(null));
				for(OrderItem item : orderItem) {
					todaySaleCount += item.getBuyCount();
				}
			}
			
			int weekSaleMoney = 0, weekSaleCoin = 0;
			for(Orders order : currWeekOrderList) {
				weekSaleMoney += order.getTotalFee();
				weekSaleCoin += order.getUsedIntegral();
			}
			
			int monthSaleMoney = 0, monthSaleCoin = 0;
			for(Orders order : currMonthOrderList) {
				monthSaleMoney += order.getTotalFee();
				monthSaleCoin += order.getUsedIntegral();
			}
			
			JSONObject upPanelDataObj = new JSONObject();
			upPanelDataObj.put("totalSaleMoney", totalSaleMoney/100.0);
			upPanelDataObj.put("totalSaleCoin", totalSaleCoin);
			upPanelDataObj.put("totalSaleCount", totalSaleCount);
			upPanelDataObj.put("totalServeCount", totalServeCount);
			upPanelDataObj.put("todayServeCount", todayServeCount);
			upPanelDataObj.put("todaySaleCount", todaySaleCount);
			retJSONObj.put("upPanelDataObj", upPanelDataObj);
			
			JSONObject downPanelDataObj = new JSONObject();
			downPanelDataObj.put("todaySaleMoney", todaySaleMoney/100.0);
			downPanelDataObj.put("todaySaleCoin", todaySaleCoin);
			downPanelDataObj.put("weekSaleMoney", weekSaleMoney/100.0);
			downPanelDataObj.put("weekSaleCoin", weekSaleCoin);
			downPanelDataObj.put("monthSaleMoney", monthSaleMoney/100.0);
			downPanelDataObj.put("monthSaleCoin", monthSaleCoin);
			retJSONObj.put("downPanelDataObj", downPanelDataObj);
			
			retJSONObj.put("code", 0);
			retJSONObj.put("message", "查询成功");
			return retJSONObj.toString();
		}
		
		return new JSONObject(retObj).toString();
	}
	
	/**********************************************************主页查询使用接口End*************************************/
	/****************************************************************************************************************/
	/****************************************************************************************************************/
	
	/**
	 * 查询近一个月商品销量排行榜
	 * */
	@RequestMapping(value="/getGoodsSellCountInMonth")
	@ResponseBody
	public String getGoodsSellCountInMonth(@RequestBody String body){
		ReturnObject retObj = new ReturnObject();
		
		if(body != null && body != ""){
			Map<String, Integer> map = new LinkedHashMap<String, Integer>(); // key是商品的id，value是销量
			List<Orders> list0 = goodsService.getOrderListByDate("foreMonth");
			for(Orders order : list0) {
				List<OrderItem> orderItem = order.getOrderItemList();
				orderItem.removeAll(Collections.singleton(null));
				for(OrderItem item : orderItem) {
					String goodsId = item.getGoodsSerialId();
					if(map.get(goodsId) != null) {
						map.put(goodsId, map.get(goodsId)+item.getBuyCount());
					} else {
						map.put(goodsId, item.getBuyCount());
					}
				}
			}
			
			map = CommonUtils.sortMapValueByDescending(map);
			
			JSONArray retArr = new JSONArray();
			// 遍历Map			
			for(Map.Entry<String, Integer> entry : map.entrySet()) {
				String goodsId = entry.getKey();
				Goods goods = goodsService.getGoodsById(goodsId);
				if(goods != null) {
					JSONObject obj = new JSONObject();
					obj.put("name", goods.getName());
					obj.put("monthSellCount", entry.getValue());
					retArr.put(obj);
				}
			}
			
			JSONObject retJSONObj = new JSONObject();
			retJSONObj.put("goodsSellCountList", retArr);
			retJSONObj.put("code", 0);
			retJSONObj.put("message", "查询成功");
			return retJSONObj.toString();
		}
		
		return new JSONObject(retObj).toString();
	}
	
	/**
	 * 获取缺货机器人列表信息
	 * */
	@RequestMapping(value="/getAbnormalShortageListFromRobot")
	@ResponseBody
	public String getAbnormalShortageListFromRobot(@RequestBody String body){
		ReturnObject retObj = new ReturnObject();
		
		if(body != null && body != ""){
			List<Robot> robotList = robotDao.getRobotsList();
			JSONArray list = new JSONArray();
			
			//将机器人列表中的商品id转化成id+名字
			int index = 1;
			for(Robot robot : robotList){
				if(robot.isRobotOutOfStore()) { // 如果当前机器人处于缺货状态
					List<OneContainerFloor> floorList = robot.getContainerFloors();					
					floorList.removeAll(Collections.singleton(null)); // 去除List中为空的元素
					for(OneContainerFloor floor : floorList) {
						if(floor.getCurrentCount() <= 0) { // 如果当前层处于缺货状态
							JSONObject obj = new JSONObject();
							obj.put("orderId", index++);
							obj.put("robotName", robot.getName());
							obj.put("containerFloorName", floor.getFloorName());
							String goodsId = floor.getGoodsSerialId();
							Goods goods = goodsService.getGoodsById(goodsId);
							if(goods != null) {
								obj.put("goodsName", goods.getName());
							}
							obj.put("date", robot.getOutOfStoreDate()); // 获取缺货异常时间
							list.put(obj);							
						}		
					}
				}				
			}
			JSONObject retJSONObj = new JSONObject();
			retJSONObj.put("list", list);
			retJSONObj.put("code", 0);
			retJSONObj.put("message", "查询成功");
			return retJSONObj.toString();
		}
		
		return new JSONObject(retObj).toString();
	}
	
	/**
	 * 获取订单出货异常的数据
	 * */
	@RequestMapping(value="/getAbnormalShipListFromOrders")
	@ResponseBody
	public String getAbnormalShipListFromOrders(@RequestBody String body){
		ReturnObject retObj = new ReturnObject();
		
		if(body != null && body != ""){
			List<Orders> list0 = goodsService.getOrderListByDate("foreWeek");
			int index = 1;
			JSONArray list = new JSONArray();
			for(Orders order : list0) {
				if("outError".equals(order.getOutStatus())) { // 如果订单出货异常再进一步查看哪个商品异常
					List<OrderItem> orderItem = order.getOrderItemList();
					orderItem.removeAll(Collections.singleton(null));
					for(OrderItem item : orderItem) {
						if("error".equals(item.getStatus())) { // 如果其中一种商品异常
							JSONObject obj = new JSONObject();
							obj.put("orderId", index++);
							String machineId = order.getMachineID();
							Robot robot = robotDao.getRobotByMachineId(machineId);
							if(robot != null) {
								obj.put("robotName", robot.getName());
							}							
							obj.put("containerFloorName", item.getGoodsFloor());
							String goodsId = item.getGoodsSerialId();
							Goods goods = goodsService.getGoodsById(goodsId);
							if(goods != null) {
								obj.put("goodsName", goods.getName());
							}
							obj.put("date", order.getDate());
							list.put(obj);
						}
					}
				}								
			}						
			JSONObject retJSONObj = new JSONObject();
			retJSONObj.put("list", list);
			retJSONObj.put("code", 0);
			retJSONObj.put("message", "查询成功");
			return retJSONObj.toString();
		}
		
		return new JSONObject(retObj).toString();
	}
	
	/**
	 * 获取仓库商品列表页面上的相关的商品统计数据
	 * */
	@RequestMapping(value="/getGoodsReplenishDataList")
	@ResponseBody
	public String getGoodsReplenishDataList(@RequestBody String body){
		ReturnObject retObj = new ReturnObject();
		
		if(body != null && body != ""){
			// 计算所有商品的当前库存总量
			List<Goods> goodsList = goodsService.getAllGoods();
			int totalRepertory = 0;
			for(Goods goods : goodsList) {
				totalRepertory += goods.getRepertory();
			}
			
			// 累计进货 查找GoodsReplenishLog表中累加currentReplenishCount字段的值
			List<GoodsReplenishLog> logList = goodsService.findAllReplenishLog(); // 获取所有的仓库补货记录
			int grandTotalStock = 0;
			for(GoodsReplenishLog log : logList) {
				grandTotalStock += log.getCurrentReplenishCount(); // 累加当前进货量
			}
			
			// 查询所有订单统计销售数量
			List<Orders> orderList = goodsService.getAllOrderList();
			int grandTotalShipment = 0;
			for(Orders order : orderList) {
				List<OrderItem> orderItem = order.getOrderItemList();
				orderItem.removeAll(Collections.singleton(null));
				for(OrderItem item : orderItem) {
					grandTotalShipment += item.getBuyCount();
				}
			}
									
			JSONObject retJSONObj = new JSONObject();
			retJSONObj.put("totalRepertory", totalRepertory);
			retJSONObj.put("grandTotalStock", grandTotalStock);
			retJSONObj.put("grandTotalShipment", grandTotalShipment);
			retJSONObj.put("code", 0);
			retJSONObj.put("message", "操作成功");
			return retJSONObj.toString();
		}
		
		return new JSONObject(retObj).toString();
	}
}
