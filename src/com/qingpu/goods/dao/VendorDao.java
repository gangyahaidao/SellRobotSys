package com.qingpu.goods.dao;

import java.util.List;

import com.qingpu.goods.entity.Vendor;

public interface VendorDao {

	List<Vendor> getVendorList();
	
	void updateVendor(Vendor vendor);
	
	void deleteVendor(Vendor vendor); // 删除厂商

	void addVendor(Vendor vendor);

	Vendor getVendor(int id);

	Vendor getVendorByVendorId(int vendorId);
}
