package beans;

import java.util.Collection;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import model.ACLMessage;
import ws.WSEndPoint;

@Stateless
@Path("/messages")
@LocalBean
public class MessageBean {
	
	@EJB
	WSEndPoint ws;
		
//	@POST
//	public Response sendMessage(ACLMessage message) {
//		return Response.status(200).entity("Message send").build();
//	}
//	
//	@GET
//	public Collection<ACLMessage> getMessages() {
//		return db.getMessages().values();
//	}

}
