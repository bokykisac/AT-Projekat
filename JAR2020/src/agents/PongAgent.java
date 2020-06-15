package agents;

import javax.ejb.EJB;
import javax.ejb.Stateful;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import beans.MessageBuilder;
import model.ACLMessage;
import model.AID;
import model.Agent;
import model.Performative;
import ws.WSEndPoint;

@Stateful
public class PongAgent extends Agent{
	
	@Override
	public void handleMessage(ACLMessage poruka) {
		if (poruka.getPerformative() == Performative.REQUEST) {
			try {
				Context context = new InitialContext();
				WSEndPoint ws = (WSEndPoint) context.lookup(WSEndPoint.LOOKUP);
				ws.echoTextMessage(this.Id.getName() + " recieved message from " + poruka.getSender().getName() + ": " + poruka.getContent());
			} catch (NamingException e) {
				e.printStackTrace();
			}
			
			ACLMessage response = new ACLMessage();
			response.setReceivers(new AID[] { poruka.getSender() });
			response.setPerformative(Performative.INFORM);
			response.setContent("Reply to message received from " + poruka.getSender().getName());
			response.setSender(Id);			
			MessageBuilder.sendACL(response);
		}

	}

}
