package com.qingpu.robots.entity;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 保存机器人对话组列表
 * */
@Entity
@Table(name="robot_group_talk")
public class RobotTalkGroup {
	@Id
	@GeneratedValue
	private int id;
	
	private int orderId; // 客户端排序使用
	private String name; // 对话组名字
	private boolean isDefaultTalk; // 是否是全场默认的对话，只能有一个是全场默认对话
	private String floorName; // 对话使用的楼层
	private String startDateStr; // 有效日期开始时间字符串，如：'2018-11-13T16:00:00.000Z' CommonUtils中有函数能将此值转换成Date对象
	private String stopDateStr; // 有效时间停止时间
	private Date date; // 添加日期
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public boolean isDefaultTalk() {
		return isDefaultTalk;
	}
	public void setDefaultTalk(boolean isDefaultTalk) {
		this.isDefaultTalk = isDefaultTalk;
	}
	public String getFloorName() {
		return floorName;
	}
	public void setFloorName(String floorName) {
		this.floorName = floorName;
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
	public String getStartDateStr() {
		return startDateStr;
	}
	public void setStartDateStr(String startDateStr) {
		this.startDateStr = startDateStr;
	}
	public String getStopDateStr() {
		return stopDateStr;
	}
	public void setStopDateStr(String stopDateStr) {
		this.stopDateStr = stopDateStr;
	}
}
