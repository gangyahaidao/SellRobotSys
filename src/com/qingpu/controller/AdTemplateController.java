package com.qingpu.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.qingpu.adtemplate.dao.AdTemplateDao;
import com.qingpu.adtemplate.entity.AdTemplate;
import com.qingpu.adtemplate.entity.FileInfoObj;
import com.qingpu.common.entity.ReturnObject;
import com.qingpu.robots.dao.RobotsDao;
import com.qingpu.robots.entity.Robot;

@Controller
@RequestMapping("/adtemplate")
public class AdTemplateController extends HandlerInterceptorAdapter {
	
	@Resource
	AdTemplateDao adTemplateDao;
	@Resource 
	RobotsDao robotDao;

	/**
	 * 创建广告模板
	 * */
	@RequestMapping(value="/createAdTemplate")
	@ResponseBody
	public String createAdTemplate(@RequestBody String body){
		ReturnObject retObj = new ReturnObject();
		
		if(body != null && body != ""){
			JSONObject jsonObj = new JSONObject(body);
			String adTemplateName = jsonObj.getString("adTemplateName"); // 模板名称
			int picShowIntervalTime = jsonObj.getInt("picShowIntervalTime"); // 图片轮播的时间间隔
			String playOrder = jsonObj.getString("playOrder"); // 广告内容播放的顺序，'ordered'  'random'
			boolean isEnabled = jsonObj.getBoolean("isEnabled");
			JSONArray picFileObjArr = jsonObj.getJSONArray("picFileObjArr"); // 图片的对象数组
			JSONArray videoFileObjArr = jsonObj.getJSONArray("videoFileObjArr"); // 视频文件的对象数组			
			
			AdTemplate adTemplate = new AdTemplate();
			adTemplate.setAdTemplateName(adTemplateName);
			adTemplate.setPicShowIntervalTime(picShowIntervalTime);
			adTemplate.setPlayOrder(playOrder);
			adTemplate.setEnabled(isEnabled);
			adTemplate.setDate(new Date());
			
			List<FileInfoObj> picFiles = new ArrayList<FileInfoObj>();
			for(int i = 0; i < picFileObjArr.length(); i++) {
				JSONObject obj = picFileObjArr.getJSONObject(i);
				FileInfoObj fileObj = new FileInfoObj();
				fileObj.setAdTemplate(adTemplate);
				fileObj.setName(obj.getString("name"));
				fileObj.setSize(obj.getInt("size"));
				fileObj.setType(obj.getString("type"));
				fileObj.setUploadUUID(obj.getString("uploadUUID"));
				fileObj.setUrl(obj.getString("url"));
				picFiles.add(fileObj);
			}
			adTemplate.setPicFileObjArr(picFiles);
			
			List<FileInfoObj> videoFiles = new ArrayList<FileInfoObj>();
			for(int j = 0; j < videoFileObjArr.length(); j++) {
				JSONObject obj = videoFileObjArr.getJSONObject(j);
				FileInfoObj fileObj = new FileInfoObj();
				fileObj.setAdTemplate(adTemplate);
				fileObj.setName(obj.getString("name"));
				fileObj.setSize(obj.getInt("size"));
				fileObj.setType(obj.getString("type"));
				fileObj.setUploadUUID(obj.getString("uploadUUID"));
				fileObj.setUrl(obj.getString("url"));
				videoFiles.add(fileObj);
			}
			adTemplate.setVideoFileObjArr(videoFiles);
			adTemplateDao.saveAdTemplate(adTemplate);
			
			retObj.setCode(0);
			retObj.setMessage("保存广告模板成功");
		}
		
		return new JSONObject(retObj).toString();
	}
	
	/**
	 * 更新广告模板
	 * */
	@RequestMapping(value="/updateAdTemplate")
	@ResponseBody
	public String updateAdTemplate(@RequestBody String body){
		ReturnObject retObj = new ReturnObject();
		
		if(body != null && body != ""){
			JSONObject jsonObj = new JSONObject(body);
			int templateId = jsonObj.getInt("id");
			AdTemplate adTemplate = adTemplateDao.getAdTemplateById(templateId);
			if(adTemplate != null) {
				String adTemplateName = jsonObj.getString("adTemplateName"); // 模板名称
				int picShowIntervalTime = jsonObj.getInt("picShowIntervalTime"); // 图片轮播的时间间隔
				String playOrder = jsonObj.getString("playOrder"); // 广告内容播放的顺序，'ordered'  'random'
				boolean isEnabled = false;
				if(jsonObj.has("isEnabled")) {
					isEnabled = jsonObj.getBoolean("isEnabled");
				}
				adTemplate.setAdTemplateName(adTemplateName);
				adTemplate.setPicShowIntervalTime(picShowIntervalTime);
				adTemplate.setPlayOrder(playOrder);
				adTemplate.setEnabled(isEnabled);
				adTemplate.setDate(new Date());
				
				List<FileInfoObj> picFileObjArr = adTemplate.getPicFileObjArr();
				picFileObjArr.removeAll(Collections.singleton(null));
				for(FileInfoObj obj : picFileObjArr) { // 删除原来的数据
					if(obj.getType().startsWith("image")) {
						adTemplateDao.deleteOneFileObj(obj.getId());
					}					
				}				
				JSONArray picFileJSONArr = jsonObj.getJSONArray("picFileObjArr"); // 图片的对象数组
				List<FileInfoObj> picFiles = new ArrayList<FileInfoObj>();
				for(int i = 0; i < picFileJSONArr.length(); i++) {
					JSONObject obj = picFileJSONArr.getJSONObject(i);
					FileInfoObj fileObj = new FileInfoObj();
					fileObj.setAdTemplate(adTemplate);
					fileObj.setName(obj.getString("name"));
					fileObj.setSize(obj.getInt("size"));
					fileObj.setType(obj.getString("type"));
					fileObj.setUploadUUID(obj.getString("uploadUUID"));
					fileObj.setUrl(obj.getString("url"));
					picFiles.add(fileObj);
				}
				adTemplate.setPicFileObjArr(picFiles);
				
				List<FileInfoObj> videoFileObjArr = adTemplate.getVideoFileObjArr();
				videoFileObjArr.removeAll(Collections.singleton(null));
				for(FileInfoObj obj : videoFileObjArr) { // 删除原来的数据
					if(obj.getType().startsWith("video")) {
						adTemplateDao.deleteOneFileObj(obj.getId());
					}					
				}				
				JSONArray videoFileJSONArr = jsonObj.getJSONArray("videoFileObjArr"); // 视频文件的对象数组
				List<FileInfoObj> videoFiles = new ArrayList<FileInfoObj>();
				for(int j = 0; j < videoFileJSONArr.length(); j++) {
					JSONObject obj = videoFileJSONArr.getJSONObject(j);
					FileInfoObj fileObj = new FileInfoObj();
					fileObj.setAdTemplate(adTemplate);
					fileObj.setName(obj.getString("name"));
					fileObj.setSize(obj.getInt("size"));
					fileObj.setType(obj.getString("type"));
					fileObj.setUploadUUID(obj.getString("uploadUUID"));
					fileObj.setUrl(obj.getString("url"));
					videoFiles.add(fileObj);
				}
				adTemplate.setVideoFileObjArr(videoFiles);
				adTemplateDao.updateAdTemplate(adTemplate);
				
				retObj.setCode(0);
				retObj.setMessage("更新广告模板成功");
			}						
		}
		
		return new JSONObject(retObj).toString();
	}
	
	/**
	 * 加载所有的广告模板
	 * */
	@RequestMapping(value="/getAdTemplateList") // ", method = RequestMethod.GET" 指定改请求为Get方式，默认get或者post方式都可以请求
	@ResponseBody
	public String getAdTemplateList(@RequestBody String body){
		ReturnObject retObj = new ReturnObject();
		
		if(body != null && body != ""){						
			JSONArray listJSONArr = new JSONArray();
			List<AdTemplate> list = adTemplateDao.getAllAdTemplate();
			int index = 1;
			for(AdTemplate item : list) {
				JSONObject obj = new JSONObject();
				obj.put("orderId", index++);
				obj.put("id", item.getId());
				obj.put("adTemplateName", item.getAdTemplateName());
				obj.put("picShowIntervalTime", item.getPicShowIntervalTime());
				obj.put("playOrder", item.getPlayOrder());
				obj.put("isEnabled", item.isEnabled());
				obj.put("date", item.getDate());
				listJSONArr.put(obj);
			}
			
			JSONObject retJSONObj = new JSONObject();
			retJSONObj.put("code", 0);
			retJSONObj.put("message", "操作成功");
			retJSONObj.put("items", listJSONArr);
			return retJSONObj.toString();
		}
		
		return new JSONObject(retObj).toString();
	}
	
	/**
	 * 根据模板id获取模板的详细信息
	 * */
	@RequestMapping(value="/getAdTemplateById") // ", method = RequestMethod.GET" 指定改请求为Get方式，默认get或者post方式都可以请求
	@ResponseBody
	public String getAdTemplateById(@RequestBody String body){
		ReturnObject retObj = new ReturnObject();
		
		if(body != null && body != ""){
			JSONObject jsonObj = new JSONObject(body);
			int templateId = jsonObj.getInt("templateId");
			
			AdTemplate adTemplate = adTemplateDao.getAdTemplateById(templateId);
			if (adTemplate != null) {
				List<FileInfoObj> listFileObjArr = adTemplate.getPicFileObjArr();
				listFileObjArr.removeAll(Collections.singleton(null)); // 去除List中为空的元素				
				adTemplate.setPicFileObjArr(null);
				adTemplate.setVideoFileObjArr(null);
				
				List<FileInfoObj> picFileObjArr = new ArrayList<FileInfoObj>();
				List<FileInfoObj> videoFileObjArr = new ArrayList<FileInfoObj>();
				for(FileInfoObj obj : listFileObjArr) {
					if(obj.getType().startsWith("image")) {
						picFileObjArr.add(obj);
					} else if(obj.getType().startsWith("video")) {
						videoFileObjArr.add(obj);
					}
				}
				
				JSONObject retJSONObj = new JSONObject();
				retJSONObj.put("uploadObj", new JSONObject(adTemplate));
				retJSONObj.put("picFileObjArr", new JSONArray(picFileObjArr));
				retJSONObj.put("videoFileObjArr", new JSONArray(videoFileObjArr));
				retJSONObj.put("code", 0);
				retJSONObj.put("message", "操作成功");				
				return retJSONObj.toString();
			}
		}
		
		return new JSONObject(retObj).toString();
	}
	
	/**
	 * 根据机器人编号获取绑定的广告模板数据
	 * */
	@RequestMapping(value="/getAdTemplateJSONObj") // ", method = RequestMethod.GET" 指定改请求为Get方式，默认get或者post方式都可以请求
	@ResponseBody
	public String getAdTemplateJSONObj(@RequestBody String body0){
		ReturnObject retObj = new ReturnObject();
		String body = "";
		if(body0 != null && body0 != ""){
			try {
				body = URLDecoder.decode(body0, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			body = body.substring(0, body.length()-1);
			JSONObject jsonObj = new JSONObject(body);
			String machineId = jsonObj.getString("machineId");
			Robot robot = robotDao.getRobotByMachineId(machineId);
			if(robot != null) {
				int adId = robot.getAdId();
				AdTemplate adTemplate = adTemplateDao.getAdTemplateById(adId);
				if (adTemplate != null) {
					List<FileInfoObj> picFileObjArr = adTemplate.getPicFileObjArr();
					picFileObjArr.removeAll(Collections.singleton(null));
					JSONArray picJSONArr = new JSONArray();
					JSONArray videoJSONArr = new JSONArray();
					for(FileInfoObj fileObj : picFileObjArr) {
						if(fileObj.getType().startsWith("image")) {
							picJSONArr.put(fileObj.getUrl());
						} else if(fileObj.getType().startsWith("video")) {
							videoJSONArr.put(fileObj.getUrl());
						}					
					}														
					
					JSONObject obj = new JSONObject();
					obj.put("cmdStr", "adData");
					obj.put("picShowIntervalTime", adTemplate.getPicShowIntervalTime()); // 图片轮播的时间间隔
					obj.put("picFileObjArr", picJSONArr);
					obj.put("videoFileObjArr", videoJSONArr);
					obj.put("code", 0);
					obj.put("message", "操作成功");
					// System.out.println("--发送广告数据 = " + obj.toString());
					return obj.toString();
				} else {
					System.out.println("--广告id对象不存在， adId = " + adId);
					retObj.setMessage("广告模板对象不存在");
				}
			} else {
				retObj.setMessage("指定编号机器人不存在");
			}
			
		}
		
		return new JSONObject(retObj).toString();
	}
	
}
