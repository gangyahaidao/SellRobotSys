package com.qingpu.common.dao;

import java.util.List;

import org.hibernate.Query;

//数据库操作的基类接口定义
public abstract interface BaseDao {
	/**
	 * 插入一条数据
	 * */
	Object save(Object entity);
	
	/**
	 * 更新一条数据
	 * */
	void update(String entityName, Object entity);
	
	void update(Object entity);
	
	Query getCurrentQuery(String hql);
	
	/**
	 * 保存或者新增对象
	 * */
	Object saveOrUpdate(Object entity);
	
	Object saveOrUpdate(String entityName, Object entity);
	
	/**
	 * 删除对象
	 * */
	void delete(Object entity);
	
	/**
	 * 获取指定id的业务对象
	 * */
	Object get(final Class<?> clazz, Object id);
	
	/**
	 * 不带参数的hql语句查询
	 * @return List
	 * */
	List<?> findByHql(String hql);
	
	List<?> findByHqlQueryParams(Query query, Object[] params);
	
	/**
	 * 带参数的hql语句查询
	 * @return List
	 * */
	List<?> findByHqlParams(String hql, Object[] params);
	
	List<?> findByHqlParamsLimit(String hql, Object[] params, int limit);
	
	/**
	 * 执行自定义hql语句，executeUpdate
	 * */
	Object execQueryHqlUpdate(final String queryHql, final Object[] params);
	
	/**
	 * 执行自定义sql语句，executeUpdate
	 * */
	Object execQuerySqlUpdate(final String querySql,final Object[] params);
	
	List<?> execQuerySqlSelect(final String querySql,final Object[] params, final Class<?> clazz);
	
	/**
	 * 查询符合条件的数量
	 * */
	long findByHqlParamsCount(final String hql, final Object[] params);
	
}
