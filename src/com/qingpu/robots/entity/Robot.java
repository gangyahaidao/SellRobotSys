package com.qingpu.robots.entity;

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
@Table(name="robot")
public class Robot {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private int id;  
	
	private int orderId; // 主要在客户端用于排序使用
	private String machineId; // 全场唯一数字字符串编号
	private String name; // 机器人名称
	private String floor; // 所属楼层的名字 floor1 | 2 | 3
	private String creator; // 创建人姓名
	private Date createTime; // 创建时间
	private String status; // 状态 ‘online’ 'offline'
	private int batteryPercent; // 当前剩余电量百分比
	private String goodsListStr; // 机器人上状态商品的字符串拼接值
	private boolean isRobotOutOfStore; // 机器人是否处于缺货状态，在每一次出货之后进行检查，当有一种商品处于缺货状态就设置此标志位，机器人到达循环路径终点时进行检查
	private Date outOfStoreDate; // 缺货的时间
	
	private int pathId; // 机器人当前所绑定的路径编号
	private int talkId; // 机器人绑定的组对话ID
	private int adId; // 所播放的广告组id
	
	private String startLoopTimeStr; // 开始启动循环的时间字符串"08:30"
	private String stopLoopTimeStr; // 停止循环的时间字符串
	
	@OneToMany(mappedBy="robot", fetch=FetchType.EAGER)
	@IndexColumn(name="id") 
	@Cascade(value={org.hibernate.annotations.CascadeType.ALL})
	private List<OneContainerFloor> containerFloors; // 货柜每一层对象， 一对多映射，在添加数据之前需要先 new ArrayList分配内存，增加一层时同时增加一层的前排和后排	

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

	public String getFloor() {
		return floor;
	}

	public void setFloor(String floor) {
		this.floor = floor;
	}

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public List<OneContainerFloor> getContainerFloors() {
		return containerFloors;
	}

	public void setContainerFloors(List<OneContainerFloor> containerFloors) {
		this.containerFloors = containerFloors;
	}

	public String getMachineId() {
		return machineId;
	}

	public void setMachineId(String machineId) {
		this.machineId = machineId;
	}

	public boolean isRobotOutOfStore() {
		return isRobotOutOfStore;
	}

	public void setRobotOutOfStore(boolean isRobotOutOfStore) {
		this.isRobotOutOfStore = isRobotOutOfStore;
	}

	public int getPathId() {
		return pathId;
	}

	public void setPathId(int pathId) {
		this.pathId = pathId;
	}

	public String getStartLoopTimeStr() {
		return startLoopTimeStr;
	}

	public void setStartLoopTimeStr(String startLoopTimeStr) {
		this.startLoopTimeStr = startLoopTimeStr;
	}

	public String getStopLoopTimeStr() {
		return stopLoopTimeStr;
	}

	public void setStopLoopTimeStr(String stopLoopTimeStr) {
		this.stopLoopTimeStr = stopLoopTimeStr;
	}

	public String getGoodsListStr() {
		return goodsListStr;
	}

	public void setGoodsListStr(String goodsListStr) {
		this.goodsListStr = goodsListStr;
	}

	public int getOrderId() {
		return orderId;
	}

	public void setOrderId(int orderId) {
		this.orderId = orderId;
	}

	public int getTalkId() {
		return talkId;
	}

	public void setTalkId(int talkId) {
		this.talkId = talkId;
	}

	public int getAdId() {
		return adId;
	}

	public void setAdId(int adId) {
		this.adId = adId;
	}

	public int getBatteryPercent() {
		return batteryPercent;
	}

	public void setBatteryPercent(int batteryPercent) {
		this.batteryPercent = batteryPercent;
	}

	public Date getOutOfStoreDate() {
		return outOfStoreDate;
	}

	public void setOutOfStoreDate(Date outOfStoreDate) {
		this.outOfStoreDate = outOfStoreDate;
	}

}
