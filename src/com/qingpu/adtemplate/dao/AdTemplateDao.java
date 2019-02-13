package com.qingpu.adtemplate.dao;

import java.util.List;

import com.qingpu.adtemplate.entity.AdTemplate;

public interface AdTemplateDao {

	void saveAdTemplate(AdTemplate adTemplate);

	List<AdTemplate> getAllAdTemplate();

	AdTemplate getAdTemplateById(int templateId);

	void deleteOneFileObj(int id);

	void updateAdTemplate(AdTemplate adTemplate);

}
