package com.qingpu.robots.entity;

import javax.persistence.Entity;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="floor_pos_name")
public class FloorPosName {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private int id;  
	
	private String floorName;
	
	@Column(length=10240) // 设置数据库字段字符串长度
	private String posNameStrArr;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getFloorName() {
		return floorName;
	}
	public void setFloorName(String floorName) {
		this.floorName = floorName;
	}
	public String getPosNameStrArr() {
		return posNameStrArr;
	}
	public void setPosNameStrArr(String posNameStrArr) {
		this.posNameStrArr = posNameStrArr;
	}
}
