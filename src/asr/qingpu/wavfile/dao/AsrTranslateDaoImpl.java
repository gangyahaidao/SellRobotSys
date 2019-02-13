package asr.qingpu.wavfile.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Repository;

import asr.qingpu.user.entity.AsrUser;
import asr.qingpu.wavfile.entity.AsrCopyRecord;
import asr.qingpu.wavfile.entity.NewWavFileUrl;
import asr.qingpu.wavfile.entity.TotalDailyRecord;
import asr.qingpu.wavfile.entity.UserDailyRecord;
import asr.qingpu.wavfile.entity.UserWorkLog;

import com.qingpu.common.dao.BaseDaoImpl;

@Repository("asrTranslateDao")
public class AsrTranslateDaoImpl extends BaseDaoImpl implements AsrTranslateDao {

	@Override
	public long getTotalCounts() {
		String hql = "select count(*) from NewWavFileUrl where isFinished=?";
		long count = findByHqlParamsCount(hql, new Object[]{true});
		
		return count;
	}

	@Override
	public void allocateWavItemsToUser(int userid, int count) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getUserNotFinishedCount(int userid) {
		// TODO Auto-generated method stub
		return 0;
	}

	@SuppressWarnings("unchecked")
	@Override
	public int getCurrentTotalCopyCount() {
		int count = 0; 
		
		String hql = "from AsrCopyRecord order by copyDate desc limit 1"; // 获取时间最近的那一条数据
		List<AsrCopyRecord> list = (List<AsrCopyRecord>) findByHql(hql);
		if(list.size() > 0) {
			count = list.get(0).getAllTotalCopys();
		}
		
		return count;
	}

	@Override
	public AsrCopyRecord saveOneCopyRecord(AsrCopyRecord copyRecord) {
		AsrCopyRecord copy = (AsrCopyRecord) save(copyRecord);
		
		return copy;
	}

	@Override
	public void saveNewFileUrl(NewWavFileUrl newUrl) {
		save(newUrl);
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized List<NewWavFileUrl> getUnfinishedItems(AsrUser user, int itemCount) {
		String hql = "from NewWavFileUrl where isOccupied = ? and isFinished = ? order by id";
		List<NewWavFileUrl> list = (List<NewWavFileUrl>) findByHqlParamsLimit(hql, new Object[]{false, false}, itemCount);		
		
		return list;		
	}

	@Override
	public NewWavFileUrl getNewWavFileUrlById(int id) {		
		return (NewWavFileUrl) get(NewWavFileUrl.class, id);
	}

	@Override
	public void updateNewWavFileUrl(NewWavFileUrl fileObj) {
		update(fileObj);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<TotalDailyRecord> getWeekFinishedData() {
		String sql = "SELECT * FROM asr_total_daily_record where date > DATE_SUB(CURDATE(), INTERVAL 1 WEEK)";
		List<TotalDailyRecord> totalDailyList = (List<TotalDailyRecord>) execQuerySqlSelect(sql, null, TotalDailyRecord.class);
		
		return totalDailyList;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<UserDailyRecord> getUserDailyWeekData(int id) {
		String sql = "SELECT * FROM asr_user_daily_record where userId = ? and date > DATE_SUB(CURDATE(), INTERVAL 1 WEEK)";
		
		return (List<UserDailyRecord>) execQuerySqlSelect(sql, new Object[]{id}, UserDailyRecord.class);
	}

	@Override
	public long getLeftTotalCounts() {
		String hql = "select count(*) from NewWavFileUrl where isFinished=?";
		long leftCount = findByHqlParamsCount(hql, new Object[]{false});
		
		return leftCount;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<UserWorkLog> getUserOneDayList(int userId) {
		String sql = "SELECT * FROM asr_user_work_log where userId = ? and to_days(date) = to_days(now())";
		
		return (List<UserWorkLog>) execQuerySqlSelect(sql, new Object[]{userId}, UserWorkLog.class);		
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<NewWavFileUrl> getUserTaskItemList(int id) {
		String hql = "from NewWavFileUrl where userId = ?";		
		return (List<NewWavFileUrl>) findByHqlParams(hql, new Object[]{id});
	}

	@Override
	public List<NewWavFileUrl> getRandomFinishedLimit(int taskpartcount) {
		long totalFinishedCount = getTotalCounts();
		if(taskpartcount > totalFinishedCount) {
			taskpartcount = (int) totalFinishedCount;
		}		
		List<NewWavFileUrl> retList = new ArrayList<NewWavFileUrl>();
		
		Random rand = new Random(new Date().getTime());
		for(int i = 0; i < taskpartcount; i++) {
			int newWavFileId = rand.nextInt((int) totalFinishedCount); // 随机获取一个新文件的id
			NewWavFileUrl add = getNewWavFileUrlById(newWavFileId);
			retList.add(add);
		}
		
		return retList;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<NewWavFileUrl> getByConditionList(int id, boolean b) {
		String hql = "from NewWavFileUrl where userId = ? and isFinished = ?";		
		return (List<NewWavFileUrl>) findByHqlParams(hql, new Object[]{id, b});
	}

}
