package com.qingpu.common.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.google.zxing.common.BitMatrix;
import com.qingpu.common.entity.ZXingEntity;

public abstract interface ZXingService {
	/**
	 * 生成一个二维码
	 * @param String content二维码内容
	 * @param String 指定的logo
	 * */
	public abstract String getQRCode(String content, String logoURL);
	
	/**
	 * 生成QRCode二维码
	 * */
	public abstract String encode(ZXingEntity zxing);
	/**
	 * @Title 生成二维码图片
	 * @Creator Gangyahaidao
	 * @Description 
	 * @param bitMatrix
	 * @param format 图片格式
	 * @param file 生成二维码图片位置
	 * @param isLogo 是否要加logo图
	 * @param logoPath logo图片地址	 
	 * @return 
	 * */
	public abstract byte[] writeToFile(BitMatrix bitMatrix, String format, ByteArrayOutputStream out,
			boolean isLogo, String logoPath) throws IOException;
	/**
	 * @Title 生成二维码内容
	 * @Creator 
	 * @Description 
	 * @param bitMatrix
	 * @return 
	 * */
	public abstract BufferedImage toBufferedImageContents(BitMatrix bitMatrix);
	/**
	 * 解析二维码
	 * @param path 图片的绝对路径
	 * */
	public abstract void decode(String path);
	
}
