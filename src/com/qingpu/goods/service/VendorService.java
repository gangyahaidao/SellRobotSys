package com.qingpu.goods.service;

import java.util.List;

import com.qingpu.goods.entity.Vendor;

public interface VendorService {
	/**
	 * 获取商品列表
	 * */
	List<Vendor> getVendorList();
	
	/**
	 * 更新vendor信息
	 * */
	void updateVendorInfo(Vendor vendor);
	
	/**
	 * 删除vendor
	 * */
	void deleteVendor(Vendor vendor);

	/**
	 * 添加一个供货商
	 * */
	void addVendor(Vendor vendor);

	Vendor getVendorById(int id);

	Vendor getVendorByVendorId(int vendorId);
}
