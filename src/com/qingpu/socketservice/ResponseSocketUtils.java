package com.qingpu.socketservice;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import org.json.JSONObject;

import com.qingpu.common.utils.DataProcessUtils;
import com.qingpu.common.utils.QingpuConstants;

public class ResponseSocketUtils {
	
	public static void sendJsonDataToClient(JSONObject jsonObject, Socket client, int cmd, int encryptType, int contentType) {
		String jsonStr = jsonObject.toString();
		try {
			byte[] bytes = buildUpSendBytes(jsonStr, cmd, encryptType, contentType);
			//发送数据到客户端
			if(client != null && !client.isOutputShutdown() && !client.isClosed())
			{
				OutputStream out = client.getOutputStream();
				out.write(bytes);
				out.flush();
			} else {
				System.out.println("--底盘连接socket断开，请检查，清除连接线程");
				// 清除连接的socket资源
				RobotClientSocket clientObj = ServerSocketThreadRobot.getRobotConnectObj(client);
				clientObj.getClientThread().closeClient();
				// ServerSocketThreadRobot.robotMachineMap.remove(clientObj.getMachineID()); // 在Map中移除底盘对象
			}
		} catch (Exception e) {
			e.printStackTrace();
			RobotClientSocket clientObj = ServerSocketThreadRobot.getRobotConnectObj(client);
			clientObj.getClientThread().closeClient();
			System.out.println("--清除底盘连接的线程资源");
		}
	}
	
	/**
	 * 将数据组合成协议要求的字节，并进行加密要求进行了响应的处理
	 * */
	private static  byte[] buildUpSendBytes(String content, int cmd, int encryptType, int contentType) throws Exception{
		//1.消息标志位
		int flag = QingpuConstants.HEADER_BYTE;
		//消息体长度 = json数据长度
		int messageBodyLength = content.getBytes("UTF8").length;
		//计算校验码
		byte check = (byte)0;
		//1.先将需要计算校验码的数据放在一个字节数组dataT中
		byte[] dataT = DataProcessUtils.toByteArray(flag, 1);
		//2.将消息ID字段
		dataT = DataProcessUtils.mergeArray(dataT, DataProcessUtils.toByteArray(cmd, 2));
		//3.合并加密方式字段
		dataT = DataProcessUtils.mergeArray(dataT, DataProcessUtils.toByteArray(encryptType, 1));
		//4.合并消息体长度字段
		dataT = DataProcessUtils.mergeArray(dataT, DataProcessUtils.toByteArray(messageBodyLength, 2));
		//5.去掉--合并业务数据类型字段
		//dataT = DataProcessUtils.mergeArray(dataT, DataProcessUtils.toByteArray(contentType, 1));
		//6.合并json字符串
		dataT = DataProcessUtils.mergeArray(dataT, content.getBytes("UTF8"));
		//7.计算校验码
		check = dataT[1];
		for(int i = 2; i < dataT.length; i++){
			check = (byte)(check ^ dataT[i]);
		}
		//合并校验码字段
		dataT = DataProcessUtils.appendByte(dataT, check);
		//合并尾部标志位
		dataT = DataProcessUtils.mergeArray(dataT, DataProcessUtils.toByteArray(flag, 1));		
		//替换数据
		//System.out.println("--替换之前的数据 = " + ByteUtils.bytesToHexString(dataT));
		byte[] rd = replaceDataBytes(dataT);
		//System.out.println("--替换之后的数据 = " + ByteUtils.bytesToHexString(rd));
		//加密数据
		String encodeStr = "";
		if(encryptType == QingpuConstants.ENCRYPT_BY_AES){
			//使用AES加密
		}else if(encryptType == QingpuConstants.ENCRYPT_BY_RSA){
			//使用RSA加密
		}else{
			//不加密			
		}
		return rd;
	}
	
	/**
	 * 替换特殊字节
	 * */
	private static byte[] replaceDataBytes(byte[] dataT) {
		// TODO Auto-generated method stub
		//将消息头包括消息体、校验码中的0x7e替换为0x7d 0x02，0x7d替换成0x7d 0x01
		byte[] ret = {dataT[0]};
		for(int i = 1; i < dataT.length-1; i++){
			if(dataT[i] == (byte)0x7e){
				ret = DataProcessUtils.appendByte(ret, (byte)0x7d);
				ret = DataProcessUtils.appendByte(ret, (byte)0x02);
			}else if(dataT[i] == (byte)0x7d){
				ret = DataProcessUtils.appendByte(ret, (byte)0x7d);
				ret = DataProcessUtils.appendByte(ret, (byte)0x01);
			}else{
				ret = DataProcessUtils.appendByte(ret, dataT[i]);
			}
		}
		//将最后一个标志位手动补上
		ret = DataProcessUtils.appendByte(ret, (byte)0x7e);
		
		return ret;
	}

	public static void sendJsonStringToClient(String jsonString, Socket client,
			int cmd, int encryptType, int contentType) {
		// TODO Auto-generated method stub
		try {
			byte[] bytes = buildUpSendBytes(jsonString, cmd, encryptType, contentType);
			//发送数据到客户端
			if(client != null && !client.isOutputShutdown())
			{
				OutputStream out = client.getOutputStream();
				out.write(bytes);
				out.flush();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void sendBytesToString(byte[] bytes, Socket client, int cmd, int encryptType, int contentType) {
		try {
			byte[] sendbytes = buildUpSendBytes(new String(bytes), cmd, encryptType, contentType);
			if(client != null && !client.isOutputShutdown())
			{
				OutputStream out = client.getOutputStream();
				out.write(sendbytes);
				out.flush();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
