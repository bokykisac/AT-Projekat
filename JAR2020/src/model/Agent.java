package model;

import javax.ejb.Singleton;

@Singleton
public abstract class Agent implements AgentInterface{
	
	protected AID Id;

	public AID getId() {
		return Id;
	}

	public void setId(AID id) {
		Id = id;
	}

	

}
