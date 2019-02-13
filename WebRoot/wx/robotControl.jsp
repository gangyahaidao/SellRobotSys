<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>
<!-- 零售机器人控制监控页面 -->
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<base href="<%=basePath%>">
<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
<title>零售机器人控制</title>
<meta http-equiv="pragma" content="no-cache">
<meta http-equiv="cache-control" content="no-cache">
<meta http-equiv="expires" content="0">
<link rel="stylesheet" type="text/css" href="<%=basePath %>jsp/hongbao/lib/weui.min.css">
<link rel="stylesheet" type="text/css" href="<%=basePath %>jsp/hongbao/css/jquery-weui.css">
<link rel="stylesheet" type="text/css" href="<%=basePath %>jsp/hongbao/demos.css">
</head>
<body ontouchstart>
	<header class='demos-header'>
      <h1 class="demos-title">零售机器人控制</h1>
    </header>
    
    <div class='demos-content-padded'>
      <a href="javascript:;" id="startLoopRun" class="weui-btn weui-btn_primary">启动循环行走</a>
      <a href="javascript:;" id="stopLoopRun" class="weui-btn weui-btn_warn">退出循环行走</a>
      <br>
      <hr>
      <br>      
      <a href="javascript:;" id="startAutoPatrolMode" class="weui-btn weui-btn_primary">启动自动导航模式</a>
      <a href="javascript:;" id="stopAutoPatrolMode" class="weui-btn weui-btn_warn">停止自动导航模式</a>
      <br>
      <hr>
      <br>
      <a href="javascript:;" id="poweroff" class="weui-btn weui-btn_primary">关闭控制电脑电源</a>
      
    </div>
	<script type="text/javascript" src="<%=basePath %>jsp/hongbao/lib/jquery-2.1.4.js"></script>
	<script type="text/javascript" src="<%=basePath %>jsp/hongbao/lib/fastclick.js"></script>
	<script type="text/javascript">  
	    // 发送json-post请求
		function sendJSONRequest(url, jsonObjData) {
			$.ajax({
				type: "POST",
				url: "<%=request.getContextPath() %>/" + url,
				contentType: "application/json; charset=utf-8",
				dataType: "json",
				data: JSON.stringify(jsonObjData),
				success: function(data){
					$.alert(data.message);
				},
				error:function(XMLHttpResponse, textStatus, errorThrown){
					$.alert("失败： " + errorThrown);
				}
			})
		}
		$(function() {
			FastClick.attach(document.body);
			
			// 启动遥控行走模式按钮绑定函数
			$("#startLoopRun").click(function() {
				var data = {
					"machineId": "3",
					"cmdType": "startLoopRun"
				}
				sendJSONRequest("robot/sendRobotControlCmd", data);
			})
			$("#stopLoopRun").click(function() {
				var data = {
					"machineId": "3",
					"cmdType": "stopLoopRun"
				}
				sendJSONRequest("robot/sendRobotControlCmd", data);
			})
			
			$("#startAutoPatrolMode").click(function() {
				var data = {
					"machineId": "3",
					"cmdType": "enterPatrolMode"
				}
				sendJSONRequest("robot/sendRobotControlCmd", data);
			})
			$("#stopAutoPatrolMode").click(function() {
				var data = {
					"machineId": "3",
					"cmdType": "outPatrolMode"
				}
				sendJSONRequest("robot/sendRobotControlCmd", data);
			})			
			$("#poweroff").click(function() {
				var data = {
					"machineId": "3",
					"cmdType": "poweroff"
				}
				sendJSONRequest("robot/sendRobotControlCmd", data);
			})
		})		 
	 	</script>
	<script type="text/javascript" src="<%=basePath %>jsp/hongbao/js/jquery-weui.js"></script>
</body>
</html>
