package com.qingpu.common.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.springframework.dao.DataAccessException;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.testng.log4testng.Logger;

import com.qingpu.common.utils.CommonUtils;
import com.qingpu.common.utils.RemoteCallException;

public class MyHandlerExceptionResolver implements HandlerExceptionResolver {

    private static Logger log = Logger.getLogger(MyHandlerExceptionResolver.class);  
    
	@Override
	public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
		ModelAndView mv = new ModelAndView();
		JSONObject ret = new JSONObject();
		if (ex instanceof DataAccessException) {
			// 数据库操作异常			
			ret.put("code", -1);
			ret.put("message", "操作数据库失败");
			CommonUtils.sendExceptionJsonStr(response, ret.toString());
			//记录日志
			log.error("--操作数据库失败:" + ex.getMessage(), ex);
		} else if (ex instanceof RemoteCallException) {
			// 自定义异常
			ret.put("code", -1);
			ret.put("message", ex.getMessage());
			CommonUtils.sendExceptionJsonStr(response, ret.toString());
			log.error("--产生自定义异常:" + ex.getMessage(), ex);
		} else if (ex instanceof RuntimeException) {
			// 系统运行时异常
			ret.put("code", -1);
			ret.put("message", ex.getMessage());
			CommonUtils.sendExceptionJsonStr(response, ret.toString());
			log.error("异常请求路径" + request.getServletPath());
			log.error("--产生系统运行异常:" + ex.getMessage(), ex);			
		}

		return mv;
	}
}
