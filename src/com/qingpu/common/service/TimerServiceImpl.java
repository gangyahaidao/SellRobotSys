package com.qingpu.common.service;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.qingpu.common.utils.CommonUtils;
import com.qingpu.common.utils.QingpuConstants;
import com.qingpu.socketservice.ResponseSocketUtils;
import com.qingpu.socketservice.RobotClientSocket;
import com.qingpu.socketservice.ServerSocketThreadRobot;

import asr.qingpu.user.dao.AsrUserDao;
import asr.qingpu.user.entity.AsrUser;
import asr.qingpu.wavfile.dao.AsrTranslateDao;
import asr.qingpu.wavfile.entity.TotalDailyRecord;
import asr.qingpu.wavfile.entity.UserDailyRecord;
import asr.qingpu.wavfile.entity.UserWorkLog;

@Service("timerService")
public class TimerServiceImpl implements TimerService {

	@Resource
	AsrUserDao asrUserDao;
	@Resource
	AsrTranslateDao asrTranslateDao;
	@Resource
	BaseLogService<Object> baseLogService; // 进行日志对象的保存
	
	/**
	 * 音频翻译任务中，计算用户完成的任务数目
	 * */
	@Override
	// @Scheduled(cron = "*/1 * * * * ?") // 一秒钟触发一次
	// @Scheduled(cron = "0 41 22 * * ?") //每天晚上的十一点触发
	public synchronized void processAsrUserFinishedCounts() {		
		System.out.println("-- 触发定时器任务" + new Date().toString());
		//查找当前所有的用户
		List<AsrUser> userList = asrUserDao.findAllUsers();
		System.out.println("--当前共有用户：" + userList.size());
		int totalDailyFinishedCount = 0;
		for(AsrUser user: userList) {
			//根据userId去完成日志数据库中查找本用户今天总的完成量
			int userId = user.getId();
			List<UserWorkLog> dailyWorkList = asrTranslateDao.getUserOneDayList(userId);
			int todayFinishCount = dailyWorkList.size();
			totalDailyFinishedCount += todayFinishCount; // 总计累加
			UserDailyRecord dailyRecord = new UserDailyRecord();
			dailyRecord.setDailyCounts(todayFinishCount);
			dailyRecord.setDate(new Date());
			dailyRecord.setUserId(userId);
			dailyRecord.setWeekDayName(CommonUtils.dateToWeekDayStr(new Date()));
			baseLogService.saveLog(dailyRecord);
		}
		TotalDailyRecord totalRecord = new TotalDailyRecord();
		totalRecord.setDailyCounts(totalDailyFinishedCount);
		totalRecord.setDate(new Date());
		totalRecord.setWeekDayName(CommonUtils.dateToWeekDayStr(new Date()));
		baseLogService.saveLog(totalRecord);
		
		System.out.println("--定时器任务执行完毕: " + new Date().toString());
	}
	
	/**
	 * 零售机器人售卖任务中用于定时执行路径规划功能
	 * */
	@Scheduled(cron = "*/10 * * * * ?") // 十秒钟触发一次
	public synchronized void processPathPlanTask() {
		// 遍历底盘连接map对象
		Iterator<Entry<String, RobotClientSocket>> it = ServerSocketThreadRobot.robotMachineMap.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry<String, RobotClientSocket> entry = it.next();
			RobotClientSocket robotClient = entry.getValue();//消息回复对象
			
			long startMiliSec = robotClient.getStartLoopMiliTime();
			long stopMiliSec = robotClient.getStopLoopMiliTime();
			long currentMiliSec = new Date().getTime();
			if(startMiliSec > 0) { // 如果有启动时间才需要进行定时器启动
				if(currentMiliSec >= startMiliSec) { // 如果到达启动时间，则开始向底盘发送启动命令
					if(!robotClient.isHasTimerSendStartMove()) { // 如果还没有发送启动命令
						robotClient.setHasTimerSendStartMove(true);// 设置已经发送了开始运动命令
						robotClient.setHasRobotReachedGoal(false); // 设置机器人处于运动模式，不响应网页上的控制命令
						
						JSONObject jsonObj = new JSONObject();
						jsonObj.put("carOneGoalPosName", robotClient.getPosStayTimeJSONArr());
						System.out.println("--定时器发送启动运行路径 = " + jsonObj.toString());
						ResponseSocketUtils.sendJsonDataToClient(
								jsonObj, 
								robotClient.getClient(),
								QingpuConstants.SEND_ROBOT_GOAL,
								QingpuConstants.ENCRYPT_BY_NONE,
								QingpuConstants.DATA_TYPE_JSON);
					}					
				}
			}			
			if(!robotClient.isHasRobotReachedGoal() && stopMiliSec > 0 && currentMiliSec >= stopMiliSec) { // 如果机器人处于非停靠状态且设置了停止时间且,则发送起始点给机器人立即进行归位								
				if(!robotClient.isHasTimerSendStopMove()) { // 如果还没有发送过停止命令
					System.out.println("--定时器发送停止循环停止命令");
					robotClient.setHasTimerSendStopMove(true); // 设置已经发送了停止命令
					robotClient.setNeedStopLoopMove(true);
				}
			}
		}
	}
	
}
