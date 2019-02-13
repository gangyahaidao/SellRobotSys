package com.qingpu.robots.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 机器人模板数据表
 * */
@Entity
@Table(name="robot_template_dialog")
public class TalkTemplate {

	@Id
	@GeneratedValue
	private int id;
	
	private String type; // 模板类型
	private String content; // 模板内容
	private int orderId; // 序号
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public int getOrderId() {
		return orderId;
	}
	public void setOrderId(int orderId) {
		this.orderId = orderId;
	}
	
}
