package fr.protogen.cch.ws;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import fr.protogen.cch.cache.ApplicationRegistery;
import fr.protogen.cch.cache.SessionCache;
import fr.protogen.masterdata.dal.dao.CCHUserDAO;
import fr.protogen.masterdata.model.CCHUser;
import fr.protogen.security.Md5;

@Path("/authjs")
public class GAuth {
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response authentifier(InputStream is){
		StringBuilder jsBuilder = new StringBuilder();
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(is));
			String line = null;
			while ((line = in.readLine()) != null) {
				jsBuilder.append(line);
			}
		} catch (Exception e) {
			System.out.println("Error Parsing: - ");
		}
		System.out.println("Data Received: " + jsBuilder.toString());
		String jsonText = jsBuilder.toString();
		
		CCHUser u = new JSONDeserializer<CCHUser>().deserialize(jsonText);
		
		//	Authentication
		String hashedPassword = Md5.encode(u.getPassword());
		u.setPassword(hashedPassword);
		CCHUserDAO dao = new CCHUserDAO();
		u = dao.getUser(u);
		GUserSession.getInstance().getOnlineUsers().add(u);
		ApplicationRegistery.getInstance().getInstances().put(u, new SessionCache());
		
		String res = new JSONSerializer().serialize(u);
		return Response.status(200).entity(res).build();
	}
}
