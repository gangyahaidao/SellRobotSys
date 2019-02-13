package com.qingpu.controller;

import java.util.Date;

import javax.annotation.Resource;

import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.qingpu.common.entity.ReturnObject;
import com.qingpu.common.service.ActionConstants;
import com.qingpu.common.service.BaseLogService;
import com.qingpu.user.entity.User;
import com.qingpu.user.entity.UserLog;
import com.qingpu.user.service.UserService;

/**
 * @Desc   
 * @author Gangyahaidao
 */
@Controller
@RequestMapping("/login")
public class UserLoginController extends HandlerInterceptorAdapter {

	@Resource
	UserService userService;
	
	@Resource
	private BaseLogService<Object> baseLogService;
	
	/**
	 * 用户使用用户名密码登录
	 * */
	@RequestMapping("/login")
	@ResponseBody
	public String userLogin(@RequestBody String body) // 添加required=false，不然在请求数据体为空的时候会报异常
	{
		JSONObject retJSON = new JSONObject();
		retJSON.put("code", -1);
		retJSON.put("message", "failed");
		
		if(body != null && body != ""){ // 判断用户是否发起的空请求			
			JSONObject jsonObject = new JSONObject(body);
			if(jsonObject.has("username") && jsonObject.has("password"))
			{
				String username = jsonObject.getString("username");
				String password = jsonObject.getString("password");			
				User user = userService.getUserByUsername(username);	
				if(user != null)
				{
					if(password.length() > 0 && password.equals(user.getPassword()))
					{//如果用户名密码相等
						retJSON.put("code", 0);		
						retJSON.put("message", "success");
						retJSON.put("token", user.getToken());
											
						user.setLoginStatus("online");//将用户状态设置为在线模式
						userService.updateUser(user);
						
						UserLog userLog = new UserLog(); // 登录日志记录
						userLog.setUserAction(ActionConstants.User_Login);
						userLog.setUserActionDescription(ActionConstants.User_Login_Desc);
						userLog.setDate(new Date());
						userLog.setUserId(user.getId());
						userLog.setUserName(user.getUsername());
						baseLogService.saveLog(userLog);
					}else{
						retJSON.put("message", "密码错误");
					}
				}else{
					retJSON.put("message", "用户不存在");
				}			
			}
		}		
		
		return retJSON.toString();
	}
	
	/**
	 * 用户点击退出登录，设置用户登录状态为退出状态 'offline'
	 * */
	@RequestMapping("/logout")
	@ResponseBody
	public String userLoginOut(@RequestBody String body)
	{
		ReturnObject retObj = new ReturnObject();
		
		if(body != null && body != ""){
			JSONObject jsonObject = new JSONObject(body);
			if(jsonObject.has("token")){
				User user = userService.getUserByUserToken(jsonObject.getString("token"));
				if(user != null){
					user.setLoginStatus("offline");
					userService.updateUser(user); //设置用户状态为离线
					retObj.setCode(0);
					retObj.setMessage("success");
					
					UserLog userLog = new UserLog(); // 退出日志记录
					userLog.setUserAction(ActionConstants.User_LoginOut);
					userLog.setUserActionDescription(ActionConstants.User_LoginOut_Desc);
					userLog.setDate(new Date());
					userLog.setUserId(user.getId());
					userLog.setUserName(user.getUsername());
					baseLogService.saveLog(userLog);
				}
			}
		}			
		
		return new JSONObject(retObj).toString();
	}
}
