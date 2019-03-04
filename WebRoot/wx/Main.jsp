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
    <title></title>
    <meta name="viewport" content="width=device-width,initial-scale=1,user-scalable=0">
    <link rel="stylesheet" href="<%=basePath %>jsp/css/weui.css"/>
    <link rel="stylesheet" href="<%=basePath %>jsp/css/weuix.css"/>

    <script src="<%=basePath %>jsp/js/zepto.min.js"></script>
    <script src="<%=basePath %>jsp/js/zepto.weui.js"></script>
    <script>
        $(function(){
        });
    </script>
</head>

<body ontouchstart>
<div class="page-hd">
    <h1 class="page-hd-title">
        机器人功能控制
    </h1>
</div>

<div class="weui-grids"  >
    <a href="scanMap.jsp" class="weui-grid js_grid"  >
        <div class="weui-grid__icon">
            <img src="<%=basePath %>jsp/images/icon_tabbar.png" alt="">
        </div>
        <p class="weui-grid__label">
            扫描建图
        </p>
    </a>
    <a href="robotControl.jsp" class="weui-grid js_grid" >
        <div class="weui-grid__icon">
            <img src="<%=basePath %>jsp/images/icon_nav_panel.png" alt="">
        </div>
        <p class="weui-grid__label">
            运行控制
        </p>
    </a>
    <a href="javascript:;" class="weui-grid js_grid" >
        <div class="weui-grid__icon">
            <img src="<%=basePath %>jsp/images/icon_nav_toast.png" alt="">
        </div>
        <p class="weui-grid__label">
            终端调试
        </p>
    </a>
</div>

<br>
<br>
<div class="weui-footer weui-footer_fixed-bottom">
    <p class="weui-footer__text">Copyright 2019 &copy; 擎谱集团</p>
</div>
</body>
</html>
