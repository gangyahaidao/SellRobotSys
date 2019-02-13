<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    <base href="<%=basePath%>">    
    <title>留言成功</title>    
	<meta charset="utf-8">
    <meta name="viewport" content="initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
    <link rel="stylesheet" href="<%=basePath %>jsp/hongbao/css/weui.min.css" />  
  </head>
  
  <body ontouchstart>
    <div class="weui-cells__title" style="text-align: center;">
		<div class="icon-box">
            <i class="weui-icon-success weui-icon_msg"></i>            
        </div>
        <div class="icon-box__ctn">
			<h3 class="icon-box__title">留言成功!</h3>
		</div>
	</div>
  </body>
</html>
