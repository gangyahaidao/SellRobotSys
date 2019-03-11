package com.qingpu.user.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.qingpu.user.dao.UserDao;
import com.qingpu.user.entity.User;
import com.qingpu.user.entity.UserWeixin;
import com.qingpu.user.entity.UserWeixinOriginal;

@Service("userService")
public class UserServiceImpl implements UserService {
	@Resource
	private UserDao userDao;

	@Override
	public User getUserByUsername(String username) {
		// TODO Auto-generated method stub
		List<User> userList = userDao.getUserByUsername(username);
		if(userList.size() > 0)
		{
			return userList.get(0);
		}else{
			return null;
		}
	}
	
	public User getUserByUserToken(String token){
		List<User> userList = userDao.getUserByUserToken(token);
		if(userList.size() > 0)
		{
			return userList.get(0);
		}else{
			return null;
		}
	}
	
	/**
	 * 更新用户对象
	 * */
	public void updateUser(User user){
		userDao.updateUser(user);
	}
	
	/**
	 * 添加一个用户
	 * */
	public void addUser(User user){
		userDao.addUser(user);
	}

	@Override
	public UserWeixin getUserByOpenid(String openId) {
		List<UserWeixin> list = userDao.getUserByOpenId(openId);
		if(list.size() > 0){
			return list.get(0);
		}else{
			return null;
		}
	}

	@Override
	public void updateWeixinUser(UserWeixin userWX) {
		userDao.updateWeixinUser(userWX);
	}

	@Override
	public void saveWeixinUser(UserWeixin userWX) {
		userDao.saveWeixinUser(userWX);
	}

	@Override
	public UserWeixinOriginal getOriginalUserByOpenid(String openid) {
		List<UserWeixinOriginal> list = userDao.getOriginalUserByOpenId(openid);
		if(list.size() > 0){
			return list.get(0);
		}else{
			return null;
		}
	}

	@Override
	public void updateWeixinUserOriginal(UserWeixinOriginal userWX) {
		userDao.updateWeixinUserOriginal(userWX);
	}

	@Override
	public void saveWeixinUserOriginal(UserWeixinOriginal userWX) {
		userDao.saveWeixinUserOriginal(userWX);
	}

	@Override
	public UserWeixinOriginal getOriginalUserCanRecvAdminInfo() {
		List<UserWeixinOriginal> list = userDao.getOriginalUserCanRecvAdminInfo();
		if(list.size() > 0){
			return list.get(0);
		}else{
			return null;
		}
	}
}
