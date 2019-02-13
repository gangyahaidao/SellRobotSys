package com.qingpu.adtemplate.entity;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.IndexColumn;

@Entity
@Table(name="ad_template")
public class AdTemplate {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private int id;  
	
	private String adTemplateName;
	private int picShowIntervalTime;
	private String playOrder; // 'ordered'  'random'
	private boolean isEnabled; // 是否启用
	private Date date;
	
	@OneToMany(mappedBy="adTemplate", fetch=FetchType.EAGER)
	@IndexColumn(name="id") 
	@Cascade(value={org.hibernate.annotations.CascadeType.ALL})
	private List<FileInfoObj> picFileObjArr;
	
	@OneToMany(mappedBy="adTemplate", fetch=FetchType.EAGER)
	@IndexColumn(name="id") 
	@Cascade(value={org.hibernate.annotations.CascadeType.ALL})
	private List<FileInfoObj> videoFileObjArr;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getAdTemplateName() {
		return adTemplateName;
	}

	public void setAdTemplateName(String adTemplateName) {
		this.adTemplateName = adTemplateName;
	}

	public int getPicShowIntervalTime() {
		return picShowIntervalTime;
	}

	public void setPicShowIntervalTime(int picShowIntervalTime) {
		this.picShowIntervalTime = picShowIntervalTime;
	}

	public String getPlayOrder() {
		return playOrder;
	}

	public void setPlayOrder(String playOrder) {
		this.playOrder = playOrder;
	}

	public List<FileInfoObj> getPicFileObjArr() {
		return picFileObjArr;
	}

	public void setPicFileObjArr(List<FileInfoObj> picFileObjArr) {
		this.picFileObjArr = picFileObjArr;
	}

	public List<FileInfoObj> getVideoFileObjArr() {
		return videoFileObjArr;
	}

	public void setVideoFileObjArr(List<FileInfoObj> videoFileObjArr) {
		this.videoFileObjArr = videoFileObjArr;
	}

	public boolean isEnabled() {
		return isEnabled;
	}

	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}
}
