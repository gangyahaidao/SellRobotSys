package com.qingpu.robots.entity;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="log_robot_replenish")
public class RobotReplenishLog {

	@Id
	@GeneratedValue
	private int id;
	
	private int orderId;
	private String robotName; // 机器人名字
	private String floorName; // 货道层名字
	private String goodsName; // 进行补货的商品名字
	private String goodsPicUrl; // 商品图片的url
	private int beforeReplenishCount; // 补货前货架剩余
	private int currentReplenishCount; // 当前补货数量
	private int afterReplenishCount; // 补货后的货架剩余
	private String replenishUserName; // 补货人姓名
	private Date date;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getRobotName() {
		return robotName;
	}
	public void setRobotName(String robotName) {
		this.robotName = robotName;
	}
	public String getFloorName() {
		return floorName;
	}
	public void setFloorName(String floorName) {
		this.floorName = floorName;
	}
	public String getGoodsName() {
		return goodsName;
	}
	public void setGoodsName(String goodsName) {
		this.goodsName = goodsName;
	}
	public String getGoodsPicUrl() {
		return goodsPicUrl;
	}
	public void setGoodsPicUrl(String goodsPicUrl) {
		this.goodsPicUrl = goodsPicUrl;
	}
	public int getBeforeReplenishCount() {
		return beforeReplenishCount;
	}
	public void setBeforeReplenishCount(int beforeReplenishCount) {
		this.beforeReplenishCount = beforeReplenishCount;
	}
	public int getCurrentReplenishCount() {
		return currentReplenishCount;
	}
	public void setCurrentReplenishCount(int currentReplenishCount) {
		this.currentReplenishCount = currentReplenishCount;
	}
	public int getAfterReplenishCount() {
		return afterReplenishCount;
	}
	public void setAfterReplenishCount(int afterReplenishCount) {
		this.afterReplenishCount = afterReplenishCount;
	}
	public String getReplenishUserName() {
		return replenishUserName;
	}
	public void setReplenishUserName(String replenishUserName) {
		this.replenishUserName = replenishUserName;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public int getOrderId() {
		return orderId;
	}
	public void setOrderId(int orderId) {
		this.orderId = orderId;
	}
}
