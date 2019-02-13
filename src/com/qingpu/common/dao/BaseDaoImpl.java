package com.qingpu.common.dao;

import java.io.Serializable;
import java.util.List;

import javax.annotation.Resource;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate4.support.HibernateDaoSupport;
import org.springframework.orm.hibernate4.HibernateCallback;

/**
 * 基础dao实现类
 * */
public class BaseDaoImpl extends HibernateDaoSupport implements BaseDao{
	
	@Resource
	public void setMySessionFactory(SessionFactory sessionFactory){
		super.setSessionFactory(sessionFactory);
	}
	
	public Query getCurrentQuery(String hql){
		return getSessionFactory().getCurrentSession().createQuery(hql);
	}
	
	/**
	 * 传递query对象和过滤参数进行查询
	 * */	
	public List<?> findByHqlQueryParams(Query query, Object[] params){
		if(params != null){
			int index = 0;
			for(int i = 0; i < params.length; i++){
				if(params[i] != null) { // 滤除数组中为null的元素
					query.setParameter(index, params[i]);
					index++;
				}				
			}
		}
		
		return query.list();
	}
	
	/**
	 * 传递参数进行hql查询
	 * */
	@Override
	public List<?> findByHqlParams(String hql, Object[] params){
		if(params == null){
			return getSessionFactory().getCurrentSession().createQuery(hql).list();
		}
		
		Query q = getSessionFactory().getCurrentSession().createQuery(hql);
		for(int i = 0; i < params.length; i++){
			q.setParameter(i, params[i]);
		}
		
		return q.list();
	}
	
	/**
	 * 传递参数进行hql查询，返回指定的条数
	 * */
	@Override
	public List<?> findByHqlParamsLimit(String hql, Object[] params, int limit){
		if(params == null){
			return getSessionFactory().getCurrentSession().createQuery(hql).list();
		}
		
		Query q = getSessionFactory().getCurrentSession().createQuery(hql);
		for(int i = 0; i < params.length; i++){
			q.setParameter(i, params[i]);
		}
		q.setMaxResults(limit);
		
		return q.list();
	}

	@Override
	public Object save(Object entity) {
		// TODO Auto-generated method stub
		getHibernateTemplate().save(entity);
		return entity;
	}

	@Override
	public void update(Object entity) {
		getHibernateTemplate().update(entity);
	}

	@Override
	public void update(String entityName, Object entity) {
		getHibernateTemplate().update(entityName, entity);
	}

	@Override
	public Object saveOrUpdate(Object entity) {
		getHibernateTemplate().saveOrUpdate(entity);
		return entity;
	}

	@Override
	public Object saveOrUpdate(String entityName, Object entity){
		getHibernateTemplate().saveOrUpdate(entityName, entity);
		return entity;
	}

	@Override
	public void delete(Object entity) {
		getHibernateTemplate().delete(entity);
	}

	@Override
	public Object get(final Class<?> clazz, Object id) {
		return getHibernateTemplate().get(clazz, (Serializable) id);
	}
	
	@SuppressWarnings("unchecked")
	public List<?> getAll(final Class clazz) {
		return (List<?>)getHibernateTemplate().execute(new HibernateCallback() {
			@Override
			public Object doInHibernate(Session session) throws HibernateException {
					String hql="from " + clazz.getName();
					Query query=session.createQuery(hql);
					return query.list();
			}
		});
	}

	@Override
	public List<?> findByHql(String hql) {
		return findByHqlParams(hql, null);
	}
	
	/**
	 * 执行自定义HQL语句，查询，删除，更新等
	 * @param queryHql
	 * @return Integer
	 * @throws DaoException
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Object execQueryHqlUpdate(final String queryHql, final Object[] params) {
		final String sql = queryHql;
		return this.getHibernateTemplate().execute(new HibernateCallback() {
			@Override
			public Object doInHibernate(Session session) throws HibernateException {
				String querySentence = sql;
				Query query = session.createQuery(querySentence);
				if (params != null) {
					for (int i = 0; i < params.length; i++) {
						query.setParameter(i, params[i]);
					}
				}
				return Integer.valueOf(query.executeUpdate());
			}
		});		
	}
	
	/**
	 * 执行自定义SQL Update更新语句
	 * @param querySql
	 * @return Integer
	 * @throws DaoException
	 */
	@SuppressWarnings("unchecked")
	public Object execQuerySqlUpdate(final String querySql,final Object[] params) {
		final String sql = querySql;
		return this.getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				String querySentence = sql;
				SQLQuery query = session.createSQLQuery(querySentence);
				if (params != null) {
					for (int i = 0; i < params.length; i++) {
						query.setParameter(i, params[i]);
					}
				}
				//注意，此处调用的是executeUpdate()方法，执行成功返回1，否则返回0。	
				return Integer.valueOf(query.executeUpdate());
			}
		});
	}
	
	/**
	 * 执行自定义Sql 查询语句
	 * */
	@SuppressWarnings("unchecked")
	public List<?> execQuerySqlSelect(final String querySql,final Object[] params, final Class<?> clazz) {
		final String sql = querySql;
		return this.getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				String querySentence = sql;
				SQLQuery query = session.createSQLQuery(querySentence);
				if (params != null) {
					for (int i = 0; i < params.length; i++) {
						query.setParameter(i, params[i]);
					}
				}	
				query.addEntity(clazz);
				return query.list(); 
			}
		});
	}

	/**
	 * 查询符合条件的数量
	 * */
	@Override
	public long findByHqlParamsCount(String hql, Object[] params) {
		if(params == null){
			return (long)getSessionFactory().getCurrentSession().createQuery(hql).uniqueResult();
		}
		
		Query q = getSessionFactory().getCurrentSession().createQuery(hql);
		for(int i = 0; i < params.length; i++){
			q.setParameter(i, params[i]);
		}
		
		return (long)q.uniqueResult();
	}

}
