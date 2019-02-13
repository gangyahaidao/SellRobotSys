package asr.qingpu.wavfile.dao;

import java.util.List;

import asr.qingpu.user.entity.AsrUser;
import asr.qingpu.wavfile.entity.AsrCopyRecord;
import asr.qingpu.wavfile.entity.NewWavFileUrl;
import asr.qingpu.wavfile.entity.TotalDailyRecord;
import asr.qingpu.wavfile.entity.UserDailyRecord;
import asr.qingpu.wavfile.entity.UserWorkLog;

public interface AsrTranslateDao {
	/**
	 * 管理员获取当前总的已经完成的数量
	 * */
	long getTotalCounts();
	
	/**
	 * 给指定用户分配指定数量的未被分配的音频条目
	 * */
	void allocateWavItemsToUser(int userid, int count);
	
	/**
	 * 获取当前用户分配的任务还有多少没有完成
	 * */
	int getUserNotFinishedCount(int userid);
	
	/**
	 * 获取当前已经复制的总条数
	 * */
	int getCurrentTotalCopyCount();
	
	/**
	 * 增加一条复制条目记录
	 * */
	AsrCopyRecord saveOneCopyRecord(AsrCopyRecord copyRecord);

	/**
	 * 保存一条复制过来的文件url为新的文件url对象
	 * */
	void saveNewFileUrl(NewWavFileUrl newUrl);

	/**
	 * 从数据库中查找分配指定数量的条目给用户
	 * @param taskpartcount2 
	 * */
	List<NewWavFileUrl> getUnfinishedItems(AsrUser user, int itemCount);

	/**
	 * 使用文件id查找文件条目
	 * */
	NewWavFileUrl getNewWavFileUrlById(int id);

	/**
	 * 更新文件条目
	 * */
	void updateNewWavFileUrl(NewWavFileUrl fileObj);

	/**
	 * 获取当前时间一周前的总完成量统计数据
	 * */
	List<TotalDailyRecord> getWeekFinishedData();

	/**
	 * 获取当前用户前一周每天的完成量
	 * */
	List<UserDailyRecord> getUserDailyWeekData(int id);

	/**
	 * 获取总的剩余的条数
	 * */
	long getLeftTotalCounts();

	/**
	 * 获取指定用户一天的完成任务列表
	 * */
	List<UserWorkLog> getUserOneDayList(int userId);

	/**
	 * 在翻译的文件数据中查找分配给指定用户的数据
	 * */
	List<NewWavFileUrl> getUserTaskItemList(int id);

	/**
	 * 随机选取指定数量的已经转译完的数据
	 * */
	List<NewWavFileUrl> getRandomFinishedLimit(int taskpartcount);

	/**
	 * 根据条件加载是否已经完成的用户数据
	 * */
	List<NewWavFileUrl> getByConditionList(int id, boolean b);

}
