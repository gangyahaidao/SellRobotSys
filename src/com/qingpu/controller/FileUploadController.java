package com.qingpu.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.qingpu.common.entity.ReturnObject;
import com.qingpu.common.service.BdfsBinaryProvider;
import com.qingpu.common.utils.CommonUtils;

@Controller
@RequestMapping("/uploadfile")
public class FileUploadController {
	
	@Resource
	@Qualifier("bdfsBinaryProvider")
	private BdfsBinaryProvider bdfsProviderService;

	public FileUploadController() {
		// TODO Auto-generated constructor stub
	}
	/**
	 * 上传一个文件
	 * */
	@RequestMapping(value="/uploadOneFile", method=RequestMethod.POST, produces="application/json;charset=UTF-8")
	@ResponseBody
	public synchronized String uploadFile(HttpServletRequest request, HttpServletResponse response, @RequestParam("file") MultipartFile file){
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("code", -1);
		jsonObject.put("message", "failed");
		
		String fileUrl = "";	    
		if (!file.isEmpty()) {			
			try {				
				byte[] b = file.getBytes();
				String type = file.getContentType();
				fileUrl = bdfsProviderService.upload(b, type.split("/")[1]); //image/png 类型	
				fileUrl = "http://www.g58mall.com:8080/" + fileUrl;
				jsonObject.put("code", 0);
				jsonObject.put("message", "success");
				jsonObject.put("fileurl", fileUrl);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally{

			}	        
        } else {
            System.out.println("--Error, File is empty");
        }
		
		return jsonObject.toString();
	}

	/**
	 * 上传多个文件
	 * */
	@RequestMapping("/uploadMultipleFile")
    public void uploadMultipleFileHandler(@RequestParam("kartik-input-710[]") MultipartFile[] files) throws IOException {        
        ArrayList<Integer> arr = new ArrayList<>();

        for (int i = 0; i < files.length; i++) {
            MultipartFile file = files[i];

            if (!file.isEmpty()) {
                InputStream in = null;
                OutputStream out = null;

                try {
                    String rootPath = System.getProperty("catalina.home");
                    File dir = new File(rootPath + File.separator + "tmpFiles");
                    if (!dir.exists())
                        dir.mkdirs();
                    File serverFile = new File(dir.getAbsolutePath() + File.separator + file.getOriginalFilename());
                    in = file.getInputStream();
                    out = new FileOutputStream(serverFile);
                    byte[] b = new byte[1024];
                    int len = 0;
                    while ((len = in.read(b)) > 0) {
                        out.write(b, 0, len);
                    }
                    out.close();
                    in.close();
                } catch (Exception e) {
                    arr.add(i);
                } finally {
                    if (out != null) {
                        out.close();
                        out = null;
                    }

                    if (in != null) {
                        in.close();
                        in = null;
                    }
                }
            } else {
                arr.add(i);
            }
        }

        if(arr.size() > 0) {
            System.out.println("Files upload fail");            
        } else {
            System.out.println("Files upload success");
        }
    }
	
}
