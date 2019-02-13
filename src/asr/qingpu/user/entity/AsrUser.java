package asr.qingpu.user.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.IndexColumn;

import asr.qingpu.wavfile.entity.NewWavFileUrl;

@Entity
@Table(name="asr_user")
public class AsrUser {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private int id;  
	
	private String username;
	private String password;
	private String token; //token取id的值 
	private String avatar; // 用户头像图片的http链接
	private int hasFinishedCount; // 该用户已经完成的任务量
	
	@ElementCollection(fetch=FetchType.EAGER, targetClass=String.class) //指定集合中元素的类型, FetchType.EAGER: 立即加载  LAZY
	@CollectionTable(name="asr_roles") //指定集合生成的表，插入List此表的id主键不能采用uuid的生成方式
	private List<String> roles = new ArrayList<String>(); // 用户角色数组列表 ['admin', 'normal'] 目前划分两种角色，一个超级管理员拥有所有权限，一个普通用户，拥有部分权限，前端页面根据权限动态渲染路由
	
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

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}

	public List<String> getRoles() {
		return roles;
	}

	public void setRoles(List<String> roles) {
		this.roles = roles;
	}

	public int getHasFinishedCount() {
		return hasFinishedCount;
	}

	public void setHasFinishedCount(int hasFinishedCount) {
		this.hasFinishedCount = hasFinishedCount;
	}
	
}
