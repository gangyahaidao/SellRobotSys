package com.qingpu.robots.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 机器人巡场和到达指定地点 或者是检测到人体的对话
 * */
@Entity
@Table(name="robot_patrol_sense_dialog")
public class RobotPatrolOrSenseDialog {
	@Id
	@GeneratedValue
	private int id;
	
	private int groupId; // 改子对话所属的对话组id值
	private int orderId; // 页面排序
	private boolean edit = false; // 主要辅助在客户端的编辑操作	
	private String message; // 所要说的话
	private String originalMessage; // 修改之前的对话
	private int probability; // 对话播放的概率
	private int originalProbability; // 修改之前存储的概率
	
	private String type; // 两种模式："patrol"巡逻模式  "sensepeople"检测到人模式
	private String state; //机器人当前的状态 "freegoing"无人状态  "reachedgoal"到达指定的地点  || "senseYes"感应到人 "senseTimeout"检测到人超时
	private String timeIntervalName; // "forenoon"上午  "noon"午饭时间  "afternoon"下午 "dinner"晚饭时间  "evening"晚上	用于巡逻模式设置时间段的过滤	
	// type=="patrol" && state == "reachedgoal"才查询此字段
	private String reachGoalName; // 到达的特定目标点名字
	// type=="sensepeole" && state=="senseYes"
	private String peopleInfo; // "感应到人"检测到有人    "感应到男性"检测到男性    "感应到女性"检测到女性 
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getOrderId() {
		return orderId;
	}
	public void setOrderId(int orderId) {
		this.orderId = orderId;
	}
	public boolean isEdit() {
		return edit;
	}
	public void setEdit(boolean edit) {
		this.edit = edit;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
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
	public String getReachGoalName() {
		return reachGoalName;
	}
	public void setReachGoalName(String reachGoalName) {
		this.reachGoalName = reachGoalName;
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
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getPeopleInfo() {
		return peopleInfo;
	}
	public void setPeopleInfo(String peopleInfo) {
		this.peopleInfo = peopleInfo;
	}
	public String getTimeIntervalName() {
		return timeIntervalName;
	}
	public void setTimeIntervalName(String timeIntervalName) {
		this.timeIntervalName = timeIntervalName;
	}
	public int getGroupId() {
		return groupId;
	}
	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}
	
}
