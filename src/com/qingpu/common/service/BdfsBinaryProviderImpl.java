package com.qingpu.common.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient1;
import org.csource.fastdfs.StorageServer;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("bdfsBinaryProvider")
@Transactional
public class BdfsBinaryProviderImpl implements BdfsBinaryProvider {
	private static boolean hasInited = false;

	@Override
	public String upload(byte[] data, String type) {
		String sealPictureFileId = "";
		try {
			if(!hasInited) {
				hasInited = true;
				ClientGlobal.init(BdfsBinaryProvider.class.getClassLoader().getResource("bdfs.conf").getPath());
			}			
			TrackerClient tracker = new TrackerClient();
			TrackerServer trackerServer = tracker.getConnection();
			StorageClient1 client = new StorageClient1(trackerServer, null);		
			sealPictureFileId = client.upload_file1(data, type, null);
			trackerServer.close(); // 关闭连接
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (MyException e) {
			e.printStackTrace();
		}
		return sealPictureFileId;
	}
	
	public byte[] download(String fillurl) {
		byte[] fileByte = null;
		
		try {
			if(!hasInited) {
				hasInited = true;
				ClientGlobal.init(BdfsBinaryProvider.class.getClassLoader().getResource("bdfs.conf").getPath());
			}
			TrackerClient tracker = new TrackerClient();
			TrackerServer trackerServer = tracker.getConnection();
			StorageServer storageServer = null;
			StorageClient1 client = new StorageClient1(trackerServer, storageServer);
					
			fileByte = client.download_file1(fillurl);
			trackerServer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (MyException e) {
			e.printStackTrace();
		}
		
		return fileByte;
	}
	
	/**
	 * 删除成功返回0
	 * */
	public int deleteFileByUrl(String fileurl) {
		int ret = 0;
		
		try {
			if(!hasInited) {
				hasInited = true;
				ClientGlobal.init(BdfsBinaryProvider.class.getClassLoader().getResource("bdfs.conf").getPath());
			}
			TrackerClient tracker = new TrackerClient();
			TrackerServer trackerServer = tracker.getConnection();
			StorageClient1 client = new StorageClient1(trackerServer, null);
					
			ret = client.delete_file1(fileurl);		
			trackerServer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (MyException e) {
			e.printStackTrace();
		}
		
		return ret;
	}

}
