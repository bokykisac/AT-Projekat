package model;

public class Agent implements AgentInterface{
	
	private AID id;
	
	public Agent() {
		
	}
	
	public Agent(AID id) {
		super();
		this.id = id;
	}

	@Override
	public void init(AID aid) {
		this.id = aid;
	}

	@Override
	public void stop() {
		System.out.println("STOP AGENT");
	}

	@Override
	public void HandleMessage(ACLMessage poruka) {
		System.out.println("AGENT MESSAGE: " + poruka);
	}

	@Override
	public void setAid(AID aid) {
		this.id = aid;
	}

	@Override
	public AID getAid() {
		return this.id;
	}
	
	public AID getId() {
		return id;
	}

	public void setId(AID id) {
		this.id = id;
	}
	
	

}
