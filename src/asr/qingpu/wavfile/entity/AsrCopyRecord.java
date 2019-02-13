package asr.qingpu.wavfile.entity;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 主要是记录从另一个数据库中复制数据的相关记录
 * */
@Entity
@Table(name="asr_copy_record")
public class AsrCopyRecord {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private int id; 
	
	private int allTotalCopys; // 已经复制的总条数，此值只计算复制成功的行数
	private int currentCopyTaskCount; // 本次进行复制的条数	
	private Date copyDate; // 进行复制的时间

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getAllTotalCopys() {
		return allTotalCopys;
	}
	public void setAllTotalCopys(int allTotalCopys) {
		this.allTotalCopys = allTotalCopys;
	}
	public int getCurrentCopyTaskCount() {
		return currentCopyTaskCount;
	}
	public void setCurrentCopyTaskCount(int currentCopyTaskCount) {
		this.currentCopyTaskCount = currentCopyTaskCount;
	}
	public Date getCopyDate() {
		return copyDate;
	}
	public void setCopyDate(Date copyDate) {
		this.copyDate = copyDate;
	}
	
}
