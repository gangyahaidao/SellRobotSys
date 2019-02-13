package com.qingpu.robots.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.qingpu.common.dao.BaseDaoImpl;
import com.qingpu.robots.entity.FloorPosName;
import com.qingpu.robots.entity.PathListData;
import com.qingpu.robots.entity.Robot;
import com.qingpu.robots.entity.RobotOtherDialog;
import com.qingpu.robots.entity.RobotPatrolOrSenseDialog;
import com.qingpu.robots.entity.RobotTalkGroup;
import com.qingpu.robots.entity.TalkTemplate;

@Repository("robotDao")
public class RobotsDaoImpl extends BaseDaoImpl implements RobotsDao {

	@SuppressWarnings("unchecked")
	@Override
	public List<Robot> getRobotsList() {
		return (List<Robot>) findByHql("from Robot");
	}

	@Override
	public void updateRobotInfo(Robot robot) {
		update(robot);
	}

	@Override
	public void addRobot(Robot robot) {
		save(robot);
	}

	@Override
	public Robot getRobotById(int id) {
		return (Robot) get(Robot.class, id);
	}
	
	@Override
	public Robot getRobotByMachineId(String machineId) {
		String hql = "from Robot where machineId=?";
		@SuppressWarnings("unchecked")
		List<Robot> list = (List<Robot>) findByHqlParams(hql, new Object[]{machineId});
		if(list.size() > 0) {
			return list.get(0);
		}else {
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Robot> getRobotListByFloorName(String floorStr) {
		String hql = "from Robot where floor=?";
		return (List<Robot>) findByHqlParams(hql, new Object[]{floorStr});
	}
	
	@Override
	public void deleteOneFloor(int id) {
		String hql = "delete from OneContainerFloor where id=?";
		execQueryHqlUpdate(hql, new Object[]{id});
	}


	@SuppressWarnings("unchecked")
	@Override
	public List<RobotPatrolOrSenseDialog> getPatrolRobotFreeGoingDialog(int groupId, String type, String state, String timeIntervalName) {
		String hql = "from RobotPatrolOrSenseDialog where groupId=? and type=? and state=? and timeIntervalName=?";
		
		return (List<RobotPatrolOrSenseDialog>) findByHqlParams(hql, new Object[]{groupId, type, state, timeIntervalName});
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<RobotPatrolOrSenseDialog> getPatrolRobotDialog(int groupId, String type, String state, String timeIntervalName, String currentSelectName) {
		String hql = "from RobotPatrolOrSenseDialog where groupId=? and type=? and state=? and timeIntervalName=? and reachGoalName=?";
		
		return (List<RobotPatrolOrSenseDialog>) findByHqlParams(hql, new Object[]{groupId, type, state, timeIntervalName, currentSelectName});		
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<RobotPatrolOrSenseDialog> getSenseRobotDialog(int groupId, String type, String state, String currentSelectName) {
		String hql = "from RobotPatrolOrSenseDialog where groupId=? and type=? and state=? and peopleInfo=?";
		
		return (List<RobotPatrolOrSenseDialog>) findByHqlParams(hql, new Object[]{groupId, type, state, currentSelectName});
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<RobotOtherDialog> getOtherRobotDialog(int groupId, String type, String state) {
		String hql = "from RobotOtherDialog where groupId=? and type=? and state=?";
		
		return (List<RobotOtherDialog>) findByHqlParams(hql, new Object[]{groupId, type, state});
	}

	@Override
	public void addPatrolOrSenseRobotDialog(RobotPatrolOrSenseDialog patrolOrSenseDialog) {
		save(patrolOrSenseDialog);
	}

	@Override
	public void addOtherRobotDialog(RobotOtherDialog otherDialog) {
		save(otherDialog);
	}

	@Override
	public RobotPatrolOrSenseDialog getPatrolOrSenseRobotDialogById(int id) {
		return (RobotPatrolOrSenseDialog) get(RobotPatrolOrSenseDialog.class, id);
	}

	@Override
	public void updatePatrolOrSenseRobotDialog(RobotPatrolOrSenseDialog patrolOrSenseRobot) {
		update(patrolOrSenseRobot);
	}

	@Override
	public RobotOtherDialog getOtherRobotDialogById(int id) {
		return (RobotOtherDialog) get(RobotOtherDialog.class, id);
	}

	@Override
	public void updateOtherRobotDialog(RobotOtherDialog otherRobotDialog) {
		update(otherRobotDialog);
	}

	@Override
	public void deletePatrolOrSenseRobotDialog(RobotPatrolOrSenseDialog patrolOrSenseRobot) {
		delete(patrolOrSenseRobot);
	}

	@Override
	public void deleteOtherRobotDialog(RobotOtherDialog otherRobotDialog) {
		delete(otherRobotDialog);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<RobotPatrolOrSenseDialog> getSensePeopleDialogs(String type, String state, String genderStr) {
		String hql = "from RobotPatrolOrSenseDialog where type=? and state=? and peopleInfo=? order by orderId"; // 查询结果根据orderId进行排序，用于计算选中概率
		
		return (List<RobotPatrolOrSenseDialog>) findByHqlParams(hql, new Object[]{type, state, genderStr});
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<RobotPatrolOrSenseDialog> getPatrolDialogByCondition(String timeIntervalName, String state, String reachGoalName) {
		String hql = null;
		if(reachGoalName != null) {
			hql = "from RobotPatrolOrSenseDialog where type='patrol' and state=? and timeIntervalName=? and reachGoalName=? order by orderId";
			return (List<RobotPatrolOrSenseDialog>) findByHqlParams(hql, new Object[]{state, timeIntervalName, reachGoalName});
		} else {
			hql = "from RobotPatrolOrSenseDialog where type='patrol' and state=? and timeIntervalName=? order by orderId";
			return (List<RobotPatrolOrSenseDialog>) findByHqlParams(hql, new Object[]{state, timeIntervalName});
		}
	}

	@Override
	public void addRobotTemplateTalk(TalkTemplate talk) {
		save(talk);
	}

	@Override
	public void deleteRobotTemplateTalk(String type, int orderId) {
		String hql = "delete from TalkTemplate where type = ? and orderId = ?";		
		execQueryHqlUpdate(hql, new Object[]{type, orderId});
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<TalkTemplate> getRobotTalkTemplateList() {
		String hql = "from TalkTemplate";
		return (List<TalkTemplate>) findByHql(hql);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<TalkTemplate> getRobotTalkTemplateByType(String type) {
		String hql = "from TalkTemplate where type = ?";
		return (List<TalkTemplate>) findByHqlParams(hql, new Object[]{type});
	}

	@Override
	public void savePathData(PathListData path) {
		save(path);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<PathListData> getPathLists() {
		String hql = "from PathListData";
		return (List<PathListData>) findByHql(hql);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<PathListData> getPathListsByFloorName(String floorName) {
		String hql = "from PathListData where floorName = ?";
		return (List<PathListData>) findByHqlParams(hql, new Object[]{floorName});
	}

	@Override
	public PathListData getPathById(int pathId) {
		return (PathListData) get(PathListData.class, pathId);
	}

	@Override
	public void updatePathData(PathListData path) {
		update(path);
	}

	@Override
	public RobotTalkGroup saveNewTalkGroup(RobotTalkGroup group) {
		return (RobotTalkGroup) save(group);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<RobotTalkGroup> getRobotTalkGroup(String searchStr) {
		String hql = null;
		if(searchStr == null) {
			hql = "from RobotTalkGroup";
			
		} else {
			hql = "from RobotTalkGroup where name like '%"+searchStr+"%'";
		}
		return (List<RobotTalkGroup>) findByHql(hql);
	}

	@Override
	public RobotTalkGroup getRobotTalkGroupById(int id) {
		return (RobotTalkGroup) get(RobotTalkGroup.class, id);
	}

	@Override
	public void updateRobotGroupTalk(RobotTalkGroup group) {
		update(group);
	}

	@Override
	public void deleteRobotTalkGroup(int id) {
		String hql = "delete from RobotTalkGroup where id=?";
		execQueryHqlUpdate(hql, new Object[]{id});
	}

	@Override
	public void deleteRobotTalkBelongtoGroupId(int id) {
		String hql = "delete from RobotPatrolOrSenseDialog where groupId=?";
		execQueryHqlUpdate(hql, new Object[]{id});
		
		String hql2 = "delete from RobotOtherDialog where groupId=?";
		execQueryHqlUpdate(hql2, new Object[]{id});
	}

	@Override
	public void deleteRobot(Robot robot) {
		delete(robot);
	}

	@Override
	public void saveFloorPosNameObj(FloorPosName posNameObj) {
		saveOrUpdate(posNameObj);
	}

	@Override
	public FloorPosName getFloorPosNameArr(String floorName) {
		String hql = "from FloorPosName where floorName = ?";
		List<FloorPosName> list = (List<FloorPosName>) findByHqlParams(hql, new Object[]{floorName});
		if(list.size() > 0) {
			return list.get(0);
		} else {
			return null;
		}
	}

}
