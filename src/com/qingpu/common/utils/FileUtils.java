package com.qingpu.common.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;

public class FileUtils {
	/**
	 * 获得指定文件的byte数组 
	 * @param filePath 文件绝对路径
	 * @return
	 */
	public static byte[] file2Byte(String filePath){
		ByteArrayOutputStream bos=null;
		BufferedInputStream in=null;
		try{
			File file=new File(filePath);
			if(!file.exists()){  
	            throw new FileNotFoundException("file not exists");  
	        }
			bos=new ByteArrayOutputStream((int)file.length());
			in=new BufferedInputStream(new FileInputStream(file));
			int buf_size=1024;
			byte[] buffer=new byte[buf_size];
			int len=0;
			while(-1 != (len=in.read(buffer,0,buf_size))){
				bos.write(buffer,0,len);
			}
			return bos.toByteArray();
		}
		catch(Exception e){
			System.out.println(e.getMessage());
            e.printStackTrace();
            return null;
		}
		finally{
			try{
				if(in!=null){
					in.close();
				}
				if(bos!=null){
					bos.close();
				}
            }
			catch(Exception e){
				System.out.println(e.getMessage());
				e.printStackTrace();  
            }
        }
    }
    /**
     * 根据byte数组，生成文件 
     * @param bfile 文件数组
     * @param filePath 文件存放路径
     * @param fileName 文件名称
     */
	public static void byte2File(byte[] bfile,String filePath,String fileName){
		BufferedOutputStream bos=null;
		FileOutputStream fos=null;
		File file=null;
		try{
			File dir=new File(filePath);
			if(!dir.exists() && !dir.isDirectory()){//判断文件目录是否存在  
				dir.mkdirs();  
            }
			file=new File(filePath+fileName);
			fos=new FileOutputStream(file);
			bos=new BufferedOutputStream(fos);
			bos.write(bfile);
		} 
		catch(Exception e){
			System.out.println(e.getMessage());
			e.printStackTrace();  
        }
		finally{
			try{
				if(bos != null){
					bos.close(); 
				}
				if(fos != null){
					fos.close();
				}
			}
			catch(Exception e){
				System.out.println(e.getMessage());
				e.printStackTrace();  
			}
		}
    }
	
	  /**
	   * 传入文件名以及字符串, 将字符串信息保存到文件中
	   * 
	   * @param strFilename
	   * @param strBuffer
	   * @param append 是否以追加的方式写入数据
	   */
	  public static void TextToFile(final String strFilename, final String strBuffer, boolean append)
	  {
	    try
	    {    
	      // 创建文件对象
	      File fileText = new File(strFilename);
	      // 向文件写入对象写入信息
	      FileWriter fileWriter = new FileWriter(fileText, append);	 
	      // 写文件      
	      fileWriter.write(strBuffer);
	      // 关闭
	      fileWriter.close();
	    }
	    catch (IOException e)
	    {
	      //
	      e.printStackTrace();
	    }
	  }
	  
	  /**
	   * 将文本文件内容读取到String中
	   * */
	  public static String getFileContent(String filePath) {
			StringBuffer sb = new StringBuffer();
			Reader reader = null;
			BufferedReader br = null;
			try {
				reader = new FileReader(filePath);
				br = new BufferedReader(reader);
				String data = null;
				while ((data = br.readLine()) != null) {
					sb.append(data);
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					reader.close();
					br.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return sb.toString();
		}
}
