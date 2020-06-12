package service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.websocket.Session;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import com.google.gson.Gson;

import beans.AgentCenterManagerBean;
import beans.Db;
import dto.HandshakeDTO;
import dto.MessageDTO;
import model.AgentCenter;
import model.ForeignMessage;
import model.SocketMessage;
import model.UpdatePackage;
import model.User;
import ws.WSEndPoint;

@Stateless
@Path("/agent-center")
@LocalBean
public class AgentCenterService implements Rest{
	
	@EJB
	Db db;
	
	@EJB
	AgentCenterManagerBean agentCenterManagerBean;
	
	@EJB
	WSEndPoint ws;
	
	@Override
    public void registerNode(AgentCenter newHost) {
		System.out.println("[INFO] [MASTER] First step - Master recieved registration from: " + newHost.getAddress());
		
        if (!agentCenterManagerBean.getHosts().containsKey(newHost.getAddress())) {
        	agentCenterManagerBean.getHosts().put(newHost.getAddress(), newHost);
        	System.out.println("[INFO] [MASTER] First step - FINISHED");
        	
        	System.out.println("[INFO] [MASTER] Second step - Send new host to other hosts");
        	for (AgentCenter h: agentCenterManagerBean.getHosts().values()) {
        		if ((!h.getAddress().equals(newHost.getAddress())) && (!h.getAddress().equals(agentCenterManagerBean.getMasterHost().getAddress()))) {
        			RestHost.sendNewHostToHostBuilder(h.getAddress(), newHost);
        			System.out.println("[INFO] [MASTER] Second step - Sent to: " + h.getAddress());
        		}
        	}
        	
        	System.out.println("[INFO] [MASTER] Second step - FINISHED");
        }
    }


	@Override
    public void sendNewHostToHost(AgentCenter newHost) {
        if (!agentCenterManagerBean.getHosts().containsKey(newHost.getAddress())) {
        	agentCenterManagerBean.getHosts().put(newHost.getAddress(), newHost);
        	System.out.println("[INFO] [" + agentCenterManagerBean.getslaveAgentCenter().getAddress() + "] Received new host: " + newHost.getAddress());
        }
    }

	@Override
    public Collection<AgentCenter> sendHostsToNewHost(AgentCenter newHost) {
    	System.out.println("[INFO] [MASTER] Third step - Received request from host: " + newHost.getAddress());
    	List<AgentCenter> otherHosts = new ArrayList<AgentCenter>();
        for(AgentCenter h: agentCenterManagerBean.getHosts().values()) {
        	if ((!h.getAddress().equals(newHost.getAddress())) && (!h.getAddress().equals(agentCenterManagerBean.getMasterHost().getAddress()))) {
        		otherHosts.add(h);
        	}
        }
        
        System.out.println("[INFO] [MASTER] Third step - Sending list of other host with size: " + otherHosts.size());
        System.out.println("[INFO] [MASTER] Third step - FINISHED");
        return otherHosts;
    }

	@Override
    public UpdatePackage sendAllLoggedInUsersToNode(HandshakeDTO handshakeDTO) {
    	AgentCenter sender = handshakeDTO.getSender();
    	UpdatePackage updatePackage = handshakeDTO.getUpdatePackage();
    	int handshake = handshakeDTO.getHandshake();

		if (handshake == 1) {
			System.out.println("[INFO] [MASTER] Fourth step - Received request from host: " + sender.getAddress());
			UpdatePackage newUpdatePackage = new UpdatePackage();
			Map<String, List<String>> loggedInUsersByHosts = new HashMap<>();
			Map<String, Set<String>> registeredUsersByHosts = new HashMap<>();
			List<String> loggedInUsernamesOnMaster = new ArrayList<>();
			Set<String> registeredUsernamesOnMaster = new HashSet<>();
			
			//Logged in users directly from master
			for (User u: db.getLoggedInUsers().values()) {
				loggedInUsernamesOnMaster.add(u.getUsername());
			}
			loggedInUsersByHosts.put(agentCenterManagerBean.getslaveAgentCenter().getAddress(), loggedInUsernamesOnMaster);
			System.out.println("[INFO] [MASTER] Fourth step - [DIRECTLY MASTER] Size of list of logged in users: " + loggedInUsernamesOnMaster.size());
			
			//Logged in users from other hosts
			for (Map.Entry<String, List<String>> entry : agentCenterManagerBean.getForeignLoggedUsers().entrySet()) {
			    if (!entry.getKey().equals(sender.getAddress())) {
			    	loggedInUsersByHosts.put(entry.getKey(), entry.getValue());
			    }
			}
			String jsonLoggedIn = new Gson().toJson(loggedInUsersByHosts);
			System.out.println("[INFO] [MASTER] Fourth step - [ALL] Map of logged in users converted to JSON");
			newUpdatePackage.getLoggedInUsers().add(jsonLoggedIn);
			System.out.println("[INFO] [MASTER] Fourth step - [ALL] Map of logged in users added to package");
			
			//Registered users directly from master
			for (User u: db.getUsers().values()) {
				registeredUsernamesOnMaster.add(u.getUsername());
			}
			registeredUsersByHosts.put(agentCenterManagerBean.getslaveAgentCenter().getAddress(), registeredUsernamesOnMaster);
			System.out.println("[INFO] [MASTER] Fourth step - [DIRECTLY MASTER] Size of set of registered users: " + registeredUsernamesOnMaster.size());
			
			//Registered users from other hosts
			for (Map.Entry<String, Set<String>> entry : agentCenterManagerBean.getForeignRegisteredUsers().entrySet()) {
				if (!entry.getKey().equals(sender.getAddress())) {
					registeredUsersByHosts.put(entry.getKey(), entry.getValue());
				}
			}
			String jsonRegistered = new Gson().toJson(registeredUsersByHosts);
			System.out.println("[INFO] [MASTER] Fourth step - [ALL] Map of registered users converted to JSON");
			newUpdatePackage.getRegisteredUsers().add(jsonRegistered);
			System.out.println("[INFO] [MASTER] Fourth step - [ALL] Map of registered users added to package");
			
			System.out.println("[INFO] [MASTER] Fourth step - FINISHED");
			return newUpdatePackage;
			
		} else {
			System.out.println("[INFO] [" + agentCenterManagerBean.getslaveAgentCenter().getAddress() + "] Got an user update from host: " + sender.getAddress());
			agentCenterManagerBean.getForeignLoggedUsers().put(sender.getAddress(), updatePackage.getLoggedInUsers());
			agentCenterManagerBean.getForeignRegisteredUsers().put(sender.getAddress(), updatePackage.getRegisteredUsers());
			updateUsersInSocket();
			
			return updatePackage;
		}
	}
    
	@Override
    public void deleteHost(@PathParam("alias") String alias) {
    	System.out.println("[DELETE] [" + agentCenterManagerBean.getslaveAgentCenter().getAddress() + "] Deleting host: " + alias);
    	AgentCenter deletedHost = agentCenterManagerBean.getHosts().remove(alias);
		if (deletedHost != null) {
			agentCenterManagerBean.getForeignLoggedUsers().remove(alias);
			agentCenterManagerBean.getForeignRegisteredUsers().remove(alias);
			System.out.println("[DELETE] [" + agentCenterManagerBean.getslaveAgentCenter().getAddress() + "] Host {" + alias + "} is removed");
			
			updateUsersInSocket();
			
			purgeMessages(alias);
			
			if (agentCenterManagerBean.getslaveAgentCenter().getAddress().equals(agentCenterManagerBean.getMasterHost().getAddress())) {
	    		for (AgentCenter h: agentCenterManagerBean.getHosts().values()) {
	    			if (!h.getAddress().equals(agentCenterManagerBean.getMasterHost().getAddress())) {
	    				System.out.println("[DELETE] [MASTER] Deleting {" + alias + "} from {" + h.getAlias() + "}");
	    				RestHost.deleteHostBuilder(h, deletedHost);
	    			}
	    		}
	    		System.out.println("[DELETE] [MASTER] All other host are purged from {" + alias + "}");
	    	}
		}
    }
    
	@Override
    public int sendMessage(ForeignMessage foreignMessage) {
    	System.out.println("[INFO] [" + agentCenterManagerBean.getslaveAgentCenter().getAddress() + "] Recieved message from host {" + foreignMessage.getIpSendingHost() + "}");
    	User receivingUser = db.getUsers().get(foreignMessage.getRecieverUsername());
    	if (receivingUser != null) {
    		receivingUser.getReceivedForeignMessages().add(foreignMessage);
    		System.out.println("[INFO] [" + agentCenterManagerBean.getslaveAgentCenter().getAddress() + "] Received message added to model");
    		
    		MessageDTO messageDTO = new MessageDTO(foreignMessage);
    		String jsonMessageDTO = new Gson().toJson(messageDTO);
			ws.echoTextMessage(jsonMessageDTO);
			System.out.println("[INFO] [" + agentCenterManagerBean.getslaveAgentCenter().getAddress() + "] Received message sent to sockets");
			return 1;
    	} else {
    		System.out.println("[ERROR] [" + agentCenterManagerBean.getslaveAgentCenter().getAddress() + "] User " + foreignMessage.getRecieverUsername() + " doesn't exist");
    		System.out.println("[ERROR] [" + agentCenterManagerBean.getslaveAgentCenter().getAddress() + "] Message is not sent");
    		return 0;
    	}
    }
	
	@Override
    public int checkIfAlive() {
    	return 1;
    }
    
	@Override
    public void deleteFromSpecificHost(@PathParam("alias") String alias) {
    	System.out.println("[DELETE] [" + agentCenterManagerBean.getslaveAgentCenter().getAddress() + "] Deleting host: " + alias);
    	AgentCenter deletedHost = agentCenterManagerBean.getHosts().remove(alias);
		if (deletedHost != null) {
			agentCenterManagerBean.getForeignLoggedUsers().remove(alias);
			agentCenterManagerBean.getForeignRegisteredUsers().remove(alias);
			System.out.println("[DELETE] [" + agentCenterManagerBean.getslaveAgentCenter().getAddress() + "] Host {" + alias + "} is removed");
			
			updateUsersInSocket();
			
			purgeMessages(alias);
		}
    }
    
    public void updateUsersInSocket() {
    	System.out.println("[INFO] Updating sockets");
    	List<String> usernames = new ArrayList<>(ws.getUserSessions().keySet());
    	for (List<String> listOfForeignLoggedInUsers: agentCenterManagerBean.getForeignLoggedUsers().values()) {
    		usernames.addAll(listOfForeignLoggedInUsers);
    	}
		SocketMessage message = new SocketMessage("logged", new Date(), new Gson().toJson(usernames));
		String jsonMessage = new Gson().toJson(message);
		try {
			for (Session s: ws.getSessions()) {
				s.getBasicRemote().sendText(jsonMessage);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		usernames = new ArrayList<>(ws.getRegisteredUsers());
		for (Set<String> setOfForeignRegisteredUsers: agentCenterManagerBean.getForeignRegisteredUsers().values()) {
    		usernames.addAll(new ArrayList<String>(setOfForeignRegisteredUsers));
    	}
		message = new SocketMessage("registered", new Date(), new Gson().toJson(usernames));
		jsonMessage = new Gson().toJson(message);
		try {
			for (Session s: ws.getSessions()) {
				s.getBasicRemote().sendText(jsonMessage);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public void purgeMessages(String hostIp) {
    	System.out.println("[INFO] [" + agentCenterManagerBean.getslaveAgentCenter().getAddress() + "] Purging messages of deleted host {" + hostIp + "}");
    	for(User u: db.getUsers().values()) {
    		List<ForeignMessage> receivedToRemove = new ArrayList<>();
    		for (ForeignMessage received: u.getReceivedForeignMessages()) {
    			if (received.getIpSendingHost().equals(hostIp)) {
    				receivedToRemove.add(received);
    			}
    		}
    		if (receivedToRemove.size() != 0) {
    			u.getReceivedForeignMessages().removeAll(receivedToRemove);
    		}
    		
    		List<ForeignMessage> sentToRemove = new ArrayList<>();
    		for (ForeignMessage sent: u.getSentForeignMessages()) {
    			if (sent.getIpReceivingHost().equals(hostIp)) {
    				sentToRemove.add(sent);
    			}
    		}
    		if (sentToRemove.size() != 0) {
    			u.getSentForeignMessages().removeAll(sentToRemove);
    		}
    	}
    	System.out.println("[INFO] [" + agentCenterManagerBean.getslaveAgentCenter().getAddress() + "] Messages purged");
    }
	
}
