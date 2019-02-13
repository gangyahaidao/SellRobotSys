package com.qingpu.plat.interceptor;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.qingpu.common.entity.ReturnObject;

public class QingpuDigLoginInterceptor extends HandlerInterceptorAdapter {
	@Override  
    public boolean preHandle(HttpServletRequest request,HttpServletResponse response, Object handler) throws Exception{
		//获取请求地址
		String url =request.getRequestURL().toString();
		//获取地址携带的参数
		Map<String, String[]> params = request.getParameterMap();
		String queryString = "";  
        for (String key : params.keySet()) {  
            String[] values = params.get(key);  
            for (int i = 0; i < values.length; i++) {  
                String value = values[i];  
                queryString += key + "=" + value + "&";  
            }  
        }
        // 去掉最后一个空格  
        if(queryString.length() > 0){
        	queryString = queryString.substring(0, queryString.length() - 1);
        }        
        
		//获得session中的用户
		ReturnObject userinfo =(ReturnObject)request.getSession().getAttribute("UserInfo");
		if(userinfo == null){
			//如果还没有登录，跳转到登录界面
			response.sendRedirect(request.getContextPath()+"/qingpudig/login");
			return false;
		}
		return super.preHandle(request, response, handler);
	}
	
	/**
	 * 此方法会在处理请求之后调用
	 * */
	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception{
		
		super.postHandle(request, response, handler, modelAndView);
	}
	
	/**
	 * 在显示视图后被调用
	 * */
	@Override  
    public void afterCompletion(HttpServletRequest request,HttpServletResponse response,Object handler,Exception ex) throws Exception{
		
		super.afterCompletion(request, response, handler, ex);
	}
}
