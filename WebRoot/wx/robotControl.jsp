<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>
<!-- 零售机器人控制监控页面 -->
<!doctype html>
<html>
	<head>
    <meta charset="utf-8">
    <title>机器人售卖控制</title>
    <meta name="viewport" content="width=device-width,initial-scale=1,user-scalable=0">
    <link rel="stylesheet" href="<%=basePath %>jsp/css/weui.css"/>
    <link rel="stylesheet" href="<%=basePath %>jsp/css/weuix.css"/>

    <script src="<%=basePath %>jsp/js/zepto.min.js"></script>
    <script src="<%=basePath %>jsp/js/zepto.weui.js"></script>
    <script>
        $(function(){
            $(".weui-payselect-li").on('click',function(){
                $(this).children().addClass("weui-payselect-on");
                $(this).siblings().children().removeClass("weui-payselect-on");
                return false;
            })
        });
    </script>
	</head>
<body ontouchstart>
	<div class="page-hd">
    <h1 class="page-hd-title">
        售卖控制
    </h1>
	</div>
    
	<div class="page-bd-15">
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
	<br>
	<br>
	<div class="weui-footer weui-footer_fixed-bottom">
			<p class="weui-footer__text">Copyright 2019 &copy; 擎谱集团</p>
	</div>	
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
</body>
</html>
