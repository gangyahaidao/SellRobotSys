package com.qingpu.user.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="user")
public class User {
	@Id
//	@GenericGenerator(name="systemUUID",strategy="uuid")
//	@GeneratedValue(generator="systemUUID")  // 使用uuid格式的主键
	@GeneratedValue(strategy=GenerationType.AUTO)
	private int id;  
	
	private String username;
	private String password;
	private String token; //token取id的值 
	private String avatar; // 用户头像图片的http链接
	private String introduction; // 用户角色介绍
	
	@ElementCollection(fetch=FetchType.EAGER, targetClass=String.class) //指定集合中元素的类型, FetchType.EAGER: 立即加载  LAZY
	@CollectionTable(name="t_roles") //指定集合生成的表，插入List此表的id主键不能采用uuid的生成方式
	//@OrderColumn(name="O_ID") //指定排序列的名称
	private List<String> roles = new ArrayList<String>(); // 用户角色数组列表 ['admin', 'normal'] 目前划分两种角色，一个超级管理员拥有所有权限，一个普通用户，拥有部分权限，前端页面根据权限动态渲染路由
	
	private String loginStatus; // 用户当前的在线状态 'online' 'offline'
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getAvatar() {
		return avatar;
	}
	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}
	public String getIntroduction() {
		return introduction;
	}
	public void setIntroduction(String introduction) {
		this.introduction = introduction;
	}
	public List<String> getRoles() {
		return roles;
	}
	public void setRoles(List<String> roles) {
		this.roles = roles;
	}
	public String getLoginStatus() {
		return loginStatus;
	}
	public void setLoginStatus(String loginStatus) {
		this.loginStatus = loginStatus;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}	
	
}
