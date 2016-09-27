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
import org.junit.runners.Parameterized.Parameters;

import com.sun.org.glassfish.gmbal.ParameterNames;

/**
 * @author songyu.li@yeepay.com
 * @createDatetime 2016年6月2日 下午2:28:42
 */

@ServerEndpoint(value = "/users")
public class QueryUserServiceEndpoint {

private static Logger logger = Logger.getLogger(QueryUserServiceEndpoint.class);
	
	private Session session;
	
	private static Set<QueryUserServiceEndpoint> connections =
            new CopyOnWriteArraySet<QueryUserServiceEndpoint>();
	
	private static Map<String, QueryUserServiceEndpoint> mapSession = new HashMap<String, QueryUserServiceEndpoint>();
	
	@OnOpen
	public void onOpen(Session session) {
	        logger.info("Connected ... sessionId[" + session.getId() + "]==userId[" + session.getQueryString() + "]");
	        this.session = session;
	        connections.add(this);
	        mapSession.put(session.getQueryString(), this);
	    }
	  
	 @OnMessage
	 public void sendMsg(Session session, String message) {
		 logger.info("message ... sessionId[" + session.getId() + "]==userId[" + session.getQueryString() + "]");
		//session.getBasicRemote().sendText(message + "[server]");
		 //广播
		 //broadcast(message  + "[server]");
		 //定向发送
		 directional(session.getQueryString(), message + "[server]");
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
	        for (QueryUserServiceEndpoint client : connections) {
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

