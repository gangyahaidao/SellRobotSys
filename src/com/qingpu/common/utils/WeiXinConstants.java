package com.qingpu.common.utils;
/**
 * 主要是存放微信相关的常量
 * */
public class WeiXinConstants {
	public static final String APPID = "wx8632f39e7c81a6b3";
	public static final String APPSECRET = "338343c63fb9b95d8f71650dab617f69";
	
	public static final String PARTNER = "1485136602";	
	public static final String PARTNERKEY = "014672e0f9fc69bbfd89a5e28a3cced5";
	
	public static final String WECHAT_TOKEN = "qingpu";
	
	public static final String FROM_USER_NAME = "gh_e623bfec3380";
	
	//微信公众号接收的消息类型
	public static final String REQ_MESSAGE_TYPE_TEXT            = "text";               //请求消息类型：文本
    public static final String REQ_MESSAGE_TYPE_IMAGE           = "image";              //请求消息类型：图片
    public static final String REQ_MESSAGE_TYPE_VOICE           = "voice";              //请求消息类型：语音
    public static final String REQ_MESSAGE_TYPE_VIDEO           = "video";              //请求消息类型：视频
    public static final String REQ_MESSAGE_TYPE_SHORTVIDEO      = "shortvideo"; 		//请求消息类型：小视频
    public static final String REQ_MESSAGE_TYPE_LINK            = "link";               //请求消息类型：链接
    public static final String REQ_MESSAGE_TYPE_LOCATION        = "location";           //请求消息类型：地理位置
    public static final String REQ_MESSAGE_TYPE_EVENT           = "event";              //请求消息类型：推送
    
    public static final String RESP_MESSAGE_TYPE_TEXT            = "text";               //回复消息类型：文本
    public static final String RESP_MESSAGE_TYPE_IMAGE           = "image";              //回复消息类型：图片
    public static final String RESP_MESSAGE_TYPE_VOICE           = "voice";              //回复消息类型：语音
    public static final String RESP_MESSAGE_TYPE_VIDEO           = "video";              //回复消息类型：视频
    public static final String RESP_MESSAGE_TYPE_MUSIC			 = "music";				 //音乐消息
    public static final String RESP_MESSAGE_TYPE_NEWS			 = "news";				 //回复消息类型：图文消息
	
	public static final String EVENT_TYPE_SUBSCRIBE = "subscribe";//事件类型：关注
	public static final String EVENT_TYPE_UNSUBSCRIBE = "unsubscribe";//事件类型：取消关注
	public static final String EVENT_TYPE_CLICK = "CLICK";//事件类型：菜单点击
	public static final String EVENT_TYPE_SCAN = "SCAN";//扫描带参数二维码已关注事件
	public static final String EVENT_TYPE_LOCATION = "LOCATION";//上报地理位置事件
	
	//js-sdk鉴权时获取access_token链接
	public static final String ACCESS_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s";
	//获取权限验证jsapi_ticket
	public static final String JSAPI_TICKET_URL = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token=%s&type=jsapi";
	//获取卡券api_ticket
	public static final String WXCARD_API_TICKET = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token=%s&type=wx_card";
	
	//发送现金红包连接
	public static final String SEND_RED_PACK = "https://api.mch.weixin.qq.com/mmpaymkttransfers/sendredpack";
	//获取用户基本信息时获取access_token和openid
	public static final String ACCESSTOKEN_OPENID = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code";
	//微信小程序获取用户的openid
	public static final String SESSIONKEY_OPENID = "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code";
	
	//使用openid获取用户的基本信息，主要是是否关注公众号信息
	public static final String UNIONID_USERINFO = "https://api.weixin.qq.com/cgi-bin/user/info?access_token=%s&openid=%s&lang=zh_CN";
	
	//上传卡劵图片url
	public static final String UPLOADIMG = "https://api.weixin.qq.com/cgi-bin/media/uploadimg?access_token=%s";
	//查询门店列表
	public static final String GETPOILIST = "https://api.weixin.qq.com/cgi-bin/poi/getpoilist?access_token=%s";
	//创建卡劵
	public static final String CREATECARD = "https://api.weixin.qq.com/card/create?access_token=%s";
	//创建卡劵二维码接口
	public static final String CREAT_CARD_QRCODE = "https://api.weixin.qq.com/card/qrcode/create?access_token=%s";
	//查询卡劵code接口
	public static final String GET_WXCRAD_CODE = "https://api.weixin.qq.com/card/code/get?access_token=%s";
	//销毁卡劵接口
	public static final String DESTROY_WXCARD_CODE = "https://api.weixin.qq.com/card/code/consume?access_token=%s";
	
	//获取设置的行业信息
	public static final String GET_INDUSTRY_TEMPLATE = "https://api.weixin.qq.com/cgi-bin/template/get_industry?access_token=%s";
	//获取模板列表
	public static final String GET_TEMPLATE_LIST = "https://api.weixin.qq.com/cgi-bin/template/get_all_private_template?access_token=%s";
	//发送模板消息
	public static final String SEND_TEMPLATE_MESSAGE = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=%s";
}
