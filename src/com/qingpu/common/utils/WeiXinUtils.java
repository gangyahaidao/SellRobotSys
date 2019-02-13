/**
* Title: WeiXinUtils.java
* Description: 
* Copyright: Copyright (c) 2016
* Company: Biceng
* @date 2017-3-8
* @version 1.0
*/
package com.qingpu.common.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.qingpu.common.entity.AccessToken;
import com.qingpu.common.utils.JsWxConfig;
import com.qingpu.common.utils.JsapiTicket;

/**
 * 主要是为微信公众号支付功能提供支持
 * @author wang_gang
 *
 */
public class WeiXinUtils {
	//网页auth2.0授权获取用户详细信息使用的Map
	private static Map<String, String> MAP = new HashMap<String, String>();
	private static Date lastTime;
	//微信js-sdk使用
	private static Map<String, String> MAPJSSDK = new HashMap<String, String>();
	private static Date lastTimeJSSDK;
	//存储jsapiticket
	private static Map<String, String> MAPTICKET = new HashMap<String, String>();
	private static Date lastTimeTICKET;
	//存储卡券api_ticket
	private static Map<String, String> APITICKETAMP = new HashMap<String, String>();	
	private static Date lastTimeAPITICKET;	
	
	/**
	 * auth2.0授权网页获取用户信息，使用code换取access_token、openid等，这是一个access_token的服务器不缓存，不然付款失败，在二维码红包付款，领取红包等地方使用，用于换取用户的详细信息
	 * */
	public synchronized static Map<String, String> getAccessTokenAndOpenid(String code){
		Map<String, String> result = new HashMap<String, String>();
		
		String accessTokenUrl = String.format(WeiXinConstants.ACCESSTOKEN_OPENID, WeiXinConstants.APPID, WeiXinConstants.APPSECRET, code);
		MAP = CommonUtils.httpsRequest(accessTokenUrl, "GET", null);
		lastTime = new Date();
		result.put("access_token", MAP.get("access_token"));
		result.put("openid", MAP.get("openid"));
		
		return result;
	}
	
	/**
	 * 微信小程序获取用户的Openid和Session_key，session_key用于进行明文数据的交互校验
	 * */
	public synchronized static Map<String, String> getSessionkeyAndOpenid(String appid, String appsecret, String code) {
		Map<String, String> result = new HashMap<String, String>();
		
		String url = String.format(WeiXinConstants.SESSIONKEY_OPENID, appid, appsecret, code);
		result = CommonUtils.httpsRequest(url, "GET", null);
		
		return result;
	}
	
	/**
	 * @return 全局AccessToken对象，此处的access_token不同于上面获取用户详细信息的access_token，
	 * */
	public static AccessToken getAccessToken(){
		AccessToken accessTokenObj = new AccessToken();
		String access_token = MAPJSSDK.get("access_token");
		String expires_in = MAPJSSDK.get("expires_in");
		
		if(access_token != null && expires_in != null && (new Date().getTime() - lastTimeJSSDK.getTime()) < 1800*1000){
			accessTokenObj.setAccess_token(access_token);
			accessTokenObj.setExpires_in(Integer.parseInt(expires_in));
		}else{
			try {
				String accessTokenUrl = String.format(WeiXinConstants.ACCESS_TOKEN_URL, WeiXinConstants.APPID, WeiXinConstants.APPSECRET);
				MAPJSSDK = CommonUtils.httpsRequest(accessTokenUrl, "GET", null);
				lastTimeJSSDK = new Date();		
				
				accessTokenObj.setAccess_token(MAPJSSDK.get("access_token"));
				accessTokenObj.setExpires_in(Integer.parseInt(MAPJSSDK.get("expires_in")));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return accessTokenObj;		
	}
	/**
	 * 获取jsapi_ticket，用于js-sdk网页编程中
	 * @param access_token
	 * @return JsapiTicket
	 * */
	public static JsapiTicket getJsapiTicket(String access_token){
		JsapiTicket jsapiTicketObj = new JsapiTicket();
		
		//错误码
		String errcode = MAPTICKET.get("errcode");
		//错误信息
		String errmsg = MAPTICKET.get("errmsg");
		//获取的凭证
		String ticket = MAPTICKET.get("ticket");
		//有效时间
		String expires_in = MAPTICKET.get("expires_in");
		if(errcode != null && errmsg != null && ticket != null && (new Date().getTime() - lastTimeTICKET.getTime()) < 1800*1000){
			jsapiTicketObj.setErrcode(Integer.parseInt(errcode));
			jsapiTicketObj.setErrmsg(errmsg);
			jsapiTicketObj.setExpires_in(Integer.parseInt(expires_in));
			jsapiTicketObj.setTicket(ticket);
		}else{
			String jsapiTicketUrl = String.format(WeiXinConstants.JSAPI_TICKET_URL, access_token);
			MAPTICKET = CommonUtils.httpsRequest(jsapiTicketUrl, "GET", null);
			lastTimeTICKET = new Date();
			
			jsapiTicketObj.setErrcode(Integer.parseInt(MAPTICKET.get("errcode")));
			jsapiTicketObj.setErrmsg(MAPTICKET.get("errmsg"));
			jsapiTicketObj.setExpires_in(Integer.parseInt(MAPTICKET.get("expires_in")));
			jsapiTicketObj.setTicket(MAPTICKET.get("ticket"));
		}
		return jsapiTicketObj;		
	}
	/**
	 * 用于获取卡券api-ticket
	 * */
	public static JsapiTicket getWxcardApiTicket(String access_token){
		JsapiTicket jsapiTicketObj = new JsapiTicket();
		
		//错误码
		String errcode = APITICKETAMP.get("errcode");
		//错误信息
		String errmsg = APITICKETAMP.get("errmsg");
		//获取的凭证
		String ticket = APITICKETAMP.get("ticket");
		//有效时间
		String expires_in = APITICKETAMP.get("expires_in");
		if(errcode != null && errmsg != null && ticket != null && (new Date().getTime() - lastTimeAPITICKET.getTime()) < 1800*1000){
			jsapiTicketObj.setErrcode(Integer.parseInt(errcode));
			jsapiTicketObj.setErrmsg(errmsg);
			jsapiTicketObj.setExpires_in(Integer.parseInt(expires_in));
			jsapiTicketObj.setTicket(ticket);
		}else{
			String apiTicketUrl = String.format(WeiXinConstants.WXCARD_API_TICKET, access_token);
			APITICKETAMP = CommonUtils.httpsRequest(apiTicketUrl, "GET", null);
			lastTimeAPITICKET = new Date();
			
			jsapiTicketObj.setErrcode(Integer.parseInt(APITICKETAMP.get("errcode")));
			jsapiTicketObj.setErrmsg(APITICKETAMP.get("errmsg"));
			jsapiTicketObj.setExpires_in(Integer.parseInt(APITICKETAMP.get("expires_in")));
			jsapiTicketObj.setTicket(APITICKETAMP.get("ticket"));
		}
		return jsapiTicketObj;
	}
	/**
	 * 生成签名
	 * @return JsWxConfig对象
	 * */
	public static JsWxConfig getWxConfig(String url){
		JsWxConfig jsWxConfig = new JsWxConfig();
		//获取随机字符串
		String nonceStr = UUIDGenerator.getUUID();
		jsWxConfig.setNonceStr(nonceStr);
		//获取jsapi_ticket，从本地缓存的数据库中获取
		String jsapi_ticket = getJsapiTicket(getAccessToken().getAccess_token()).getTicket();
		long timestamp = System.currentTimeMillis() / 1000;
		jsWxConfig.setTimestamp(timestamp);		
		String string1 = "jsapi_ticket="+jsapi_ticket
				+"&noncestr="+nonceStr
				+"&timestamp="+timestamp
				+"&url="+url;
		
		String signature = "";
		try
		{
			MessageDigest crypt = MessageDigest.getInstance("SHA-1");
			crypt.reset();
			crypt.update(string1.getBytes("UTF-8"));
			signature = byteToHex(crypt.digest());
		}
		catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
		jsWxConfig.setSignature(signature);
		
		return jsWxConfig;
	}
	
	private static String byteToHex(byte[] hash){
		Formatter formatter = new Formatter();
		for(byte b : hash){
			formatter.format("%02x", b);
		}
		String result = formatter.toString();
		formatter.close();
		
		return result;
	}
	
	/**
	 * 获取卡券签名
	 * */
	public static JSONObject getWxcardApiTicketSig(String card_id){
		String access_token = getAccessToken().getAccess_token();			
		JsapiTicket ticket = getWxcardApiTicket(access_token);
		
		String api_ticket = ticket.getTicket();		
		String nonceStr = UUIDGenerator.getUUID();
		long timestamp = System.currentTimeMillis() / 1000;
		
		WxCardSign signer = new WxCardSign();
        signer.AddData(api_ticket);
        signer.AddData(card_id);
        signer.AddData(nonceStr);
        signer.AddData(timestamp);
        	
		String signature = signer.GetSignature();
		
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("card_id", card_id);
		jsonObj.put("timestamp", timestamp+"");
		jsonObj.put("nonce_str", nonceStr);
		jsonObj.put("signature", signature);
		
		return jsonObj;
	}
	
	/**
	 * 微信发送数据模板替换函数
	 * */
	public static String render(String template, Map<String, String> data){
		Pattern p = Pattern.compile("\\s*|\t|\r|\n");
        Matcher m = p.matcher(template);
        
        template = m.replaceAll("");
		
		String regex = "\\{\\{(.+?)\\}\\}";
		if(StringUtils.isBlank(template)){
            return "";
        }
        if(StringUtils.isBlank(regex)){
            return template;
        }
        if(data == null || data.size() == 0){
            return template;
        }
        try {
            StringBuffer sb = new StringBuffer();
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(template);
            while (matcher.find()) {
                String name = matcher.group(1);// 键名
                String value = data.get(name);// 键值
                if (value != null){ // 只替换提供值的模板变量
                	matcher.appendReplacement(sb, value);
                }
            }
            matcher.appendTail(sb);
            //去除空格和换行
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return template;
	}	
}
