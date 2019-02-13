package com.qingpu.common.utils;

@SuppressWarnings("serial")
public class RemoteCallException extends RuntimeException
{
	private int nCode;
	public static final int RCE_LOGOUT=-1;//no login or session time out 	
	public static final int RCE_DATABASE=-2;//database exception
	public static final int RCE_PARAMETER=-3;//parameter exception
	public static final int RCE_NOSESSION=-4;//no session for this call
	public static final int RCE_IO=-5;//IO error occur
	public static final int RCE_EXIST=-6;//the data already exist ecxeption
	public static final int RCE_UPLOAD_FAIL=-7;//上传文件失败
	public static final int RCE_UNKNOWN=-8;//unknown error

	public int getNCode()
	{
		return nCode;
	}

	public void setNCode(int code)
	{
		nCode = code;
	}
	public RemoteCallException(int code)
	{
		super("");
		this.nCode = code;
	}
	public RemoteCallException(int code, String message)
	{
		super(message);
		this.nCode = code;
	}
	public RemoteCallException(int code, Exception cause)
	{
		super(cause);
		this.nCode = code;
	}
	@Override
	public String getMessage()
	{
		String str=super.getMessage();
		if (!"".equals(str))
			return str;
		switch(this.nCode)
		{
		case RCE_LOGOUT:
			return "尚未登录或者会话超时。";
		case RCE_DATABASE:
			return "数据库操作出现问题。";
		case RCE_PARAMETER:
			return "不合理的调用参数。";
		case RCE_NOSESSION:
			return "没有会话信息。";
		case RemoteCallException.RCE_IO:
			return "文件读写发生错误。";
		case RemoteCallException.RCE_EXIST:
			return "数据已存在！";
		case RemoteCallException.RCE_UPLOAD_FAIL:
			return "上传文件服务器失败，请重新上传。";
		case RemoteCallException.RCE_UNKNOWN:
			return "未知异常。";
		}
		return "未知错误。";
	}
	@Override
	public String toString()
	{
		return super.toString()+" with error code: "+this.nCode;
	}
}
