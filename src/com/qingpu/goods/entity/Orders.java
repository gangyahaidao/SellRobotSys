package com.qingpu.goods.entity;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.IndexColumn;

//用户购买商品订单
@Entity
@Table(name="orders")
public class Orders {

	@Id
	@GeneratedValue
	private int id;
	
	private String orderId; // 手动设置的uuid值	
	private String preOrderId; // 微信返回的预订单编号
	private String openid;
	private int totalFee; // 订单价钱
	private int usedIntegral; // 使用的积分
	private String machineID; // 机器人编号
	private String floor; // 创建订单的机器人所属楼层的名字 floor1 | 2 | 3
	private String payStatus; // 订单状态  待支付:"prepay" 已支付:"payed" 
	private String outStatus; //出货状态 全部货品都出来才是出货正常:"outOK" 出货异常:"outError"
	
	private boolean hasOutErrorProcessed; // 出货异常是否已经被处理了
	
	private Date date;// 下单时间
	private boolean hasGetNotify = false;//是否已经收到微信服务器发送的支付结果通知
	
	@OneToMany(mappedBy="orders", fetch=FetchType.EAGER)
	@IndexColumn(name="id") 
	@Cascade(value={org.hibernate.annotations.CascadeType.ALL})
	private List<OrderItem> orderItemList; // 订单每个商品的详细信息	
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getPreOrderId() {
		return preOrderId;
	}

	public void setPreOrderId(String preOrderId) {
		this.preOrderId = preOrderId;
	}

	public String getOpenid() {
		return openid;
	}

	public void setOpenid(String openid) {
		this.openid = openid;
	}
	
	public String getMachineID() {
		return machineID;
	}

	public void setMachineID(String machineID) {
		this.machineID = machineID;
	}

	public String getPayStatus() {
		return payStatus;
	}

	public void setPayStatus(String payStatus) {
		this.payStatus = payStatus;
	}

	public String getOutStatus() {
		return outStatus;
	}

	public void setOutStatus(String outStatus) {
		this.outStatus = outStatus;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public List<OrderItem> getOrderItemList() {
		return orderItemList;
	}

	public void setOrderItemList(List<OrderItem> orderItemList) {
		this.orderItemList = orderItemList;
	}

	public boolean isHasGetNotify() {
		return hasGetNotify;
	}

	public void setHasGetNotify(boolean hasGetNotify) {
		this.hasGetNotify = hasGetNotify;
	}

	public int getTotalFee() {
		return totalFee;
	}

	public void setTotalFee(int totalFee) {
		this.totalFee = totalFee;
	}

	public String getFloor() {
		return floor;
	}

	public void setFloor(String floor) {
		this.floor = floor;
	}

	public int getUsedIntegral() {
		return usedIntegral;
	}

	public void setUsedIntegral(int usedIntegral) {
		this.usedIntegral = usedIntegral;
	}

	public boolean isHasOutErrorProcessed() {
		return hasOutErrorProcessed;
	}

	public void setHasOutErrorProcessed(boolean hasOutErrorProcessed) {
		this.hasOutErrorProcessed = hasOutErrorProcessed;
	}
		
}
