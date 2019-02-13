package asr.qingpu.wavfile.entity;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 总的完成数每天的记录，此记录是每天晚上十一点统计当天所有人的完成量生成的
 * */
@Entity
@Table(name="asr_total_daily_record")
public class TotalDailyRecord {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private int id; 
	
	private int dailyCounts; // 今天的完成总和量，是计时器通过遍历累加单个用户的完成记录来计算的
	private Date date; // 记录的时间，加载一周的记录时通过此值进行判断
	private String weekDayName; // 今天星期几，"周一"-->"周日"
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
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
