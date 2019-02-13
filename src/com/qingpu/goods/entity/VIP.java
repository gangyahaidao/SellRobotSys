package com.qingpu.goods.entity;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.qingpu.goods.entity.Goods;

@Entity
@Table(name="vip")
public class VIP { // 包含不同vip等级所享受的折扣价，以及购买商品使用积分的数量，如果用户是vip需要再此基础上再进行折扣
	@Id
	@GeneratedValue
	private int id;
	
	private String vipName; // vip名称
	private double vipValue; // vip的折扣值
	private int integral; // 购买商品需要的积分
	private boolean edit; //辅助客户端网页编辑使用，是否处于编辑的状态
	private double originalVipValue; // 存储编辑之前的值
	private int originalIntegralValue; // 存储编辑之前的值
	
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="goodsID")
	private Goods goods; // 建立一个一对一的映射关系，一种商品对应一种折扣价

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getVipName() {
		return vipName;
	}

	public void setVipName(String vipName) {
		this.vipName = vipName;
	}

	public int getIntegral() {
		return integral;
	}

	public void setIntegral(int integral) {
		this.integral = integral;
	}

	public boolean isEdit() {
		return edit;
	}

	public void setEdit(boolean edit) {
		this.edit = edit;
	}

	public double getVipValue() {
		return vipValue;
	}

	public void setVipValue(double vipValue) {
		this.vipValue = vipValue;
	}

	public double getOriginalVipValue() {
		return originalVipValue;
	}

	public void setOriginalVipValue(double originalVipValue) {
		this.originalVipValue = originalVipValue;
	}

	public Goods getGoods() {
		return goods;
	}

	public void setGoods(Goods goods) {
		this.goods = goods;
	}

	public int getOriginalIntegralValue() {
		return originalIntegralValue;
	}

	public void setOriginalIntegralValue(int originalIntegralValue) {
		this.originalIntegralValue = originalIntegralValue;
	}
	
}
