package com.qingpu.robots.entity;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="t_onefloor")
public class OneContainerFloor {
	@Id
	@GeneratedValue
	private int id;
	
	private String floorName; // 货柜层的名字 goodsFloor1-F/B | 2 | 3  F表示前排 B表示后排，暂时前后排的区别不做
	private String goodsSerialId; // 商品的主键
	private int currentCount; // 当前层剩余的商品数
	private int totalCount; // 每层商品的总数
	
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="robotID") // 应该是robotID
	private Robot robot; // 当前层所属的机器人对象

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getFloorName() {
		return floorName;
	}

	public void setFloorName(String floorName) {
		this.floorName = floorName;
	}

	public String getGoodsSerialId() {
		return goodsSerialId;
	}

	public void setGoodsSerialId(String goodsSerialId) {
		this.goodsSerialId = goodsSerialId;
	}

	public Robot getRobot() {
		return robot;
	}

	public void setRobot(Robot robot) {
		this.robot = robot;
	}

	public int getCurrentCount() {
		return currentCount;
	}

	public void setCurrentCount(int currentCount) {
		this.currentCount = currentCount;
	}

	public int getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}
		
}
