package com.qingpu.user.entity;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

//消费者信息数据表格
@Entity
@Table(name="user_weixin_original")
public class UserWeixinOriginal {
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private int id;  
	
	private String openid;
	private String nickname;
	private String headimageurl;
	private String sex;
	private String province;
	private String city;
	private boolean canRecvAdminInfo; // 能接收通知消息，零售机器人将把运行消息发送到此用户
	private Date date;

	public UserWeixinOriginal(){
		//无参构造函数
	}
	
	public UserWeixinOriginal(String openid, String nickname, String sex, String province, String city, String headimageurl){
		this.openid = openid;
		this.nickname = nickname;
		this.sex = sex;
		this.province = province;
		this.city = city;
		this.headimageurl = headimageurl;
	}
	
	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public boolean isCanRecvAdminInfo() {
		return canRecvAdminInfo;
	}

	public void setCanRecvAdminInfo(boolean canRecvAdminInfo) {
		this.canRecvAdminInfo = canRecvAdminInfo;
	}
	
	public String getOpenid() {
		return openid;
	}
	public void setOpenid(String openid) {
		this.openid = openid;
	}
	public String getNickname() {
		return nickname;
	}
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	public String getHeadimageurl() {
		return headimageurl;
	}
	public void setHeadimageurl(String headimageurl) {
		this.headimageurl = headimageurl;
	}
	public String getSex() {
		return sex;
	}
	public void setSex(String sex) {
		this.sex = sex;
	}
	public String getProvince() {
		return province;
	}
	public void setProvince(String province) {
		this.province = province;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	
}
