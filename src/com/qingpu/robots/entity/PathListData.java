package com.qingpu.robots.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 存储创建的机器人运行路径
 * */
@Entity
@Table(name="robot_path_list_data")
public class PathListData {
	@Id
	@GeneratedValue
	private int id;
	
	private int orderId; // 用于客户端排序使用
	private String floorName; // 楼层名字
	private String name; // 路径的名称
	private int loopStaySec; // 循环一圈之后暂停的时间
	
	@Column(length=10240) // 设置数据库字段字符串长度
	private String jsonPathStr; // 路径的json字符串数据，如：[{"posName":"实验室","staySec":30, "X":1.0, "Y":1.0, "Z":1.0},{"posName":"商会","staySec":60, "X":1.0, "Y":1.0, "Z":1.0}]
	
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
	public String getJsonPathStr() {
		return jsonPathStr;
	}
	public void setJsonPathStr(String jsonPathStr) {
		this.jsonPathStr = jsonPathStr;
	}
	public String getFloorName() {
		return floorName;
	}
	public void setFloorName(String floorName) {
		this.floorName = floorName;
	}
	public int getLoopStaySec() {
		return loopStaySec;
	}
	public void setLoopStaySec(int loopStaySec) {
		this.loopStaySec = loopStaySec;
	}
	public int getOrderId() {
		return orderId;
	}
	public void setOrderId(int orderId) {
		this.orderId = orderId;
	}	
}
