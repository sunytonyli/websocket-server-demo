package com.tony.tyrusdemo.wsserver;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.apache.log4j.Logger;

/**
 * @author songyu.li@yeepay.com
 * @createDatetime 2016年5月31日 下午4:17:01
 */

@ServerEndpoint(value = "/users/{userId}")
public class UserServiceEndpoint {

	private static Logger logger = Logger.getLogger(UserServiceEndpoint.class);
	
	private Session session;
	
	private static Set<UserServiceEndpoint> connections =
            new CopyOnWriteArraySet<UserServiceEndpoint>();
	
	private static Map<String, UserServiceEndpoint> mapSession = new HashMap<String, UserServiceEndpoint>();
	
	@OnOpen
	public void onOpen(Session session, @PathParam("userId") String userId) {
	        logger.info("Connected ... sessionId[" + session.getId() + "]==userId[" + userId + "]");
	        this.session = session;
	        connections.add(this);
	        mapSession.put(userId, this);
	    }
	  
	 @OnMessage
	 public void sendMsg(Session session, String message, @PathParam("userId") String userId) {
		 logger.info("message ... sessionId[" + session.getId() + "]==userId[" + userId + "]");
		 /*for (Session s : session.getOpenSessions()) {
		        if (s.isOpen()) {
		          s.getBasicRemote().sendText(message + "[server]");
		        }
		 }*/
		//session.getBasicRemote().sendText(message + "[server]");
		 //广播
		 //broadcast(message  + "[server]");
		 //定向发送
		 directional(userId, message + "[server]");
	 }
	
	 
	 @OnClose
	 public void onClose(Session session, CloseReason closeReason) throws IOException {
	        logger.info(String.format("Session %s closed because of %s", session.getId(), closeReason));
	        session.close();
	 }
	
	 /**
	  * 广播消息
	  * @param msg
	  * @throws IOException
	  */
	 private static void broadcast(String msg) {
	        for (UserServiceEndpoint client : connections) {
	        	if(client.session.isOpen()){
	        		try{
	        			client.session.getBasicRemote().sendText(msg);
	        		}catch(IOException ioe){
	        			try {
							client.session.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
	        			connections.remove(client);
	 	                broadcast(msg);
	        		}
	        	}
	        }
	    }
	 
	 /**
	  * 定向发送消息
	  * @param userId
	  * @param msg
	 * @throws IOException 
	  */
	 public static void directional(String userId, String msg) {
		 logger.info("userId[" + userId + "]" + ",session[" + mapSession.get(userId).session.getId() + "]");
		 if(mapSession.get(userId).session.isOpen()){
			 try {
				mapSession.get(userId).session.getBasicRemote().sendText(msg);
			} catch (IOException e) {
				try {
					mapSession.get(userId).session.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				e.printStackTrace();
			} 
		 }
	 }
}
