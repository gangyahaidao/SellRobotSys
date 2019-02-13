package com.qingpu.goods.service;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.qingpu.goods.dao.GoodsDao;
import com.qingpu.goods.entity.Goods;
import com.qingpu.goods.entity.GoodsReplenishLog;
import com.qingpu.goods.entity.Orders;
import com.qingpu.robots.entity.RobotReplenishLog;

@Service("goodsService")
public class GoodsServiceImpl implements GoodsService {
	@Resource
	private GoodsDao goodsDao;

	@Override
	public List<Goods> getGoodsList(int page, int pageLimit, String sortType, String name, String type) {
		int startItem = (page-1)*pageLimit;
		int endItem = page*pageLimit;
		return goodsDao.getGoodsList(startItem, endItem, sortType, name, type);
	}

	@Override
	public void addGoods(Goods goods) {
		goodsDao.addGoods(goods);
	}

	@Override
	public void updateGoodsInfo(Goods goods) {
		goodsDao.updateGoods(goods);
	}
	
	public Goods getGoodsById(String id){
		if(id == null) return null;
		return goodsDao.getGoodsById(id);
	}
	
	public Goods getGoodsByGoodsId(int goodsId) {
		return goodsDao.getGoodsByGoodsId(goodsId);
	}

	@Override
	public List<Goods> getAllGoods() {
		return goodsDao.getAllGoods();
	}

	@Override
	public void saveOrder(Orders order) {
		goodsDao.saveOrder(order);
	}

	@Override
	public Orders getOrderById(String orderId) {
		List<Orders> orderList = goodsDao.getOrderById(orderId);
		if(orderList.size() > 0){
			return orderList.get(0);
		}else{
			return null;
		}
	}

	@Override
	public void updateOrder(Orders order) {
		goodsDao.updateOrder(order);
	}

	@Override
	public List<Goods> getAllOnlineGoods() {
		return goodsDao.getAllOnlineGoods();
	}

	@Override
	public List<Orders> getOrderListByDate(String saleDuration) {
		String sql = null;
		if("currentDay".equals(saleDuration)) { // 查找当天的记录
			sql = "SELECT * FROM orders where payStatus='payed' and to_days(date) = to_days(now()) order by date DESC";	
		} else if("foreWeek".equals(saleDuration)) { // 查找一周前
			sql = "SELECT * FROM orders where payStatus='payed' and date >= DATE_SUB(CURDATE(), INTERVAL 1 WEEK) order by date DESC";			
		} else if("foreMonth".equals(saleDuration)) { // 查找一个月前
			sql = "SELECT * FROM orders where payStatus='payed' and date >= DATE_SUB(CURDATE(), INTERVAL 1 MONTH) order by date DESC";			
		}
		return goodsDao.getPayedOrdersByDate(sql);
	}

	@Override
	public List<Orders> getOrderListByWeekNum(int listWeekNum) {
		String sql = "SELECT * FROM orders where payStatus='payed' and date >= DATE_SUB(CURDATE(), INTERVAL "+ listWeekNum +" WEEK)";
		return goodsDao.getPayedOrdersByDate(sql);
	}

	@Override
	public List<Orders> getOrderListByMachineId(String machineId, int weekNum) {
		String sql = "SELECT * FROM orders where payStatus='payed' and machineID='"+ machineId +"' and date >= DATE_SUB(CURDATE(), INTERVAL "+ weekNum +" WEEK)";
		return goodsDao.getPayedOrdersByDate(sql);
	}

	@Override
	public List<Orders> getCurrentWeekList() {
		String sql = "SELECT * FROM orders where payStatus='payed' and YEARWEEK(date_format(date,'%Y-%m-%d')) = YEARWEEK(now())";		
		return goodsDao.getPayedOrdersByDate(sql);
	}

	@Override
	public List<Orders> getOrdersByCurrentMonth() {
		String sql = "SELECT * FROM orders WHERE DATE_FORMAT(date, '%Y%m' ) = DATE_FORMAT(CURDATE() , '%Y%m' )";		
		return goodsDao.getPayedOrdersByDate(sql);
	}

	@Override
	public List<Orders> getUserConsumerOrderListByConditions(String machineId, String outStatus, Date startDate, Date stopDate, int startItem, int endItem) {
		if("all".equals(machineId)) {
			machineId = null;
		}
		if("all".equals(outStatus)){
			outStatus = null;
		}
		return goodsDao.getUserConsumerOrderList(machineId, outStatus, startDate, stopDate, startItem, endItem);
	}
	
	@Override
	public List<GoodsReplenishLog> findReplenishLog(String goodsName, Date startDate, Date stopDate, int startItem, int endItem) {
		return goodsDao.findReplenishLog(goodsName, startDate, stopDate, startItem, endItem);
	}

	@Override
	public List<GoodsReplenishLog> findAllReplenishLog() {
		return goodsDao.findAllReplenishLog();
	}

	@Override
	public List<Orders> getAllOrderList() {
		return goodsDao.getAllOrders();
	}

	@Override
	public long getReplenishLogTotalCount() {
		return goodsDao.getReplenishLogTotalCount();
	}

	@Override
	public List<RobotReplenishLog> findRobotReplenishLog(String searchStr, Date startDate, Date stopDate, int startItem, int endItem) {
		return goodsDao.findRobotReplenishLog(searchStr, startDate, stopDate, startItem, endItem);
	}

	@Override
	public long getRobotReplenishLogTotalCount() {
		return goodsDao.getRobotReplenishLogTotalCount();
	}

	@Override
	public List<Orders> getTotolOrdersByFloorName(String floorName) {
		return goodsDao.getTotolOrdersByFloorName(floorName);
	}

	@Override
	public List<Orders> getCurrentDayOrderByFloorName(String floorName) {
		String sql = "SELECT * FROM orders where payStatus='payed' and to_days(date) = to_days(now()) and floor='"+floorName+"'";
		return goodsDao.getPayedOrdersByDate(sql);
	}

	@Override
	public List<Orders> getCurrentWeekListByFloorName(String floorName) {
		String sql = "SELECT * FROM orders where payStatus='payed' and YEARWEEK(date_format(date,'%Y-%m-%d')) = YEARWEEK(now()) and floor='"+floorName+"'";		
		return goodsDao.getPayedOrdersByDate(sql);
	}

	@Override
	public List<Orders> getOrdersByCurrentMonthByFloorName(String floorName) {
		String sql = "SELECT * FROM orders WHERE DATE_FORMAT(date, '%Y%m' ) = DATE_FORMAT(CURDATE() , '%Y%m' ) and floor='"+floorName+"'";
		return goodsDao.getPayedOrdersByDate(sql);
	}

	@Override
	public List<Orders> getAllOrdersByMachineId(String machineId) {
		String sql = "SELECT * FROM orders where payStatus='payed' and machineID='"+ machineId + "'";
		return goodsDao.getPayedOrdersByDate(sql);
	}

}
