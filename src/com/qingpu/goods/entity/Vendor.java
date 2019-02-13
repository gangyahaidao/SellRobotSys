package com.qingpu.goods.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

// 管理零售商品供货商信息
@Entity
@Table(name="vendor")
public class Vendor {
	@Id
	@GeneratedValue
	private int id;
	
	private int vendorId; // 序号
	private String name; // 厂商名字
	private String linkname; // 厂商联系人姓名
	private String linknum; // 联系人电话号码
	private String introduction; // 提供的商品说明
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
	public String getLinkname() {
		return linkname;
	}
	public void setLinkname(String linkname) {
		this.linkname = linkname;
	}
	public String getLinknum() {
		return linknum;
	}
	public void setLinknum(String linknum) {
		this.linknum = linknum;
	}
	public String getIntroduction() {
		return introduction;
	}
	public void setIntroduction(String introduction) {
		this.introduction = introduction;
	}
	public int getVendorId() {
		return vendorId;
	}
	public void setVendorId(int vendorId) {
		this.vendorId = vendorId;
	}
	
}
