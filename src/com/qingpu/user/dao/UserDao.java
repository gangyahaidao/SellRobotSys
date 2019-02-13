package com.qingpu.user.dao;

import java.util.List;

import com.qingpu.user.entity.User;
import com.qingpu.user.entity.UserWeixin;

public interface UserDao {
	List<User> getUserByUsername(String username);
	
	void updateUser(User user);

	public List<User> getUserByUserToken(String token);
	
	public void addUser(User user);

	List<UserWeixin> getUserByOpenId(String openId);

	void updateWeixinUser(UserWeixin userWX);

	void saveWeixinUser(UserWeixin userWX);
}
