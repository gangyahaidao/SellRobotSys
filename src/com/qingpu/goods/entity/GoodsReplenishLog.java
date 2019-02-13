package com.qingpu.goods.entity;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 仓库补货的数据表
 * */
@Entity
@Table(name="log_goods_replenish")
public class GoodsReplenishLog {
	@Id
	@GeneratedValue
	private int id;
	
	private int orderId; // 客户端用于排序显示
	private int beforeReplenishCount; // 补货前库存
	private int currentReplenishCount; // 当前补货数量
	private int afterReplenishCount; // 补货后的库存
	private Date date;
	private String replenishUserName; // 补货人姓名
	private String goodsType; //商品类型
	private String goodsName; //商品名字
	private String goodsPicUrl; // 商品图片链接
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
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
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public String getReplenishUserName() {
		return replenishUserName;
	}
	public void setReplenishUserName(String replenishUserName) {
		this.replenishUserName = replenishUserName;
	}
	public String getGoodsType() {
		return goodsType;
	}
	public void setGoodsType(String goodsType) {
		this.goodsType = goodsType;
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
	public int getOrderId() {
		return orderId;
	}
	public void setOrderId(int orderId) {
		this.orderId = orderId;
	}
}
