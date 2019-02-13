package asr.qingpu.user.dao;

import java.util.List;

import asr.qingpu.user.entity.AsrUser;

public interface AsrUserDao {

	List<AsrUser> getUserByName(String username);
	
	void updateUser(AsrUser user);
	
	AsrUser saveUser(AsrUser user);
	
	List<AsrUser> getUserByToken(String token);
	
	/**
	 * 查找当前数据中所有的用户
	 * */
	List<AsrUser> findAllUsers();
	
}
