package com.qingpu.controller;

import java.util.ArrayList;
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
import com.qingpu.common.utils.UUIDGenerator;
import com.qingpu.user.entity.User;
import com.qingpu.user.entity.UserLog;
import com.qingpu.user.service.UserService;

@Controller
@RequestMapping("/user")
public class UserInfoController extends HandlerInterceptorAdapter {

	@Resource
	UserService userService;
	
	/**
	 * 添加用户
	 * */
	@RequestMapping("/register")
	@ResponseBody
	public String addUser(@RequestBody String body) {
		ReturnObject retObj = new ReturnObject();
		
		if(body != null && body != ""){ // 判断用户是否发起的空请求			
			JSONObject jsonObject = new JSONObject(body);
			
			String username = jsonObject.getString("username");
			User user = userService.getUserByUsername(username);
			if (user == null) {
				User newUser = new User();
				newUser.setAvatar("https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif");
				newUser.setUsername(username);
				newUser.setPassword(jsonObject.getString("password"));
				newUser.setToken(UUIDGenerator.getUUID());
				
				List<String> roleList = new ArrayList<String>();
				if("admin".equals(username)) {
					roleList.add("admin");
				} else {
					roleList.add("editor");			
				}
				newUser.setRoles(roleList);
				userService.addUser(newUser);
				retObj.setCode(0);
				retObj.setMessage("注册成功，请登录");
			} else {
				retObj.setMessage("注册失败，用户已经被注册");
			}				
		}
		
		return new JSONObject(retObj).toString();
	}	
	
	/**
	 * 使用token获取用户信息
	 * */
	@RequestMapping("/getUserInfo")
	@ResponseBody
	public String getUserInfo(HttpServletRequest request)
	{
		JSONObject retJSON = new JSONObject();
		retJSON.put("code", -1);
		retJSON.put("message", "failed");
		
		String token = request.getParameter("token"); // 获取get方式请求的参数
		if(token != null && token != ""){
			User user = userService.getUserByUserToken(token);
			if(user != null){
				retJSON.put("code", 0);
				retJSON.put("message", "success");	
				
				retJSON.put("roles", user.getRoles());
				retJSON.put("name", user.getUsername());
				retJSON.put("avatar", user.getAvatar());
				retJSON.put("introduction", user.getIntroduction());
			}else{
				retJSON.put("message", "用户token错误");	
			}
		}		
		
		return retJSON.toString();
	}		
}
