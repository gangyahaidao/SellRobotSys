package com.qingpu.goods.dao;

import java.util.Date;
import java.util.List;

import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import com.qingpu.common.dao.BaseDaoImpl;
import com.qingpu.goods.dao.GoodsDao;
import com.qingpu.goods.entity.Goods;
import com.qingpu.goods.entity.GoodsReplenishLog;
import com.qingpu.goods.entity.Orders;
import com.qingpu.robots.entity.RobotReplenishLog;

@Repository("goodsDao")
public class GoodsDaoImpl extends BaseDaoImpl implements GoodsDao {

	@SuppressWarnings("unchecked")
	@Override
	public List<Goods> getGoodsList(int startItem, int endItem, String sortType, String name, String type) {
		String hql = "";
		Object[] params = null;
		if(name != null && type == null){
			if("+id".equals(sortType)){
				hql = "from Goods where name like '%"+ name + "%' order by goodsId asc"; // 使用正则表达式进行查找
			}else if("-id".equals(sortType)){
				hql = "from Goods where name like '%"+ name + "%' order by goodsId desc";
			}
		}else if(name == null && type != null){
			if("+id".equals(sortType)){
				hql = "from Goods where type=? order by goodsId asc";
			}else if("-id".equals(sortType)){
				hql = "from Goods where type=? order by goodsId desc";
			}
			params = new Object[]{type};
		}else if(name != null && type != null){
			if("+id".equals(sortType)){
				hql = "from Goods where name like '%"+ name + "%' and type=? order by goodsId asc";
			}else if("-id".equals(sortType)){
				hql = "from Goods where name like '%"+ name + "%' and type=? order by goodsId desc";
			}
			params = new Object[]{type};
		}else if(name == null && type == null){
			if("+id".equals(sortType)){
				hql = "from Goods order by goodsId asc";
			}else if("-id".equals(sortType)){
				hql = "from Goods order by goodsId desc";
			}
		}					
		Query query = getCurrentQuery(hql);
		query.setFirstResult(startItem);
		query.setMaxResults(endItem);
		
		return (List<Goods>) findByHqlQueryParams(query, params);
	}

	@Override
	public void addGoods(Goods goods) {
		save(goods);
	}

	@Override
	public void updateGoods(Goods goods) {
		update(goods);
	}

	@Override
	public Goods getGoodsById(String id) {
		return (Goods) get(Goods.class, id);
	}

	@Override
	public Goods getGoodsByGoodsId(int goodsId) {
		@SuppressWarnings("unchecked")
		List<Goods> list = (List<Goods>) findByHqlParams("from Goods where goodsId=?", new Object[]{goodsId});
		if(list.size() > 0) {
			return list.get(0);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Goods> getAllGoods() {
		return (List<Goods>) findByHql("from Goods");
	}
	
	@Override
	public void saveOrder(Orders order) {
		save(order);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Orders> getOrderById(String orderId) {
		String hql = "from Orders where orderId=?";		
		List<Orders> orderList = (List<Orders>) findByHqlParams(hql, new Object[]{orderId});
		return orderList;
	}

	@Override
	public void updateOrder(Orders order) {
		update(order);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Goods> getAllOnlineGoods() {
		String hql = "from Goods where status='上架'";		
		return (List<Goods>) findByHql(hql);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Orders> getPayedOrdersByDate(String sql) {
		return (List<Orders>) execQuerySqlSelect(sql, null, Orders.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Orders> getUserConsumerOrderList(String machineId,
			String outStatus, Date startDate, Date stopDate, int startItem,
			int endItem) {
		String hql = "from Orders ";
		
		if(machineId != null && outStatus != null) {
			hql += "where payStatus = 'payed' and machineID = ? and outStatus = ? ";			
		}else if(machineId == null && outStatus != null) {
			hql += "where payStatus = 'payed' and outStatus = ? ";
		}else if(machineId != null && outStatus == null) {
			hql += "where payStatus = 'payed' and machineID = ? ";
		} else {
			hql += "where payStatus = 'payed' ";
		}
		
		if(startDate != null && stopDate != null) {
			if(machineId == null && outStatus == null) {
				hql += "where date between ? and ?";
			} else {
				hql += "and date between ? and ?";
			}			
		}
		hql += " order by date desc";
		
		Object[] params = new Object[]{machineId, outStatus, startDate, stopDate};
		Query query = getCurrentQuery(hql);
		query.setFirstResult(startItem);
		query.setMaxResults(endItem);
		
		return (List<Orders>) findByHqlQueryParams(query, params);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<GoodsReplenishLog> findReplenishLog(String goodsName, Date startDate, Date stopDate, int startItem, int endItem) {
		String hql = "from GoodsReplenishLog ";
		if(goodsName != null) {
			hql += "where goodsName like '%" + goodsName + "%' "; // 进行商品名字的模糊查询
		}
		if(startDate != null && stopDate != null) {
			if(goodsName == null) {
				hql += "where date between ? and ?";
			} else {
				hql += "and date between ? and ?";
			}			
		}
		
		Object[] params = new Object[]{goodsName, startDate, stopDate};
		Query query = getCurrentQuery(hql);
		query.setFirstResult(startItem);
		query.setMaxResults(endItem);
		
		return (List<GoodsReplenishLog>) findByHqlQueryParams(query, params);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<GoodsReplenishLog> findAllReplenishLog() {
		String hql = "from GoodsReplenishLog";		
		return (List<GoodsReplenishLog>) findByHql(hql);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Orders> getAllOrders() {
		String hql = "from Orders where payStatus = 'payed'"; // 获取所有已经被支付的订单
		return (List<Orders>) findByHql(hql);
	}

	@Override
	public long getReplenishLogTotalCount() {
		String hql = "select count(*) from GoodsReplenishLog";
		return findByHqlParamsCount(hql, null);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<RobotReplenishLog> findRobotReplenishLog(String searchStr, Date startDate, Date stopDate, int startItem, int endItem) {
		String hql = "from RobotReplenishLog ";
		if(searchStr != null) {
			hql += "where goodsName like '%" + searchStr + "%' "; // 进行商品名字的模糊查询
		}
		if(startDate != null && stopDate != null) {
			if(searchStr == null) {
				hql += "where date between ? and ?";
			} else {
				hql += "and date between ? and ?";
			}			
		}
		
		Object[] params = new Object[]{searchStr, startDate, stopDate};
		Query query = getCurrentQuery(hql);
		query.setFirstResult(startItem);
		query.setMaxResults(endItem);
		
		return (List<RobotReplenishLog>) findByHqlQueryParams(query, params);
	}

	@Override
	public long getRobotReplenishLogTotalCount() {
		String hql = "select count(*) from RobotReplenishLog";
		return findByHqlParamsCount(hql, null);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Orders> getTotolOrdersByFloorName(String floorName) {
		String hql = "from Orders where payStatus = 'payed' and floor = ?"; // 获取所有已经被支付的订单
		return (List<Orders>) findByHqlParams(hql, new Object[]{floorName});
	}
}
