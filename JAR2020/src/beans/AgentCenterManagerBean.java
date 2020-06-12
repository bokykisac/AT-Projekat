package beans;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import com.google.gson.Gson;

import model.ACLMessage;
import model.Agent;
import model.AgentCenter;
import model.AgentType;
import model.UpdatePackage;
import model.User;
import service.AgentCenterService;
import service.RestHost;

@Singleton
@LocalBean
@Startup
public class AgentCenterManagerBean {
	private Map<String, AgentCenter> agentCenters = new HashMap<>();
	private String masterInfo = "";
	private String hostInfo = "";
	private AgentCenter masterAgentCenter = new AgentCenter();
	private AgentCenter slaveAgentCenter = new AgentCenter();
	private Map<String, List<String>> foreignLoggedUsers = new HashMap<>();
	private Map<String, Set<String>> foreignRegisteredUsers = new HashMap<>();
	private Map<String, AgentType> agentsTypes = new HashMap<>();
	private HashMap<String, Agent> runningAgents = new HashMap<>();
	private Map<String, User> users;
	public HashMap<String, Agent> getRunningAgents() {
		return runningAgents;
	}

	public void setRunningAgents(HashMap<String, Agent> runningAgents) {
		this.runningAgents = runningAgents;
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

	private Map<String, User> loggedInUsers;
	private HashMap<UUID, ACLMessage> messages = new HashMap<>();
	
	
	@EJB
	Db db;
	
	@EJB
	AgentCenterService agentCenterService;
	
	
	@PostConstruct
	public void handshakeInit() {		
		findMasterIpAddress();
		setHosts();
		
		if (!masterAgentCenter.equals(slaveAgentCenter)) {
			System.out.println("Handshake started");
			
			try {
				RestHost.registerNodeBuilder(this.slaveAgentCenter, this.masterAgentCenter );
			} catch (Exception e) {
				startAgain("First");
			}
			
			try {
				Collection<AgentCenter> otherHosts = RestHost.sendHostsToNewHostBuilder(this.slaveAgentCenter, this.masterAgentCenter);
				for (AgentCenter h: otherHosts) {
					this.agentCenters.put(h.getAddress(), h);
				}
			} catch (Exception e) {
				startAgain("Third");
			}
			
			try {
				UpdatePackage newUpdatePackage = RestHost.sendAllLoggedInUsersToNodeBuilder(this.slaveAgentCenter, this.masterAgentCenter, new UpdatePackage(), 1);
				foreignLoggedUsers = new Gson().fromJson(newUpdatePackage.getLoggedInUsers().get(0), foreignLoggedUsers.getClass());
				Map<String, List<String>> helpMap = new Gson().fromJson(newUpdatePackage.getRegisteredUsers().iterator().next(), foreignRegisteredUsers.getClass());
				helpConversion(helpMap);
			} catch (Exception e) {
				startAgain("Fourth");
			}
			
			System.out.println("Handshake over - SUCCESS");
			
		}	
		
	}
	
	@PreDestroy
	public void shutDownHost() {
		System.out.println("Shutting down the host");
		RestHost.deleteHostBuilder(this.masterAgentCenter, this.slaveAgentCenter);
		System.out.println("Host deleted from master");
	}
	
	@Schedule(hour="*", minute = "*", second = "*/60", info = "Every 60 seconds")
	public void heartbeatProtocol() {
		System.out.println("Heartbeat Starting");
		
		for (AgentCenter h: agentCenters.values()) {
			if (!h.getAddress().contains(slaveAgentCenter.getAddress())) {
				System.out.println("[INFO] [HEARTBEAT] Checking is alive {" + h.getAddress() + "}");
				int succ = 0;
				try {
					succ = RestHost.checkIfAliveBuilder(h);
				} catch (Exception e) {
					System.out.println("[INFO] [HEARTBEAT] Host {" + h.getAddress() + "} didn't answer");
					System.out.println("[INFO] [HEARTBEAT] Checking is alive {" + h.getAddress() + "} - Second time");
					try {
						succ = RestHost.checkIfAliveBuilder(h);
					} catch (Exception eSecond) {
						System.out.println("[INFO] [HEARTBEAT] Host {" + h.getAddress() + "} didn't answer");
						System.out.println("[INFO] [HEARTBEAT] Host {" + h.getAddress() + "} is dead");
					}
				}
				
				if (succ != 1) {
					System.out.println("[INFO] [HEARTBEAT] Deleting host {" + h.getAddress() + "} from current host");
					deleteHostFromCurrentHost(h.getAddress());
					System.out.println("[INFO] [HEARTBEAT] Host deleted {" + h.getAddress() + "} from current host");
					System.out.println("[INFO] [HEARTBEAT] Deleting host {" + h.getAddress() + "} from other hosts");
					deleteHostFromOtherHosts(h);
					System.out.println("[INFO] [HEARTBEAT] Host deleted {" + h.getAddress() + "} from other hosts");
				} else {
					System.out.println("[INFO] [HEARTBEAT] Host {" + h.getAddress() + "} is OK");
				}
			}
		}
		System.out.println("[INFO] [HEARTBEAT] Finished");
	}
	
	public void startAgain(String err) {
		try {
			System.out.println("[INFO] " + err + "step retrying");
			switch(err) {
				case "First":
					System.out.println("[INFO] [NEW HOST] First step - Register to master: " + this.slaveAgentCenter.getAddress());
					System.out.println("[INFO] [NEW HOST] Second step - Master should send new host to other hosts");
					RestHost.registerNodeBuilder(this.slaveAgentCenter, this.masterAgentCenter);
					break;
				case "Third":
					Collection<AgentCenter> otherHosts = RestHost.sendHostsToNewHostBuilder(this.slaveAgentCenter, this.masterAgentCenter);
					System.out.println("[INFO] [NEW HOST] Third step - Received list of other hosts from master with size: " + otherHosts.size());
					for (AgentCenter h: otherHosts) {
						this.agentCenters.put(h.getAddress(), h);
					}
					break;
				case "Fourth":
					UpdatePackage newUpdatePackage = RestHost.sendAllLoggedInUsersToNodeBuilder(this.slaveAgentCenter, this.masterAgentCenter, new UpdatePackage(), 1);
					System.out.println("[INFO] [NEW HOST] Fourth step - Received list of logged users with size: " + newUpdatePackage.getLoggedInUsers().size());
					System.out.println("[INFO] [NEW HOST] Fourth step - Received set of registered users with size: " + newUpdatePackage.getRegisteredUsers().size());
					break;
			}	
		} catch (Exception e) {
			System.out.println("[INFO] [ERROR] Some error has occured in " + err.toLowerCase() + " step");
			System.out.println("[INFO] [ERROR] Deleting host from master");
			RestHost.deleteHostBuilder(this.masterAgentCenter, this.slaveAgentCenter);
			System.out.println("[INFO] [ERROR] Host deleted from master");
		}
	}
	
	public void setHosts() {
		String aliasMaster = this.masterInfo.split(":")[0];
		String ipMaster = this.masterInfo.split(":")[1];
		String portMaster = this.masterInfo.split(":")[2];
		
		String aliasSlave = this.hostInfo.split(":")[0];
		String ipSlave = this.hostInfo.split(":")[1];
		String portSlave = this.hostInfo.split(":")[2];
		
		AgentCenter masterHost = new AgentCenter(ipMaster + ":" + portMaster, ipMaster + ":" + portMaster);
		this.agentCenters.put(masterHost.getAddress(), masterHost);
		this.masterAgentCenter = masterHost;
		
		if (!ipMaster.equals(System.getProperty("jboss.bind.address"))) {
			AgentCenter slaveHost = new AgentCenter(ipSlave + ":" + portSlave, ipSlave + ":" + portSlave);
			this.agentCenters.put(slaveHost.getAddress(), slaveHost);
			this.slaveAgentCenter = slaveHost;
		} else {
			this.slaveAgentCenter = masterHost;
		}
	}
	
	public void findMasterIpAddress() {
		String masterIp = "";
		String hostIp = "";
		
		try {
		      File ipConfigFile = new File(User.class.getProtectionDomain().getCodeSource().getLocation().getPath() 
		    		  + File.separator + "META-INF" 
		    		  + File.separator + "ip_config.txt");
		      Scanner reader = new Scanner(ipConfigFile);
		      
		      if (reader.hasNextLine()) {
		    	  masterIp = reader.nextLine();
		      }
		      
		      if (reader.hasNextLine()) {
		    	  hostIp = reader.nextLine();
		      }
		      
		      reader.close();
		      
		    } catch (FileNotFoundException e) {
		      System.out.println("Config file is not found.");
		      e.printStackTrace();
		    }
		
		if (masterIp.equals("master:mLocalhost:8080") || hostIp.equals("host:hLocalhost:8080") || !hostIp.split(":")[1].equals(System.getProperty("jboss.bind.address"))) {
			throw new Error ("Set up ip_config.txt file in META-INF folder");
		}
		
		this.masterInfo = masterIp;
		this.hostInfo = hostIp;
		
	}
	
	public void helpConversion(Map<String, List<String>> helpMap) {
		Map<String, Set<String>> newMap = new HashMap<>();
		for(Map.Entry<String, List<String>> entry: helpMap.entrySet()) {
			newMap.put(entry.getKey(), new HashSet<String>(entry.getValue()));
		}
		foreignRegisteredUsers = newMap;
	}
	
	public void deleteHostFromCurrentHost(String hostIp) {
		this.agentCenters.remove(hostIp);
		this.foreignLoggedUsers.remove(hostIp);
		this.foreignRegisteredUsers.remove(hostIp);
		System.out.println("[DELETE] [" + slaveAgentCenter.getAddress() + "] Host {" + hostIp + "} is removed");
		
		agentCenterService.updateUsersInSocket();
		
		agentCenterService.purgeMessages(hostIp);
	}
	
	public void deleteHostFromOtherHosts(AgentCenter deletedHost) {
		for(AgentCenter h: agentCenters.values()) {
			if ((!h.getAddress().equals(slaveAgentCenter.getAddress())) && (!h.getAddress().equals(deletedHost.getAddress()))) {
				System.out.println("[DELETE] [" + slaveAgentCenter.getAddress() + "] Deleting host {" + deletedHost.getAddress() + "} - from host {" + h.getAddress() + "}");
				try {
					RestHost.deleteFromSpecificHostBuilder(h, deletedHost);
				} catch (Exception e) {
					System.out.println("[DELETE] [" + slaveAgentCenter.getAddress() + "] [ERROR] Deleting host {" + deletedHost.getAddress() + "} - from host {" + h.getAddress() + "} - Second time");
					try {
						RestHost.deleteFromSpecificHostBuilder(h, deletedHost);
					} catch (Exception eSecond) {
						System.out.println("[DELETE] [" + slaveAgentCenter.getAddress() + "] [ERROR] Not able to delete host {" + deletedHost.getAddress() + "} - from host {" + h.getAddress() + "}");
					}
				}
			}
		}
	}
	
	public Map<String, AgentCenter> getHosts() {
		return agentCenters;
	}

	public void setHosts(Map<String, AgentCenter> hosts) {
		this.agentCenters = hosts;
	}


	public String getMasterInfo() {
		return masterInfo;
	}


	public void setMasterInfo(String masterInfo) {
		this.masterInfo = masterInfo;
	}


	public String getHostInfo() {
		return hostInfo;
	}


	public void setHostInfo(String hostInfo) {
		this.hostInfo = hostInfo;
	}


	public AgentCenter getMasterHost() {
		return masterAgentCenter;
	}


	public void setMasterHost(AgentCenter masterHost) {
		this.masterAgentCenter = masterHost;
	}


	public AgentCenter getslaveAgentCenter() {
		return slaveAgentCenter;
	}


	public void setslaveAgentCenter(AgentCenter slaveAgentCenter) {
		this.slaveAgentCenter = slaveAgentCenter;
	}

	public Map<String, List<String>> getForeignLoggedUsers() {
		return foreignLoggedUsers;
	}

	public void setForeignLoggedUsers(Map<String, List<String>> foreignLoggedUsers) {
		this.foreignLoggedUsers = foreignLoggedUsers;
	}

	public Map<String, Set<String>> getForeignRegisteredUsers() {
		return foreignRegisteredUsers;
	}

	public void setForeignRegisteredUsers(Map<String, Set<String>> foreignRegisteredUsers) {
		this.foreignRegisteredUsers = foreignRegisteredUsers;
	}

	public Map<String, AgentType> getAgentsTypes() {
		return agentsTypes;
	}

	public void setAgentsTypes(Map<String, AgentType> agentsTypes) {
		this.agentsTypes = agentsTypes;
	}
	
	
	
}
