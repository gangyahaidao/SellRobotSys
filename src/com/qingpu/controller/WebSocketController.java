package com.qingpu.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.json.JSONObject;

import com.qingpu.common.service.ZXingService;
import com.qingpu.common.service.ZXingServiceImplInstance;
import com.qingpu.common.utils.UUIDGenerator;

/**
 * 使用websocket与PC用户连接的功能类
 * */
@ServerEndpoint("/websocket")
public class WebSocketController {

	//静态变量，用来记录当前在线连接数。应该把它设计成线程安全的
	private static int onlineCount = 0;
	
	//存储客户端连接的Map
	private static Map<String, WebSocketController> webSocketMap = new HashMap<String, WebSocketController>();
	
	//与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;
    
    private String uuid;
    
    private static ZXingService zxingService = new ZXingServiceImplInstance();
    
    /**
     * 连接建立成功调用的方法
     * @param session  可选的参数。session为与某个客户端的连接会话，需要通过它来给客户端发送数据
     */
    @OnOpen
    public void onOpen(Session session){
        this.session = session;
        //生一个UUID用来标识当前客户端的连接
        this.uuid = UUIDGenerator.getUUID();
        //将当前的连接对象置入Map中
        webSocketMap.put(this.uuid, this);
        addOnlineCount();           //在线数加1
        //发送uuid到客户端
        try {
        	JSONObject jsonObject = new JSONObject();
        	jsonObject.put("cmd", 0x81);
        	jsonObject.put("UUID", uuid);
        	sendMessage(jsonObject.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
     
    /**
     * 连接关闭调用的方法,客户端页面刷新或者关闭都会调用此方法
     */
    @OnClose
    public void onClose(){
        webSocketMap.remove(this.uuid);        
        subOnlineCount();           //在线数减1    
    }
    
    /**
     * 收到客户端消息后调用的方法
     * @param message 客户端发送过来的消息
     * @param session 可选的参数
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        //针对客户端的请求做出不同的响应，this.sendMessage()
        //将接受到的JSON字符串转化成JSONObject
        JSONObject jsonObject = new JSONObject(message);
        JSONObject jsonObject2 = new JSONObject();
        
        int cmd = jsonObject.getInt("cmd");
        String data = jsonObject.getString("data");
        
        if(cmd == 0x01){
        	//请求一个登陆二维码
        	String QRUrl = zxingService.getQRCode("http://www.g58mall.com/qpsspring/wx/pc/pc-login.jsp?uuidlogin="+ data,
					"resources/logo.jpg");
        	//将二维码发送到客户端
        	try {        		
        		jsonObject2.put("cmd", 0x82);
        		jsonObject2.put("loginQRCode", QRUrl);
        		sendMessage(jsonObject2.toString());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }else{
        	
        }
    }
    
    /**
     * 发生错误时调用
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error){
        error.printStackTrace();
    }
     
    /**
     * 这个方法与上面几个方法不一样。没有用注解，是根据自己需要添加的方法。
     * @param message
     * @throws IOException
     */
    public void sendMessage(String message) throws IOException{
        this.session.getBasicRemote().sendText(message);
        //this.session.getAsyncRemote().sendText(message);
    }
 
    public static synchronized int getOnlineCount() {
        return onlineCount;
    }
 
    public static synchronized void addOnlineCount() {
        WebSocketController.onlineCount++;
    }
     
    public static synchronized void subOnlineCount() {
        WebSocketController.onlineCount--;
    }

	public static Map<String, WebSocketController> getWebSocketMap() {
		return webSocketMap;
	}

	public static void setWebSocketMap(Map<String, WebSocketController> webSocketMap) {
		WebSocketController.webSocketMap = webSocketMap;
	}

}
