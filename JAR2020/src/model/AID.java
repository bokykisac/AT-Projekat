package model;

import java.io.Serializable;

public class AID implements Serializable{
	
	private String name;
	private AgentCenter host;
	private AgentType type;
	
	public AID() {
		super();
	}

	public AID(String name, AgentCenter host, AgentType type) {
		super();
		this.name = name;
		this.host = host;
		this.type = type;
	}
	
	public AID(String url) {
		String[] s = url.split("\\$");
		this.name = s[0];
		this.host = new AgentCenter(s[2], s[1]);
		this.type = new AgentType(s[3], s[4]);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public AgentCenter getHost() {
		return host;
	}

	public void setHost(AgentCenter host) {
		this.host = host;
	}

	public AgentType getType() {
		return type;
	}

	public void setType(AgentType type) {
		this.type = type;
	}

}
