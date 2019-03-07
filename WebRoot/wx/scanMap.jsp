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
    	var showAlert = false;
        function sendJSONRequest(url, jsonObjData) {
			$.ajax({
				type: "POST",
				url: "<%=request.getContextPath() %>/" + url,
				contentType: "application/json; charset=utf-8",
				dataType: "json",
				data: JSON.stringify(jsonObjData),
				success: function(data){
					console.log(data);
					if(data.code == -1) {
						$.alert(data.message);
					}
					if(showAlert) {
						$.alert(data.message);
						showAlert = false;
					}
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
            //*****************************************************************************************//
            $("#startScanMapMode").click(function(){
                var data = {
                    "machineId": "3",
                    "moveCmd": "startScanMapMode"
                }
                showAlert = true;
                sendJSONRequest("robot/teleopControlCmd", data);
            })
            $("#stopScanMapMode").click(function(){
                var data = {
                    "machineId": "3",
                    "moveCmd": "stopScanMapMode"
                }
                showAlert = true;
                sendJSONRequest("robot/teleopControlCmd", data);
            })
            $("#startNewPosMode").click(function(){
                var data = {
                    "machineId": "3",
                    "moveCmd": "startNewPosMode"
                }
                showAlert = true;
                sendJSONRequest("robot/teleopControlCmd", data);
            })
            $("#stopNewPosMode").click(function(){
                var data = {
                    "machineId": "3",
                    "moveCmd": "stopNewPosMode"
                }
                showAlert = true;
                sendJSONRequest("robot/teleopControlCmd", data);
            })
            $("#addNewPosName").click(function(){
                $.prompt({
                    title: "新地点",
                    input: "",
                    empty: false, // 是否允许为空
                    onOK: function (input) {
                        //点击确认
                        var data = {
                            "machineId": "3",
                            "moveCmd": "addNewPosName",
                            "posName": input
                        }
                        showAlert = true;
                        sendJSONRequest("robot/teleopControlCmd", data);
                    },
                    onCancel: function () {
                        //点击取消
                    }
                });
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
        <h1 class="weui-payselect-title">建图</h1>
        <a href="javascript:;" id="startScanMapMode" class="weui-btn weui-btn_primary">启动建图模式</a>
        <a href="javascript:;" id="stopScanMapMode" class="weui-btn weui-btn_warn">关闭建图并保存</a>
        <div class="weui-loadmore weui-loadmore_line weui-loadmore_dot">
            <span class="weui-loadmore__tips"></span>
        </div>   
        <a href="javascript:;" id="startNewPosMode" class="weui-btn weui-btn_primary">启动描点模式</a>        
        <a href="javascript:;" id="addNewPosName" class="weui-btn weui-btn_primary">增加路径地点</a>
        <a href="javascript:;" id="stopNewPosMode" class="weui-btn weui-btn_warn">关闭描点模式</a>
        <div class="weui-loadmore weui-loadmore_line weui-loadmore_dot">
            <span class="weui-loadmore__tips"></span>
        </div>
        <h1 class="weui-payselect-title">遥控</h1>
        <ul class="weui-payselect-ul">
            <div class="margin20">
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
