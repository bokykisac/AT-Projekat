package dto;

import java.io.Serializable;

import model.AgentCenter;
import model.UpdatePackage;

public class HandshakeDTO implements Serializable {
	
	private AgentCenter sender;
	private UpdatePackage updatePackage;
	private int handshake;
	
	public HandshakeDTO() {
		this.sender = new AgentCenter();
		this.updatePackage = new UpdatePackage();
		this.handshake = 0;
	}

	public AgentCenter getSender() {
		return sender;
	}

	public void setSender(AgentCenter sender) {
		this.sender = sender;
	}

	public UpdatePackage getUpdatePackage() {
		return updatePackage;
	}

	public void setUpdatePackage(UpdatePackage updatePackage) {
		this.updatePackage = updatePackage;
	}

	public int getHandshake() {
		return handshake;
	}

	public void setHandshake(int handshake) {
		this.handshake = handshake;
	}
}
	
