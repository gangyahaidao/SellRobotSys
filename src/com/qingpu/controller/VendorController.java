package com.qingpu.controller;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.qingpu.common.entity.ReturnObject;
import com.qingpu.common.service.ActionConstants;
import com.qingpu.common.service.BaseLogService;
import com.qingpu.goods.entity.Vendor;
import com.qingpu.goods.entity.VendorLog;
import com.qingpu.goods.service.VendorService;
import com.qingpu.user.entity.User;
import com.qingpu.user.service.UserService;

/**
 * 供货商管理相关接口
 * */
@Controller
@RequestMapping("/vendor")
public class VendorController extends HandlerInterceptorAdapter {
	
	@Resource
	VendorService vendorService;
	@Resource
	UserService userService;
	@Resource
	BaseLogService<Object> baseLogService; // 进行日志对象的保存
	
	/**
	 * 获取商户列表
	 * */
	@RequestMapping(value="/list") // ", method = RequestMethod.GET" 指定改请求为Get方式，默认get或者post方式都可以请求
	@ResponseBody
	public String getVendorList(@RequestBody String body){
		ReturnObject retObj = new ReturnObject();
		
		if(body != null && body != ""){
			JSONObject jsonObj = new JSONObject(body);
			User user = userService.getUserByUserToken(jsonObj.getString("token"));
			if(user != null){ // 如果用于查询的token存在则说明此次查询是合法的
				List<Vendor> list = vendorService.getVendorList();
				JSONArray jsonArray = new JSONArray(list);
				JSONObject obj = new JSONObject();
				obj.put("items", jsonArray);
				obj.put("total", list.size()); // 列表数据条数
				obj.put("code", 0);
				obj.put("message", "success");
				
				return obj.toString();
			}
		}
		
		return new JSONObject(retObj).toString();
	}
	
	/**
	 * 添加供货商
	 * */
	@RequestMapping(value="/add")
	@ResponseBody
	public String addVendor(@RequestBody String body){
		ReturnObject retObj = new ReturnObject();
		
		if(body != null && body != ""){			
			JSONObject jsonObj = new JSONObject(body);
			Vendor vendor = new Vendor();
			vendor.setVendorId(jsonObj.getInt("vendorId"));
			vendor.setName(jsonObj.getString("name"));
			vendor.setLinkname(jsonObj.getString("linkname"));
			vendor.setLinknum(jsonObj.getString("linknum"));
			vendor.setIntroduction(jsonObj.getString("introduction"));
			String userName = jsonObj.getString("userName");
			
			vendorService.addVendor(vendor);
			retObj.setCode(0);
			retObj.setMessage("success");
			
			VendorLog log = new VendorLog();
			log.setActionName(ActionConstants.Vendor_Create);
			log.setActionDescription(ActionConstants.Vendor_Create_Desc);
			log.setDate(new Date());
			log.setUserName(userName); // 操作人的名字需要页面传递过来
			baseLogService.saveLog(log);
		}
		
		return new JSONObject(retObj).toString();
	}
	
	/**
	 * 更新信息
	 * */
	@RequestMapping(value="/updateVendor")
	@ResponseBody
	public String updateRowData(@RequestBody String body){
		ReturnObject retObj = new ReturnObject();
		
		if(body != null && body != ""){
			JSONObject jsonObj = new JSONObject(body);
			int vendorId = jsonObj.getInt("vendorId");
			Vendor vendor = vendorService.getVendorByVendorId(vendorId);
			if(vendor != null) {
				vendor.setName(jsonObj.getString("name"));
				vendor.setLinkname(jsonObj.getString("linkname"));
				vendor.setLinknum(jsonObj.getString("linknum"));
				vendor.setIntroduction(jsonObj.getString("introduction"));
				vendorService.updateVendorInfo(vendor);
				
				retObj.setCode(0);
				retObj.setMessage("succuss");
				
				VendorLog log = new VendorLog();
				log.setActionName(ActionConstants.Vendor_Edit);
				log.setActionDescription(ActionConstants.Vendor_Edit_Desc);
				log.setDate(new Date());
				log.setUserName(jsonObj.getString("userName"));
				baseLogService.saveLog(log);
			}
		}
		
		return new JSONObject(retObj).toString();
	}
	
	/**
	 * 删除供货商
	 * */
	@RequestMapping(value="/deleteVendor")
	@ResponseBody
	public String deleteVendor(@RequestBody String body){
		ReturnObject retObj = new ReturnObject();
		
		if(body != null && body != ""){
			JSONObject jsonObj = new JSONObject(body);
			int vendorId = jsonObj.getInt("vendorId");
			Vendor vendor = vendorService.getVendorByVendorId(vendorId);
			if(vendor != null) {				
				vendorService.deleteVendor(vendor);
				
				retObj.setCode(0);
				retObj.setMessage("succuss");
			}
		}
		
		return new JSONObject(retObj).toString();
	}
}
