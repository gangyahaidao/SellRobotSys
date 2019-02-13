package com.qingpu.test;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import com.qingpu.common.service.BdfsBinaryProviderImpl;
import com.qingpu.common.utils.FileUtils;

import asr.qingpu.controller.TranslateWavFileController;

public class DownloadFastdfsWav {

	static Connection connection = null;
	static BdfsBinaryProviderImpl bdfsFileServer = new BdfsBinaryProviderImpl();
	
	static Connection getConnection() {
		if (connection != null)
			return connection;
		else {
			try {
				Properties prop = new Properties();
				prop.load(new FileInputStream(TranslateWavFileController.class.getClassLoader().getResource("fastdfs-db.properties").getPath()));
				
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
     * 从输入流中获取字节数组
     * @param inputStream
     * @return
     * @throws IOException
     */
    public static  byte[] readInputStream(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[1024];
        int len = 0;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while((len = inputStream.read(buffer)) != -1) {
            bos.write(buffer, 0, len);
        }
        bos.close();
        return bos.toByteArray();
    }
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		connection = getConnection(); // 连接第三方数据库
		
		String sql = "select id, newFileUrl, translateWords from asr_new_fileurl where translateWords is not null order by id asc limit 1000, 10000"; // limit xxx
		//String sql = "select id, newFileUrl, translateWords from asr_new_fileurl where translateWords is not null order by id asc limit 1, 999"; // limit xxx
		
		try {
			Statement statement = connection.createStatement();
			ResultSet rs = statement.executeQuery(sql);
			int totalCount = 0;
			while (rs.next()) {					
				String part_record1 = rs.getString("translateWords");
				if(part_record1 != null) { // 保存到数据库
					if(part_record1.length() > 0) {
						String wavFileUrl = rs.getString("newFileUrl");
						//替换ip地址
						String reg = "((2[0-4]\\d|25[0-5]|[01]?\\d\\d?)\\.){3}(2[0-4]\\d|25[0-5]|[01]?\\d\\d?)"; //匹配ip的正则
						wavFileUrl = wavFileUrl.replaceFirst(reg, "192.168.0.125");//替换第一个
						// System.out.println("wavFileUrl = " + wavFileUrl);
						//下载文件，重新命名并保存在本地
						URL url = new URL(wavFileUrl);
						HttpURLConnection conn = (HttpURLConnection)url.openConnection();
						InputStream inputStream = conn.getInputStream();
						byte[] fileData = readInputStream(inputStream);
						if(fileData != null) {
							
							FileUtils.byte2File(fileData, "F:/wavs/", totalCount+".wav");
							//FileUtils.byte2File(fileData, "F:/wavs-test/", totalCount+".wav");
							
							//将翻译文本数据写入json文件
							String str = totalCount + " " + part_record1 + "\r\n";
							
							FileUtils.TextToFile("F:/wavs/train.txt", str, true);
							//FileUtils.TextToFile("F:/wavs-test/train-test.txt", str, true);
							
						} else {
							System.out.println("--download fileData == null");
						}											
					}else{
						System.out.println("--id = " + rs.getInt("id") + " translateWords is null");
					}
				}
				totalCount++;
				System.out.println("--index = " + totalCount);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
