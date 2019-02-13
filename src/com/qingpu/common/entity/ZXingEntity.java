package com.qingpu.common.entity;

import java.io.Serializable;

/**
 * 封装二维码持久化类，基于ZXing实现
 * @author wang_gang
 *
 */
public class ZXingEntity implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	//二维码内容
	private String contents;

	//图片的宽度
	private int width;
	
	//图片的高度
	private int height;
	
	//生成图片的地址
	private String path;
	
	//logo图片地址
	private String logoPath;
	
	//生成图片的格式
	private String format;
	
	//纠错级别
	private Object errorCorrectionLevel;
	
	//编码格式
	private String characterSet;
	
	//二维码边缘留白
	private int margin;
	
	//是否中间贴图
	private boolean flag;
	/**
	 * 
	 */
	public ZXingEntity() {
		// TODO Auto-generated constructor stub
	}
	public String getContents() {
		return contents;
	}
	public void setContents(String contents) {
		this.contents = contents;
	}
	public int getWidth() {
		return width;
	}
	public void setWidth(int width) {
		this.width = width;
	}
	public int getHeight() {
		return height;
	}
	public void setHeight(int height) {
		this.height = height;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getLogoPath() {
		return logoPath;
	}
	public void setLogoPath(String logoPath) {
		this.logoPath = logoPath;
	}
	public String getFormat() {
		return format;
	}
	public void setFormat(String format) {
		this.format = format;
	}
	public Object getErrorCorrectionLevel() {
		return errorCorrectionLevel;
	}
	public void setErrorCorrectionLevel(Object errorCorrectionLevel) {
		this.errorCorrectionLevel = errorCorrectionLevel;
	}
	public String getCharacterSet() {
		return characterSet;
	}
	public void setCharacterSet(String characterSet) {
		this.characterSet = characterSet;
	}
	public int getMargin() {
		return margin;
	}
	public void setMargin(int margin) {
		this.margin = margin;
	}
	public boolean isFlag() {
		return flag;
	}
	public void setFlag(boolean flag) {
		this.flag = flag;
	}
	

}
