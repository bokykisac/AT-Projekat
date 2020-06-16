package node;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import agent_manager.AgentManager;
import model.AgentCenter;

@Startup
@Singleton
public class NodeManager implements NodeManagerLocal{

	private List<AgentCenter> nodes;
	private AgentCenter masterNode;
	private AgentCenter thisNode;
	
	private static String masterIp = "localhost:8080";
	private static String nodeIp = "localhost:8080";
	
	@EJB
	AgentManager am;

	public NodeManager() {
		nodes = new ArrayList<AgentCenter>();
	}
	
	@PostConstruct
	public void nodeInit() {
		System.out.println("STARTED!");
		setAgentCentre();
		try {
			am.startInit(getThisNode());
			System.out.println("AGENT MANAGER INITIATED");
			//heartbeat
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (!masterNode.getAddress().equals(thisNode.getAddress())) {
			//contact nodes
		}
		
	}
	
	private void setAgentCentre() {
		try {
			System.out.println("MASTER: " + masterIp);
			System.out.println("THIS HOST: " + nodeIp);
			this.masterNode = new AgentCenter("master", masterIp);
			this.thisNode = new AgentCenter("master", nodeIp);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public AgentCenter getThisNode() {
		return thisNode;
	}

	
	
}
