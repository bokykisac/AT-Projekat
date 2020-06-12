package beans;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import model.AID;
import model.Agent;
import model.AgentType;
import ws.WSEndPoint;


@Stateless
@Path("/agents")
@LocalBean
public class AgentBean {
	
	@EJB
	WSEndPoint ws;
	
	@EJB
	AgentCenterManagerBean acmb;
	
	@GET
	@Path("/test")
	@Produces(MediaType.TEXT_PLAIN)
	public String test() {
		return "OK";
	}
	
	@GET
	@Path("/classes")
	public List<AgentType> getAgentsTypes() {
		
		acmb.getAgentsTypes().put("AGENT1", new AgentType("agent-name", "agent-module"));
		
		List<AgentType> agentTypes = new ArrayList<>();
		for(AgentType agent : acmb.getAgentsTypes().values()) {
			System.out.println(agent.getName() + " added.");
			agentTypes.add(agent);
		}
		return agentTypes;
	}
	
	@GET
	@Path("/running")
	public List<Agent> getRunningAgents() {
				
		List<Agent> agents = new ArrayList<>();
		for(Agent agent : acmb.getRunningAgents().values()) {
			agents.add(agent);
		}
		return agents;
	}
	
	@PUT
	@Path("/running/{type}/{name}")
	public Agent startAgent(@PathParam("type") String type, @PathParam("name") String name) {
		for(String key : acmb.getRunningAgents().keySet()) {
			if(name.equals(key)) {
				return null;
			}
		}
		
		//proci kroz hostove i posalji ovog agenta
	
		AgentType agentType = acmb.getAgentsTypes().get(type);
		AID aid = new AID(name, acmb.getslaveAgentCenter(), agentType);
		Agent agent = new Agent(aid);
		return agent;
	}
	
	
	@DELETE
	@Path("/running/{aid}")
	public Response stopAgent(@PathParam("aid") String aid) {
		if(acmb.getRunningAgents().get(aid) == null) {
			return Response.status(400).entity("Not Found").build();
		}
		
		Agent removed =  acmb.getRunningAgents().remove(aid);
		//izbrisati ovog agenta iz ostalih hostova
		
		return Response.status(200).entity(removed).build();
	}
	
	
}
