package com.qingpu.common.service;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import org.springframework.stereotype.Service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.qingpu.common.entity.ZXingEntity;

@Service("zxingService")
public class ZXingServiceImpl implements ZXingService {
	public static final int BLACK = 0xff000000;
	public static final int WHITE = 0xffffffff;
	
	@Resource
	private BdfsBinaryProvider bdfsBinaryProvider;
	
	@Override
	public String encode(ZXingEntity zxing) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		String fileUrl = "";
		Hashtable<EncodeHintType, Object> hints = new Hashtable<EncodeHintType, Object>();
		//设置纠错级别(L-7% M-15% Q-25% H-30%)，纠错级别越高存储的信息越少
		hints.put(EncodeHintType.ERROR_CORRECTION, zxing.getErrorCorrectionLevel());
		//设置编码格式
		hints.put(EncodeHintType.CHARACTER_SET, zxing.getCharacterSet());
		//设置边缘空白
		hints.put(EncodeHintType.MARGIN, zxing.getMargin());
		
		try {
			BitMatrix bitMatrix = new MultiFormatWriter().encode(zxing.getContents(), 
					BarcodeFormat.QR_CODE,zxing.getWidth(),zxing.getHeight(),hints);
			
			byte[] b = writeToFile(bitMatrix, zxing.getFormat(), out, zxing.isFlag(), zxing.getLogoPath());			
			
			int len = b.length;								
			
			fileUrl = bdfsBinaryProvider.upload(b, "png");			
		} catch (WriterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch(IOException e){
			e.printStackTrace();
		}
		return fileUrl;	
	}

	@Override
	public byte[] writeToFile(BitMatrix bitMatrix, String format,
			ByteArrayOutputStream out, boolean isLogo, String logoPath)
			throws IOException {
		BufferedImage bi = toBufferedImageContents(bitMatrix);
		if(isLogo){
			int width_4 = bitMatrix.getWidth() / 4;  
            int width_8 = width_4 / 2;  
            int height_4 = bitMatrix.getHeight() / 4;  
            int height_8 = height_4 / 2;  
            /*返回由指定矩形区域定义的子图像*/  
            BufferedImage bi2 = bi.getSubimage(width_4 + width_8, height_4 + height_8, width_4, height_4);  
            /*获取一个绘图工具笔*/  
            Graphics2D g2 = bi2.createGraphics();  
            /*读取logo图片信息*/  
            Image img = ImageIO.read(new File(logoPath));//实例化一个Image对象。  
            /*当前图片的宽与高*/  
            int currentImgWidth = img.getWidth(null);  
            int currentImgHeight = img.getHeight(null);  
            /*处理图片的宽与高*/  
            int resultImgWidth = 0;  
            int resultImgHeight = 0;  
            if(currentImgWidth != width_4){  
                resultImgWidth = width_4;  
            }  
            if(currentImgHeight != width_4){  
                resultImgHeight = width_4;  
            }  
            /*绘制图片*/  
            g2.drawImage(img,0,0, resultImgWidth,resultImgHeight,null);  
            g2.dispose();    
            bi.flush();  
		}		
		
		ImageIO.write(bi, format, out);
		
		return out.toByteArray();
	}

	@Override
	public BufferedImage toBufferedImageContents(BitMatrix bitMatrix) {
		int width = bitMatrix.getWidth();
		int height = bitMatrix.getHeight();
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		for(int x = 0; x < width; x++){
			for(int y = 0; y < height; y++){
				image.setRGB(x, y, bitMatrix.get(x, y) == true ? BLACK : WHITE);
			}
		}
		return image;
	}

	@Override
	public void decode(String path) {
        try{  
            if(path == null || path.equals("")) {  
                System.out.println("** File path cannot be null **");  
            }  
            File file = new File(path);  
            BufferedImage image = ImageIO.read(file);  
            /*判断是否是图片*/  
            if (image == null) {   
                System.out.println("Could not decode image");   
            }  
            /*解析二维码用到的辅助类*/  
            LuminanceSource source = new com.google.zxing.client.j2se.BufferedImageLuminanceSource(image);   
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));   
            Hashtable<DecodeHintType,Object> hints= new Hashtable<DecodeHintType,Object>();   
            /*解码设置编码方式为：UTF-8*/  
            hints.put(DecodeHintType.CHARACTER_SET, "UTF-8");   
              
            Result result = new MultiFormatReader().decode(bitmap,hints);   
            String resultStr = result.getText();   
            System.out.println("**decode content："+resultStr);  
        }catch(Exception ex){  
            System.out.println("Cannot Decode**");  
        }  
    }

	@Override
	public String getQRCode(String content, String logoURL) {
		// TODO Auto-generated method stub
		ZXingEntity zxing = new ZXingEntity();
		zxing.setContents(content);
		zxing.setCharacterSet("UTF-8");
		zxing.setErrorCorrectionLevel(ErrorCorrectionLevel.H);
		zxing.setFlag(true);
		zxing.setFormat("png");
		zxing.setMargin(0);
		zxing.setWidth(300);
		zxing.setHeight(300);
		//zxing.setLogoPath(RequestSocketService.class.getClassLoader().getResource(logoURL).getPath());

		String picurl = this.encode(zxing);
		picurl = "http://www.g58mall.com:8080/" + picurl;
		
		return picurl;
	}
}
