package node;

import java.util.List;

import javax.ejb.Local;

import model.AgentCenter;
import model.AgentType;

@Local
public interface NodeManagerLocal {
	public static String LOOKUP = "java:app/JAR2020/NodeManager!node.NodeManagerLocal";
	
//	AgentCenter getMasterNode();
	AgentCenter getThisNode();
	void testiram();
//	List<AgentCenter> getSlaves();
//	void deleteSlave(AgentCenter slave);
//	void deleteSlave(String alias);
//	void addSlave(AgentCenter slave, List<AgentType> slaveAgentTypes);
//	void addSlave(AgentCenter slave);
//	void addSlaveAgentTypes(AgentCenter slave, List<AgentType> slaveAgentTypes);
}
