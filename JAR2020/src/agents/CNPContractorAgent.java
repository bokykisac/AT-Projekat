package agents;

import java.util.ArrayList;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import agent_manager.AgentManager;
import beans.MessageBuilder;
import model.ACLMessage;
import model.AID;
import model.Agent;
import model.Performative;
import ws.WSEndPoint;

public class CNPContractorAgent extends Agent{

	@Override
	public void handleMessage(ACLMessage message) {
		switch (message.getPerformative()) {
		case REQUEST:
			startCNP();
			break;
		case PROPOSE:
			propose(message);
			break;
		case REFUSE:
			refuse(message);
			break;
		case INFORM:
			inform(message);
			break;
		case FAILED:
			fail(message);
			break;
		default:
			System.out.println("Unexpected message.");
		}
	}
	
	private void startCNP() {
		ArrayList<AID> participants = getParticipants();
		if (participants.isEmpty()) {
			System.out.println("Error: No participants. Exiting now.");
			try {
				Context context = new InitialContext();
				WSEndPoint ws = (WSEndPoint) context.lookup(WSEndPoint.LOOKUP);
				ws.echoTextMessage("No participants found for CNP.");
			} catch (NamingException e) {
				e.printStackTrace();
			}
			return;
		}

		ACLMessage cfpMsg = new ACLMessage();
		cfpMsg.setPerformative(Performative.CALL_FOR_PROPOSAL);
		cfpMsg.setReplyBy(System.currentTimeMillis() + 3000);
		AID[] niz = new AID[participants.size()];
		for (int i = 0; i < participants.size(); i++) {
			niz[i] = participants.get(i);
		}
		cfpMsg.setReceivers(niz);
		cfpMsg.setSender(Id);
		MessageBuilder.sendACL(cfpMsg);
	}
	
	private ArrayList<AID> getParticipants() {
		try {
			Context ctx = new InitialContext();
			AgentManager manager = (AgentManager) ctx.lookup(AgentManager.LOOKUP);
			ArrayList<AID> retVal = new ArrayList<>();
			for (AID id : manager.getRunningAgents()) {
				if (id.getType().getName().contains("CNPManagerAgent")) {
					retVal.add(id);
				}
			}
			return retVal;
		} catch (NamingException e) {
			e.printStackTrace();
			return new ArrayList<AID>();
		}
	}
	
	private void propose(ACLMessage msg) {
		System.out.println("Received proposal from agent " + msg.getSender());
		try {
			Context context = new InitialContext();
			WSEndPoint ws = (WSEndPoint) context.lookup(WSEndPoint.LOOKUP);
			ws.echoTextMessage(Id.getName() + " - Received proposal from agent " + msg.getSender().getName());
		} catch (NamingException e) {
			e.printStackTrace();
		}

		ACLMessage reply = new ACLMessage();
		reply.setReceivers(new AID[] { msg.getSender() });
		reply.setSender(Id);
		if (Math.random() < 0.5) {
			System.out.println(Id.getName() + " - Rejecting proposal by agent " + msg.getSender());
			reply.setPerformative(Performative.REJECT);
			try {
				Context context = new InitialContext();
				WSEndPoint ws = (WSEndPoint) context.lookup(WSEndPoint.LOOKUP);
				ws.echoTextMessage(Id.getName() + " - Rejecting proposal by agent " + msg.getSender().getName());
			} catch (NamingException e) {
				e.printStackTrace();
			}

		} else {
			System.out.println("Accepting proposal by agent " + msg.getSender());
			reply.setPerformative(Performative.ACCEPT);
			try {
				Context context = new InitialContext();
				WSEndPoint ws = (WSEndPoint) context.lookup(WSEndPoint.LOOKUP);
				ws.echoTextMessage(Id.getName() + " - Accepting proposal by agent " + msg.getSender().getName());
			} catch (NamingException e) {
				e.printStackTrace();
			}
		}

		MessageBuilder.sendACL(reply);
	}
	
	private void inform(ACLMessage msg) {
		System.out.println("Inform message from " + msg.getSender() + ": " + msg.getContent());
		try {
			Context context = new InitialContext();
			WSEndPoint ws = (WSEndPoint) context.lookup(WSEndPoint.LOOKUP);
			ws.echoTextMessage(Id.getName() +  " - Inform message from " + msg.getSender().getName() + ": " + msg.getContent());
		} catch (NamingException e) {
			e.printStackTrace();
		}
	}

	private void refuse(ACLMessage msg) {
		System.out.println("Agent " + msg.getSender() + " refused.");
		try {
			Context context = new InitialContext();
			WSEndPoint ws = (WSEndPoint) context.lookup(WSEndPoint.LOOKUP);
			ws.echoTextMessage(Id.getName() + " - Agent " + msg.getSender().getName() + " refused.");
		} catch (NamingException e) {
			e.printStackTrace();
		}
	}

	private void fail(ACLMessage msg) {
		System.out.println("Agent -> " + msg.getSender() + " failed.");
		try {
			Context context = new InitialContext();
			WSEndPoint ws = (WSEndPoint) context.lookup(WSEndPoint.LOOKUP);
			ws.echoTextMessage(Id.getName() +  " - Agent " + msg.getSender().getName() + " failed.");
		} catch (NamingException e) {
			e.printStackTrace();
		}
	}

}
