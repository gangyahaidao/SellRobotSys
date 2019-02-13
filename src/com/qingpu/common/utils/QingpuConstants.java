package com.qingpu.common.utils;
/**
 * 定义数据常量
 * */
public class QingpuConstants {
	
	/**
	 * 与零售机器人货柜进行通信协议相关的变量
	 * */
	public static final String URL = "www.g58mall.com";//www.ldhxtj.com

	public static final int HEADER_CHAR = '#';
	public static final int TAILER_CHAR = '@';
	
	/**
	 * 与零售机器人相关的常量
	 * */
	public static final int DATA_TYPE_JSON = 0x00;
	//消息加密方式
	public static final int ENCRYPT_BY_NONE = 0x00;
	public static final int ENCRYPT_BY_RSA = 0x01;
	public static final int ENCRYPT_BY_AES = 0x02;
	public static final int HEADER_BYTE = 0x7e;
	
	public static final int RECV_ROBOT_REGISTER_CODE = 0x9000; // 接收机器人底盘的注册信息
	public static final int RECV_ROBOT_POS_SPEED = 0x9001; // 接收速度和位置
	public static final int RECV_ROBOT_REACHED_GOAL = 0x9002; // 到达目标点
	public static final int SEND_ROBOT_GOAL = 0x9003; // 发送机器人要前往的目标地点
	public static final int SEND_ROBOT_RUN_CONTROL = 0x9004; // 发送机器人是否继续运动的命令
	
	public static final int RECV_HEART_BEAT = 0x9007; // 接收底盘心跳
	public static final int SEND_BACK_HEART_BEAT = 0x9008; // 发送底盘心跳
		
	/**
	 * 音频翻译相关的常量
	 * */
	public static final int TaskPartCount = 200;
	
	/**
	 * 文件服务器地址，从数据库中取出的时候就进行剪切之后插入，发送到客户端再与此值进行拼接，用于进行音频翻译
	 * */
	public static final String FASTDFS_SERVER_ADDR = "192.168.1.195";
	// public static final String FASTDFS_SERVER_ADDR = "192.168.0.125";
	
	/**
	 * 零售机器人运动对话播放相关的时间变量
	 * */
	public static final int RECV_STOP_DELAY_TIME = 1000*15; // 持续接收到停止命令停止的时间
	public static final int RECV_STOP_DELAY_GOON = 1000*6; // 停止超时之后继续运动的时间
	public static final int RECV_MOVE_CONTINUE_TIME = 1000*25; // 自由巡逻状态下每隔此时间则查找一个对话进行播放
	public static final int SCANQR_OVERFLOW_TIME = 30; // 秒，扫码未付款超时的时间

}
