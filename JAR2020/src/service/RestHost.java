package service;

import java.util.Collection;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import dto.HandshakeDTO;
import model.AgentCenter;
import model.ForeignMessage;
import model.UpdatePackage;

public class RestHost {
	
	public static void registerNodeBuilder(AgentCenter slave, AgentCenter host) {
		ResteasyClient client = new ResteasyClientBuilder().build();
		ResteasyWebTarget target = client.target("http://" + host.getAddress() + "/WAR2020/rest/host");
		Rest rest = target.proxy(Rest.class);
		rest.registerNode(slave);
	}
	
	public static void sendNewHostToHostBuilder(String ip, AgentCenter host) {
		ResteasyClient client = new ResteasyClientBuilder().build();
		ResteasyWebTarget target = client.target("http://" + ip + "/WAR2020/rest/host");
		Rest rest = target.proxy(Rest.class);
		rest.sendNewHostToHost(host);
	}
	
	public static Collection<AgentCenter> sendHostsToNewHostBuilder(AgentCenter slave, AgentCenter master) {
		ResteasyClient client = new ResteasyClientBuilder().build();
		ResteasyWebTarget target = client.target("http://" + master.getAddress() + "/WAR2020/rest/host");
		Rest rest = target.proxy(Rest.class);
		return rest.sendHostsToNewHost(slave);
	}
	
	public static UpdatePackage sendAllLoggedInUsersToNodeBuilder(AgentCenter sender, AgentCenter receiver, UpdatePackage updatePackage, int handshake) {
		ResteasyClient client = new ResteasyClientBuilder().build();
		ResteasyWebTarget target = client.target("http://" + receiver.getAddress() + "/WAR2020/rest/host");
		Rest rest = target.proxy(Rest.class);

		HandshakeDTO handshakeDTO = new HandshakeDTO();
    	handshakeDTO.setSender(sender);
    	handshakeDTO.setUpdatePackage(updatePackage);
    	handshakeDTO.setHandshake(handshake);
    	
		return rest.sendAllLoggedInUsersToNode(handshakeDTO);
	}
	
	public static void deleteHostBuilder(AgentCenter receiver, AgentCenter deletedHost) {
		ResteasyClient client = new ResteasyClientBuilder().build();
		ResteasyWebTarget target = client.target("http://" + receiver.getAddress() + "/WAR2020/rest/host");
		Rest rest = target.proxy(Rest.class);
		rest.deleteHost(deletedHost.getAlias());
	}
	
	public static int sendMessageBuilder(ForeignMessage foreignMessage) {
		ResteasyClient client = new ResteasyClientBuilder().build();
		ResteasyWebTarget target = client.target("http://" + foreignMessage.getIpReceivingHost() + "/WAR2020/rest/host");
		Rest rest = target.proxy(Rest.class);
		int succ = rest.sendMessage(foreignMessage);
		return succ;
	}
	
	public static int checkIfAliveBuilder(AgentCenter host) {
		ResteasyClient client = new ResteasyClientBuilder().build();
		ResteasyWebTarget target = client.target("http://" + host.getAddress() + "/WAR2020/rest/host");
		Rest rest = target.proxy(Rest.class);
		int succ = rest.checkIfAlive();
		return succ;
	}
	
	public static void deleteFromSpecificHostBuilder(AgentCenter receiver, AgentCenter deletedHost) {
		ResteasyClient client = new ResteasyClientBuilder().build();
		ResteasyWebTarget target = client.target("http://" + receiver.getAddress() + "/WAR2020/rest/host");
		Rest rest = target.proxy(Rest.class);
		rest.deleteFromSpecificHost(deletedHost.getAlias());
	}

}
