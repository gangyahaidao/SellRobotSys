package com.qingpu.common.service;

import java.util.Date;
import java.util.List;

import com.qingpu.common.dao.BaseLogDao;
import com.qingpu.goods.entity.GoodsReplenishLog;

public interface BaseLogService<T> extends BaseLogDao<T> {
	
	/**
	 * 保存日志
	 * */
	@Override
	public void saveLog(T t);
		
	/**
	 * 根据数据表类的名字，使用hql语句查询日志
	 * */
	public List<T> findLogList(String name);
	
	/**
	 * 使用sql语句查询相关日志列表，只要是原来的红包雨项目中使用，暂时作为参考
	 * */
	public List<T> findLogListBySql(String type, String desc, final Object[] params, final Class objectClass);
	
	/**
	 * 保存或者更新日志
	 * */
	@Override
	Object saveOrUpdate(Object entity);
	
}
