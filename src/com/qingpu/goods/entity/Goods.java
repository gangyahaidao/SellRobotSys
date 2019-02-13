package com.qingpu.goods.entity;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.IndexColumn;

@Entity
@Table(name="goods")
public class Goods {

	@Id
	@GenericGenerator(name="systemUUID",strategy="uuid")
	@GeneratedValue(generator="systemUUID")  // 使用uuid格式的主键
	private String id;
	
	private int goodsId; // 商品编号，用于客户端排序
	private String name; // 商品名称
	private double price; // 价格
	private String type; // 类型  'Drink'  'Water'  'Snack'
	private String status; // 商品状态 '上架' '下架'
	private String introduction; // 商品宣传口号
	private Date deadDate; // 保质期时间
	private String addUserName; //添加商品人姓名
	private int goodsPopularity; // 商品受欢迎程度 1 2 3
	private int goodsSales; // 销量
	private int repertory; // 商品仓库余量
	private String fileurl; // 图片的链接
	private int vendorId; // 此商品所属供货商的id
	private String vendorName;
	
	@OneToMany(mappedBy="goods", fetch=FetchType.EAGER)
	@IndexColumn(name="id") 
	@Cascade(value={org.hibernate.annotations.CascadeType.ALL})		
	private List<VIP> vipList;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	public int getGoodsId() {
		return goodsId;
	}
	public void setGoodsId(int goodsId) {
		this.goodsId = goodsId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public double getPrice() {
		return price;
	}
	public void setPrice(double price) {
		this.price = price;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getIntroduction() {
		return introduction;
	}
	public void setIntroduction(String introduction) {
		this.introduction = introduction;
	}
	public Date getDeadDate() {
		return deadDate;
	}
	public void setDeadDate(Date deadDate) {
		this.deadDate = deadDate;
	}
	public String getAddUserName() {
		return addUserName;
	}
	public void setAddUserName(String addUserName) {
		this.addUserName = addUserName;
	}
	public int getGoodsPopularity() {
		return goodsPopularity;
	}
	public void setGoodsPopularity(int goodsPopularity) {
		this.goodsPopularity = goodsPopularity;
	}
	public int getGoodsSales() {
		return goodsSales;
	}
	public void setGoodsSales(int goodsSales) {
		this.goodsSales = goodsSales;
	}
	public String getFileurl() {
		return fileurl;
	}
	public void setFileurl(String fileurl) {
		this.fileurl = fileurl;
	}
	public List<VIP> getVipList() {
		return vipList;
	}
	public void setVipList(List<VIP> vipList) {
		this.vipList = vipList;
	}
	public int getRepertory() {
		return repertory;
	}
	public void setRepertory(int repertory) {
		this.repertory = repertory;
	}
	public int getVendorId() {
		return vendorId;
	}
	public void setVendorId(int vendorId) {
		this.vendorId = vendorId;
	}
	public String getVendorName() {
		return vendorName;
	}
	public void setVendorName(String vendorName) {
		this.vendorName = vendorName;
	}
	
}
