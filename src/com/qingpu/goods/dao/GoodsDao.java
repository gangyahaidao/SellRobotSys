package com.qingpu.goods.dao;

import java.util.Date;
import java.util.List;

import com.qingpu.goods.entity.Goods;
import com.qingpu.goods.entity.GoodsReplenishLog;
import com.qingpu.goods.entity.Orders;
import com.qingpu.robots.entity.RobotReplenishLog;

public interface GoodsDao {
	/**
	 * 获取商品列表
	 * */
	public List<Goods> getGoodsList(int startItem, int endItem, String sortType, String name, String type);
	
	/**
	 * 添加商品
	 * */
	public void addGoods(Goods goods);
	
	/**
	 * 更新商品对象信息
	 * */
	public void updateGoods(Goods goods);

	public Goods getGoodsById(String id);

	public Goods getGoodsByGoodsId(int goodsId);

	public List<Goods> getAllGoods();

	public void saveOrder(Orders order);

	public List<Orders> getOrderById(String orderId);

	public void updateOrder(Orders order);

	public List<Goods> getAllOnlineGoods();

	public List<Orders> getPayedOrdersByDate(String sql);

	public List<Orders> getUserConsumerOrderList(String machineId, String outStatus, Date startDate, Date stopDate, int startItem, int endItem);

	public List<GoodsReplenishLog> findReplenishLog(String goodsName, Date startDate, Date stopDate, int startItem, int endItem);

	public List<GoodsReplenishLog> findAllReplenishLog();

	public List<Orders> getAllOrders();

	public long getReplenishLogTotalCount();

	public List<RobotReplenishLog> findRobotReplenishLog(String searchStr, Date startDate, Date stopDate, int startItem, int endItem);

	public long getRobotReplenishLogTotalCount();

	public List<Orders> getTotolOrdersByFloorName(String floorName);
}
