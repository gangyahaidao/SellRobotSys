package com.qingpu.user.dao;

import java.util.List;

import org.json.JSONObject;
import org.springframework.stereotype.Repository;

import com.qingpu.common.dao.BaseDaoImpl;
import com.qingpu.user.entity.User;
import com.qingpu.user.entity.UserWeixin;
import com.qingpu.user.entity.UserWeixinOriginal;

@Repository("userDao")
public class UserDaoImpl extends BaseDaoImpl implements UserDao {

	@SuppressWarnings("unchecked")
	@Override
	public List<User> getUserByUsername(String username) {
		// TODO Auto-generated method stub
		return (List<User>) findByHqlParams("from User where username=?", new Object[]{username});	
	}
	
	/**
	 * 更新用户对象
	 * */
	public void updateUser(User user){
		update(user); 
	}
	
	/**
	 * 使用id查询用户
	 * */
	@SuppressWarnings("unchecked")
	public List<User> getUserByUserToken(String token){
		return (List<User>) findByHqlParams("from User where token=?", new Object[]{token});
	}
	
	/**
	 * 新增加一个用户
	 * */
	public void addUser(User user){
		save(user);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<UserWeixin> getUserByOpenId(String openId) {
		return (List<UserWeixin>)findByHqlParams("from UserWeixin where openid=?", new Object[]{openId});
	}

	@Override
	public void updateWeixinUser(UserWeixin userWX) {
		update(userWX);
	}

	@Override
	public void saveWeixinUser(UserWeixin userWX) {
		UserWeixin user = (UserWeixin) save(userWX);
		System.out.println("--SellRobotSys保存UserWeiXin对象 user = " + new JSONObject(user));
	}

	@Override
	public List<UserWeixinOriginal> getOriginalUserByOpenId(String openid) {
		return (List<UserWeixinOriginal>)findByHqlParams("from UserWeixinOriginal where openid=?", new Object[]{openid});
	}

	@Override
	public void updateWeixinUserOriginal(UserWeixinOriginal userWX) {
		update(userWX);
	}

	@Override
	public void saveWeixinUserOriginal(UserWeixinOriginal userWX) {
		save(userWX);
	}

	@Override
	public List<UserWeixinOriginal> getOriginalUserCanRecvAdminInfo() {
		return (List<UserWeixinOriginal>)findByHqlParams("from UserWeixinOriginal where canRecvAdminInfo=? order by date desc", new Object[]{true}); // 按照时间降序排序
	}
}
