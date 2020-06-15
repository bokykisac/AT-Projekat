package beans;
	
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import agent_manager.AgentManager;
import model.ACLMessage;
import model.AID;
import ws.WSEndPoint;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;

@MessageDriven(activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
		@ActivationConfigProperty(propertyName = "destination", propertyValue = "jms/queue/mojQueue") })
public class MDBConsumer implements MessageListener{
	
	@EJB
	AgentManager am;
	
	@EJB
	WSEndPoint ws;

	@Override
	public void onMessage(Message message) {
		try {
			ACLMessage msg = (ACLMessage) ((ObjectMessage) message).getObject();
			AID[] receivers = msg.getReceivers();
			ws.echoTextMessage("New message in queue");
			System.out.println("MESSAGE ON QUEUE! - " + message);
			for (AID a : receivers) {
				am.msgToAgent(a, msg);
			}
		} catch (JMSException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
