package node;

import javax.ejb.Local;

import model.AgentCenter;

@Local
public interface NodeManagerLocal {
	public static String LOOKUP = "java:app/JAR2020/NodeManager!node.NodeManagerLocal";
	
	AgentCenter getThisNode();
}
