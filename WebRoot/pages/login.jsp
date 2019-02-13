<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>
<!DOCTYPE html>
<html>
    <head>
    	<base href="<%=basePath%>CallCenter2/">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <meta name="description" content="A fully featured admin theme which can be used to build CRM, CMS, etc.">
        <meta name="author" content="Coderthemes">

        <link rel="shortcut icon" href="assets/images/favicon_1.ico">

        <title>系统登录</title>

        <link href="assets/css/bootstrap.min.css" rel="stylesheet" type="text/css" />
        <link href="assets/css/core.css" rel="stylesheet" type="text/css" />
        <link href="assets/css/components.css" rel="stylesheet" type="text/css" />
        <link href="assets/css/icons.css" rel="stylesheet" type="text/css" />
        <link href="assets/css/pages.css" rel="stylesheet" type="text/css" />
        <link href="assets/css/responsive.css" rel="stylesheet" type="text/css" />

        <!-- HTML5 Shiv and Respond.js IE8 support of HTML5 elements and media queries -->
        <!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
        <!--[if lt IE 9]>
        <script src="https://oss.maxcdn.com/libs/html5shiv/3.7.0/html5shiv.js"></script>
        <script src="https://oss.maxcdn.com/libs/respond.js/1.3.0/respond.min.js"></script>
        <![endif]-->

        <script src="assets/js/modernizr.min.js"></script>
        
    </head>
    <body>

        <div class="account-pages"></div>
        <div class="clearfix"></div>
        <div class="wrapper-page">
        	<div class=" card-box">
            <div class="panel-heading"> 
                <h3 class="text-center"> 擎谱集团 <strong class="text-custom">呼叫中心</strong> </h3>
            </div> 

            <div class="panel-body">
            <form class="form-horizontal m-t-20" action="">
                
                <div class="form-group ">
                    <div class="col-xs-12">
                        <input id="username" class="form-control" type="text" required="" placeholder="账号">
                    </div>
                </div>

                <div class="form-group">
                    <div class="col-xs-12">
                        <input id="password" class="form-control" type="password" required="" placeholder="密码">
                    </div>
                </div>

                <div class="form-group ">
                    <div class="col-xs-12">
                        <div class="checkbox checkbox-primary">
                            <input id="checkbox-signup" type="checkbox">
                            <label for="checkbox-signup">
                                	记住账号密码
                            </label>
                        </div>
                        
                    </div>
                </div>
                
                <div class="form-group text-center m-t-40">
                    <div class="col-xs-12">
                        <button id="loginBtn" class="btn btn-info btn-block text-uppercase waves-effect waves-light" type="submit">登  录</button>
                    </div>
                </div>

                <div class="form-group m-t-30 m-b-0">
                    <div class="col-sm-12">
                        <a href="#" class="text-dark"><i class="fa fa-lock m-r-5"></i> 忘 记 密 码 </a>
                    </div>
                </div>
            </form> 
            
            </div>   
            </div>                              
                <div class="row">
            	<div class="col-sm-12 text-center">
            		<p>还没有账户？ <a href="#" class="text-primary m-l-5"><b>联系管理员</b></a></p>
                        
                    </div>
            </div>
            
        </div>            
        
    	<script>
            var resizefunc = [];
        </script>

        <!-- jQuery  -->
        <script src="assets/js/jquery.min.js"></script>
        <script src="assets/js/bootstrap.min.js"></script>
        <script src="assets/js/detect.js"></script>
        <script src="assets/js/fastclick.js"></script>
        <script src="assets/js/jquery.slimscroll.js"></script>
        <script src="assets/js/jquery.blockUI.js"></script>
        <script src="assets/js/waves.js"></script>
        <script src="assets/js/wow.min.js"></script>
        <script src="assets/js/jquery.nicescroll.js"></script>
        <script src="assets/js/jquery.scrollTo.min.js"></script>
        
        <script src="assets/js/jquery.core.js"></script>
        <script src="assets/js/jquery.app.js"></script>
        <script src="js/JsonUtils.js"></script>
        <script type="text/javascript">
        	function loginExecSuccess(data){
        		if(data.code == -1)
        		{
        			alert(data.message);
        			return;	
        		}else if(data.code == 0)
        		{
        			window.location.href = "MainPage.html";
        		}					
			}
			function loginExecFailed(message){
				alert(message);
			}
        	$(document).ready(function(){
				//判断当前浏览器是否支持WebSocket
			    if(!('WebSocket' in window)){
			        alert('当前浏览器不支持WebSocket通信，请更换浏览器重试');
			        return;
			    }			
			    $("#loginBtn").click(function(){
			    	var username = $("#username").val().trim();
			    	var password = $("#password").val().trim();
			    	if(username.length > 0 && password.length > 0){
			    		var params = {
			    			username: username,
			    			password: password
			    		}
			    		jsonrpc("/CallCenter2/CallCenter2/userLogin", params, loginExecSuccess, loginExecFailed);
			    	}
			    });
        	});
        </script>
	
	</body>
</html>