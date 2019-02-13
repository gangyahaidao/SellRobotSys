package com.qingpu.dial.entity;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @Desc   电话导入实体类
 * @author Gangyahaidao
 */
@Entity
@Table(name="phone_import")
public class PhoneImport {
	@Id
	@GeneratedValue
	private int id;
		
	private String phoneNumber;//导入的电话号码

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}		
}
