package com.qingpu.robots.dao;

import java.util.List;

import com.qingpu.robots.entity.FloorPosName;
import com.qingpu.robots.entity.PathListData;
import com.qingpu.robots.entity.Robot;
import com.qingpu.robots.entity.RobotOtherDialog;
import com.qingpu.robots.entity.RobotPatrolOrSenseDialog;
import com.qingpu.robots.entity.RobotTalkGroup;
import com.qingpu.robots.entity.TalkTemplate;

public interface RobotsDao {
	List<Robot> getRobotsList();
	
	void addRobot(Robot robot);
	
	void updateRobotInfo(Robot robot);

	Robot getRobotById(int id);
	
	Robot getRobotByMachineId(String machineId);

	List<Robot> getRobotListByFloorName(String floorStr);	

	void deleteOneFloor(int id);
	
	/**
	 * 机器人对话相关的
	 * @param groupId 
	 * */
	List<RobotPatrolOrSenseDialog> getPatrolRobotFreeGoingDialog(int groupId, String type, String state, String timeIntervalName);
	
	List<RobotPatrolOrSenseDialog> getPatrolRobotDialog(int groupId, String type, String state, String timeIntervalName, String currentSelectName);

	List<RobotPatrolOrSenseDialog> getSenseRobotDialog(int groupId, String type, String state, String currentSelectName);

	List<RobotOtherDialog> getOtherRobotDialog(int groupId, String type, String state);

	void addPatrolOrSenseRobotDialog(RobotPatrolOrSenseDialog patrolOrSenseDialog);

	void addOtherRobotDialog(RobotOtherDialog otherDialog);

	RobotPatrolOrSenseDialog getPatrolOrSenseRobotDialogById(int id);

	void updatePatrolOrSenseRobotDialog(RobotPatrolOrSenseDialog patrolOrSenseRobot);

	RobotOtherDialog getOtherRobotDialogById(int id);

	void updateOtherRobotDialog(RobotOtherDialog otherRobotDialog);

	void deletePatrolOrSenseRobotDialog(RobotPatrolOrSenseDialog patrolOrSenseRobot);

	void deleteOtherRobotDialog(RobotOtherDialog otherRobotDialog);

	/**
	 * 获取感应到人的对话
	 * 参数1：对话类型
	 * 参数2：对话状态
	 * 参数3：检测到的性别结果
	 * */
	List<RobotPatrolOrSenseDialog> getSensePeopleDialogs(String type, String state, String genderStr);

	/**
	 * 根据条件查询巡逻模式下的对话
	 * */
	List<RobotPatrolOrSenseDialog> getPatrolDialogByCondition(String timeIntervalName, String state, String reachGoalName);

	void addRobotTemplateTalk(TalkTemplate talk);

	void deleteRobotTemplateTalk(String type, int orderId);

	List<TalkTemplate> getRobotTalkTemplateList();

	List<TalkTemplate> getRobotTalkTemplateByType(String type);

	void savePathData(PathListData path);

	List<PathListData> getPathLists();

	List<PathListData> getPathListsByFloorName(String floorName);

	PathListData getPathById(int pathId);

	void updatePathData(PathListData path);

	RobotTalkGroup saveNewTalkGroup(RobotTalkGroup group);

	List<RobotTalkGroup> getRobotTalkGroup(String searchStr);

	RobotTalkGroup getRobotTalkGroupById(int id);

	void updateRobotGroupTalk(RobotTalkGroup group);

	void deleteRobotTalkGroup(int id);

	void deleteRobotTalkBelongtoGroupId(int id);

	void deleteRobot(Robot robot);

	void saveFloorPosNameObj(FloorPosName posNameObj);

	FloorPosName getFloorPosNameArr(String floorName);

}
