package com.qingpu.adtemplate.entity;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="ad_file_obj")
public class FileInfoObj {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private int id; 
	
	private String uploadUUID; // 图片上传组件返回的uuid，可用于前端页面上已上传文件的删除
	private int size;
	private String name;
	private String type;
	private String url; // 文件在文件服务器的url
	
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="adTempalteId")
	private AdTemplate adTemplate; // 当前层所属的广告模板对象

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUploadUUID() {
		return uploadUUID;
	}

	public void setUploadUUID(String uploadUUID) {
		this.uploadUUID = uploadUUID;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public AdTemplate getAdTemplate() {
		return adTemplate;
	}

	public void setAdTemplate(AdTemplate adTemplate) {
		this.adTemplate = adTemplate;
	}
}
