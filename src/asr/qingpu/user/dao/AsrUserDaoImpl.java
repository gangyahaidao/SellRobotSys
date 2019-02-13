package asr.qingpu.user.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.qingpu.common.dao.BaseDaoImpl;

import asr.qingpu.user.entity.AsrUser;

@Repository("asrUserDao")
public class AsrUserDaoImpl extends BaseDaoImpl implements AsrUserDao {

	@SuppressWarnings("unchecked")
	@Override
	public List<AsrUser> getUserByName(String username) {
		return (List<AsrUser>) findByHqlParams("from AsrUser where username = ?", new Object[]{username});
	}

	@Override
	public void updateUser(AsrUser user) {
		update(user);
	}

	@Override
	public AsrUser saveUser(AsrUser user) {
		return (AsrUser) save(user);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<AsrUser> getUserByToken(String token) {
		return (List<AsrUser>) findByHqlParams("from AsrUser where token=?", new Object[]{token});
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<AsrUser> findAllUsers() {
		return (List<AsrUser>) findByHql("from AsrUser");
	}
}
