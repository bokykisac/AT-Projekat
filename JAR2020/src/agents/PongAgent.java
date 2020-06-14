package agents;

import javax.ejb.Stateful;

import beans.MessageBuilder;
import model.ACLMessage;
import model.AID;
import model.Agent;
import model.Performative;

@Stateful
public class PongAgent extends Agent{

	@Override
	public void handleMessage(ACLMessage poruka) {
		System.out.println("Message received from Ping " + poruka.getContent());
		if (poruka.getPerformative() == Performative.REQUEST) {
			ACLMessage response = new ACLMessage();
			response.setReceivers(new AID[] { poruka.getSender() });
			response.setPerformative(Performative.INFORM);
			response.setContent("Reply to message received from Ping ");
			response.setSender(Id);			
			MessageBuilder.sendACL(response);
		}

	}

}
