package beans;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.jms.ConnectionFactory;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import model.ACLMessage;
import model.Performative;
import ws.WSEndPoint;

@Stateless
@Path("/messages")
@LocalBean
public class MessageBean {
	
	@EJB
	WSEndPoint ws;
	
	@Resource(mappedName = "java:/ConnectionFactory")
	ConnectionFactory connectionFactory;
	
	@Resource(mappedName = "java:jboss/exported/jms/queue/mojQueue")
	Queue queue;
		
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<String> getMessages() {
		// vrati listu performativa
		List<String> ret = new ArrayList<String>();
		for(Enum<Performative> e : Performative.values()) {
			ret.add(e.toString());
		}
		return ret;
	}
	
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response sendMessage(ACLMessage msg) {
		try {
			QueueConnection connection = (QueueConnection) connectionFactory.createConnection("guest", "guest.guest.1");
			QueueSession session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
			QueueSender sender = session.createSender(queue);
			ObjectMessage omsg = session.createObjectMessage(msg);
			sender.send(omsg);
			return Response.status(200).entity("Poruka poslata").build();
			
		}catch (Exception e) {
			System.out.println("USAO U EXCEPTION");
			e.printStackTrace();
		}
		return Response.status(400).entity("Unable to send message").build();
	}	

}
