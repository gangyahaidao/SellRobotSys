package com.qingpu.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.qingpu.common.entity.ReturnObject;
import com.qingpu.common.service.ActionConstants;
import com.qingpu.common.service.BaseLogService;
import com.qingpu.goods.entity.Goods;
import com.qingpu.goods.entity.GoodsManageLog;
import com.qingpu.goods.entity.GoodsReplenishLog;
import com.qingpu.goods.entity.VIP;
import com.qingpu.goods.entity.Vendor;
import com.qingpu.goods.service.GoodsService;
import com.qingpu.goods.service.VendorService;
import com.qingpu.user.entity.User;
import com.qingpu.user.service.UserService;

/**
 * @Desc   
 * @author Gangyahaidao
 */
@Controller
@RequestMapping("/goods")
public class GoodsController extends HandlerInterceptorAdapter {

	@Resource
	GoodsService goodsService;	
	@Resource
	VendorService vendorService;	
	@Resource
	BaseLogService<Object> baseLogService; // 进行日志对象的保存
	@Resource 
	UserService userService;
	
	/**
	 * 在商品显示列表页面，按条件查询商品列表
	 * */
	@RequestMapping(value="/list") // ", method = RequestMethod.GET" 指定改请求为Get方式，默认get或者post方式都可以请求
	@ResponseBody
	public String getGoodsList(@RequestBody String body){
		ReturnObject retObj = new ReturnObject();
		
		if(body != null && body != ""){
			JSONObject jsonObj = new JSONObject(body);
			int page = jsonObj.getInt("page");
			int pageLimit = jsonObj.getInt("limit");
			String sortType = jsonObj.getString("sort");
			String name = null, type = null;
			if(jsonObj.has("name")){
				name = jsonObj.getString("name"); // 按照名字加载
				if(name.length() == 0){
					name = null;
				}
			}
			if(jsonObj.has("type")){
				type = jsonObj.getString("type"); // 按照类型加载商品
				if(type.length() == 0){
					type = null;
				}				
			}						
			List<Goods> goodsList = goodsService.getGoodsList(page, pageLimit, sortType, name, type);
			List<List<VIP>> listVips = new ArrayList<List<VIP>>();
			int goodsId = 1;
			for(Goods goods : goodsList) {
				goods.setGoodsId(goodsId++);
				List<VIP> item = goods.getVipList(); // 提取出会员vip的信息列表
				item.removeAll(Collections.singleton(null)); // 去除List中为空的元素
				for(VIP vip : item) {
					if(vip != null){
						vip.setGoods(null);						
					}
				}				
				listVips.add(item);
				goods.setVipList(null); // 去除嵌套的vipList对象
				
				int id = goods.getVendorId(); // 根据供应商的id去获取供应商名字
				Vendor vendor = vendorService.getVendorById(id);
				if(vendor != null) {
					goods.setVendorName(vendor.getName());
				}
			}
						
			JSONArray vipArray = new JSONArray(listVips);
			JSONArray jsonArray = new JSONArray(goodsList);
			
			JSONObject obj = new JSONObject();
			obj.put("vipitems", vipArray);
			obj.put("items", jsonArray);
			obj.put("total", goodsList.size()); // 列表数据条数
			obj.put("code", 0);
			obj.put("message", "success");
			
			return obj.toString();
		}
		
		return new JSONObject(retObj).toString();
	}
	
	/**
	 * 加载在显示机器人上架商品列表的时候显示，用于修改机器人上正在售卖的商品，要过滤值加载上架的可选商品
	 * */
	@RequestMapping(value="/listAll")
	@ResponseBody
	public String getGoodsListAll(@RequestBody String body) {
		ReturnObject retObj = new ReturnObject();
		
		if(body != null && body != "") {
			JSONObject jsonObj = new JSONObject(body);
			if(jsonObj.has("token")){
				List<Goods> goodsList = goodsService.getAllOnlineGoods();
				JSONArray jsonArr = new JSONArray();
				for(Goods goods: goodsList){
					JSONObject obj = new JSONObject();
					obj.put("id", goods.getId());
					obj.put("name", goods.getName());
					obj.put("imageUrl", goods.getFileurl());
					obj.put("goodsPrice", goods.getPrice());
					jsonArr.put(obj);
				}
				JSONObject retJSONObj = new JSONObject();
				retJSONObj.put("items", jsonArr);
				retJSONObj.put("code", 0);
				retJSONObj.put("message", "success");
				
				return retJSONObj.toString();
			}
		}
		
		return new JSONObject(retObj).toString();
	}
	
	/**
	 * 添加商品
	 * */
	@RequestMapping(value="/create")
	@ResponseBody
	public String createGoods(@RequestBody String body){
		ReturnObject retObj = new ReturnObject();
		
		if(body != null && body != ""){			
			JSONObject jsonObj = new JSONObject(body);
			Goods newGoods = new Goods();
			newGoods.setName(jsonObj.getString("name"));
			newGoods.setIntroduction(jsonObj.getString("introduction"));
			newGoods.setPrice(jsonObj.getDouble("price"));		
			newGoods.setStatus(jsonObj.getString("status"));
			newGoods.setType(jsonObj.getString("type"));
			newGoods.setGoodsPopularity(jsonObj.getInt("goodsPopularity"));
			newGoods.setGoodsSales(0); // 销量
			newGoods.setAddUserName(jsonObj.getString("addUserName"));			
			newGoods.setFileurl(jsonObj.getString("fileurl"));
			newGoods.setRepertory(jsonObj.getInt("repertory")); // 商品仓库余量
			newGoods.setVendorId(jsonObj.getInt("vendorId"));
			
			List<VIP> vipList = new ArrayList<VIP>();
			JSONArray jsonArray = jsonObj.getJSONArray("vipList");
			for(int i = 0; i < jsonArray.length(); i++) {
				JSONObject obj = jsonArray.getJSONObject(i);
				VIP newVip = new VIP();
				newVip.setEdit(false);
				newVip.setGoods(newGoods); //设置vip数据对应的商品
				newVip.setIntegral(obj.getInt("integral"));
				newVip.setOriginalIntegralValue(obj.getInt("originalIntegralValue"));
				newVip.setOriginalVipValue(obj.getDouble("originalVipValue"));
				newVip.setVipName(obj.getString("vipName"));
				newVip.setVipValue(obj.getDouble("vipValue"));
				vipList.add(newVip);
			}			
			newGoods.setVipList(vipList);
			
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd"); //将前端页面传递过来的时间转换成Date
			try {
				newGoods.setDeadDate(simpleDateFormat.parse(jsonObj.getString("deadDate")));
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
			}
			goodsService.addGoods(newGoods);
			retObj.setCode(0); // 更新返回的状态值
			retObj.setMessage("success");
			
			GoodsManageLog log = new GoodsManageLog(); // 添加日志
			log.setActionName(ActionConstants.Goods_Create);
			log.setActionDescription(ActionConstants.Goods_Create_Desc);
			log.setDate(new Date());
			log.setUserName(jsonObj.getString("addUserName"));
			baseLogService.saveLog(log);
		}
		
		return new JSONObject(retObj).toString();
	}
	
	/**
	 * 更新一个商品对象
	 * */
	@SuppressWarnings("unchecked")
	@RequestMapping(value="/updateRowData")
	@ResponseBody
	public String updateRowData(@RequestBody String body){
		ReturnObject retObj = new ReturnObject();
		
		if(body != null && body != ""){
			JSONObject jsonObj = new JSONObject(body);
			String goodsSerialId = jsonObj.getString("id");
			Goods goods = goodsService.getGoodsById(goodsSerialId);
			if(goods != null){
				goods.setName(jsonObj.getString("name"));
				goods.setIntroduction(jsonObj.getString("introduction"));
				goods.setPrice(jsonObj.getDouble("price"));		
				goods.setType(jsonObj.getString("type"));
				goods.setStatus(jsonObj.getString("status")); // 更新商品上架状态
				goods.setFileurl(jsonObj.getString("fileurl"));
				goods.setRepertory(jsonObj.getInt("repertory")); // 商品仓库余量
				goods.setVendorId(jsonObj.getInt("vendorId"));
				
				List<VIP> vipList = goods.getVipList();
				vipList.removeAll(Collections.singleton(null)); // 去除List中为空的元素
				if (jsonObj.has("vipList")) {
					JSONArray jsonArray = jsonObj.getJSONArray("vipList");
					for(int i = 0; i < jsonArray.length(); i++) {
						JSONObject obj = jsonArray.getJSONObject(i);
						VIP newVip = vipList.get(i);
						newVip.setEdit(false);
						newVip.setIntegral(obj.getInt("integral"));
						newVip.setOriginalIntegralValue(obj.getInt("originalIntegralValue"));
						newVip.setOriginalVipValue(obj.getDouble("originalVipValue"));
						newVip.setVipName(obj.getString("vipName"));
						newVip.setVipValue(obj.getDouble("vipValue"));
					}			
					goods.setVipList(vipList);
				}				
				
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd"); //将前端页面传递过来的时间转换成Date
				try {
					goods.setDeadDate(simpleDateFormat.parse(jsonObj.getString("deadDate")));
				} catch (JSONException e) {
					e.printStackTrace();
				} catch (ParseException e) {
					e.printStackTrace();
				}
				
				goodsService.updateGoodsInfo(goods);
				retObj.setCode(0);
				retObj.setMessage("success");
				
				GoodsManageLog log = new GoodsManageLog(); // 添加日志
				log.setActionName(ActionConstants.Goods_Edit);
				log.setActionDescription(ActionConstants.Goods_Edit_Desc);
				log.setUserName(jsonObj.getString("addUserName")); // 获取操作者名字
				log.setDate(new Date());
				// log.setUserName(jsonObj.getString("addUserName"));
				baseLogService.saveLog(log);
			}					
		}
		
		return new JSONObject(retObj).toString();
	}
	
	/**
	 * 增加指定商品id的库存，进行补货操作
	 * */
	@RequestMapping(value="/updateRepertory")
	@ResponseBody
	public String updateRepertory(@RequestBody String body) {
		ReturnObject retObj = new ReturnObject();
		
		if(body != null && body != "") {
			JSONObject jsonObj = new JSONObject(body);
			
			String goodsId = jsonObj.getString("addGoodsId");
			int goodsAddCount = jsonObj.getInt("goodsAddCount");
			String token = jsonObj.getString("token");
			
			Goods goods = goodsService.getGoodsById(goodsId);
			if(goods != null) {
				GoodsReplenishLog replenishLog = new GoodsReplenishLog();
				replenishLog.setBeforeReplenishCount(goods.getRepertory()); // 补货前库存
				replenishLog.setCurrentReplenishCount(goodsAddCount);
				replenishLog.setAfterReplenishCount(goods.getRepertory() + goodsAddCount);
				replenishLog.setDate(new Date());
				replenishLog.setGoodsType(goods.getType());
				replenishLog.setGoodsName(goods.getName());
				replenishLog.setGoodsPicUrl(goods.getFileurl());
				User user = userService.getUserByUserToken(token); // 设置补货人名字
				if(user != null) {
					replenishLog.setReplenishUserName(user.getUsername());
				}
				
				goods.setRepertory(goodsAddCount + goods.getRepertory());
				goodsService.updateGoodsInfo(goods); // 更新库存信息
				baseLogService.saveLog(replenishLog); // 增加补货日志
				
				retObj.setCode(0);
				retObj.setMessage("增加库存成功");
			} else {
				retObj.setMessage("商品不存在");
			}									
		}		
		return new JSONObject(retObj).toString();
	}	
}
