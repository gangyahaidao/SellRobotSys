package asr.qingpu.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

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

import asr.qingpu.user.dao.AsrUserDao;
import asr.qingpu.user.entity.AsrUser;

@Controller
@RequestMapping("/asrUser")
public class AsrUserController extends HandlerInterceptorAdapter {
	
	@Resource
	AsrUserDao asrUserDao;
	
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
				
				List<AsrUser> userList = asrUserDao.getUserByName(username);
				if(userList.size() > 0){
					AsrUser user = userList.get(0);
					if(password.length() > 0 && password.equals(user.getPassword())){
						retJSON.put("code", 0);
						retJSON.put("message", "登录成功");
						retJSON.put("token", user.getToken());
					}else {
						retJSON.put("message", "密码错误");
					}					
				}else{
					retJSON.put("message", "用户不存在");
				}
			}
		}
		
		return retJSON.toString();
	}	
	
	@RequestMapping("/register")
	@ResponseBody
	public String addUser(@RequestBody String body) {
		ReturnObject retObj = new ReturnObject();
		
		if(body != null && body != ""){ // 判断用户是否发起的空请求			
			JSONObject jsonObject = new JSONObject(body);
			
			String username = jsonObject.getString("username");
			List<AsrUser> list = asrUserDao.getUserByName(username);
			if(list.size() > 0) {
				retObj.setMessage("注册失败，用户已经被注册");
			}else {
				AsrUser newUser = new AsrUser();
				newUser.setAvatar("https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif");
				newUser.setUsername(username);
				newUser.setPassword(jsonObject.getString("password"));
				newUser.setToken(UUIDGenerator.getUUID());
				
				List<String> roleList = new ArrayList<String>();
				if("admin".equals(username)) {
					roleList.add("admin");
					roleList.add("editor");
				} else {
					roleList.add("editor");			
				}
				newUser.setRoles(roleList);
				asrUserDao.saveUser(newUser);
				retObj.setCode(0);
				retObj.setMessage("注册成功，请登录");
			}					
		}
		
		return new JSONObject(retObj).toString();
	}
	
	@RequestMapping("/getUserInfo")
	@ResponseBody
	public String getUserInfo(@RequestBody String body) {
		ReturnObject retObj = new ReturnObject();
		
		if(body != null && body != ""){ // 判断用户是否发起的空请求			
			JSONObject jsonObject = new JSONObject(body);
			
			String token = jsonObject.getString("token");
			List<AsrUser> userList = asrUserDao.getUserByToken(token);
			if(userList.size() > 0) {
				AsrUser user = userList.get(0);
				
				JSONObject retJSON = new JSONObject();
				retJSON.put("code", 0);
				retJSON.put("message", "操作成功");
				
				retJSON.put("roles", user.getRoles());
				retJSON.put("name", user.getUsername());
				retJSON.put("avatar", user.getAvatar());
				
				return retJSON.toString();
			} else {
				retObj.setMessage("用户Token不存在");
			}
		} else {
			retObj.setMessage("传递参数为空");
		}
		
		return new JSONObject(retObj).toString();
	}
	
	@RequestMapping("/logout")
	@ResponseBody
	public String userLoginOut(@RequestBody String body)
	{
		ReturnObject retObj = new ReturnObject();
		
		if(body != null && body != ""){
			JSONObject jsonObject = new JSONObject(body);
			if(jsonObject.has("token")){
				retObj.setCode(0);
				retObj.setMessage("操作成功");
			}
		}			
		
		return new JSONObject(retObj).toString();
	}
}
