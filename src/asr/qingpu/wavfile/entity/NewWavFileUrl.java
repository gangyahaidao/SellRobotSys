package asr.qingpu.wavfile.entity;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import asr.qingpu.user.entity.AsrUser;

/**
 * 新的文件条目
 * */
@Entity
@Table(name="asr_new_fileurl")
public class NewWavFileUrl {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private int id;  
	
	private int orderId; // 主要是发送到客户端供排序使用
	private String newFileUrl; // 转换之后存储到文件服务器返回的文件url
	private boolean isOccupied; // 是否已经被分配给用户
	private String translateWords; // 被翻译的文字，如果是无效文件则没有内容
	private boolean isFinished; // 是否已经完成识别
	private int finishedUserId; // 完成此条记录用户的id
	private Date date; // 插入时间
	private int userId; // 已经分配给的用户id
	
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
	public String getNewFileUrl() {
		return newFileUrl;
	}
	public void setNewFileUrl(String newFileUrl) {
		this.newFileUrl = newFileUrl;
	}
	public boolean isOccupied() {
		return isOccupied;
	}
	public void setOccupied(boolean isOccupied) {
		this.isOccupied = isOccupied;
	}
	public String getTranslateWords() {
		return translateWords;
	}
	public void setTranslateWords(String translateWords) {
		this.translateWords = translateWords;
	}
	public boolean isFinished() {
		return isFinished;
	}
	public void setFinished(boolean isFinished) {
		this.isFinished = isFinished;
	}
	public int getFinishedUserId() {
		return finishedUserId;
	}
	public void setFinishedUserId(int finishedUserId) {
		this.finishedUserId = finishedUserId;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}	
	
}
