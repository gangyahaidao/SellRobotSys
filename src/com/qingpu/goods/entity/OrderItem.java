package com.qingpu.goods.entity;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

//订单中的一个商品相关信息
@Entity
@Table(name="t_orderitem")
public class OrderItem {

	@Id
	@GeneratedValue
	private int id;
	
	private String goodsSerialId; //商品唯一编号，商品表的id主键值
	private int buyCount; // 此商品购买数量
	private String goodsFloor; // 商品所在的层数 goodsFloor1 | 2 | 3
	private String status; // 状态 已经正常出货"success" 未能正常出货"error"
	private int whichCountGoodsHasError; // 所购买的第几个商品出货异常
	
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="orderID")
	private Orders orders;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getGoodsSerialId() {
		return goodsSerialId;
	}
	public void setGoodsSerialId(String goodsSerialId) {
		this.goodsSerialId = goodsSerialId;
	}
	public int getBuyCount() {
		return buyCount;
	}
	public void setBuyCount(int buyCount) {
		this.buyCount = buyCount;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Orders getOrders() {
		return orders;
	}
	public void setOrders(Orders orders) {
		this.orders = orders;
	}
	public String getGoodsFloor() {
		return goodsFloor;
	}
	public void setGoodsFloor(String goodsFloor) {
		this.goodsFloor = goodsFloor;
	}
	public int getWhichCountGoodsHasError() {
		return whichCountGoodsHasError;
	}
	public void setWhichCountGoodsHasError(int whichCountGoodsHasError) {
		this.whichCountGoodsHasError = whichCountGoodsHasError;
	}
	
}
