package com.qingpu.common.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

@Repository("baseLogDao")
public class BaseLogDaoImpl<T> extends BaseDaoImpl implements BaseLogDao<T> {

	@Override
	public void saveLog(T t) {
		// TODO Auto-generated method stub
		this.save(t);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<T> findLogList(final String hql) {
		// TODO Auto-generated method stub
		return (List<T>) this.findByHql(hql);
	}
}
