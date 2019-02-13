package com.qingpu.adtemplate.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.qingpu.adtemplate.entity.AdTemplate;
import com.qingpu.common.dao.BaseDaoImpl;

@Repository("adTemplateDao")
public class AdTemplateDaoImpl extends BaseDaoImpl implements AdTemplateDao {

	@Override
	public void saveAdTemplate(AdTemplate adTemplate) {
		save(adTemplate);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<AdTemplate> getAllAdTemplate() {
		return (List<AdTemplate>) findByHql("from AdTemplate");
	}

	@Override
	public AdTemplate getAdTemplateById(int templateId) {
		return (AdTemplate) get(AdTemplate.class, templateId);
	}

	@Override
	public void deleteOneFileObj(int id) {
		String hql = "delete from FileInfoObj where id=?";
		execQueryHqlUpdate(hql, new Object[]{id});
	}

	@Override
	public void updateAdTemplate(AdTemplate adTemplate) {
		update(adTemplate);
	}

}
