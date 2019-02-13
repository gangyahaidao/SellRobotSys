package com.qingpu.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;

public class SeperateYesNoWavs {
	
	/** 
     * 复制单个文件 
     * @param oldPath String 原文件路径 如：c:/fqf.txt 
     * @param newPath String 复制后路径 如：f:/fqf.txt 
     * @return boolean 
     */ 
   public static void copyFile(String oldPath, String newPath) { 
       try { 
           int bytesum = 0; 
           int byteread = 0; 
           File oldfile = new File(oldPath); 
           if (oldfile.exists()) { //文件存在时 
               InputStream inStream = new FileInputStream(oldPath); //读入原文件 
               FileOutputStream fs = new FileOutputStream(newPath); 
               byte[] buffer = new byte[1000*200]; 
               while ( (byteread = inStream.read(buffer)) != -1) { 
                   bytesum += byteread; //字节数 文件大小 
                   System.out.println(bytesum); 
                   fs.write(buffer, 0, byteread); 
               } 
               inStream.close(); 
           } 
       } 
       catch (Exception e) { 
           System.out.println("复制单个文件操作出错"); 
           e.printStackTrace();
       }
   }
   
   /** 
    * 删除文件 
    * @param filePathAndName String 文件路径及名称 如c:/fqf.txt 
    * @param fileContent String 
    * @return boolean 
    */ 
  public static void delFile(String filePathAndName) { 
      try { 
          String filePath = filePathAndName; 
          filePath = filePath.toString(); 
          java.io.File myDelFile = new java.io.File(filePath); 
          myDelFile.delete();
      } 
      catch (Exception e) { 
          System.out.println("删除文件操作出错"); 
          e.printStackTrace();
      }
  }
  
	  /** 
	   * 移动文件到指定目录 
	   * @param oldPath String 如：c:/fqf.txt 
	   * @param newPath String 如：d:/fqf.txt 
	   */ 
	 public static void moveFile(String oldPath, String newPath) { 
	     copyFile(oldPath, newPath); 
	     delFile(oldPath);
	 }

	public static void main(String[] args) {
		File file=new File("F:\\wavs\\train.txt");
		BufferedReader reader=null;
		String temp=null;
		int line=1;
		
		try {
			reader=new BufferedReader(new FileReader(file));
			while((temp=reader.readLine())!=null){
				System.out.println("line"+line+":"+temp);
				line++;
				String[] parseStrArr = temp.split(" ");
				String moveFileName = parseStrArr[0] + ".wav";
				FileUtils.copyFile(new File("F:\\wavs\\"+moveFileName), new File("F:\\wavs\\no\\"+moveFileName));
				new File("F:\\wavs\\"+moveFileName).delete();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
