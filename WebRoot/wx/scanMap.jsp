<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>

<!-- 零售机器人功能选择页面 -->
<!doctype html>
<html>
<head>
    <meta charset="utf-8">
    <title>机器人扫描建图</title>
    <meta name="viewport" content="width=device-width,initial-scale=1,user-scalable=0">
    <link rel="stylesheet" href="<%=basePath %>jsp/css/weui.css"/>
    <link rel="stylesheet" href="<%=basePath %>jsp/css/weuix.css"/>

    <script src="<%=basePath %>jsp/js/zepto.min.js"></script>
    <script src="<%=basePath %>jsp/js/zepto.weui.js"></script>
    <script>
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
        $(function(){        	
            $("#moveForward").click(function(){
                var data = {
                    "machineId": "3",
                    "moveCmd": "F"
                }
                sendJSONRequest("robot/teleopControlCmd", data);
            })
            $("#turnLeft").click(function(){
                var data = {
                    "machineId": "3",
                    "moveCmd": "L"
                }
                sendJSONRequest("robot/teleopControlCmd", data);
            })
            $("#turnRight").click(function(){
                var data = {
                    "machineId": "3",
                    "moveCmd": "R"
                }
                sendJSONRequest("robot/teleopControlCmd", data);
            })
            $("#stopMove").click(function(){
                var data = {
                    "machineId": "3",
                    "moveCmd": "S"
                }
                sendJSONRequest("robot/teleopControlCmd", data);
            })
            $("#moveBack").click(function(){
                var data = {
                    "machineId": "3",
                    "moveCmd": "B"
                }
                sendJSONRequest("robot/teleopControlCmd", data);
            })
        });
    </script>
</head>

<body ontouchstart>
    <div class="page-hd">
        <h1 class="page-hd-title">
            扫描建图控制
        </h1>
    </div>

    <div class="weui-pay">
        <h1 class="weui-payselect-title">遥控</h1>
        <ul class="weui-payselect-ul">
            <div style="margin-bottom:100px">
            	<li class="weui-payselect-li">
	                <a href="javascript:;" class="weui-btn weui-btn_primary" style="visibility:hidden">隐藏</a>
	            </li>
	            <li class="weui-payselect-li">
	                <a href="javascript:;" id="moveForward" class="weui-btn weui-btn_primary">前进</a>
	            </li>
	            <li class="weui-payselect-li">
	                <a href="javascript:;" class="weui-btn weui-btn_primary" style="visibility:hidden">隐藏</a>
	            </li>
            </div>
            <div style="margin-bottom:100px">
            	<li class="weui-payselect-li">
	                <a href="javascript:;" id="turnLeft" class="weui-btn weui-btn_primary">左转</a>
	            </li>
	            <li class="weui-payselect-li">
	                <a href="javascript:;" id="stopMove" class="weui-btn weui-btn_warn">停止</a>
	            </li>
	            <li class="weui-payselect-li">
	                <a href="javascript:;" id="turnRight" class="weui-btn weui-btn_primary">右转</a>
	            </li>
            </div>  
            <div>
            	<li class="weui-payselect-li">
	                <a href="javascript:;" class="weui-btn weui-btn_primary" style="visibility:hidden">隐藏</a>
	            </li>
	            <li class="weui-payselect-li">
	                <a href="javascript:;" id="moveBack" class="weui-btn weui-btn_primary">后退</a>
	            </li>
	            <li class="weui-payselect-li">
	                <a href="javascript:;" class="weui-btn weui-btn_primary" style="visibility:hidden">隐藏</a>
	            </li>
            </div>                      
        </ul>
    </div>


<br>
<br>
<div class="weui-footer weui-footer_fixed-bottom">
    <p class="weui-footer__text">Copyright 2019 &copy; 擎谱集团</p>
</div>
</body>
</html>
