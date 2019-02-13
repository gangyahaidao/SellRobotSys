package com.qingpu.weixin.github.wxpaysdk;
import java.io.*;

import com.qingpu.common.utils.WeiXinConstants;

public class MyPayConfig extends WXPayConfig {
    private byte[] certData;

    public MyPayConfig() throws Exception {
        String certPath = MyPayConfig.class.getClassLoader().getResource("apiclient_cert.p12").getPath();
        File file = new File(certPath);
        InputStream certStream = new FileInputStream(file);
        this.certData = new byte[(int) file.length()];
        certStream.read(this.certData);
        certStream.close();
    }

    public String getAppID() {
        return WeiXinConstants.APPID;
    }
    
    public String getAppSecret() {
        return WeiXinConstants.APPSECRET;
    }

    public String getMchID() {
        return WeiXinConstants.PARTNER;
    }

    public String getKey() {
        return WeiXinConstants.PARTNERKEY;
    }

    public InputStream getCertStream() {
        ByteArrayInputStream certBis = new ByteArrayInputStream(this.certData);
        return certBis;
    }

	@Override
	IWXPayDomain getWXPayDomain() {
		// TODO Auto-generated method stub
		return null;
	}
}
