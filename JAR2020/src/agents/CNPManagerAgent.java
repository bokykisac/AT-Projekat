package agents;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import beans.MessageBuilder;
import model.ACLMessage;
import model.AID;
import model.Agent;
import model.Performative;
import ws.WSEndPoint;

public class CNPManagerAgent extends Agent{

	@Override
	public void handleMessage(ACLMessage message) {
		switch (message.getPerformative()) {
		case CALL_FOR_PROPOSAL:
			callForProposal(message);
			break;
		case REJECT:
			rejectProposal(message);
			break;
		case ACCEPT:
			acceptProposal(message);
			break;
		default:
			System.out.println("Unexpected message!");
			try {
				Context context = new InitialContext();
				WSEndPoint ws = (WSEndPoint) context.lookup(WSEndPoint.LOOKUP);
				ws.echoTextMessage("Unexpected performative for CNP agents.");
			} catch (NamingException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	private void callForProposal(ACLMessage msg) {
		ACLMessage reply = new ACLMessage();
		reply.setSender(Id);
		if (Math.random() < 0.5) {
			reply.setPerformative(Performative.REFUSE);
			reply.setReceivers(new AID[] { msg.getSender() });
			try {
				Context context = new InitialContext();
				WSEndPoint ws = (WSEndPoint) context.lookup(WSEndPoint.LOOKUP);
				ws.echoTextMessage(Id.getName() + " - is refusing to comunicate.");
			} catch (NamingException e) {
				e.printStackTrace();
			}
		} else {
			reply.setPerformative(Performative.PROPOSE);
			reply.setReceivers(new AID[] { msg.getSender() });
			try {
				Context context = new InitialContext();
				WSEndPoint ws = (WSEndPoint) context.lookup(WSEndPoint.LOOKUP);
				ws.echoTextMessage(Id.getName() +  " -  is accepting to comunicate.");
			} catch (NamingException e) {
				e.printStackTrace();
			}
		}

		MessageBuilder.sendACL(reply);

	}
	
	private void rejectProposal(ACLMessage msg) {
		try {
			Context context = new InitialContext();
			WSEndPoint ws = (WSEndPoint) context.lookup(WSEndPoint.LOOKUP);
			ws.echoTextMessage(Id.getName() + " - rejected by contractor.");
		} catch (NamingException e) {
			e.printStackTrace();
		}
	}
	
	private void acceptProposal(ACLMessage msg) {
		ACLMessage reply = new ACLMessage();
		reply.setSender(Id);
		reply.setReceivers(new AID[] { msg.getSender() });

		try {

			if (Math.random() > 0.5) {
				reply.setPerformative(Performative.FAILED);
				reply.setContent("Work failed.");
				try {
					Context context = new InitialContext();
					WSEndPoint ws = (WSEndPoint) context.lookup(WSEndPoint.LOOKUP);
					ws.echoTextMessage(Id.getName() + " - work failed, sending information to contractor.");
				} catch (NamingException e) {
					e.printStackTrace();
				}
			} else {
				reply.setPerformative(Performative.INFORM);
				reply.setContent("Work succeeded.");
				try {
					Context context = new InitialContext();
					WSEndPoint ws = (WSEndPoint) context.lookup(WSEndPoint.LOOKUP);
					ws.echoTextMessage(Id.getName() + " - work succeeded, sending information to contractor.");
				} catch (NamingException e) {
					e.printStackTrace();
				}
			}
			MessageBuilder.sendACL(reply);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
