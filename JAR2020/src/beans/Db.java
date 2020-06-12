package beans;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.ejb.LocalBean;
import javax.ejb.Singleton;

import model.ACLMessage;
import model.Agent;
import model.AgentCenter;
import model.AgentType;
import model.User;

@Singleton
@LocalBean
public class Db implements Serializable{
	
	private HashMap<String, AgentType> agentsTypes = new HashMap<>();
	private HashMap<String, Agent> runningAgents = new HashMap<>();
	private HashMap<String, AgentCenter> agentCenters = new HashMap<>();
	private Map<String, User> users;
	private Map<String, User> loggedInUsers;
	private HashMap<UUID, ACLMessage> messages = new HashMap<>();

	
	public Db() {
		
	}
	
	public Db(HashMap<String, AgentType> agentsTypes, HashMap<String, Agent> runningAgents,
			HashMap<String, AgentCenter> agentCenters, Map<String, User> users, Map<String, User> loggedInUsers,
			HashMap<UUID, ACLMessage> messages) {
		super();
		this.agentsTypes = agentsTypes;
		this.runningAgents = runningAgents;
		this.agentCenters = agentCenters;
		this.users = users;
		this.loggedInUsers = loggedInUsers;
		this.messages = messages;
	}

	public HashMap<String, AgentType> getAgentsTypes() {
		return agentsTypes;
	}

	public void setAgentsTypes(HashMap<String, AgentType> agentsTypes) {
		this.agentsTypes = agentsTypes;
	}

	public HashMap<String, Agent> getRunningAgents() {
		return runningAgents;
	}

	public void setRunningAgents(HashMap<String, Agent> runningAgents) {
		this.runningAgents = runningAgents;
	}

	public HashMap<String, AgentCenter> getAgentCenters() {
		return agentCenters;
	}

	public void setAgentCenters(HashMap<String, AgentCenter> agentCenters) {
		this.agentCenters = agentCenters;
	}

	public Map<String, User> getUsers() {
		return users;
	}

	public void setUsers(Map<String, User> users) {
		this.users = users;
	}

	public Map<String, User> getLoggedInUsers() {
		return loggedInUsers;
	}

	public void setLoggedInUsers(Map<String, User> loggedInUsers) {
		this.loggedInUsers = loggedInUsers;
	}

	public HashMap<UUID, ACLMessage> getMessages() {
		return messages;
	}

	public void setMessages(HashMap<UUID, ACLMessage> messages) {
		this.messages = messages;
	}
	
	
	

}
