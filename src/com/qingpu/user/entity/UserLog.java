package com.qingpu.user.entity;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="log_user")
public class UserLog {
	@Id
	@GeneratedValue
	private int id;
	
	private String userName;
	private int userId;
	private String userAction; // "登录" "退出" 用户进行的操作 
	private String userActionDescription; // 操作描述
	private Date date;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public String getUserAction() {
		return userAction;
	}
	public void setUserAction(String userAction) {
		this.userAction = userAction;
	}
	public String getUserActionDescription() {
		return userActionDescription;
	}
	public void setUserActionDescription(String userActionDescription) {
		this.userActionDescription = userActionDescription;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
		
}
