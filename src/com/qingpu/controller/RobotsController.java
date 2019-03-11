package com.qingpu.controller;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.qingpu.adtemplate.dao.AdTemplateDao;
import com.qingpu.adtemplate.entity.AdTemplate;
import com.qingpu.common.entity.ReturnObject;
import com.qingpu.common.service.ActionConstants;
import com.qingpu.common.service.BaseLogService;
import com.qingpu.common.service.WeiXinTemplateService;
import com.qingpu.common.utils.CommonUtils;
import com.qingpu.common.utils.FileUtils;
import com.qingpu.common.utils.QingpuConstants;
import com.qingpu.common.utils.WeiXinUtils;
import com.qingpu.goods.entity.Goods;
import com.qingpu.goods.entity.OrderItem;
import com.qingpu.goods.entity.Orders;
import com.qingpu.goods.service.GoodsService;
import com.qingpu.robots.dao.RobotsDao;
import com.qingpu.robots.entity.FloorPosName;
import com.qingpu.robots.entity.OneContainerFloor;
import com.qingpu.robots.entity.PathListData;
import com.qingpu.robots.entity.Robot;
import com.qingpu.robots.entity.RobotOtherDialog;
import com.qingpu.robots.entity.RobotPatrolOrSenseDialog;
import com.qingpu.robots.entity.RobotReplenishLog;
import com.qingpu.robots.entity.RobotTalkGroup;
import com.qingpu.robots.entity.RobotsLog;
import com.qingpu.robots.entity.TalkTemplate;
import com.qingpu.socketservice.ContainerClientSocket;
import com.qingpu.socketservice.DetectClientSocket;
import com.qingpu.socketservice.ResponseSocketUtils;
import com.qingpu.socketservice.RobotClientSocket;
import com.qingpu.socketservice.ServerSocketThread;
import com.qingpu.socketservice.ServerSocketThreadDetect;
import com.qingpu.socketservice.ServerSocketThreadRobot;
import com.qingpu.user.entity.User;
import com.qingpu.user.service.UserService;

@Controller
@RequestMapping("/robot")
public class RobotsController extends HandlerInterceptorAdapter {
	
	@Resource 
	RobotsDao robotDao;
	@Resource
	GoodsService goodsService;
	@Resource
	BaseLogService<Object> baseLogService; // 进行日志对象的保存
	@Resource 
	UserService userService;
	@Resource
	AdTemplateDao adTemplateDao;
	
	/**
	 * 页面上加载所有的机器人列表
	 * */
	@RequestMapping(value="/listAll") // ", method = RequestMethod.GET" 指定改请求为Get方式，默认get或者post方式都可以请求
	@ResponseBody
	public String getRobotsListAll(@RequestBody String body){
		ReturnObject retObj = new ReturnObject();
		
		if(body != null && body != ""){
			JSONObject jsonObj = new JSONObject(body);
			String token = jsonObj.getString("token");
			
			if(token != ""){
				List<Robot> robotList = robotDao.getRobotsList();
				
				//将机器人列表中的商品id转化成id+名字
				JSONArray goodsFloorArr = new JSONArray();
				int robotId = 1;
				for(Robot robot : robotList){
					robot.setOrderId(robotId++); // 机器人对象的序号，客户端显示
					List<OneContainerFloor> floorList = robot.getContainerFloors();					
					floorList.removeAll(Collections.singleton(null)); // 去除List中为空的元素
					JSONArray arr = new JSONArray();
					String goodsListStr = "";
					for(OneContainerFloor floor : floorList) {
						JSONObject obj = new JSONObject();
						Goods goods = goodsService.getGoodsById(floor.getGoodsSerialId());// 使用商品的主键获取商品对象
						if(goods != null) {
							obj.put("goodsSerialId", goods.getId()); // 商品的主键
							obj.put("goodsName", goods.getName()); // 商品的名字
							obj.put("goodsFloor", floor.getFloorName()); // 商品所在货架的层名 goodsFloor1 | 2 | 3 | 4
							obj.put("currentCount", floor.getCurrentCount()); // 每层商品剩余的个数
							obj.put("totalCount", floor.getTotalCount()); // 每层商品前后排共可摆放的总数							
							obj.put("stockLeftCount", goods.getRepertory()); // 库存剩余
							obj.put("addGoodsCount", 0); // 用于客户端显示需要补货的列
							arr.put(obj);
							
							goodsListStr += goods.getName() + "；";
						}							
					}
					robot.setGoodsListStr(goodsListStr); // 设置机器人商品名称字符串列表
					
					goodsFloorArr.put(arr);
					robot.setContainerFloors(null);
				}
				
				JSONArray jsonArray = new JSONArray(robotList);
				
				JSONObject ret = new JSONObject();
				ret.put("items", jsonArray);
				ret.put("goodsitem", goodsFloorArr);
				ret.put("total", robotList.size());
				ret.put("code", 0);
				ret.put("message", "success");
				
				return ret.toString();
			}			
		}
		
		return new JSONObject(retObj).toString();
	}
	
	/**
	 * 浏览器上根据过滤条件加载机器人列表
	 * */
	@RequestMapping(value="/list") // ", method = RequestMethod.GET" 指定改请求为Get方式，默认get或者post方式都可以请求
	@ResponseBody
	public String getRobotsListByCondition(@RequestBody String body){
		ReturnObject retObj = new ReturnObject();
		
		if(body != null && body != ""){
			JSONObject jsonObj = new JSONObject(body);
			String token = jsonObj.getString("token");
			
			if(token != ""){
				String floorStr = jsonObj.getString("floor"); // 根据楼层加载机器人
				List<Robot> robotList = null;
				if(floorStr.length() > 0) {
					robotList = robotDao.getRobotListByFloorName(floorStr);
				}else { // 如果是没有搜索条件直接点击搜索按钮
					robotList = robotDao.getRobotsList();
				}
				
				//将机器人列表中的商品id转化成id+名字
				JSONArray goodsFloorArr = new JSONArray();
				int robotId = 1;
				for(Robot robot : robotList){
					robot.setOrderId(robotId++); // 机器人对象的序号，客户端显示
					List<OneContainerFloor> floorList = robot.getContainerFloors();					
					floorList.removeAll(Collections.singleton(null)); // 去除List中为空的元素
					JSONArray arr = new JSONArray();
					for(OneContainerFloor floor : floorList) {
						JSONObject obj = new JSONObject();
						Goods goods = goodsService.getGoodsById(floor.getGoodsSerialId());// 使用商品的主键获取商品对象
						obj.put("goodsSerialId", goods.getId()); // 商品的主键
						obj.put("goodsName", goods.getName()); // 商品的名字
						obj.put("goodsFloor", floor.getFloorName()); // 商品所在货架的层名 goodsFloor1 | 2 | 3 | 4
						obj.put("currentCount", floor.getCurrentCount());
						arr.put(obj);
					}
					goodsFloorArr.put(arr);
					robot.setContainerFloors(null);				
				}
				
				JSONArray jsonArray = new JSONArray(robotList);
				
				JSONObject ret = new JSONObject();
				ret.put("items", jsonArray);
				ret.put("goodsitem", goodsFloorArr);
				ret.put("total", robotList.size());
				ret.put("code", 0);
				ret.put("message", "success");
				
				return ret.toString();
			}			
		}
		
		return new JSONObject(retObj).toString();
	}
	
	/**
	 * 创建楼层机器人
	 * */
	@RequestMapping(value="/create") // ", method = RequestMethod.GET" 指定改请求为Get方式，默认get或者post方式都可以请求
	@ResponseBody
	public String createRobot(@RequestBody String body){
		ReturnObject retObj = new ReturnObject();
		
		if(body != null && body != ""){
			JSONObject jsonObj = new JSONObject(body);
			
			Robot robot = new Robot();
			String name = jsonObj.getString("name"); // 机器人的名字
			robot.setName(name);
			String floor = jsonObj.getString("floor"); // 楼层名字floor1 | 2 | 3
			robot.setFloor(floor);
			String status = jsonObj.getString("status");
			robot.setStatus(status);
			String creator = jsonObj.getString("creator");
			robot.setCreator(creator);
			List<Robot> listRobot = robotDao.getRobotsList();
			robot.setMachineId(""+listRobot.size()+1); // 设置机器人的全场编号，在当前所有机器人基础上+1
			robot.setCreateTime(new Date());
			
			List<OneContainerFloor> floorList = new ArrayList<OneContainerFloor>();
			JSONArray floorGoodsArray = jsonObj.getJSONArray("goodsFloorList");
			for(int i = 0; i < floorGoodsArray.length(); i++){
				JSONObject obj = floorGoodsArray.getJSONObject(i);
				OneContainerFloor oneFloor = new OneContainerFloor();
				oneFloor.setFloorName(obj.getString("goodsFloor")); // 设置货架层数的名字
				oneFloor.setGoodsSerialId(obj.getString("goodsSerialId"));
				// oneFloor.setCurrentCount(obj.getInt("currentCount")); // 在创建机器人的时候不能设置当前层已经放置的商品数量
				oneFloor.setTotalCount(obj.getInt("totalCount")); // 设置当前层前后排总共可以放置的商品数量
				oneFloor.setRobot(robot); // 设置货柜层所属的机器人
				floorList.add(oneFloor);				
			}
			robot.setContainerFloors(floorList);
			
			robotDao.addRobot(robot);
			retObj.setCode(0); // 更新返回的状态值
			retObj.setMessage("success");
			
			RobotsLog log = new RobotsLog();
			log.setActionName(ActionConstants.Robots_Create);
			log.setActionDescription(ActionConstants.Robots_Create_Desc);
			log.setDate(new Date());
			log.setUserName(jsonObj.getString("creator"));
			baseLogService.saveLog(log);
		}
		
		return new JSONObject(retObj).toString();
	}
	
	/**
	 * 查询机器人空对象列表
	 * */
	@RequestMapping(value="/listEmptyRobots") // ", method = RequestMethod.GET" 指定改请求为Get方式，默认get或者post方式都可以请求
	@ResponseBody
	public String listEmptyRobots(@RequestBody String body){
		ReturnObject retObj = new ReturnObject();
		
		if(body != null && body != ""){			
			List<Robot> robotList = robotDao.getRobotsList();			
			JSONArray robotsArr = new JSONArray();
			int index = 1;
			for(Robot robot : robotList){
				JSONObject obj = new JSONObject();
				obj.put("orderId", index++);
				obj.put("machineId", robot.getMachineId());
				obj.put("name", robot.getName());
				obj.put("floorName", robot.getFloor());
				obj.put("date", robot.getCreateTime());
				
				List<OneContainerFloor> floorList = robot.getContainerFloors();					
				floorList.removeAll(Collections.singleton(null)); // 去除List中为空的元素
				JSONArray arr = new JSONArray();
				for(OneContainerFloor floor : floorList) {
					if("goodsFloor1".equals(floor.getFloorName())) {
						arr.put("一层");
					} else if("goodsFloor2".equals(floor.getFloorName())) {
						arr.put("二层");
					} else if("goodsFloor3".equals(floor.getFloorName())) {
						arr.put("三层");
					} else if("goodsFloor4".equals(floor.getFloorName())) {
						arr.put("四层");
					}
				}
				obj.put("checkedContainerFloors", arr);
				robotsArr.put(obj);
			}
			JSONObject ret = new JSONObject();
			ret.put("items", robotsArr);
			ret.put("code", 0);
			ret.put("message", "查询成功");
			return ret.toString();
		}
		
		return new JSONObject(retObj).toString();
	}
	/**
	 * 创建一个空的机器人对象
	 * */
	@RequestMapping(value="/createEmptyRobot") // ", method = RequestMethod.GET" 指定改请求为Get方式，默认get或者post方式都可以请求
	@ResponseBody
	public String createEmptyRobot(@RequestBody String body){
		ReturnObject retObj = new ReturnObject();
		
		if(body != null && body != ""){
			JSONObject jsonObj = new JSONObject(body);
			
			Robot robot = new Robot();
			String machineId = jsonObj.getString("machineId"); // 编号
			robot.setMachineId(machineId);
			String name = jsonObj.getString("name"); // 机器人的名字
			robot.setName(name);
			String floor = jsonObj.getString("floorName"); // 楼层名字floor1 | 2 | 3
			robot.setFloor(floor);
			robot.setCreateTime(new Date());
			
			JSONArray arr = jsonObj.getJSONArray("checkedContainerFloors"); // 货柜层数			
			List<OneContainerFloor> floorList = new ArrayList<OneContainerFloor>();
			for(int i = 0; i < arr.length(); i++) {
				OneContainerFloor oneFloor = new OneContainerFloor();
				oneFloor.setFloorName("goodsFloor"+(i+1)); // 按照层数依次设置货架名称			
				oneFloor.setRobot(robot);
				floorList.add(oneFloor);				
			}
			robot.setContainerFloors(floorList);			
			
			robotDao.addRobot(robot);
			retObj.setCode(0); // 更新返回的状态值
			retObj.setMessage("success");
			
			RobotsLog log = new RobotsLog(); // 系统操作日志
			log.setActionName(ActionConstants.Robots_Create);
			log.setActionDescription(ActionConstants.Robots_Create_Desc);
			log.setDate(new Date());
			baseLogService.saveLog(log);
		}
		
		return new JSONObject(retObj).toString();
	}
	/**
	 * 更新空的机器人对象信息
	 * */
	@RequestMapping(value="/updateEmptyRobot") // ", method = RequestMethod.GET" 指定改请求为Get方式，默认get或者post方式都可以请求
	@ResponseBody
	public String updateEmptyRobot(@RequestBody String body){
		ReturnObject retObj = new ReturnObject();
		
		if(body != null && body != ""){
			JSONObject jsonObj = new JSONObject(body);
			String machineId = jsonObj.getString("machineId");
			Robot robot = robotDao.getRobotByMachineId(machineId);
			if(robot != null) {
				String name = jsonObj.getString("name"); // 机器人的名字
				robot.setName(name);
				String floor = jsonObj.getString("floorName"); // 楼层名字floor1 | 2 | 3
				robot.setFloor(floor);
				
				List<OneContainerFloor> floorList = robot.getContainerFloors();
				floorList.removeAll(Collections.singleton(null)); // 去除List中为空的元素
				JSONArray floorGoodsArray = jsonObj.getJSONArray("checkedContainerFloors");
				int oldListSize = floorList.size();
				int newArrayLen = floorGoodsArray.length();
				if(newArrayLen > oldListSize) { // 当需要新增元素时
					OneContainerFloor newFloor = new OneContainerFloor();
					int addLen = newArrayLen - oldListSize;
					for(int i = 0; i < addLen; i++) {
						newFloor.setFloorName("goodsFloor"+(oldListSize+(i+1))); // 按照层数依次设置货架名称
						newFloor.setRobot(robot);
						floorList.add(newFloor); // 将新添加的货柜层加入到列表中
					}				
				} else if(newArrayLen < oldListSize) {
					int delLen = oldListSize - newArrayLen;
					for(int i = 0; i < delLen; i++) {
						OneContainerFloor oneFloor = floorList.get(newArrayLen+i);
						robotDao.deleteOneFloor(oneFloor.getId()); // 删除其中的一层
						floorList.remove(newArrayLen+i); // 如果只是单纯从list列表中删除再进行update更新，其他元素不会被删除，需要直接操作数据库中删除
					}
				}				
				robot.setContainerFloors(floorList);				
				robotDao.updateRobotInfo(robot);
				retObj.setCode(0); // 更新返回的状态值
				retObj.setMessage("更新成功");
				
				RobotsLog log = new RobotsLog();
				log.setActionName(ActionConstants.Robots_Edit);
				log.setActionDescription(ActionConstants.Robots_Edit_Desc);
				log.setDate(new Date());
				baseLogService.saveLog(log);
			}
		}
		
		return new JSONObject(retObj).toString();
	}
	
	/**
	 * 删除空机器人及其货柜等信息
	 * */
	@RequestMapping(value="/deleteEmptyRobot") // ", method = RequestMethod.GET" 指定改请求为Get方式，默认get或者post方式都可以请求
	@ResponseBody
	public String deleteEmptyRobot(@RequestBody String body){
		ReturnObject retObj = new ReturnObject();
		
		if(body != null && body != ""){
			JSONObject jsonObj = new JSONObject(body);
			
			String machineId = jsonObj.getString("machineId");
			Robot robot = robotDao.getRobotByMachineId(machineId);
			if(robot != null) {
				robotDao.deleteRobot(robot); // 删除机器人本身，可以级联删除，同时会删除子项数据
				
				retObj.setCode(0); // 更新返回的状态值
				retObj.setMessage("删除成功");
			}
		}
		
		return new JSONObject(retObj).toString();
	}
	
	
	/**
	 * 更新机器人商品相关信息
	 * */
	@RequestMapping(value="/update")
	@ResponseBody
	public String updateRobot(@RequestBody String body){
		ReturnObject retObj = new ReturnObject();
		
		if(body != null && body != ""){
			JSONObject jsonObj = new JSONObject(body);
			
			int id = jsonObj.getInt("id");
			Robot robot = robotDao.getRobotById(id);
			if(robot != null) {
				String machineId = robot.getMachineId();
				String name = jsonObj.getString("name"); // 机器人的名字
				robot.setName(name);
				String floor = jsonObj.getString("floor"); // 楼层名字floor1 | 2 | 3
				robot.setFloor(floor);
				String status = jsonObj.getString("status");
				robot.setStatus(status);
				
				List<OneContainerFloor> floorList = robot.getContainerFloors();
				floorList.removeAll(Collections.singleton(null)); // 去除List中为空的元素
				JSONArray floorGoodsArray = jsonObj.getJSONArray("goodsFloorList");
				int oldListSize = floorList.size();
				int newArrayLen = floorGoodsArray.length();
				
				if(oldListSize < newArrayLen){// 当有新增的元素时
					for(int i = 0; i < newArrayLen; i++){
						JSONObject obj = floorGoodsArray.getJSONObject(i);
						
						if((i+1) > oldListSize){ // 需要新增元素
							OneContainerFloor newFloor = new OneContainerFloor();
							newFloor.setFloorName(obj.getString("goodsFloor")); // 设置货架层数的名字
							newFloor.setGoodsSerialId(obj.getString("goodsSerialId"));
							newFloor.setCurrentCount(obj.getInt("currentCount"));
							newFloor.setTotalCount(obj.getInt("totalCount"));
							newFloor.setRobot(robot);
							floorList.add(newFloor); // 将新添加的货柜层加入到列表中
						}else{
							OneContainerFloor oneFloor = floorList.get(i);
							oneFloor.setFloorName(obj.getString("goodsFloor")); // 设置货架层数的名字
							oneFloor.setGoodsSerialId(obj.getString("goodsSerialId"));
							oneFloor.setCurrentCount(obj.getInt("currentCount"));
							oneFloor.setTotalCount(obj.getInt("totalCount"));
						}
					}
				}else if(oldListSize > newArrayLen){ // 需要删除一些元素
					for(int i = 0; i < oldListSize; i++){
						if((i+1) <= newArrayLen){ // 进行更新
							JSONObject obj = floorGoodsArray.getJSONObject(i);
							OneContainerFloor oneFloor = floorList.get(i);
							oneFloor.setFloorName(obj.getString("goodsFloor")); // 设置货架层数的名字
							oneFloor.setGoodsSerialId(obj.getString("goodsSerialId"));
							oneFloor.setCurrentCount(obj.getInt("currentCount"));
							oneFloor.setTotalCount(obj.getInt("totalCount"));
						}else { // 删除列表中的其他货柜层
							OneContainerFloor oneFloor = floorList.get(i);
							robotDao.deleteOneFloor(oneFloor.getId()); // 删除其中的一层
							floorList.remove(floorList.size()-1); // 如果只是单纯从list列表中删除再进行update更新，其他元素不会被删除，需要直接操作数据库中删除							
						}
					}					
				}else { // 长度不变
					boolean isRobotOutOfStore = false; // 机器人是否处于缺货状态
					for(int i = 0; i < newArrayLen; i++){					
						JSONObject obj = floorGoodsArray.getJSONObject(i);
						OneContainerFloor oneFloor = floorList.get(i);
						oneFloor.setFloorName(obj.getString("goodsFloor")); // 设置货架层数的名字
						oneFloor.setGoodsSerialId(obj.getString("goodsSerialId"));
						int currentCount = obj.getInt("currentCount");
						
						int addGoodsCount = obj.getInt("addGoodsCount"); // 补货的数量
						int stockLeftCount = obj.getInt("stockLeftCount"); // 减去添加之前的库存剩余的数量
						currentCount += addGoodsCount; // 增加货柜商品数量
						stockLeftCount = stockLeftCount - addGoodsCount; // 减少库存数量
						// 从数据库中减去库存的减少数量
						Goods goods = goodsService.getGoodsById(obj.getString("goodsSerialId"));
						goods.setRepertory(stockLeftCount);
						goodsService.updateGoodsInfo(goods); // 更新数据库
						
						if(currentCount <= 0) {
							isRobotOutOfStore = true;
						}
						oneFloor.setCurrentCount(currentCount);
						oneFloor.setTotalCount(obj.getInt("totalCount"));
					}
					if(!isRobotOutOfStore) { // 如果补货完成
						robot.setRobotOutOfStore(false);
						if(ServerSocketThread.containerMachineMap.get(machineId) != null) {
							ServerSocketThread.containerMachineMap.get(machineId).setRobotOutOfStore(false); // 设置为不缺货状态，这时等待在终点的机器人会重新开始循环行走
						}						
					}
				}
								
				robot.setContainerFloors(floorList);				
				robotDao.updateRobotInfo(robot);
				retObj.setCode(0); // 更新返回的状态值
				retObj.setMessage("success");
				
				RobotsLog log = new RobotsLog();
				log.setActionName(ActionConstants.Robots_Edit);
				log.setActionDescription(ActionConstants.Robots_Edit_Desc);
				log.setDate(new Date());
				log.setUserName(jsonObj.getString("creator"));
				baseLogService.saveLog(log);
			}												
		}
		
		return new JSONObject(retObj).toString();
	}
	
	/**
	 * 更新机器人全局创建状态信息
	 * */
	@RequestMapping(value="/updateStatus") // ", method = RequestMethod.GET" 指定改请求为Get方式，默认get或者post方式都可以请求
	@ResponseBody
	public String updateRobotStatus(@RequestBody String body){
		ReturnObject retObj = new ReturnObject();
		
		if(body != null && body != ""){
			JSONObject jsonObj = new JSONObject(body);
			
			int id = jsonObj.getInt("id");
			Robot robot = robotDao.getRobotById(id);
			if(robot != null) {
				String status = jsonObj.getString("status");
				robot.setStatus(status);
				robotDao.updateRobotInfo(robot);
				
				retObj.setCode(0); // 更新返回的状态值
				retObj.setMessage("success");
			}
		}
		
		return new JSONObject(retObj).toString();
	}
	
	/**
	 * 获取机器人名字以及编号列表
	 * */
	@RequestMapping(value="/getRobotNameList") 
	@ResponseBody
	public String getRobotNameList(@RequestBody String body){
		ReturnObject retObj = new ReturnObject();
		
		if(body != null && body != ""){
			JSONObject jsonObj = new JSONObject(body);
			JSONArray jsonArr = new JSONArray();			
			List<Robot> robotList = robotDao.getRobotsList();
			
			if(jsonObj.has("floorName")) {
				String floorName = jsonObj.getString("floorName");
				int orderId = 1;
				for(int i = 0; i < robotList.size(); i++) {
					Robot robot = robotList.get(i);
					if(floorName.equals(robot.getFloor())) { // 查找符合楼层条件的机器人
						JSONObject obj = new JSONObject();
						obj.put("orderId", orderId++);
						obj.put("label", robot.getName());
						obj.put("value", robot.getMachineId());
						obj.put("pathId", robot.getPathId()); // 添加机器人所绑定的路线编号
						jsonArr.put(obj);
					}
				}
			} else {
				for(int i = 0; i < robotList.size(); i++) {
					Robot robot = robotList.get(i);
					JSONObject obj = new JSONObject();
					obj.put("label", robot.getName());
					obj.put("value", robot.getMachineId());
					jsonArr.put(obj);
				}
			}
			
			JSONObject retJSONObj = new JSONObject();
			retJSONObj.put("code", 0);
			retJSONObj.put("message", "查询成功");
			retJSONObj.put("robotNameList", jsonArr);
			return retJSONObj.toString();
		}		
		
		return new JSONObject(retObj).toString();
	}
	
	
	@RequestMapping(value="/getDialogList")
	@ResponseBody
	public String getDialogList(@RequestBody String body){
		ReturnObject retObj = new ReturnObject();
		
		if(body != null && body != ""){
			JSONArray jsonRetObj = null;
			int retSize = 0;
			JSONObject jsonObj = new JSONObject(body);
			int groupId = 0;
			if(jsonObj.has("groupId")) {
				groupId = jsonObj.getInt("groupId"); // 对话所属组的名字
			}			
			String type = jsonObj.getString("type"); // 获取查询的模型名称
			String state = jsonObj.getString("tabPaneState"); // 查询当前模式的哪一种状态
			if("patrol".equals(type)) { // 查询巡逻模式
				List<RobotPatrolOrSenseDialog> dialogList = new ArrayList<RobotPatrolOrSenseDialog>();
				String timeIntervalName = jsonObj.getString("timeIntervalName");
				if("freegoing".equals(state)) { // 如果是自由行走模式
					dialogList = robotDao.getPatrolRobotFreeGoingDialog(groupId, type, state, timeIntervalName);
				} else if("reachedgoal".equals(state)) {
					String currentSelectName = jsonObj.getString("currentSelectName");
					dialogList = robotDao.getPatrolRobotDialog(groupId, type, state, timeIntervalName, currentSelectName);
				}
				for(int i = 0; i < dialogList.size(); i++){ // 客户端排序
					RobotPatrolOrSenseDialog obj = dialogList.get(i);
					obj.setOrderId(i+1);
				}
				jsonRetObj = new JSONArray(dialogList);
				retSize = dialogList.size();
			} else if("sensepeople".equals(type)) { // 查询人体检测模式
				String currentSelectName = jsonObj.getString("currentSelectName");
				List<RobotPatrolOrSenseDialog> dialogList = robotDao.getSenseRobotDialog(groupId, type, state, currentSelectName);
				for(int i = 0; i < dialogList.size(); i++){
					RobotPatrolOrSenseDialog obj = dialogList.get(i);
					obj.setOrderId(i+1);
				}
				jsonRetObj = new JSONArray(dialogList);
				retSize = dialogList.size();
			} else { // 查询其他模式
				List<RobotOtherDialog> dialogList = robotDao.getOtherRobotDialog(groupId, type, state);
				for(int i = 0; i < dialogList.size(); i++){
					RobotOtherDialog obj = dialogList.get(i);
					obj.setOrderId(i+1);
				}
				jsonRetObj = new JSONArray(dialogList);
				retSize = dialogList.size();
			}						
			
			JSONObject ret = new JSONObject();
			ret.put("items", jsonRetObj);
			ret.put("total", retSize);
			ret.put("code", 0);
			ret.put("message", "success");
			
			return ret.toString();
		}
		
		return new JSONObject(retObj).toString();
	}
	
	@RequestMapping(value="/addDialog")
	@ResponseBody
	public String addRobotDialog(@RequestBody String body) {
		ReturnObject retObj = new ReturnObject();
		
		if(body != null && body != ""){
			JSONObject jsonObj = new JSONObject(body);
			
			int groupId = jsonObj.getInt("groupId"); // 对话所属组的名字
			String type = jsonObj.getString("type"); // 获取查询的模型名称
			String state = jsonObj.getString("tabPaneState"); // 查询当前模式的哪一种状态
			
			JSONObject ret = new JSONObject();
			RobotPatrolOrSenseDialog patrolOrSenseDialog = new RobotPatrolOrSenseDialog();
			RobotOtherDialog otherDialog = new RobotOtherDialog();
			if("patrol".equals(type) || "sensepeople".equals(type)) { // 如果是巡逻模式或者是人体检测模式				
				if(jsonObj.has("timeIntervalName") && "patrol".equals(type)) { // 只有巡逻模式才需要添加此字段
					String timeIntervalName = jsonObj.getString("timeIntervalName");
					patrolOrSenseDialog.setTimeIntervalName(timeIntervalName);
				}				
				if(jsonObj.has("currentSelectName") && "patrol".equals(type)) { // 只有巡逻模式才需要添加此字段
					String currentSelectName = jsonObj.getString("currentSelectName");
					patrolOrSenseDialog.setReachGoalName(currentSelectName);
				}
				if(jsonObj.has("currentSelectName") && "sensepeople".equals(type)) { // 只有人体检测模式才需要添加此字段
					String currentSelectName = jsonObj.getString("currentSelectName");
					patrolOrSenseDialog.setPeopleInfo(currentSelectName);
				}
				patrolOrSenseDialog.setEdit(false);
				patrolOrSenseDialog.setMessage("自定义内容");
				patrolOrSenseDialog.setOriginalMessage("自定义内容");
				patrolOrSenseDialog.setProbability(0);
				patrolOrSenseDialog.setOriginalProbability(0);
				patrolOrSenseDialog.setType(type);
				patrolOrSenseDialog.setState(state);
				patrolOrSenseDialog.setGroupId(groupId); // 设置所属的对话组
				
				robotDao.addPatrolOrSenseRobotDialog(patrolOrSenseDialog); // 新增对话
				ret.put("item", new JSONObject(patrolOrSenseDialog));
			} else {
				otherDialog.setEdit(false);
				otherDialog.setMessage("自定义内容");
				otherDialog.setOriginalMessage("自定义内容");
				otherDialog.setProbability(0);
				otherDialog.setOriginalProbability(0);
				otherDialog.setType(type);
				otherDialog.setState(state);
				otherDialog.setGroupId(groupId); // 设置所属的对话组
				
				robotDao.addOtherRobotDialog(otherDialog);
				ret.put("item", new JSONObject(otherDialog));
			}
									
			ret.put("code", 0);
			ret.put("message", "success");
			
			return ret.toString();
		}
		
		return new JSONObject(retObj).toString();
	}
	
	@RequestMapping(value="/updateDialog") // ", method = RequestMethod.GET" 指定改请求为Get方式，默认get或者post方式都可以请求
	@ResponseBody
	public String updateRobotDialog(@RequestBody String body){
		ReturnObject retObj = new ReturnObject();
		
		if(body != null && body != ""){
			JSONObject jsonObj = new JSONObject(body);
			
			int id = jsonObj.getInt("id");
			String type = jsonObj.getString("type"); // 获取查询的模型名称
			if("patrol".equals(type) || "sensepeople".equals(type)) { // 如果是巡逻模式或者是人体检测模式
				RobotPatrolOrSenseDialog patrolOrSenseRobot = robotDao.getPatrolOrSenseRobotDialogById(id);
				if(patrolOrSenseRobot != null) {
					patrolOrSenseRobot.setEdit(false);
					patrolOrSenseRobot.setMessage(jsonObj.getString("message"));
					patrolOrSenseRobot.setOriginalMessage(jsonObj.getString("originalMessage"));
					patrolOrSenseRobot.setProbability(jsonObj.getInt("probability"));
					patrolOrSenseRobot.setOriginalProbability(jsonObj.getInt("originalProbability"));
					robotDao.updatePatrolOrSenseRobotDialog(patrolOrSenseRobot);				
					retObj.setCode(0);
					retObj.setMessage("更新成功");
				}				
			} else { // 其他模式
				RobotOtherDialog otherRobotDialog = robotDao.getOtherRobotDialogById(id);
				if(otherRobotDialog != null) {
					otherRobotDialog.setEdit(false);
					otherRobotDialog.setMessage(jsonObj.getString("message"));
					otherRobotDialog.setOriginalMessage(jsonObj.getString("originalMessage"));
					otherRobotDialog.setProbability(jsonObj.getInt("probability"));
					otherRobotDialog.setOriginalProbability(jsonObj.getInt("originalProbability"));
					robotDao.updateOtherRobotDialog(otherRobotDialog);
					retObj.setCode(0);
					retObj.setMessage("更新成功");
				}				
			}			
		}
		
		return new JSONObject(retObj).toString();		
	}
	
	/**
	 * 删除对话
	 * */
	@RequestMapping(value="/deleteDialog") // ", method = RequestMethod.GET" 指定改请求为Get方式，默认get或者post方式都可以请求
	@ResponseBody
	public String deleteRobotDialog(@RequestBody String body){
		ReturnObject retObj = new ReturnObject();
		
		if(body != null && body != ""){
			JSONObject jsonObj = new JSONObject(body);
			
			int id = jsonObj.getInt("id");
			String type = jsonObj.getString("type"); // 获取查询的模型名称
			if("patrol".equals(type) || "sensepeople".equals(type)) { // 如果是巡逻模式或者是人体检测模式
				RobotPatrolOrSenseDialog patrolOrSenseRobot = robotDao.getPatrolOrSenseRobotDialogById(id);
				if(patrolOrSenseRobot != null) {
					robotDao.deletePatrolOrSenseRobotDialog(patrolOrSenseRobot);
					retObj.setCode(0);
					retObj.setMessage("删除对话成功");
				}else {
					retObj.setMessage("所要删除的对话不存在");
				}
			} else { // 其他模式
				RobotOtherDialog otherRobotDialog = robotDao.getOtherRobotDialogById(id);
				if(otherRobotDialog != null) {
					robotDao.deleteOtherRobotDialog(otherRobotDialog);
					retObj.setCode(0);
					retObj.setMessage("删除对话成功");
				}else {
					retObj.setMessage("所要删除的对话不存在");
				}
			}
		}
		
		return new JSONObject(retObj).toString();		
	}
	
	/**
	 * 根据名字新增一个对话组对象
	 * */
	@RequestMapping(value="/createNewTalk")
	@ResponseBody
	public String createNewTalk(@RequestBody String body){
		ReturnObject retObj = new ReturnObject();
		
		if(body != null && body != ""){
			JSONObject jsonObj = new JSONObject(body);
			String talkName = jsonObj.getString("talkName");
			String floorName = jsonObj.getString("floorName");
			
			RobotTalkGroup group = new RobotTalkGroup();
			group.setFloorName(floorName);
			group.setName(talkName);
			group.setDate(new Date());
			RobotTalkGroup newGroup = robotDao.saveNewTalkGroup(group);
			
			JSONObject obj = new JSONObject();
			obj.put("code", 0);
			obj.put("message", "添加成功");
			obj.put("newTalkObjId", newGroup.getId());
			return obj.toString();
		}
		
		return new JSONObject(retObj).toString();		
	}
	
	/**
	 * 按照条件查询已经创建的对话组列表
	 * */
	@RequestMapping(value="/getTalkGroupList")
	@ResponseBody
	public String getTalkGroupList(@RequestBody String body){
		ReturnObject retObj = new ReturnObject();
		
		if(body != null && body != ""){
			JSONObject jsonObj = new JSONObject(body);
			String searchStr = null;			
			if(jsonObj.get("searchStr") != JSONObject.NULL) {
				searchStr = jsonObj.getString("searchStr");
			}
			
			List<RobotTalkGroup> list = robotDao.getRobotTalkGroup(searchStr);
			JSONArray jsonArr = new JSONArray();
			int index = 1;
			for(RobotTalkGroup item : list) {
				JSONObject obj = new JSONObject();
				obj.put("id", item.getId());
				obj.put("orderId", index++);
				obj.put("name", item.getName());
				obj.put("floorName", item.getFloorName());
				JSONArray validTimeArrObj = new JSONArray();
				validTimeArrObj.put(item.getStartDateStr());
				validTimeArrObj.put(item.getStopDateStr());
				obj.put("validTimeArrObj", validTimeArrObj);
				obj.put("date", item.getDate());
				obj.put("isDefaultTalk", item.isDefaultTalk());
				jsonArr.put(obj);
			}

			
			JSONObject obj = new JSONObject();
			obj.put("code", 0);
			obj.put("message", "查询成功");
			obj.put("talkDataList", jsonArr);
			return obj.toString();
		}
		
		return new JSONObject(retObj).toString();		
	}
	/**
	 * 更新对话组对象
	 * */
	@RequestMapping(value="/updateGroupRowTalk")
	@ResponseBody
	public String updateGroupRowTalk(@RequestBody String body){
		ReturnObject retObj = new ReturnObject();
		
		if(body != null && body != ""){
			JSONObject jsonObj = new JSONObject(body);
			JSONObject obj = jsonObj.getJSONObject("row");
			
			int id = obj.getInt("id");
			RobotTalkGroup group = robotDao.getRobotTalkGroupById(id);
			if(group != null) {
				if(obj.has("floorName")) {
					String floorName = obj.getString("floorName");
					group.setFloorName(floorName);
				}				
				JSONArray arr = obj.getJSONArray("validTimeArrObj");
				if(arr.length() > 0) {
					if(arr.get(0) != JSONObject.NULL) { // json对象判断其中的元素是否为null
						group.setStartDateStr(arr.getString(0));
					}
					if(arr.get(1) != JSONObject.NULL) {
						group.setStopDateStr(arr.getString(1));
					}
				}				
				boolean isDefaultTalk = obj.getBoolean("isDefaultTalk");
				group.setDefaultTalk(isDefaultTalk);
				robotDao.updateRobotGroupTalk(group);
				
				retObj.setCode(0);
				retObj.setMessage("更新成功");
			}			
		}
		
		return new JSONObject(retObj).toString();		
	}
	/**
	 * 删除对话组
	 * */
	@RequestMapping(value="/deleteTalkGroupById")
	@ResponseBody
	public String deleteTalkGroupById(@RequestBody String body){
		ReturnObject retObj = new ReturnObject();
		
		if(body != null && body != ""){
			JSONObject jsonObj = new JSONObject(body);
			
			int id = jsonObj.getInt("id");
			robotDao.deleteRobotTalkGroup(id);
			// 还要删除属于此对话组的子数据
			robotDao.deleteRobotTalkBelongtoGroupId(id);
			retObj.setCode(0);
			retObj.setMessage("删除成功");
		}
		
		return new JSONObject(retObj).toString();		
	}
	/**
	 * 跳转到对话详情页面时，根据对话组id查找对话组名称
	 * */
	@RequestMapping(value="/getTalkGroupById")
	@ResponseBody
	public String getTalkGroupById(@RequestBody String body){
		ReturnObject retObj = new ReturnObject();
		
		if(body != null && body != ""){
			JSONObject jsonObj = new JSONObject(body);
			
			int id = jsonObj.getInt("groupId");
			RobotTalkGroup group = robotDao.getRobotTalkGroupById(id);
			if(group != null) {
				JSONObject retJSONObj = new JSONObject();
				retJSONObj.put("talkGroupId", group.getId());
				retJSONObj.put("floorName", group.getFloorName());
				retJSONObj.put("talkGroupName", group.getName());
				retJSONObj.put("code", 0);
				retJSONObj.put("message", "操作成功");
				return retJSONObj.toString();				
			}			
		}
		
		return new JSONObject(retObj).toString();
	}
	
	/*********************************************************************************************************************/
	/*********************************************************************************************************************/
	/*********************************************************************************************************************/
	
	/**
	 * 获取地图json数据------弃用
	 * @throws IOException 
	 * */
	@Deprecated
	@RequestMapping(value="/getMapJsonData")
	@ResponseBody
	public String getMapJsonData(@RequestBody String body) throws IOException {
		ReturnObject retObj = new ReturnObject();		
		
		if(body != null && body != "") {
			String jsonDataFilePath = RobotsController.class.getClassLoader().getResource("testmap.json").getPath();
			FileInputStream fis = new FileInputStream(jsonDataFilePath); // 以utf-8格式读取文件
			InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
			BufferedReader br = new BufferedReader(isr);
			String line = null;
			String fileContent = "";
			while ((line = br.readLine()) != null) {   
				fileContent += line;   
				fileContent += "\r\n"; // 补上换行符   
			}			
			
			Pattern p = Pattern.compile("\\s*|\t|\r|\n");
			Matcher m = p.matcher(fileContent);
			fileContent = m.replaceAll("");
			
			JSONObject retJSON = new JSONObject();
			retJSON.put("code", 0);
			retJSON.put("message", "获取地图数据成功");
			retJSON.put("mapdata", new JSONObject(fileContent));
			return retJSON.toString();
		}
		return new JSONObject(retObj).toString();
	}
	
	/**
	 * 获取路径数组
	 * @throws IOException 
	 * */
	@Deprecated
	@RequestMapping(value="/getPathData")
	@ResponseBody
	public String getPathData(@RequestBody String body) throws IOException {
		ReturnObject retObj = new ReturnObject();
		
		if(body != null && body != "") {
			String jsonDataFilePath = RobotsController.class.getClassLoader().getResource("wayPoint.json").getPath();
			FileInputStream fis = new FileInputStream(jsonDataFilePath); // 以utf-8格式读取文件
			InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
			BufferedReader br = new BufferedReader(isr);
			String line = null;
			String fileContent = "";
			while ((line = br.readLine()) != null) {   
				fileContent += line;   
				fileContent += "\r\n"; // 补上换行符   
			}			
			
			Pattern p = Pattern.compile("\\s*|\t|\r|\n");
			Matcher m = p.matcher(fileContent);
			fileContent = m.replaceAll("");			
			
			JSONObject retJSON = new JSONObject();
			retJSON.put("code", 0);
			retJSON.put("message", "获取地图数据成功");
			retJSON.put("fileContent", new JSONObject(fileContent)); // 使用JSONObject进行格式会打乱原来的顺序，直接发送字符串到客户端
			
			return retJSON.toString();
		}
		
		return new JSONObject(retObj).toString();
	}
	
	/**
	 * 新增或者更新楼层节点名称数组
	 * */
	@RequestMapping(value="/saveFloorPosNames")
	@ResponseBody
	public String saveFloorPosNames(@RequestBody String body) throws IOException {
		ReturnObject retObj = new ReturnObject();
		
		if(body != null && body != "") {
			JSONObject object = new JSONObject(body);
			String floorName = object.getString("floorName");
			JSONArray posNameArr = object.getJSONArray("posNameArr");
			FloorPosName posNameObj = robotDao.getFloorPosNameArr(floorName);
			if(posNameObj == null) {
				posNameObj = new FloorPosName();
			}
			posNameObj.setFloorName(floorName);
			posNameObj.setPosNameStrArr(posNameArr.toString());
			robotDao.saveFloorPosNameObj(posNameObj);
			retObj.setCode(0);
			retObj.setMessage("保存节点名称成功");
		}
		
		return new JSONObject(retObj).toString();
	}
	/**
	 * 根据楼层查询该楼层节点的名称列表
	 * */
	@RequestMapping(value="/getFloorPosNameArr")
	@ResponseBody
	public String getFloorPosNameArr(@RequestBody String body) throws IOException {
		ReturnObject retObj = new ReturnObject();
		
		if(body != null && body != "") {
			JSONObject object = new JSONObject(body);
			String floorName = object.getString("floorName");
			FloorPosName posNameObj = robotDao.getFloorPosNameArr(floorName);
			if(posNameObj != null) {
				JSONObject retJSONObj = new JSONObject();
				retJSONObj.put("code", 0);
				retJSONObj.put("message", "操作成功");
				retJSONObj.put("posNameArr", new JSONArray(posNameObj.getPosNameStrArr()));
				return retJSONObj.toString();
			} else {
				retObj.setMessage("当前楼层还没有创建节点，请创建");
			}
		}
		
		return new JSONObject(retObj).toString();
	}
	
	/**
	 * 根据机器人编号获取绑定的路径信息
	 * */
	@RequestMapping(value="/getCarPathByMachineId")
	@ResponseBody
	public String getCarPathByMachineId(@RequestBody String body) throws IOException {
		ReturnObject retObj = new ReturnObject();
		
		if(body != null && body != "") {
			JSONObject object = new JSONObject(body);
			String machineId = object.getString("machineId");
			Robot robot = robotDao.getRobotByMachineId(machineId);
			if(robot != null) {
				PathListData path = robotDao.getPathById(robot.getPathId());
				if(path != null) {
					JSONObject retJSONObj = new JSONObject();
					retJSONObj.put("code", 0);
					retJSONObj.put("message", "操作成功");
					retJSONObj.put("jsonPathArr", new JSONArray(path.getJsonPathStr()));
					return retJSONObj.toString();
				} else {
					retObj.setMessage("编号" + machineId + "机器人还未绑定路径，请绑定");
				}
			} else {
				retObj.setMessage("编号" + machineId + "机器人不存在");
			}
		}
		
		return new JSONObject(retObj).toString();
	}
	
	
	/**
	 * 获取当前小车的位置和速度
	 * 需要上传当前小车的编号
	 * */
	@RequestMapping(value="/getPosAndSpeed")
	@ResponseBody
	public String getPosAndSpeed(@RequestBody String body) {
		ReturnObject retObj = new ReturnObject();		
		
		if(body != null && body != "") {
			JSONObject object = new JSONObject(body);
			if(object.has("machineId")) {
				String machineId = object.getString("machineId");
				JSONObject retJSON = new JSONObject();
				RobotClientSocket client = ServerSocketThreadRobot.robotMachineMap.get(machineId);
				if(client != null) {
					JSONObject obj = client.getRecvRobotPosAndSpeedObj(); 
					retJSON.put("code", 0);
					retJSON.put("message", "Success");
					if(obj!= null && obj.has("carOneSpeed")) {
						retJSON.put("carOneSpeed", obj.getInt("carOneSpeed"));
						retJSON.put("batteryValue", CommonUtils.getRandomNum(20, 26));
						retJSON.put("carOneStartPosName", obj.getString("carOneStartPosName"));
						retJSON.put("carOneEndPosName", obj.getString("carOneEndPosName"));
						retJSON.put("carOnePosPercent", obj.getInt("carOnePosPercent"));
					} else {
						System.out.println("@@编号"+ machineId +" 底盘连接了但是还未发送当前位置和速度等信息");
					}					
					return retJSON.toString();
				}else{
					System.out.println("@@获取机器人速度socket连接对象为null");
					retObj.setMessage(machineId+" 号机器人底盘未连接，请检查");
				}
			}else{
				retObj.setMessage("获取机器人速度请求缺少机器人id参数");
			}
		}
		return new JSONObject(retObj).toString();
	}
	
	/**
	 * 设置机器人的下一个目标点
	 * */
	@RequestMapping(value="/sendGoalName")
	@ResponseBody
	public String sendGoalName(@RequestBody String body) {
		ReturnObject retObj = new ReturnObject();
		
		if(body != null && body != "") {				
			synchronized (this) {
				JSONObject object = new JSONObject(body);
				String machineId = object.getString("machineId");
				RobotClientSocket client = ServerSocketThreadRobot.robotMachineMap.get(machineId);
				if(client != null && client.getClient() != null) { // 如果机器人底盘连接且处于空闲状态
					if(client.isHasRobotReachedGoal()) {
						client.setHasRobotReachedGoal(false); // 设置机器人处于忙状态
						String goalName = object.getString("carOneGoalPosName"); // 获取需要前进到的目标点
						String currentPosName = client.getCurrentPosName();							
						
						if(goalName.equals(currentPosName)) {
							retObj.setMessage("当前点与目标点重合，请重新设置");
						} else {
							List<String> pathArr = new ArrayList<String>(); // 获取到达的路径值列表
							pathArr.add(currentPosName); // 需要添加当前点
							pathArr.add(goalName);
							
							JSONObject jsonObject = new JSONObject();
							jsonObject.put("carOneGoalPosName", new JSONArray(pathArr));
							System.out.println("@@发送路径sendPath = " + jsonObject.toString());
							ResponseSocketUtils.sendJsonDataToClient(
									jsonObject, 
									client.getClient(),
									QingpuConstants.SEND_ROBOT_GOAL,
									QingpuConstants.ENCRYPT_BY_NONE,
									QingpuConstants.DATA_TYPE_JSON);
							retObj.setCode(0);
							retObj.setMessage("发送命令成功，准备前往目的地 -- " + goalName);
						}				
					}else {
						retObj.setMessage("机器人正在前往上一个目标点，请稍后再试");
					}
				}else {
					retObj.setMessage("机器人底盘未连接服务器，请检查");
				}				
			}
		}
		
		return new JSONObject(retObj).toString();
	}		
	
	/**
	 * 设置某个机器人停止路径循环
	 * */	
	@RequestMapping(value="/sendRobotStopLoop")
	@ResponseBody
	public String sendRobotStopLoop(@RequestBody String body) {
		ReturnObject retObj = new ReturnObject();		
		
		if(body != null && body != "") {		
			JSONObject object = new JSONObject(body);
			String machineId = object.getString("machineId");
			
			RobotClientSocket clientObj = ServerSocketThreadRobot.robotMachineMap.get(machineId);
			if(clientObj != null) {
				clientObj.setNeedStopLoopMove(true); // 设置机器人在到达起始点的时候停止循环
				ServerSocketThreadRobot.robotMachineMap.put(machineId, clientObj); // 更新对象
				retObj.setCode(0);
				retObj.setMessage("设置成功，机器人在到达起点时将停止循环");
			} else {
				retObj.setMessage("底盘未连接，请检查");
			}
		}
		
		return new JSONObject(retObj).toString();
	}
	
	/**
	 * 添加一条机器人模板
	 * */	
	@RequestMapping(value="/addTemplateTalk")
	@ResponseBody
	public String addTemplateTalk(@RequestBody String body) {
		ReturnObject retObj = new ReturnObject();		
		
		if(body != null && body != "") {		
			JSONObject object = new JSONObject(body);
			String type = object.getString("type"); // common male female nosex
			JSONObject newTodo = object.getJSONObject("newtodo");
			String content = newTodo.getString("content");
			int orderId = newTodo.getInt("orderId");
			
			TalkTemplate talk = new TalkTemplate();
			talk.setContent(content);
			talk.setType(type);
			talk.setOrderId(orderId);
			robotDao.addRobotTemplateTalk(talk);
			
			retObj.setCode(0);
			retObj.setMessage("添加成功");
		}
		
		return new JSONObject(retObj).toString();
	}
	
	/**
	 * 删除一条机器人对话需要的模板
	 * */	
	@RequestMapping(value="/deleteTemplateTalk")
	@ResponseBody
	public String deleteTemplateTalk(@RequestBody String body) {
		ReturnObject retObj = new ReturnObject();		
		
		if(body != null && body != "") {		
			JSONObject object = new JSONObject(body);
			String type = object.getString("type"); // common male female nosex
			JSONObject newTodo = object.getJSONObject("newtodo");
			String content = newTodo.getString("content");
			int orderId = newTodo.getInt("orderId");			
			robotDao.deleteRobotTemplateTalk(type, orderId);
			
			retObj.setCode(0);
			retObj.setMessage("删除成功");
		}
		
		return new JSONObject(retObj).toString();
	}
	
	/**
	 * 获取所有的对话模板数据，并组装成客户端需要的格式
	 * */	
	@RequestMapping(value="/getRobotTemplateLists")
	@ResponseBody
	public String getRobotTemplateLists(@RequestBody(required = false) String body) {		
		List<TalkTemplate> list = robotDao.getRobotTalkTemplateList();
		List<TalkTemplate> todolistCommon = new ArrayList<TalkTemplate>();
		List<TalkTemplate> todolistMale = new ArrayList<TalkTemplate>();
		List<TalkTemplate> todolistFemale = new ArrayList<TalkTemplate>();
		List<TalkTemplate> todolistNoSex = new ArrayList<TalkTemplate>();
		
		for(TalkTemplate talk : list) {
			if("common".equals(talk.getType())) {
				todolistCommon.add(talk);
			} else if("male".equals(talk.getType())) {
				todolistMale.add(talk);
			} else if("female".equals(talk.getType())) {
				todolistFemale.add(talk);
			} else if("nosex".equals(talk.getType())) {
				todolistNoSex.add(talk);
			}
		}
		
		JSONObject retObj = new JSONObject();
		retObj.put("code", 0);
		retObj.put("message", "查询成功");
		retObj.put("todolistCommon", new JSONArray(todolistCommon));
		retObj.put("todolistMale", new JSONArray(todolistMale));
		retObj.put("todolistFemale", new JSONArray(todolistFemale));
		retObj.put("todolistNoSex", new JSONArray(todolistNoSex));
		
		return retObj.toString();
	}
	
	/**
	 * 添加存储一条路径数据到数据库
	 * */
	@RequestMapping(value="/saveNewPath")
	@ResponseBody
	public String saveNewPath(@RequestBody String body) {
		ReturnObject retObj = new ReturnObject();		
		
		if(body != null && body != "") {
			JSONObject object = new JSONObject(body);
			JSONObject pathTaskObj = object.getJSONObject("pathTaskObj");
			String name = pathTaskObj.getString("name"); // 路径的名字
			String startPosName = pathTaskObj.getString("startPosName");
			int loopStaySec = pathTaskObj.getInt("loopStaySec");
			String floorName = pathTaskObj.getString("floorName"); // 路线所属楼层的名字
			JSONArray pathJSONArr = pathTaskObj.getJSONArray("pathDataList");
			
			PathListData path = new PathListData();
			path.setName(name);
			path.setLoopStaySec(loopStaySec);
			path.setJsonPathStr(pathJSONArr.toString());
			path.setFloorName(floorName);
			robotDao.savePathData(path);
			
			JSONObject retJSONObj = new JSONObject();
			retJSONObj.put("code", 0);
			retJSONObj.put("message", "添加成功");
			
			return retJSONObj.toString();
		}
		return new JSONObject(retObj).toString();
	}
	
	/**
	 * 获取某个指定楼层的路径列表
	 * */
	@RequestMapping(value="/getPathByFloorName")
	@ResponseBody
	public String getPathByFloorName(@RequestBody String body) {
		ReturnObject retObj = new ReturnObject();		
		
		if(body != null && body != "") {
			JSONObject object = new JSONObject(body);
			
			String floorName = object.getString("floorName");			
			List<PathListData> pathList = robotDao.getPathListsByFloorName(floorName); // 获取已经创建的路径列表，传递到客户端刷新数据
			int orderId = 1;
			for(PathListData path: pathList) {
				path.setOrderId(orderId++);
			}
			JSONObject retJSONObj = new JSONObject();
			retJSONObj.put("pathLists", new JSONArray(pathList));
			retJSONObj.put("code", 0);
			retJSONObj.put("message", "查询成功");
			
			return retJSONObj.toString();
		}		
		return new JSONObject(retObj).toString();
	}
	
	/**
	 * 更新某个修改之后的路径
	 * */
	@RequestMapping(value="/updateNewPath")
	@ResponseBody
	public String updateNewPath(@RequestBody String body) {
		ReturnObject retObj = new ReturnObject();		
		
		if(body != null && body != "") {
			JSONObject object = new JSONObject(body);
			JSONObject pathTaskObj = object.getJSONObject("pathTaskObj");
			JSONObject retJSONObj = new JSONObject();
			
			int pathId = pathTaskObj.getInt("pathId");
			PathListData path = robotDao.getPathById(pathId);
			if(path != null) {
				String name = pathTaskObj.getString("name"); // 路径的名字
				String startPosName = pathTaskObj.getString("startPosName");
				int loopStaySec = pathTaskObj.getInt("loopStaySec");
				String floorName = pathTaskObj.getString("floorName"); // 路线所属楼层的名字
				JSONArray pathJSONArr = pathTaskObj.getJSONArray("pathDataList");				
				path.setName(name);
				path.setLoopStaySec(loopStaySec);
				path.setFloorName(floorName);
				path.setJsonPathStr(pathJSONArr.toString());				
				robotDao.updatePathData(path);
				
				retJSONObj.put("code", 0);
				retJSONObj.put("message", "更新成功");				
				return retJSONObj.toString();
			} else {
				retObj.setMessage("更新失败");				
			}												
		}
		return new JSONObject(retObj).toString();
	}
		
	/**
	 * 给指定的机器人绑定路线
	 * */
	@RequestMapping(value="/bindRobotWithPathId")
	@ResponseBody
	public String bindRobotWithPathId(@RequestBody String body) {
		ReturnObject retObj = new ReturnObject();		
		
		if(body != null && body != "") {
			JSONObject object = new JSONObject(body);
			String machineId = object.getString("machineId");
			int selectPathId = object.getInt("selectPathId"); // 路径id号
			
			Robot robot = robotDao.getRobotByMachineId(machineId);
			if(robot != null) {
				robot.setPathId(selectPathId);
				robotDao.updateRobotInfo(robot);
				
				retObj.setCode(0);
				retObj.setMessage("绑定成功");				
			}else {
				retObj.setMessage("机器人不存在");
			}							
		}
		return new JSONObject(retObj).toString();
	}
	
	/**
	 * 手机页面上向指定编号的机器人发送路径导航循环控制命令
	 * "startLoopRun"：开始循环行走
	 * "stopLoopRun"：停止循环行走
	 * "enterPatrolMode"：进入雷达导航状态
	 * "outPatrolMode"：退出雷达导航状态
	 * */
	@RequestMapping(value="/sendRobotControlCmd")
	@ResponseBody
	public String sendRobotControlCmd(@RequestBody String body) {
		ReturnObject retObj = new ReturnObject();		
		
		if(body != null && body != "") {
			JSONObject object = new JSONObject(body);
			String machineId = object.getString("machineId");
			String cmdType = object.getString("cmdType"); // 请求的命令类型
			System.out.println("@@手机发送控制命令cmdType = " + cmdType);
			RobotClientSocket robotObj = ServerSocketThreadRobot.robotMachineMap.get(machineId);
			DetectClientSocket detectClientObj = ServerSocketThreadDetect.detectMachineMap.get(machineId);			
			
			if(detectClientObj != null) {
				if("startLoopRun".equals(cmdType) || "stopLoopRun".equals(cmdType)) { // 开始循环行走
					if(robotObj != null) {
						if("startLoopRun".equals(cmdType)) {
							if(robotObj.isHasRobotReachedGoal()) { // 设定的停止时间到了之后，定时器发送归位命令，底盘到达目标点之后置位此标识							
								Robot robot = robotDao.getRobotByMachineId(machineId);
								if(robot != null) {
									PathListData path = robotDao.getPathById(robot.getPathId());
									JSONArray pathJSONArr = new JSONArray(path.getJsonPathStr()); // 设置机器人的当前位置点名称和起点位置
									JSONObject itemObj = pathJSONArr.getJSONObject(0);
									robotObj.setCurrentPosName(itemObj.getString("posName"));
									robotObj.setStartPosName(itemObj.getString("posName"));
									
									JSONArray posStayTimeArr = new JSONArray(path.getJsonPathStr());
									int loopStartPosStaySec = path.getLoopStaySec();
									
									ContainerClientSocket clientSocket = ServerSocketThread.containerMachineMap.get(machineId);
									if(clientSocket != null) {
										clientSocket.setRobotOutOfStore(false); // 启动肯定是进行了补货之后，所以设置机器人处于非缺货状态
									}
									long startLoopMiliTime = 0; 
									long currentTime = System.currentTimeMillis() ;
									long stopLoopMiliTime = currentTime + 60*60*1000; // 在当前毫秒基础上增加一个小时
									robotObj.setStartLoopMiliTime(startLoopMiliTime); // 只有在停靠状态才可以更新起止时间和
									robotObj.setStopLoopMiliTime(stopLoopMiliTime);
									robotObj.setStartPosName(robotObj.getStartPosName());
									robotObj.setPosStayTimeJSONArr(posStayTimeArr);
									robotObj.setLoopStartPosStaySec(loopStartPosStaySec); // 一个循环之后暂停的时间
									robotObj.setHasTimerSendStartMove(false); // 重置定时器发送控制命令
									robotObj.setHasTimerSendStopMove(false);
									robotObj.setNeedStopLoopMove(false); // 设置机器人不需要停止循环									
									robotObj.setHasTimerSendStartMove(true); // 设置定时器已经发送了启动命令，定时启动的控制选项
									robotObj.setHasRobotReachedGoal(false); // 设置机器人已经处于运行状态，不再响应其他设置命令
									ServerSocketThreadRobot.robotMachineMap.put(machineId, robotObj);
									
									// 发送新设置的路径给底盘
									if(robotObj.getCurrentPosName().equals(robotObj.getStartPosName())) { // 如果当前路径点与设置的起始路径点名字相同，则发送路径列表给底盘
										JSONObject jsonObj = new JSONObject();
										jsonObj.put("carOneGoalPosName", new JSONArray(path.getJsonPathStr()));
										System.out.println("@@手机控制立即执行路径 = " + jsonObj.toString());
										ResponseSocketUtils.sendJsonDataToClient(
												jsonObj, 
												robotObj.getClient(),
												QingpuConstants.SEND_ROBOT_GOAL,
												QingpuConstants.ENCRYPT_BY_NONE,
												QingpuConstants.DATA_TYPE_JSON);
										retObj.setCode(0);
										retObj.setMessage("机器人处于空闲状态，立即执行任务 = " + jsonObj.toString());
									} else {
										retObj.setMessage("机器人没有处于起点位置，请先控制机器人到达起点");
									}								
								} else {
									retObj.setMessage("查找指定编号的机器人失败");
								}				
							}else {
								retObj.setMessage("机器人还未到达起点，请先停止机器人循环模式");
							}
						} else if("stopLoopRun".equals(cmdType)) {														
							robotObj.setNeedStopLoopMove(true);// 设置机器人在到达起始点的时候停止循环
							ServerSocketThreadRobot.robotMachineMap.put(machineId, robotObj);
							System.out.println("@@发送停止循环命令成功");
							retObj.setMessage("发送停止循环命令成功，将在回到起点时停止运行");
						}						
					}else {
						retObj.setMessage("指定编号机器人底盘未连接，请检查");
					}					
				} else if("enterPatrolMode".equals(cmdType)) {
					ServerSocketThreadDetect.sendControlCmdToDetectSocket(machineId, cmdType);
					System.out.println("@@发送启动激光雷达导航命令成功");
					retObj.setMessage("发送启动激光雷达导航命令成功");
				} else if("outPatrolMode".equals(cmdType)) {
					ServerSocketThreadDetect.sendControlCmdToDetectSocket(machineId, cmdType);
					System.out.println("@@发送停止底盘雷达命令成功");
					retObj.setMessage("发送停止底盘雷达命令成功");
				} else if("poweroff".equals(cmdType)) {
					ServerSocketThreadDetect.sendControlCmdToDetectSocket(machineId, cmdType);
					System.out.println("@@发送电脑关机命令成功");
					retObj.setMessage("发送电脑关机命令成功");
				}
			} else {
				retObj.setMessage("底盘相关处理程序未连接服务器，请检查");
			}
		}
		
		String retStr = new JSONObject(retObj).toString();
		System.out.println("@@手机端发送控制命令返回值 = " + retStr);
		return retStr;
	}

	/**
	 * 遥控行走建图和设置路径点接口
	 */
	@RequestMapping(value="/teleopControlCmd")
	@ResponseBody
	public String teleopControlCmd(@RequestBody String body) {
		ReturnObject retObj = new ReturnObject();		
		
		if(body != null && body != "") {
			JSONObject object = new JSONObject(body);
			String machineId = object.getString("machineId");
			String cmdType = object.getString("moveCmd");
			System.out.println("@@接收到遥控命令 = " + cmdType);
			RobotClientSocket robotObj = ServerSocketThreadRobot.robotMachineMap.get(machineId);
			DetectClientSocket detectClientObj = ServerSocketThreadDetect.detectMachineMap.get(machineId);
			if(detectClientObj != null) {
				if(robotObj != null) {
					System.out.println("@@机器人底盘处于连接状态");
					retObj.setCode(0);
					if("F".equals(cmdType)) {
						ServerSocketThreadDetect.sendControlCmdToDetectSocket(machineId, cmdType);
						System.out.println("@@发送前进命令");
					}else if("L".equals(cmdType)) {
						ServerSocketThreadDetect.sendControlCmdToDetectSocket(machineId, cmdType);
						System.out.println("@@发送左转命令");
					}else if("R".equals(cmdType)) {
						ServerSocketThreadDetect.sendControlCmdToDetectSocket(machineId, cmdType);
						System.out.println("@@发送右转命令");
					}else if("S".equals(cmdType)) {
						ServerSocketThreadDetect.sendControlCmdToDetectSocket(machineId, cmdType);
						System.out.println("@@发送停止命令");
					}else if("B".equals(cmdType)) {
						ServerSocketThreadDetect.sendControlCmdToDetectSocket(machineId, cmdType);
						System.out.println("@@发送后退命令");
					} else if("stopScanMapMode".equals(cmdType)) {
						retObj.setCode(-1);
						retObj.setMessage("@@退出建图模式");
						ServerSocketThreadDetect.sendControlCmdToDetectSocket(machineId, cmdType);
						System.out.println("@@退出建图模式");
					} else if("stopNewPosMode".equals(cmdType)) {
						retObj.setCode(-1);
						retObj.setMessage("退出创建路径点模式");
						ServerSocketThreadDetect.sendControlCmdToDetectSocket(machineId, cmdType);
						System.out.println("@@退出创建路径点模式");
					} else if("addNewPosName".equals(cmdType)) {						
						// 向底盘发送命令获取当前的坐标信息
						Robot robot = robotDao.getRobotByMachineId(machineId); // 获取机器人当前绑定的路径信息，如果存在相同名字的地点，则更新此地点的坐标信息，否则增加新的地点和坐标
						if(robot != null) {
							PathListData path = robotDao.getPathById(robot.getPathId());
							if(path != null) {
								String posName = object.getString("posName");
								// 向底盘通过socket发送命令，获取当前位置的坐标点								
								JSONObject sendJsonObj = new JSONObject();
								sendJsonObj.put("getCurrentPos", 1);
								ResponseSocketUtils.sendJsonDataToClient(
										sendJsonObj,
										robotObj.getClient(),
										QingpuConstants.SEND_GET_CURRENT_POS,
										QingpuConstants.ENCRYPT_BY_NONE,
										QingpuConstants.DATA_TYPE_JSON);
								int delayCount = 0;
								while(robotObj.getCurrentPosObj() == null) { // 等待接收底盘上传的当前坐标
									try {
										Thread.sleep(20);
										delayCount++;
										if(delayCount >= 50*6) {
											System.out.println("@@长时间等待底盘未上传当前的坐标点");
											retObj.setMessage("长时间等待底盘未上传当前的坐标点");
											break;
										}
									} catch (InterruptedException e) {
										e.printStackTrace();
									}									
								}
								if(robotObj.getCurrentPosObj() != null) {
									JSONObject posObj = robotObj.getCurrentPosObj();
									robotObj.setCurrentPosObj(null); // 清空当前存储的坐标点，为下一次接收做准备
									double X = posObj.getDouble("X");
									double Y = posObj.getDouble("Y");
									double Z = posObj.getDouble("Z");
									JSONArray pathNameArr = null;
									if(path.getJsonPathStr().length() > 0) { // 如果当前路径字符串不是一个空的
										pathNameArr = new JSONArray(path.getJsonPathStr());
									} else {
										pathNameArr = new JSONArray();
									}									
									if(pathNameArr.length() <= 0) { // 如果当前绑定的路径为空
										JSONObject newPosObj = new JSONObject();
										newPosObj.put("posName", posName);
										newPosObj.put("staySec", 0);
										newPosObj.put("X", X);
										newPosObj.put("Y", Y);
										newPosObj.put("Z", Z);
										pathNameArr.put(0, newPosObj);
									} else {
										boolean hasSameName = false;
										int index = 0;
										for(int i = 0; i < pathNameArr.length(); i++) {
											JSONObject obj = pathNameArr.getJSONObject(i);
											String posNameStr = obj.getString("posName");
											if(posName.equals(posNameStr)) {
												hasSameName = true;
												index = i;
											}
										}
										if(hasSameName) { // 如果当前输入的地点名称已经存在，则只更新XYZ的值
											JSONObject obj = pathNameArr.getJSONObject(index);
											obj.put("X", X);
											obj.put("Y", Y);
											obj.put("Z", Z);
											pathNameArr.put(index, obj);
										} else {
											JSONObject newPosObj = new JSONObject();
											newPosObj.put("posName", posName);
											newPosObj.put("staySec", 0);
											newPosObj.put("X", X);
											newPosObj.put("Y", Y);
											newPosObj.put("Z", Z);
											pathNameArr.put(pathNameArr.length(), newPosObj);
										}
									}	
									// 保存路径对象字符串到数据库
									path.setJsonPathStr(pathNameArr.toString());
									robotDao.updatePathData(path);
									retObj.setMessage("@@保存中间路径点成功");
									retObj.setCode(0);
								} else {
									robotObj.setCurrentPosObj(null);
								}							
							} else {
								retObj.setMessage("@@当前机器人未绑定路径，请先绑定一条路径后重试");
							}
						} else {
							retObj.setMessage("@@机器人不存在");
						}
					}
				} else {
					System.out.println("@@机器人底盘客户端没有处于连接状态");
					retObj.setCode(0);
					if("F".equals(cmdType)) {
						ServerSocketThreadDetect.sendControlCmdToDetectSocket(machineId, cmdType);
						System.out.println("@@发送前进命令");
					}else if("L".equals(cmdType)) {
						ServerSocketThreadDetect.sendControlCmdToDetectSocket(machineId, cmdType);
						System.out.println("@@发送左转命令");
					}else if("R".equals(cmdType)) {
						ServerSocketThreadDetect.sendControlCmdToDetectSocket(machineId, cmdType);
						System.out.println("@@发送右转命令");
					}else if("S".equals(cmdType)) {
						ServerSocketThreadDetect.sendControlCmdToDetectSocket(machineId, cmdType);
						System.out.println("@@发送停止命令");
					}else if("B".equals(cmdType)) {
						ServerSocketThreadDetect.sendControlCmdToDetectSocket(machineId, cmdType);
						System.out.println("@@发送后退命令");
					} else if("startScanMapMode".equals(cmdType)) {
						retObj.setCode(-1);
						retObj.setMessage("进入建图");
						ServerSocketThreadDetect.sendControlCmdToDetectSocket(machineId, cmdType);
						System.out.println("@@进入建图模式"); // 建图建图模式，此模式底盘ROS客户端没有连接
					} else if("stopScanMapMode".equals(cmdType)) {
						retObj.setCode(-1);
						retObj.setMessage("退出建图");
						ServerSocketThreadDetect.sendControlCmdToDetectSocket(machineId, cmdType);
						System.out.println("@@退出建图模式");
					} else if("startNewPosMode".equals(cmdType)) { // 进入创建路径点模式
						retObj.setCode(-1);
						retObj.setMessage("进入创建路径点模式");
						ServerSocketThreadDetect.sendControlCmdToDetectSocket(machineId, cmdType);
						System.out.println("@@进入创建路径点模式");
					} else {
						retObj.setCode(-1);
						retObj.setMessage("电机行走控制客户端未连接，请检查");
					}
				}
			} else {
				retObj.setMessage("底盘命令处理程序未连接，请检查");
			}
			
		}
		String retStr = new JSONObject(retObj).toString();
		System.out.println("@@手机端发送控制命令返回值 = " + retStr);
		return retStr;
	}
	
	/**
	 * 电脑浏览器网页上点击启动循环按钮启动机器人，更新机器人的路径循环控制信息，如果没有填写启动时间则默认是立即启动
	 * */
	@RequestMapping(value="/updateRobotControlInfo")
	@ResponseBody
	public String updateRobotControlInfo(@RequestBody String body) {
		ReturnObject retObj = new ReturnObject();		
		
		if(body != null && body != "") {
			JSONObject object = new JSONObject(body);
			
			JSONObject rowObj = object.getJSONObject("rowObj");
			String machineId = rowObj.getString("machineId"); // 需要在请求数据的时候将此信息发送到客户端
			
			Robot robot = robotDao.getRobotByMachineId(machineId);
			if(robot != null) {
				String startLoopTimeStr = "";
				if(rowObj.has("startTime")) {
					startLoopTimeStr = rowObj.getString("startTime"); // 只是保存一下机器人最后一次设置的起止时间到数据库
				}				
				String stopLoopTimeStr = rowObj.getString("endTime");
				robot.setStartLoopTimeStr(startLoopTimeStr);
				robot.setStopLoopTimeStr(stopLoopTimeStr);
				robotDao.updateRobotInfo(robot); // 更新设置
				
				RobotClientSocket robotObj = ServerSocketThreadRobot.robotMachineMap.get(machineId);
				if(robotObj != null) {					
					if(robotObj.isHasRobotReachedGoal()) { // 设定的停止时间到了之后，定时器发送归位命令，底盘到达起点最终点之后置位此标识												
						long startLoopMiliTime = CommonUtils.translateHourStrToMiniSec(startLoopTimeStr); 
						long stopLoopMiliTime = CommonUtils.translateHourStrToMiniSec(stopLoopTimeStr);
						
						PathListData path = robotDao.getPathById(robot.getPathId());						
						JSONArray pathJSONArr = new JSONArray(path.getJsonPathStr()); // 设置机器人的当前位置点名称和起点位置
						JSONObject itemObj = pathJSONArr.getJSONObject(0);
						robotObj.setCurrentPosName(itemObj.getString("posName"));
						robotObj.setStartPosName(itemObj.getString("posName"));						
						JSONArray posStayTimeArr = new JSONArray(path.getJsonPathStr());
						int loopStartPosStaySec = path.getLoopStaySec();
						
						ContainerClientSocket clientSocket = ServerSocketThread.containerMachineMap.get(machineId);
						if(clientSocket != null) {
							clientSocket.setRobotOutOfStore(false); // 启动肯定是进行了补货之后，所以设置机器人处于非缺货状态
						}

						robotObj.setStartLoopMiliTime(startLoopMiliTime); // 只有在停靠状态才可以更新起止时间和
						robotObj.setStopLoopMiliTime(stopLoopMiliTime);
						robotObj.setStartPosName(robotObj.getStartPosName());
						robotObj.setPosStayTimeJSONArr(posStayTimeArr);
						robotObj.setLoopStartPosStaySec(loopStartPosStaySec); // 一个循环之后暂停的时间
						robotObj.setHasTimerSendStartMove(false); // 重置定时器发送控制命令
						robotObj.setHasTimerSendStopMove(false);
						robotObj.setNeedStopLoopMove(false); // 设置机器人不需要停止循环						
						ServerSocketThreadRobot.robotMachineMap.put(machineId, robotObj);
						retObj.setCode(0);
						retObj.setMessage("机器人处于空闲状态，将定时执行任务");
						
						if(startLoopMiliTime <= 0) { // 如果没有设置启动时间则默认是马上启动
							robotObj.setHasTimerSendStartMove(true); // 设置已经发送了启动命令
							robotObj.setHasRobotReachedGoal(false); // 设置机器人已经处于运行状态，不再响应其他设置命令
							ServerSocketThreadRobot.robotMachineMap.put(machineId, robotObj);
							// 发送新设置的路径给底盘
							if(robotObj.getCurrentPosName().equals(robotObj.getStartPosName())) { // 如果当前路径点与设置的起始路径点名字相同，则发送路径列表给底盘
								JSONObject jsonObj = new JSONObject();
								jsonObj.put("carOneGoalPosName", new JSONArray(path.getJsonPathStr()));
								System.out.println("@@电脑网页控制立即执行路径 = " + jsonObj.toString());
								ResponseSocketUtils.sendJsonDataToClient(
										jsonObj, 
										robotObj.getClient(),
										QingpuConstants.SEND_ROBOT_GOAL,
										QingpuConstants.ENCRYPT_BY_NONE,
										QingpuConstants.DATA_TYPE_JSON);
								retObj.setCode(0);
								retObj.setMessage("机器人处于空闲状态，立即执行任务");
							} else {
								retObj.setMessage("机器人没有处于起点位置，请先控制机器人到达起点");
							}
						}					
					}else {
						retObj.setMessage("机器人还未到达起点，请先停止机器人循环模式");
					}
				}else {
					retObj.setMessage("指定编号机器人未连接，请检查");
				}
			} else {
				retObj.setMessage("设置的机器人不存在，设置失败");
			}						
		}
		
		return new JSONObject(retObj).toString();
	}
	
	/**
	 * 获取机器人监控页面的机器人路径绑定信息以及当前状态起始结束时间等信息
	 * */
	@RequestMapping(value="/getRobotMonitorInfo")
	@ResponseBody
	public String getRobotMonitorInfo(@RequestBody String body) {
		ReturnObject retObj = new ReturnObject();
		
		if(body != null && body != "") {
			JSONObject object = new JSONObject(body);
			String floorName = object.getString("floorName");
			List<Robot> robotList = null;
			if("all".equals(floorName)) { // 加载商场所有机器人
				robotList = robotDao.getRobotsList();
			} else {
				robotList = robotDao.getRobotListByFloorName(floorName);
			}			
			JSONArray jsonArr = new JSONArray();
			
			if(robotList.size() > 0) {
				for(int i = 0; i < robotList.size(); i++) {
					JSONObject obj = new JSONObject();
					
					Robot robot = robotList.get(i);
					obj.put("orderId", i+1);
					obj.put("robotName", robot.getName());
					obj.put("machineId", robot.getMachineId());
					obj.put("startTime", robot.getStartLoopTimeStr());
					obj.put("endTime", robot.getStopLoopTimeStr());
					
					obj.put("pathId", robot.getPathId()); // 添加机器人所绑定的路线编号
					PathListData path = robotDao.getPathById(robot.getPathId()); // 获取发送到客户端的路线的名字
					if(path != null) {
						obj.put("pathName", path.getName());
						obj.put("floor", robot.getFloor()); // 查看所绑定路径的时候需要指定楼层以及路径id值
					}
					obj.put("talkId", robot.getTalkId()); // 设置机器人绑定的对话名称
					RobotTalkGroup talkGroup = robotDao.getRobotTalkGroupById(robot.getTalkId());
					if(talkGroup != null) {
						obj.put("talkGroupName", talkGroup.getName());
					}
					obj.put("adId", robot.getAdId());
					AdTemplate adTemplate = adTemplateDao.getAdTemplateById(robot.getAdId());
					if(adTemplate != null) {
						obj.put("adTemplateName", adTemplate.getAdTemplateName());
					}
					
					// 获取机器人上装载的商品名字列表
					List<OneContainerFloor> floorList = robot.getContainerFloors();
					floorList.removeAll(Collections.singleton(null)); // 去除List中为空的元素
					String goodsListStr = "";
					for(OneContainerFloor floor : floorList) {
						Goods goods = goodsService.getGoodsById(floor.getGoodsSerialId());// 使用商品的主键获取商品对象
						if(goods != null) {						
							goodsListStr += goods.getName() + "；";
						}							
					}
					obj.put("goodsListStr", goodsListStr);
					
					obj.put("batteryPercent", robot.getBatteryPercent()); // 电池电量
					if(robot.isRobotOutOfStore()) { // 机器人货柜状态，是否缺货
						obj.put("isRobotOutOfStore", "货柜缺货");
					} else {
						obj.put("isRobotOutOfStore", "货柜正常");
					}
					
					// 查询当前机器人是否进行了socket连接
					if(ServerSocketThreadRobot.robotMachineMap.get(robot.getMachineId()) != null) {
						obj.put("status", "运行中"); // 此处的在线状态不同于创建机器人时设置的状态，创建机器人设置的状态是机器人是否进行使用
					} else {
						obj.put("status", "离线");
					}
					jsonArr.put(obj);
				}
				JSONObject retJSONObj = new JSONObject();
				retJSONObj.put("code", 0);
				retJSONObj.put("message", "操作成功");
				retJSONObj.put("robotDayRunTableList", jsonArr);
				return retJSONObj.toString();	
			} else {
				retObj.setMessage("该楼层还没有创建机器人，请先创建机器人");
			}
		}
		return new JSONObject(retObj).toString();
	}
	
	/**
	 * 按照客户端select要求的格式获取对话组名称数据列表
	 * */	
	@RequestMapping(value="/getTalkGroupNamesList")
	@ResponseBody
	public String getSelectTalkGroupList(@RequestBody String body){
		ReturnObject retObj = new ReturnObject();
		
		if(body != null && body != ""){
			List<RobotTalkGroup> list = robotDao.getRobotTalkGroup(null);
			JSONArray jsonArr = new JSONArray();
			for(RobotTalkGroup item : list) {
				JSONObject obj = new JSONObject();
				obj.put("groupId", item.getId());
				obj.put("name", item.getName());
				jsonArr.put(obj);
			}
			JSONObject retJSONObj = new JSONObject();
			retJSONObj.put("talkGroupList", jsonArr);
			retJSONObj.put("code", 0);
			retJSONObj.put("message", "查询成功");
			return retJSONObj.toString();			
		}
		
		return new JSONObject(retObj).toString();
	}
	/**
	 * 获取路径名称数据列表，根据机器人编号查找所在楼层
	 * */
	@RequestMapping(value="/getPathNamesList")
	@ResponseBody
	public String getPathNamesList(@RequestBody String body){
		ReturnObject retObj = new ReturnObject();
		
		if(body != null && body != ""){
			JSONObject jsonObj = new JSONObject(body);
			String machineId = jsonObj.getString("machineId");
			Robot robot = robotDao.getRobotByMachineId(machineId);
			
			String floorName = robot.getFloor();
			List<PathListData> list = robotDao.getPathListsByFloorName(floorName); // 获取已经创建的路径列表，传递到客户端刷新数据
			JSONArray jsonArr = new JSONArray();
			for(PathListData item : list) {
				JSONObject obj = new JSONObject();
				obj.put("pathId", item.getId());
				obj.put("name", item.getName());
				jsonArr.put(obj);
			}
			
			JSONObject retJSONObj = new JSONObject();
			retJSONObj.put("pathList", jsonArr);
			retJSONObj.put("code", 0);
			retJSONObj.put("message", "查询成功");
			return retJSONObj.toString();			
		}
		
		return new JSONObject(retObj).toString();
	}
	
	/**
	 * 获取广告模板名称列表
	 * */
	
	/**
	 * 根据机器人编号获取机器人的详情展示页面的信息
	 * */
	@RequestMapping(value="/getRobotDetailInfoByMachineId")
	@ResponseBody
	public String getRobotDetailInfoByMachineId(@RequestBody String body){
		ReturnObject retObj = new ReturnObject();
		
		if(body != null && body != ""){
			JSONObject jsonObj = new JSONObject(body);
			String machineId = jsonObj.getString("machineId");
			Robot robot = robotDao.getRobotByMachineId(machineId);
			if(robot != null) {
				JSONObject obj = new JSONObject();
				obj.put("machineId", robot.getMachineId());
				obj.put("name", robot.getName());
				obj.put("floorName", robot.getFloor());
				obj.put("isRobotOutOfStore", robot.isRobotOutOfStore());
				obj.put("batteryPercent", robot.getBatteryPercent());
				obj.put("talkId", robot.getTalkId());
				obj.put("pathId", robot.getPathId());
				obj.put("adId", robot.getAdId());
				
				List<OneContainerFloor> floorList = robot.getContainerFloors();					
				floorList.removeAll(Collections.singleton(null)); // 去除List中为空的元素
				JSONArray goodsArr = new JSONArray();
				int index = 1;
				for(OneContainerFloor floor : floorList) { // 加载机器人货架商品列表
					Goods goods = goodsService.getGoodsById(floor.getGoodsSerialId());
					if(goods != null) {
						JSONObject jsonO = new JSONObject();
						jsonO.put("orderId", index++);
						jsonO.put("containerFloorId", floor.getId());
						jsonO.put("containerFloorName", floor.getFloorName());
						jsonO.put("goodsName", goods.getName());
						jsonO.put("goodsPicUrl", goods.getFileurl());
						jsonO.put("goodsPrice", goods.getPrice());
						jsonO.put("totalCount", floor.getTotalCount());		
						jsonO.put("currentCount", floor.getCurrentCount());												
						goodsArr.put(jsonO);
					}
				}				
				obj.put("goodsList", goodsArr);
				
				// 获取机器人的一些销售统计信息
				float totalRunPathCount = 0;
				int totalServePeopleCount = 0, totalSaleCount = 0, totalSaleMoney = 0, totalSaleCoin = 0;  
				List<Orders> orderList = goodsService.getAllOrdersByMachineId(machineId);
				totalServePeopleCount = orderList.size();
				for(Orders order : orderList) {
					totalSaleMoney += order.getTotalFee(); // 统计所有的订单金额，以分为单位
					totalSaleCoin += order.getUsedIntegral(); //统计所有的金币数
					List<OrderItem> orderItem = order.getOrderItemList();
					orderItem.removeAll(Collections.singleton(null));
					for(OrderItem item : orderItem) {
						totalSaleCount += item.getBuyCount(); // 统计总的销售数量
					}
				}
				JSONObject robotPanelObj = new JSONObject();
				robotPanelObj.put("totalRunPathCount", totalRunPathCount);
				robotPanelObj.put("totalServePeopleCount", totalServePeopleCount);
				robotPanelObj.put("totalSaleCount", totalSaleCount);
				robotPanelObj.put("totalSaleMoney", totalSaleMoney/100.0);
				robotPanelObj.put("totalSaleCoin", totalSaleCoin);				
				
				JSONObject retJSONObj = new JSONObject();
				retJSONObj.put("infoObj", obj);
				retJSONObj.put("robotPanelObj", robotPanelObj);
				retJSONObj.put("code", 0);
				retJSONObj.put("message", "查询成功");
				return retJSONObj.toString();		
			}	
		}
		
		return new JSONObject(retObj).toString();
	}
	
	/**
	 * 获取指定编号机器人动态获取货柜层数列表
	 * */	
	@RequestMapping(value="/getTidyRobotGoodsData")
	@ResponseBody
	public String getTidyRobotGoodsData(@RequestBody String body){
		ReturnObject retObj = new ReturnObject();
		
		if(body != null && body != ""){
			JSONObject jsonObj = new JSONObject(body);
			String machineId = jsonObj.getString("machineId");
			Robot robot = robotDao.getRobotByMachineId(machineId);
			if(robot != null) {				
				List<OneContainerFloor> floorList = robot.getContainerFloors();					
				floorList.removeAll(Collections.singleton(null)); // 去除List中为空的元素
				JSONArray floorArr = new JSONArray();
				for(OneContainerFloor floor : floorList) {
					JSONObject object = new JSONObject();
					object.put("value", floor.getId());
					if("goodsFloor1".equals(floor.getFloorName())) {
						object.put("label", "一层货柜");
					} else if("goodsFloor2".equals(floor.getFloorName())) {
						object.put("label", "二层货柜");
					} else if("goodsFloor3".equals(floor.getFloorName())) {
						object.put("label", "三层货柜");
					} else if("goodsFloor4".equals(floor.getFloorName())) {
						object.put("label", "四层货柜");
					}
					floorArr.put(object);
				}		
				
				JSONObject retJSONObj = new JSONObject();
				retJSONObj.put("containerFloorOptions", floorArr);
				retJSONObj.put("code", 0);
				retJSONObj.put("message", "查询成功");
				return retJSONObj.toString();		
			}	
		}
		
		return new JSONObject(retObj).toString();
	}
	
	/**
	 * 根据机器人编号以及指定的货柜层获取当前层所加载的商品信息
	 * */
	@RequestMapping(value="/getRobotGoodsInfoByContainerFloorId")
	@ResponseBody
	public String getRobotGoodsInfoByContainerFloorId(@RequestBody String body){
		ReturnObject retObj = new ReturnObject();
		
		if(body != null && body != ""){
			JSONObject jsonObj = new JSONObject(body);
			String machineId = jsonObj.getString("machineId");
			int selectContainerFloorId = jsonObj.getInt("selectContainerFloorId"); // 查询货柜层的id
			
			Robot robot = robotDao.getRobotByMachineId(machineId);
			JSONObject retJSONObj = new JSONObject();
			if(robot != null) {				
				List<OneContainerFloor> floorList = robot.getContainerFloors();					
				floorList.removeAll(Collections.singleton(null)); // 去除List中为空的元素
				for(OneContainerFloor floor : floorList) {
					if(selectContainerFloorId == floor.getId()) { // 如果找到指定的层
						String goodsId = floor.getGoodsSerialId();						
						retJSONObj.put("selectGoodsId", goodsId);
						Goods goods = goodsService.getGoodsById(goodsId);
						if(goods != null) {
							retJSONObj.put("imageUrl", goods.getFileurl());
							retJSONObj.put("goodsPrice", goods.getPrice());
							retJSONObj.put("totalLeftCount", goods.getRepertory());
						}
						retJSONObj.put("totalCount", floor.getTotalCount());						
						retJSONObj.put("currentLeftCount", floor.getCurrentCount());						
					}
				}										
				retJSONObj.put("code", 0);
				retJSONObj.put("message", "查询成功");
				return retJSONObj.toString();		
			}	
		}
		
		return new JSONObject(retObj).toString();
	}
	
	/**
	 * 修改机器人货柜中的商品，每次只能修改一个货架的商品
	 * */
	@RequestMapping(value="/updateContainerGoodsData")
	@ResponseBody
	public String updateContainerGoodsData(@RequestBody String body){
		ReturnObject retObj = new ReturnObject();
		
		if(body != null && body != ""){
			JSONObject jsonObj = new JSONObject(body);
			String machineId = jsonObj.getString("machineId");
			int selectContainerFloorId = jsonObj.getInt("selectContainerFloorId"); // 查询货柜层的id
			String selectGoodsId = jsonObj.getString("selectGoodsId"); // 商品id
			int currentLeftCount = jsonObj.getInt("currentLeftCount"); // 修改商品之后初始上架商品数量
			int totalCount = jsonObj.getInt("totalCount"); // 每层货架商品满载的总数
			
			Robot robot = robotDao.getRobotByMachineId(machineId);
			if(robot != null) {				
				List<OneContainerFloor> floorList = robot.getContainerFloors();					
				floorList.removeAll(Collections.singleton(null)); // 去除List中为空的元素
				for(OneContainerFloor floor : floorList) {
					if(selectContainerFloorId == floor.getId()) { // 如果找到指定的层
						floor.setGoodsSerialId(selectGoodsId);
						floor.setCurrentCount(currentLeftCount);
						floor.setTotalCount(totalCount);
					}
				}
				robot.setContainerFloors(floorList);
				robotDao.updateRobotInfo(robot); // 更新货架信息
				
				retObj.setCode(0);
				retObj.setMessage("修改货架商品成功");	
			}	
		}
		
		return new JSONObject(retObj).toString();
	}
	
	/**
	 * 机器人对指定的层进行补货操作
	 * */	
	@RequestMapping(value="/updateRobotContainerRepertory")
	@ResponseBody
	public String updateRobotContainerRepertory(@RequestBody String body){
		ReturnObject retObj = new ReturnObject();
		
		if(body != null && body != ""){
			JSONObject jsonObj = new JSONObject(body);
			
			String token = jsonObj.getString("token");			
			
			String machineId = jsonObj.getString("machineId");
			int containerFloorId = jsonObj.getInt("containerFloorId"); // 查询货柜层的id
			int goodsAddCount = jsonObj.getInt("goodsAddCount"); // 本次补货增加量
			
			Robot robot = robotDao.getRobotByMachineId(machineId);
			if(robot != null) {				
				List<OneContainerFloor> floorList = robot.getContainerFloors();					
				floorList.removeAll(Collections.singleton(null)); // 去除List中为空的元素
				for(OneContainerFloor floor : floorList) {
					if(containerFloorId == floor.getId()) { // 如果找到指定的层
						int beforeCount = floor.getCurrentCount();
						floor.setCurrentCount(beforeCount + goodsAddCount);
						
						//增加机器人货柜补货日志
						RobotReplenishLog replenishLog = new RobotReplenishLog();
						replenishLog.setBeforeReplenishCount(beforeCount);
						replenishLog.setCurrentReplenishCount(goodsAddCount);
						replenishLog.setAfterReplenishCount(beforeCount+goodsAddCount);
						if("goodsFloor1".equals(floor.getFloorName())) {
							replenishLog.setFloorName("一层");
						} else if("goodsFloor2".equals(floor.getFloorName())) {
							replenishLog.setFloorName("二层");
						} else if("goodsFloor3".equals(floor.getFloorName())) {
							replenishLog.setFloorName("三层");
						}
						replenishLog.setDate(new Date());						
						Goods goods = goodsService.getGoodsById(floor.getGoodsSerialId());
						if(goods != null) {
							replenishLog.setGoodsName(goods.getName());
							replenishLog.setGoodsPicUrl(goods.getFileurl());
						}
						User user = userService.getUserByUserToken(token); // 获取操作人姓名
						if (user != null) {
							replenishLog.setReplenishUserName(user.getUsername());
						}						
						replenishLog.setRobotName(robot.getName());
						baseLogService.saveLog(replenishLog); // 保存日志
					}
				}
				robot.setContainerFloors(floorList);
				robotDao.updateRobotInfo(robot); // 更新货架信息				
				
				retObj.setCode(0);
				retObj.setMessage("货架补货成功");	
			}	
		}
		
		return new JSONObject(retObj).toString();
	}
	
	/**
	 * 更新机器人绑定的对话以及路径等信息
	 * */
	@RequestMapping(value="/updateRobotRetailBindInfo")
	@ResponseBody
	public String updateRobotRetailBindInfo(@RequestBody String body){
		ReturnObject retObj = new ReturnObject();
		
		if(body != null && body != ""){
			JSONObject jsonObj = new JSONObject(body);
			String machineId = jsonObj.getString("machineId");			
			
			Robot robot = robotDao.getRobotByMachineId(machineId);
			JSONObject retJSONObj = new JSONObject();
			if(robot != null) {
				int talkId = 0, pathId = 0, adId = 0;
				if(jsonObj.get("talkId") != JSONObject.NULL) { // 设置了对话组
					talkId = jsonObj.getInt("talkId");
					robot.setTalkId(talkId);
				}
				if(jsonObj.get("pathId") != JSONObject.NULL) {
					pathId = jsonObj.getInt("pathId");
					robot.setPathId(pathId);
				}
				if(jsonObj.get("adId") != JSONObject.NULL) {
					adId = jsonObj.getInt("adId");
					robot.setAdId(adId);
				}
				robotDao.updateRobotInfo(robot);
												
				retJSONObj.put("code", 0);
				retJSONObj.put("message", "设置成功");
				return retJSONObj.toString();		
			}	
		}
		
		return new JSONObject(retObj).toString();
	}
}
