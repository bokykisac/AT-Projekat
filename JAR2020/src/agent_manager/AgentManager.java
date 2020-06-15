package agent_manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Singleton;

import model.ACLMessage;
import model.AID;
import model.Agent;
import model.AgentCenter;
import model.AgentType;
import node.NodeManager;
import node.NodeManagerLocal;
import ws.WSEndPoint;

@Singleton
public class AgentManager {
	
	public static String LOOKUP = "java:app/JAR2020/AgentManager!agent_manager.AgentManager";
	
	private HashMap<AID, Agent> runningAgents;
	private HashMap<AgentCenter, ArrayList<AgentType>> agentTypes;
	
	@EJB
	NodeManagerLocal nm;
	
	@EJB
	WSEndPoint ws;
	
	
	public void startInit(AgentCenter center) {
		runningAgents = new HashMap<AID, Agent>();
		initAgentTypes(center);
		ws.echoTextMessage("Agent center initiated: " + center.getAlias());
	}
	
	private void initAgentTypes(AgentCenter center) {
		agentTypes = new HashMap<>();
		
		AgentType at1 = new AgentType("PingAgent", "agents");
		AgentType at2 = new AgentType("PongAgent", "agents");
		AgentType at3 = new AgentType("CNPContractorAgent", "agents");
		AgentType at4 = new AgentType("CNPManagerAgent", "agents");
		ArrayList<AgentType> tmp = new ArrayList<AgentType>();
		tmp.add(at1);
		tmp.add(at2);
		tmp.add(at3);
		tmp.add(at4);
		agentTypes.put(center, tmp);

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
			Object obj = Class.forName(agent.getType().toString()).newInstance();
			if (obj instanceof Agent) {
				((Agent) obj).setId(agent);
				runningAgents.put(agent, (Agent) obj);
				
				ws.echoTextMessage("New agent started: " + agent.getName());
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
			System.out.println("Agenti trce: ");
			System.out.println(tmp.getName() + " | " + tmp.getHost().getAddress() + " | " + tmp.getHost().getAlias() + " | " + tmp.getType().getName() + " | " +  tmp.getType().getModule());
			System.out.println("Uporedjuje: ");
			System.out.println(key.getName() + " | " + key.getHost().getAddress() + " | " + key.getHost().getAlias() + " | " + key.getType().getName() + " | " +  key.getType().getModule());

			if (tmp.getHost().getAlias().equals(key.getHost().getAlias())
					&& tmp.getHost().getAddress().equals(key.getHost().getAddress())
					&& tmp.getName().equals(key.getName()) && tmp.getType().getName().equals(key.getType().getName())
					&& tmp.getType().getModule().equals(key.getType().getModule())) {
				System.out.println("AGENT VEC POSTOJI");
				return tmp;
			}	
		}
		System.out.println("NE POSTOJI TAKAV AGENT");
		return null;
	}
	
	public void addAgentType(AgentType at) {
		try {
			AgentCenter center = nm.getThisNode();

			if (agentTypes.get(center) != null) {
				System.out.println("DODAJE TIP AGENTA U CENTAR");
				agentTypes.get(center).add(at);
				ws.echoTextMessage("Agent type added: " + at.getName());
				
			} else {
				System.out.println("CENTAR JE BIO NULL");
				ArrayList<AgentType> tmp = new ArrayList<AgentType>();
				tmp.add(at);
				agentTypes.put(center, tmp);
				ws.echoTextMessage("Agent type added: " + at.getName());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public void stopAgent(AID agent) {
		AID a = containsAgent(agent);
		if (a != null) {
			runningAgents.remove(a);
			ws.echoTextMessage("Agent stopped: " + agent.getName());
		} else {
			System.out.println("No such agent!");
		}
	}
	
	public boolean msgToAgent(AID agent, ACLMessage msg) {
		AID temp = containsAgent(agent);
		Agent receiver = runningAgents.get(temp);
		if (receiver != null) {
			receiver.handleMessage(msg);
			return true;
		} else {
			return false;
		}
	}
}
