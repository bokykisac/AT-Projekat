package model;

import java.io.Serializable;

public class AgentCenter implements Serializable{
	
	private String alias;
	
	private String address;
	
	public AgentCenter() {
	}
	
	public AgentCenter(String alias, String address) {
		super();
		this.alias = alias;
		this.address = address;
	}

	public String getAlias() {
		return alias;
	}
	public void setAlias(String alias) {
		this.alias = alias;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}

}
