package com.qingpu.goods.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.qingpu.common.dao.BaseDaoImpl;
import com.qingpu.goods.entity.Vendor;

@Repository("vendorDao")
public class VendorDaoImpl extends BaseDaoImpl implements VendorDao {

	@SuppressWarnings("unchecked")
	@Override
	public List<Vendor> getVendorList() {
		return (List<Vendor>) findByHql("from Vendor");
	}

	@Override
	public void updateVendor(Vendor vendor) {
		update(vendor);
	}

	@Override
	public void deleteVendor(Vendor vendor) {
		delete(vendor);
	}

	@Override
	public void addVendor(Vendor vendor) {
		save(vendor);
	}

	@Override
	public Vendor getVendor(int id) {
		return (Vendor) get(Vendor.class, id);
	}

	@Override
	public Vendor getVendorByVendorId(int vendorId) {
		@SuppressWarnings("unchecked")
		List<Vendor> list = (List<Vendor>) findByHqlParams("from Vendor where vendorId=?", new Object[]{vendorId});
		if(list.size() > 0){
			return list.get(0);
		}else{
			return null;
		}		
	}

}
