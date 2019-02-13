package com.qingpu.user.service;

import com.qingpu.user.entity.User;
import com.qingpu.user.entity.UserWeixin;

public interface UserService {
	
	public User getUserByUsername(String username);
	
	public void updateUser(User user);
	
	public User getUserByUserToken(String id);
	
	public void addUser(User user);

	public UserWeixin getUserByOpenid(String openId);

	public void updateWeixinUser(UserWeixin userWX);

	public void saveWeixinUser(UserWeixin userWX);
}
