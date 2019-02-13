package com.qingpu.common.dao;

import java.util.List;

public interface BaseLogDao<T> extends BaseDao{

	/**
	 * @description 保存日志
	 */
	public void saveLog(T t);
	
	/**
	 *@description hql获取日志
	 */
	public List<T> findLogList(final String hql);
	
}
