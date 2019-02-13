package com.qingpu.robots.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 机器人在其它模式中的对话
 * */
@Entity
@Table(name="robot_other_dialog")
public class RobotOtherDialog {
	@Id
	@GeneratedValue
	private int id;
	
	private int groupId; // 改子对话所属的对话组id值
	private int orderId; // 主要用于页面排序
	private String message; // 对话内容
	private String originalMessage; // 修改之前的对话
	private int probability; // 对话播放的概率
	private int originalProbability; // 修改之前存储的概率
	private boolean edit = false; // 主要辅助在客户端的编辑操作
	private String type; // 表示该对话所属的模式    "userscan"用户扫码操作    "userpay"用户支付   "goodsout"商品出货   "opendoor"取货
	private String state; // 当前模式所处的状态，目前只支持两种，没有异常和有异常  "OK"  "ERROR"	
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getOriginalMessage() {
		return originalMessage;
	}
	public void setOriginalMessage(String originalMessage) {
		this.originalMessage = originalMessage;
	}
	public int getProbability() {
		return probability;
	}
	public void setProbability(int probability) {
		this.probability = probability;
	}
	public int getOriginalProbability() {
		return originalProbability;
	}
	public void setOriginalProbability(int originalProbability) {
		this.originalProbability = originalProbability;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public boolean isEdit() {
		return edit;
	}
	public void setEdit(boolean edit) {
		this.edit = edit;
	}
	public int getOrderId() {
		return orderId;
	}
	public void setOrderId(int orderId) {
		this.orderId = orderId;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public int getGroupId() {
		return groupId;
	}
	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}
	
}
