package com.qingpu.goods.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.qingpu.goods.dao.VendorDao;
import com.qingpu.goods.entity.Vendor;

@Service("vendorService")
public class VendorServiceImpl implements VendorService {
	@Resource
	private VendorDao vendorDao;

	@Override
	public List<Vendor> getVendorList() {
		List<Vendor> list = vendorDao.getVendorList();
		
		return list;
	}

	@Override
	public void updateVendorInfo(Vendor vendor) {
		vendorDao.updateVendor(vendor);
	}

	@Override
	public void deleteVendor(Vendor vendor) {
		vendorDao.deleteVendor(vendor);
	}

	@Override
	public void addVendor(Vendor vendor) {
		vendorDao.addVendor(vendor);
	}

	@Override
	public Vendor getVendorById(int id) {
		return vendorDao.getVendor(id);
	}

	@Override
	public Vendor getVendorByVendorId(int vendorId) {
		return vendorDao.getVendorByVendorId(vendorId);
	}

}
