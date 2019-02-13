package com.qingpu.goods.service;

import java.util.Date;
import java.util.List;

import com.qingpu.goods.entity.Goods;
import com.qingpu.goods.entity.GoodsReplenishLog;
import com.qingpu.goods.entity.Orders;
import com.qingpu.robots.entity.RobotReplenishLog;

public interface GoodsService {

	/**
	 * 获取全部商品列表
	 * */
	public List<Goods> getGoodsList(int page, int pageLimit, String sortType, String name, String type);
	
	/**
	 * 添加一个商品
	 * */
	public void addGoods(Goods goods);
	
	/**
	 * 更新一个商品信息
	 * */
	public void updateGoodsInfo(Goods goods);
	
	/**
	 * 通过Id获取一个商品
	 * */
	public Goods getGoodsById(String id);

	public Goods getGoodsByGoodsId(int id);

	public List<Goods> getAllGoods();
	
	/**
	 * 获取所有上架商品列表
	 * */
	public List<Goods> getAllOnlineGoods();

	public void saveOrder(Orders order);

	public Orders getOrderById(String orderId);

	public void updateOrder(Orders order);
	
	/**
	 * 查询所有的订单，前提都是已经被支付的订单
	 * */
	public List<Orders> getAllOrderList();

	/**
	 * 根据指定的时间段查找已经被支付的订单列表，可以查询当天，前一周，前一个月
	 * */
	public List<Orders> getOrderListByDate(String saleDuration);

	/**
	 * 查找指定星期数的订单数据
	 * */	
	public List<Orders> getOrderListByWeekNum(int listWeekNum);

	/**
	 * 查找指定机器人特定时间的订单数据
	 * */
	public List<Orders> getOrderListByMachineId(String machineId, int weekNum);

	/**
	 * 查询本周的订单数据
	 * */
	public List<Orders> getCurrentWeekList();
	/**
	 * 查询本月的订单数据
	 * */	
	public List<Orders> getOrdersByCurrentMonth();

	/**
	 * 历史订单查询，可以根据机器人与截止时间进行分页查询
	 * */
	public List<Orders> getUserConsumerOrderListByConditions(String machineId, String outStatus, Date startDate, Date stopDate, int startItem, int endItem);
	
	
	
	/**
	 * 根据条件查询仓库商品补货记录
	 * */
	public List<GoodsReplenishLog> findReplenishLog(String goodsName, Date startDate, Date stopDate, int startItem, int endItem);
	
	/**
	 * 获取所有的仓库补货记录
	 * */
	public List<GoodsReplenishLog> findAllReplenishLog();

	/**
	 * 获取所有的仓库商品补货数据条数
	 * */
	public long getReplenishLogTotalCount();

	/**
	 * 根据查询条件获取机器人补货记录
	 * */
	public List<RobotReplenishLog> findRobotReplenishLog(String searchStr, Date startDate, Date stopDate, int startItem, int endItem);
	/**
	 * 获取机器人补货记录的总条数
	 * */
	public long getRobotReplenishLogTotalCount();

	/*****************************************************************************************************************/
	/***************************************************根据指定的楼层进行订单查询***************************************/
	/*****************************************************************************************************************/
	/**
	 * 根据指定楼层的所有订单
	 * */
	public List<Orders> getTotolOrdersByFloorName(String floorName);
	/**
	 * 根据楼层查询当天的订单
	 * */
	public List<Orders> getCurrentDayOrderByFloorName(String floorName);

	public List<Orders> getCurrentWeekListByFloorName(String floorName);

	public List<Orders> getOrdersByCurrentMonthByFloorName(String floorName);

	/**
	 * 获取指定机器人编号的所有订单数据
	 * */
	public List<Orders> getAllOrdersByMachineId(String machineId);
}
