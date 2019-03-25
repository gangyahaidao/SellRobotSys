package com.qingpu.socketservice;

import javax.annotation.Resource;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.springframework.web.context.support.WebApplicationContextUtils;

import com.qingpu.adtemplate.dao.AdTemplateDao;
import com.qingpu.common.service.WeiXinTemplateService;
import com.qingpu.goods.service.GoodsService;
import com.qingpu.robots.dao.RobotsDao;

/**
 * 主socket进程监听器，随servlet的启动而一起启动
 * */
@WebListener
public class ServerSocketListener implements ServletContextListener {
	
	private ServerSocketThread socketService;
	private ServerSocketThreadRobot socketServiceRobot;
	private ServerSocketThreadDetect socketServiceDetect;
	private ServerSocketThreadAD socketServiceAd;
		
	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		// 当服务器关闭servlet上下文时执行此方法
		if(null != socketService && !socketService.isInterrupted()){			
			socketService.closeSocketService();//关闭serversocket
		}
	}
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		// 当servlet上下文被加载时执行此方法
		if(null == socketService){
			ServletContext context = sce.getServletContext();
			GoodsService goodsService = WebApplicationContextUtils.getWebApplicationContext(context).getBean(GoodsService.class);
			RobotsDao robotDao = WebApplicationContextUtils.getWebApplicationContext(context).getBean(RobotsDao.class);
			AdTemplateDao adTemplateDao = WebApplicationContextUtils.getWebApplicationContext(context).getBean(AdTemplateDao.class); // 广告模板数据读取
			WeiXinTemplateService weiXinTemplateService = WebApplicationContextUtils.getWebApplicationContext(context).getBean(WeiXinTemplateService.class);
			
			socketService = new ServerSocketThread(goodsService, robotDao, weiXinTemplateService); // 启动货柜ServerSocket
			socketServiceRobot = new ServerSocketThreadRobot(weiXinTemplateService); // 启动底盘ServerSocket
			socketServiceDetect = new ServerSocketThreadDetect(goodsService, robotDao); // 人体检测ServerSocket
			// socketServiceAd = new ServerSocketThreadAD(robotDao, adTemplateDao, goodsService);
			//启动主线程
			socketService.start();
			socketServiceRobot.start();
			socketServiceDetect.start();
			// socketServiceAd.start();
        }
	}
}
