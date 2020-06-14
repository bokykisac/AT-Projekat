package ws;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import com.google.gson.Gson;

import model.SocketMessage;

@Singleton
@ServerEndpoint("/ws/{user}")
@LocalBean
public class WSEndPoint {

	static List<Session> sessions = new ArrayList<Session>();
	static Map<String, List<Session>> userSessions = new HashMap<>();
    Set<String> registeredUsers = new HashSet<String>();
	
	@OnOpen
	public void onOpen(@PathParam("user") String user, Session session) {
		if(!sessions.contains(session)) {
			sessions.add(session);
		}
	}
	
	@OnMessage
	public void echoTextMessage(Session session, String msg, boolean last) {
		try {
			if(session.isOpen()) {
				for(Session s : sessions) {
					if(!s.getId().equals(session.getId())) {
						s.getBasicRemote().sendText(msg, last);
					}
				}
			}
		}catch (IOException e) {
			try {
				session.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}

 	}
	
	@OnClose
	public void close(Session session) {
		sessions.remove(session);
	}
	
	@OnError
	public void error(Session session, Throwable t) {
		sessions.remove(sessions);
		t.printStackTrace();
	}
	
	public List<String> conversionUnique(List<String> list) {
    	Set<String> set = new HashSet<>(list);
    	List<String> unique = new ArrayList<>(set);
    	return unique;
    }

	public static Map<String, List<Session>> getUserSessions() {
		return userSessions;
	}

	public static void setUserSessions(Map<String, List<Session>> userSessions) {
		WSEndPoint.userSessions = userSessions;
	}

	public static List<Session> getSessions() {
		return sessions;
	}

	public static void setSessions(List<Session> sessions) {
		WSEndPoint.sessions = sessions;
	}

	public Set<String> getRegisteredUsers() {
		return registeredUsers;
	}

	public void setRegisteredUsers(Set<String> registeredUsers) {
		this.registeredUsers = registeredUsers;
	}

}
