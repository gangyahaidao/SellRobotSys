package asr.qingpu.controller;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.annotation.Resource;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.qingpu.common.entity.ReturnObject;
import com.qingpu.common.service.BaseLogService;
import com.qingpu.common.utils.QingpuConstants;

import asr.qingpu.user.dao.AsrUserDao;
import asr.qingpu.user.entity.AsrUser;
import asr.qingpu.wavfile.dao.AsrTranslateDao;
import asr.qingpu.wavfile.entity.AsrCopyRecord;
import asr.qingpu.wavfile.entity.AsrUserAdvice;
import asr.qingpu.wavfile.entity.NewWavFileUrl;
import asr.qingpu.wavfile.entity.TotalDailyRecord;
import asr.qingpu.wavfile.entity.UserDailyRecord;
import asr.qingpu.wavfile.entity.UserWorkLog;

/**
 * 本类主要是从另一个数据库加载指定数量的原始文件url，然后保存在本地数据库中，之后从本地数据库中加载url从文件服务器下载音频文件，上传到百度语音识别进行VAD切分，将切分之后的文件上传在本地的另一个文件服务器中，保存新的url
 * */
@Controller
@RequestMapping("/processwav")
public class TranslateWavFileController extends HandlerInterceptorAdapter {

	private static Connection connection = null;
	
	@Resource
	AsrUserDao asrUserDao;
	@Resource
	AsrTranslateDao asrTranslateDao;
	@Resource
	BaseLogService<Object> baseLogService; // 进行日志对象的保存
	
	private Connection getConnection() {
		if (connection != null)
			return connection;
		else {
			try {
				Properties prop = new Properties();
				prop.load(new FileInputStream(TranslateWavFileController.class.getClassLoader().getResource("db.properties").getPath()));
				
				String driver = prop.getProperty("driver");
				String url = prop.getProperty("url");
				String user = prop.getProperty("user");
				String password = prop.getProperty("password");
			
				Class.forName(driver);
				
				connection = DriverManager.getConnection(url, user, password);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return connection;
		}
	}		
	
	/**
	 * admin用户获取全部的已经翻译的数量
	 * */
	@RequestMapping("/getTotalTranslateCount")
	@ResponseBody
	public String getTotalTranslateCount(@RequestBody String body) {
		ReturnObject retObj = new ReturnObject();
		
		if(body != null && body != "") {
			JSONObject jsonObj = new JSONObject(body);
			
			String token = jsonObj.getString("token");
			long count = asrTranslateDao.getTotalCounts(); // 需要减去1，因为当没有数据时默认返回是1
			JSONObject retJSON = new JSONObject();
			retJSON.put("count", count);
			retJSON.put("code", 0);
			retJSON.put("message", "操作成功");
			
			return retJSON.toString();		
		}
		
		return new JSONObject(retObj).toString();
	}
	
	/**
	 * 普通用户获取自己已经完成的数量
	 * */
	@RequestMapping("/getUserTranslateCount")
	@ResponseBody
	public String getUserTranslateCount(@RequestBody String body) {
		ReturnObject retObj = new ReturnObject();
		
		if(body != null && body != "") {
			JSONObject jsonObj = new JSONObject(body);
			
			String token = jsonObj.getString("token");
			List<AsrUser> userList = asrUserDao.getUserByToken(token);
			if(userList.size() > 0) {
				AsrUser user = userList.get(0);
				int count = user.getHasFinishedCount();
				JSONObject retJSON = new JSONObject();
				retJSON.put("count", count);
				retJSON.put("code", 0);
				retJSON.put("message", "操作成功");
				
				return retJSON.toString();
			}else {
				retObj.setMessage("用户Token不存在");
			}			
		}
		
		return new JSONObject(retObj).toString();
	}
	
	/**
	 * 获取当前总的还剩余待翻译的数据量
	 * */
	@RequestMapping("/getCurrentTotalLeftCounts")
	@ResponseBody
	public String getCurrentTotalLeftCounts(@RequestBody String body) {
		ReturnObject retObj = new ReturnObject();
		
		if(body != null && body != "") {
			JSONObject jsonObj = new JSONObject(body);
			
			long count = asrTranslateDao.getLeftTotalCounts(); // 获取剩余的数量
			
			JSONObject retJSON = new JSONObject();
			retJSON.put("count", count);
			retJSON.put("code", 0);
			retJSON.put("message", "操作成功");
			
			return retJSON.toString();
		}
		
		return new JSONObject(retObj).toString();
	}
	
	/**
	 * 从第三方数据库下载指定数量的原始文件url
	 * */
	@RequestMapping("/downloadFromThird")
	@ResponseBody
	public String downloadFromThird(@RequestBody String body) {
		ReturnObject retObj = new ReturnObject();
		
		if(body != null && body != ""){ // 判断用户是否发起的空请求			
			JSONObject jsonObject = new JSONObject(body);
			int downloadItemsCount = jsonObject.getInt("addCount");
			//获取当前已经查询的条数
			int currentTotalCount = asrTranslateDao.getCurrentTotalCopyCount();
			this.connection = getConnection(); // 连接第三方数据库
			String sql = "select create_time, part_record1, part_record2, start_time from group_call_detail where start_time is not null and part_record1 is not null or part_record2 is not null order by start_time asc limit " + currentTotalCount + "," + downloadItemsCount; 
			
			try {
				Statement statement = connection.createStatement();
				ResultSet rs = statement.executeQuery(sql);
				while (rs.next()) {					
					String part_record1 = rs.getString("part_record1");
					if(part_record1 != null && part_record1.length() > 0) { // 保存到数据库
						NewWavFileUrl newFileObj = new NewWavFileUrl();
						newFileObj.setDate(new Date());
						newFileObj.setFinished(false); // 还没有被完成
						newFileObj.setFinishedUserId(0); // 还没有完成的用户
						newFileObj.setNewFileUrl(part_record1);
						newFileObj.setOccupied(false); // 还没有被分配给用户
						asrTranslateDao.saveNewFileUrl(newFileObj);											
					}
					String part_record2 = rs.getString("part_record2");
					if(part_record2 != null && part_record2.length() > 0) {
						NewWavFileUrl newFileObj = new NewWavFileUrl();
						newFileObj.setDate(new Date());
						newFileObj.setFinished(false);
						newFileObj.setFinishedUserId(0);
						newFileObj.setNewFileUrl(part_record2);
						newFileObj.setOccupied(false);
						asrTranslateDao.saveNewFileUrl(newFileObj);						
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			//更新复制记录
			AsrCopyRecord copy = new AsrCopyRecord();
			copy.setCurrentCopyTaskCount(downloadItemsCount);
			copy.setAllTotalCopys(currentTotalCount + downloadItemsCount);
			copy.setCopyDate(new Date());			
			asrTranslateDao.saveOneCopyRecord(copy);
			
			retObj.setCode(0);
			retObj.setMessage("操作成功");
		}
		
		return new JSONObject(retObj).toString();
	}
	
	/**
	 * 加载所有当前分配给用户的数据，每天晚上十一点会进行一次统计用于计算周完成量，不去掉用户已经完成的数据，只是在页面上添加一个加载过滤选项
	 * 如果用户待完成的任务量<=50则分配指定量的数据
	 * */
	@RequestMapping("/getUserTaskItems")
	@ResponseBody
	public String getUnfinishedItems(@RequestBody String body) {
		ReturnObject retObj = new ReturnObject();
		
		if(body != null && body != "") {
			JSONObject jsonObj = new JSONObject(body);
			
			String token = jsonObj.getString("token");
			String wavStatus = jsonObj.getString("wavStatus"); // 所要加载的数据完成状态过滤，默认是all
			
			List<AsrUser> userList = asrUserDao.getUserByToken(token);
			if(userList.size() > 0) {
				AsrUser user = userList.get(0);
				List<NewWavFileUrl> taskItemsList = null;
				if("admin".equals(user.getUsername())) { // 如果是管理员用户，则随机加载指定条数的数据，只要是为了检测转译质量
					taskItemsList = asrTranslateDao.getRandomFinishedLimit(QingpuConstants.TaskPartCount);
				} else {
					if("all".equals(wavStatus)) { // 加载所有的数据
						taskItemsList = asrTranslateDao.getUserTaskItemList(user.getId()); // 加载所有已经分配给指定用户的数据，加载还没有完成的数据
						int unFinishedCount = 0;
						for(NewWavFileUrl item : taskItemsList) { // 计算列表中待完成的任务
							if(!item.isFinished()) {
								unFinishedCount++;
							}
						}
						synchronized(this) { // 分配任务和更新待完成数据条目状态使用互斥的方式									
							if(unFinishedCount >= 0 && unFinishedCount <= 50) { // 如果用待完成数据条数少于50，在当前任务中新添加200条
								List<NewWavFileUrl> newList = asrTranslateDao.getUnfinishedItems(user, QingpuConstants.TaskPartCount);
								for (NewWavFileUrl item : newList) {
									item.setOccupied(true); // 设置已经被占用
									item.setUserId(user.getId()); //设置所属的用户id
									asrTranslateDao.updateNewWavFileUrl(item);// 设置完成之后进行更新,在查找中进行更新不被允许，因为getUnfinishedItems方法是只读的
								}
								taskItemsList.addAll(newList);
							}
						}
					} else if("undone".equals(wavStatus)) { // 只加载未完成的
						taskItemsList = asrTranslateDao.getByConditionList(user.getId(), false);
						synchronized(this) { // 分配任务和更新待完成数据条目状态使用互斥的方式									
							if(taskItemsList.size() >= 0 && taskItemsList.size() <= 50) { // 如果用待完成数据条数少于50，在当前任务中新添加200条
								List<NewWavFileUrl> newList = asrTranslateDao.getUnfinishedItems(user, QingpuConstants.TaskPartCount);
								for (NewWavFileUrl item : newList) {
									item.setOccupied(true); // 设置已经被占用
									item.setUserId(user.getId()); //设置所属的用户id
									asrTranslateDao.updateNewWavFileUrl(item);// 设置完成之后进行更新,在查找中进行更新不被允许，因为getUnfinishedItems方法是只读的
								}
								taskItemsList.addAll(newList);
							}
						}
					} else if("done".equals(wavStatus)) { //加载已经完成的
						taskItemsList = asrTranslateDao.getByConditionList(user.getId(), true);
					}					
				}	
				taskItemsList.removeAll(Collections.singleton(null)); // 去除List中为空的元素
				
				int page = jsonObj.getInt("page"); // 加载的页数
				int limit = jsonObj.getInt("limit"); // 一页数据的条数
				String sortType = jsonObj.getString("sort");				
				int startIndex = (page-1)*limit;
				int endIndex = page*limit > taskItemsList.size() ? taskItemsList.size() : page*limit;
								
				JSONArray retJSONArr = new JSONArray();
				int hasFinishedCountInPage = 0; // 在所加载的一页数据中已经完成了多少				
				if("+id".equals(sortType)) { // 增序
					for(int index = startIndex; index < endIndex; index++) {
						NewWavFileUrl item = taskItemsList.get(index);
						JSONObject obj = new JSONObject();
						if(item.isFinished()) {
							hasFinishedCountInPage++;
						}
						obj.put("id", item.getId());
						obj.put("orderId", index+1);
						String fileurl = item.getNewFileUrl();
						//进行文件服务器地址的替换
						String reg = "((2[0-4]\\d|25[0-5]|[01]?\\d\\d?)\\.){3}(2[0-4]\\d|25[0-5]|[01]?\\d\\d?)"; //匹配ip的正则
						fileurl = fileurl.replaceFirst(reg, QingpuConstants.FASTDFS_SERVER_ADDR);//替换第一个
						obj.put("fileurl", fileurl);
						obj.put("translateStr", item.getTranslateWords());
						obj.put("status", item.isFinished() ? "done" : "undone");
						retJSONArr.put(obj);
					}
				} else if("-id".equals(sortType)) {
					for(int index = endIndex-1; index > startIndex-1; index--) {
						NewWavFileUrl item = taskItemsList.get(index);
						JSONObject obj = new JSONObject();
						if(item.isFinished()) {
							hasFinishedCountInPage++;
						}
						obj.put("id", item.getId()); // 数据的主键
						obj.put("orderId", index+1);
						String fileurl = item.getNewFileUrl();
						//进行文件服务器地址的替换
						String reg = "((2[0-4]\\d|25[0-5]|[01]?\\d\\d?)\\.){3}(2[0-4]\\d|25[0-5]|[01]?\\d\\d?)"; //匹配ip的正则
						fileurl = fileurl.replaceFirst(reg, QingpuConstants.FASTDFS_SERVER_ADDR);//替换第一个
						obj.put("fileurl", fileurl);
						obj.put("translateStr", item.getTranslateWords());
						obj.put("status", item.isFinished() ? "done" : "undone");
						retJSONArr.put(obj);						
					}
				}
				
				JSONObject retJSON = new JSONObject();
				retJSON.put("items", retJSONArr);
				retJSON.put("total", taskItemsList.size());
				if(hasFinishedCountInPage == limit) {
					hasFinishedCountInPage = 0;
				}
				retJSON.put("hasFinishedCountInPage", hasFinishedCountInPage);
				retJSON.put("code", 0);
				retJSON.put("message", "操作成功");
				
				return retJSON.toString();
			} else {
				retObj.setMessage("Token对应用户不存在");
			}
		}
		
		return new JSONObject(retObj).toString();		
	}
	
	/**
	 * 更新一条翻译数据
	 * */
	@RequestMapping("/updateOneItemData")
	@ResponseBody
	public String updateOneItemData(@RequestBody String body) {
		ReturnObject retObj = new ReturnObject();
		
		if(body != null && body != "") {
			JSONObject jsonObj = new JSONObject(body);
			
			String token = jsonObj.getString("token");
			List<AsrUser> userList = asrUserDao.getUserByToken(token);
			if(userList.size() > 0) {
				AsrUser user = userList.get(0);
				int id = jsonObj.getInt("id");
				//1.更新NewFileUrl对象
				NewWavFileUrl fileObj = asrTranslateDao.getNewWavFileUrlById(id);
				if(fileObj != null) {
					//修改AsrUser中完成的字段
					if(!fileObj.isFinished()) { //如果是还没有完成则+1
						user.setHasFinishedCount(user.getHasFinishedCount()+1);
						asrUserDao.updateUser(user);
					}					
					
					if(jsonObj.has("translateStr")) {
						fileObj.setTranslateWords(jsonObj.getString("translateStr"));
					}					
					fileObj.setFinished(true);
					fileObj.setOccupied(false);
					fileObj.setFinishedUserId(user.getId()); // 设置完成人的主键id
					asrTranslateDao.updateNewWavFileUrl(fileObj);															
					
					//3.增加一条记录log
					UserWorkLog workLog = new UserWorkLog();
					workLog.setUserId(user.getId());
					workLog.setNewWavFileId(fileObj.getId());
					workLog.setDate(new Date());
					baseLogService.saveLog(workLog);
					
					retObj.setCode(0);
					retObj.setMessage("操作成功");
				} else {
					retObj.setMessage("所要更新的文件不存在");
				}
				
			} else {
				retObj.setMessage("用户Token不存在");
			}							
		}
		
		return new JSONObject(retObj).toString();
	}
	
	/**
	 * 获取总的近一周每天的总完成量，从当前往前数一周的数据，每天的完成量 
	 * */
	@RequestMapping("/getTotalFinishWeekData")
	@ResponseBody
	public String gettotalFinishWeekData(@RequestBody String body) {
		ReturnObject retObj = new ReturnObject();
		
		if(body != null && body != "") {
			List<TotalDailyRecord> totalWeekRecordList = asrTranslateDao.getWeekFinishedData(); // 查找一周前完成的总任务量
			List<String> weekNameList = new ArrayList<String>();
			List<Integer> dailyList = new ArrayList<Integer>();
			for(int index = 0; index < totalWeekRecordList.size(); index++) {
				TotalDailyRecord item = totalWeekRecordList.get(index);
				weekNameList.add(item.getWeekDayName());
				dailyList.add(item.getDailyCounts());
			}
			
			JSONObject retJSON = new JSONObject();
			retJSON.put("weekNameItems", new JSONArray(weekNameList));
			retJSON.put("dailyCountsItem", new JSONArray(dailyList));
			retJSON.put("code", 0);
			retJSON.put("message", "操作成功");
			
			return retJSON.toString();			
		}
		
		return new JSONObject(retObj).toString();
	}
	
	/**
	 * 用户获取当前自己的前一周任务量
	 * */
	@RequestMapping("/getUserFinishWeekData")
	@ResponseBody
	public String getUserFinishWeekData(@RequestBody String body) {
		ReturnObject retObj = new ReturnObject();
		
		if(body != null && body != "") {
			JSONObject jsonObj = new JSONObject(body);
			
			String token = jsonObj.getString("token");
			List<AsrUser> userList = asrUserDao.getUserByToken(token);
			if(userList.size() > 0) {
				AsrUser user = userList.get(0);
				
				List<UserDailyRecord> userDailyList = asrTranslateDao.getUserDailyWeekData(user.getId());
				List<String> weekNameList = new ArrayList<String>();
				List<Integer> dailyList = new ArrayList<Integer>();
				for (int i =0; i < userDailyList.size(); i++) {
					UserDailyRecord item = userDailyList.get(i);
					weekNameList.add(item.getWeekDayName());
					dailyList.add(item.getDailyCounts());
				}
				JSONObject retJSON = new JSONObject();
				retJSON.put("weekNameItems", new JSONArray(weekNameList));
				retJSON.put("dailyCountsItem", new JSONArray(dailyList));
				retJSON.put("code", 0);
				retJSON.put("message", "操作成功");
				
				return retJSON.toString();
			}
		}
		
		return new JSONObject(retObj).toString();
	}
	
	/**
	 * 提交意见
	 * */
	@RequestMapping("/uploadUserAdvice")
	@ResponseBody
	public String uploadUserAdvice(@RequestBody String body) {
		ReturnObject retObj = new ReturnObject();
		
		if(body != null && body != "") {
			JSONObject jsonObj = new JSONObject(body);
			
			String token = jsonObj.getString("token");
			List<AsrUser> userList = asrUserDao.getUserByToken(token);
			if(userList.size() > 0) {
				AsrUser user = userList.get(0);
				AsrUserAdvice advice = new AsrUserAdvice();
				advice.setUserId(user.getId());
				advice.setContent(jsonObj.getString("content"));
				baseLogService.saveLog(advice);
				
				retObj.setCode(0);
				retObj.setMessage("操作成功");
			}else{
				retObj.setMessage("用户不存在");
			}
		}else {
			retObj.setMessage("提交数据失败");
		}
		
		return new JSONObject(retObj).toString();
	}
}
