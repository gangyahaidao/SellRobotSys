package com.qingpu.common.service;

import java.util.List;

import javax.annotation.Resource;

import org.hibernate.HibernateException;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.springframework.orm.hibernate4.HibernateCallback;
import org.springframework.stereotype.Service;

import com.qingpu.common.dao.BaseLogDao;
import com.qingpu.common.dao.BaseLogDaoImpl;

@Service("baseLogService")
public class BaseLogServiceImpl<T> extends BaseLogDaoImpl<T> implements BaseLogService<T>{
	
	@Resource
	private BaseLogDao<T> baseLogDao;

	@Override
	public void saveLog(T t) {
		// TODO Auto-generated method stub
		baseLogDao.save(t);
	}

	@Override
	public List<T> findLogList(String LogObjName) {
		String hql = "from " + LogObjName;
		
		return baseLogDao.findLogList(hql);
	}
	
	@Override
	public Object saveOrUpdate(Object entity){
		return baseLogDao.saveOrUpdate(entity);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<T> findLogListBySql(String type, String desc, final Object[] params, final Class objectClass) {
		String sql = null;
		if("PlayNoRedpackGameScoreLog".equals(type)
				&& "findGameRangeBeforeTen".equals(desc)){
			//查找当天红包游戏分数前十名
			sql = "select * from (select * from play_redpack_gamescore_log where TO_DAYS(time) = TO_DAYS(CURDATE())  order by scores desc)tt group by openid order by scores desc";
		}else if("UserFarmLog".equals(type) && "findUserFarmLogGetWxcardPheasantry".equals(desc)){
			//查找领取鸡舍卡券数量前三名用户
			sql = "select *, count(*) as count from (select * from farm_user_log where type='领取卡券' and farmModule like '%鸡蛋%')tt group by openid order by time desc";
		}else if("GetRedPackLog".equals(type) && "getKFCRewardList".equals(desc)){
			//查找用户领取KFC卡券的记录，针对活动使用
			sql = "select * from user_get_redpack_record_log rr where openid=? and redpackMoney=-1 order by time desc";
		}
		final String sql2 = sql;
		
		return (List<T>) this.getHibernateTemplate().execute(new HibernateCallback() {
			@Override
			public Object doInHibernate(Session session) throws HibernateException {
				String querySentence = sql2;
				SQLQuery query = session.createSQLQuery(querySentence).addEntity(objectClass);//增加addEntity方法，这样查询出来的数据会自动转换为对象
				if (params != null) {
					for (int i = 0; i < params.length; i++) {
						query.setParameter(i, params[i]);
					}
				}
				//注意，此处调用的是executeUpdate()方法，执行成功返回1，否则返回0
				return query.list();
			}
		});		
	}

}
