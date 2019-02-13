package asr.qingpu.wavfile.entity;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 单个用户每天的完成记录，每个用户每天晚上十一点进行一次统计插入数据
 * */
@Entity
@Table(name="asr_user_daily_record")
public class UserDailyRecord {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private int id;  
	
	private int userId; // 用户的id
	private int dailyCounts; // 今天完成的条数
	private Date date; //创建的日期，根据此值查找前一周的任务完成数
	private String weekDayName; // 今天星期几，"周一"-->"周日"
	
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
	public int getDailyCounts() {
		return dailyCounts;
	}
	public void setDailyCounts(int dailyCounts) {
		this.dailyCounts = dailyCounts;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public String getWeekDayName() {
		return weekDayName;
	}
	public void setWeekDayName(String weekDayName) {
		this.weekDayName = weekDayName;
	}
		
}
