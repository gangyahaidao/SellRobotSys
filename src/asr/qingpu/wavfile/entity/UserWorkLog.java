package asr.qingpu.wavfile.entity;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 主要是存储用户操作的每一个记录，便于晚上十一点定时任务统计
 * */
@Entity
@Table(name="asr_user_work_log")
public class UserWorkLog {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private int id; 
	
	private int userId;
	private int newWavFileId; // 所完成的文件id
	private Date date; // 插入时间
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public int getNewWavFileId() {
		return newWavFileId;
	}
	public void setNewWavFileId(int newWavFileId) {
		this.newWavFileId = newWavFileId;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	
}
