package agent_manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Singleton;

import model.AID;
import model.Agent;
import model.AgentCenter;
import model.AgentType;
import node.NodeManager;

@Singleton
public class AgentManager {
	private HashMap<AID, Agent> runningAgents;
	private HashMap<AgentCenter, ArrayList<AgentType>> agentTypes;
	
	@EJB
	NodeManager nm;
	
	
	public void startInit(AgentCenter center) {
		runningAgents = new HashMap<AID, Agent>();
		initAgentTypes(center);
	}
	
	private void initAgentTypes(AgentCenter center) {
		agentTypes = new HashMap<>();
		
		AgentType at1 = new AgentType("PingAgent", "agents");
		ArrayList<AgentType> tmp = new ArrayList<AgentType>();
		tmp.add(at1);
		agentTypes.put(center, tmp);

//		final File basePackage = new File(
//				AgentManagerLocal.class.getProtectionDomain().getCodeSource().getLocation().getPath() + File.separator
//						+ "agent");

//		ArrayList<AgentType> agentTypesList = processFile(basePackage);

//		agentTypes.put(center, agentTypesList);
		
//		for (AgentType at: agentTypesList) {
//			AID a = new AID(at.getName()+ "1", center, at);
//			startAgent(a);
//		}
	}
	
	public List<AID> getRunningAgents() {
		return new ArrayList<>(runningAgents.keySet());
	}
	
	public List<AgentType> getAgentTypes() {
		ArrayList<AgentType> retVal = new ArrayList<>();
		for (AgentCenter key : agentTypes.keySet()) {
			retVal.addAll(agentTypes.get(key));
		}
		return retVal;
	}
	
	public boolean startAgent(AID agent) {
		AID a = containsAgent(agent);
		if (a != null) {
			System.out.println("Agent " +  agent.getName() +" already exists!");
			return false;
		}

		try {
			System.out.println(agent.getType().toString() + "\n\n\n");
			Object obj = Class.forName(agent.getType().toString()).newInstance();
			if (obj instanceof Agent) {
				((Agent) obj).setId(agent);
				runningAgents.put(agent, (Agent) obj);
				return true;
			} else {
				System.out.println("Type " + agent.getType() + " cannot be added!");
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	private AID containsAgent(AID key) {
		for (AID tmp : runningAgents.keySet()) {
			if (tmp.getHost().getAlias().equals(key.getHost().getAlias())
					&& tmp.getHost().getAddress().equals(key.getHost().getAddress())
					&& tmp.getName().equals(key.getName()) && tmp.getType().getName().equals(key.getType().getName())
					&& tmp.getType().getModule().equals(key.getType().getModule()))
				System.out.println("AGENT VEC POSTOJI");
				return tmp;
		}
		System.out.println("NE POSTOJI TAKAV AGENT, DODAJE SE");
		return null;
	}
	
	public void addAgentType(AgentType at) {
		try {
			AgentCenter center = nm.getThisNode();

			if (agentTypes.get(center) != null) {
				System.out.println("DODAJE TIP AGENTA U CENTAR");
				agentTypes.get(center).add(at);
				
			} else {
				System.out.println("CENTAR JE BIO NULL");
				ArrayList<AgentType> tmp = new ArrayList<AgentType>();
				tmp.add(at);
				agentTypes.put(center, tmp);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public void stopAgent(AID agent) {
		AID a = containsAgent(agent);
		if (a != null) {
			runningAgents.remove(a);
		} else {
			System.out.println("No such agent!");
		}
	}
}
