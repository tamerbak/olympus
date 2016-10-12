package fr.protogen.cch.ws;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import fr.protogen.cch.cache.ApplicationRegistery;
import fr.protogen.cch.cache.SessionCache;
import fr.protogen.cch.configuration.StringFormat;
import fr.protogen.cch.genration.GenerationService;
import fr.protogen.masterdata.dal.DAL;
import fr.protogen.masterdata.model.CAttribute;
import fr.protogen.masterdata.model.CAttributetype;
import fr.protogen.masterdata.model.CBusinessClass;
import fr.protogen.masterdata.model.CCHUser;
import fr.protogen.masterdata.model.CWindow;
import fr.protogen.masterdata.model.CoreRole;
import fr.protogen.masterdata.model.SApplication;
import fr.protogen.masterdata.model.SMenuitem;
import fr.protogen.masterdata.model.SRubrique;
import fr.protogen.masterdata.model.SScreensequence;
import fr.protogen.masterdata.model.WorkflowDefinition;

@Path("/generium")
public class GService {
	
	
	/*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$
	 * 	Application 
	 *$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*/
	@Path("/allapps")
	@GET
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response chargerAppplications(){
		
		DAL dal = new DAL();
		List<SApplication> applications = dal.selectAllApplications();
		
		String res = new JSONSerializer().serialize(applications);
		return Response.status(200).entity(res).build();
	}
	
	@Path("/newapp")
	@POST
	@Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response novelleApplication(InputStream is){
		StringBuilder jsBuilder = new StringBuilder();
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			String line = null;
			while ((line = in.readLine()) != null) {
				jsBuilder.append(line);
			}
		} catch (Exception e) {
			System.out.println("Error Parsing: - ");
		}
		System.out.println("Data Received: " + jsBuilder.toString());
		String jsonText = jsBuilder.toString();
		
		SApplication app = new JSONDeserializer<SApplication>().deserialize(jsonText);
		CCHUser user = null;
		String res = "{status:false}";
		for(CCHUser u : GUserSession.getInstance().getOnlineUsers())
			if(u.getUid().equals(app.getAppKey())){
				user = u;
				break;
			}
		
		if(user == null || ApplicationRegistery.getInstance().getSession(user) == null)
			return Response.status(200).entity(res).build();
		
		SessionCache cache = ApplicationRegistery.getInstance().getSession(user);
		cache.setApplication(app);
		
		res = "{\"status\":\"true\"}";
		return Response.status(200).entity(res).build();
	}
	
	
	@Path("/getapp")
	@POST
	@Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response chargerApplication(InputStream is){
		StringBuilder jsBuilder = new StringBuilder();
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			String line = null;
			while ((line = in.readLine()) != null) {
				jsBuilder.append(line);
			}
		} catch (Exception e) {
			System.out.println("Error Parsing: - ");
		}
		System.out.println("Data Received: " + jsBuilder.toString());
		String jsonText = jsBuilder.toString();
		
		SApplication app = new JSONDeserializer<SApplication>().deserialize(jsonText);
		CCHUser user = null;
		String res = "{status:false}";
		for(CCHUser u : GUserSession.getInstance().getOnlineUsers())
			if(u.getUid().equals(app.getAppKey())){
				user = u;
				break;
			}
		
		if(user == null || ApplicationRegistery.getInstance().getSession(user) == null)
			return Response.status(200).entity(res).build();
		
		SessionCache cache = ApplicationRegistery.getInstance().getSession(user);
		app = cache.getApplication();
		
		res = new JSONSerializer().serialize(app);
		return Response.status(200).entity(res).build();
	}
	
	@Path("/savenewapp")
	@POST
	@Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response enregistrerApplication(InputStream is){
		StringBuilder jsBuilder = new StringBuilder();
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			String line = null;
			while ((line = in.readLine()) != null) {
				jsBuilder.append(line);
			}
		} catch (Exception e) {
			System.out.println("Error Parsing: - ");
		}
		System.out.println("Data Received: " + jsBuilder.toString());
		String jsonText = jsBuilder.toString();
		
		SApplication app = new JSONDeserializer<SApplication>().deserialize(jsonText);
		CCHUser user = null;
		String res = "{status:false}";
		for(CCHUser u : GUserSession.getInstance().getOnlineUsers())
			if(u.getUid().equals(app.getAppKey())){
				user = u;
				break;
			}
		
		if(user == null || ApplicationRegistery.getInstance().getSession(user) == null)
			return Response.status(200).entity(res).build();
		
		SessionCache session = ApplicationRegistery.getInstance().getSession(user);
		GenerationService service = new GenerationService();
		service.generateNewApplication(session);
    	service.generateNewSequences(session);
		
		service.generateEntities(session);
		
		service.generateScreens(session);
		service.generateActions(session);
		service.generateDocuments(session);
		service.generateProcesses(session);

		service.generateMenu(session);
		service.generateSecurity(session);
		service.generateWidgets(session);
		
		res = "{\"status\":\"true\"}";
		return Response.status(200).entity(res).build();
	}
	
	
	/*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$
	 * 	Modules 
	 *$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*/
	@Path("/newmodule/add")
	@POST
	@Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response addModule(InputStream is){
		StringBuilder jsBuilder = new StringBuilder();
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			String line = null;
			while ((line = in.readLine()) != null) {
				jsBuilder.append(line);
			}
		} catch (Exception e) {
			System.out.println("Error Parsing: - ");
		}
		System.out.println("Data Received: " + jsBuilder.toString());
		String jsonText = jsBuilder.toString();
		
		SScreensequence seq = new JSONDeserializer<SScreensequence>().deserialize(jsonText);
		CCHUser user = null;
		String res = "{status:false}";
		for(CCHUser u : GUserSession.getInstance().getOnlineUsers())
			if(u.getUid().equals(seq.getAppKey())){
				user = u;
				break;
			}
		
		if(user == null || ApplicationRegistery.getInstance().getSession(user) == null)
			return Response.status(200).entity(res).build();
		
		SessionCache cache = ApplicationRegistery.getInstance().getSession(user);
		if(cache.getSequences() == null)
			cache.setSequences(new ArrayList<SScreensequence>());
		cache.getSequences().add(seq);
		
		res = "{\"status\":\"true\"}";
		return Response.status(200).entity(res).build();
	}
	
	@Path("/newmodule/update")
	@POST
	@Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response updateModule(InputStream is){
		StringBuilder jsBuilder = new StringBuilder();
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			String line = null;
			while ((line = in.readLine()) != null) {
				jsBuilder.append(line);
			}
		} catch (Exception e) {
			System.out.println("Error Parsing: - ");
		}
		System.out.println("Data Received: " + jsBuilder.toString());
		String jsonText = jsBuilder.toString();
		
		SScreensequence seq = new JSONDeserializer<SScreensequence>().deserialize(jsonText);
		CCHUser user = null;
		String res = "{status:false}";
		for(CCHUser u : GUserSession.getInstance().getOnlineUsers())
			if(u.getUid().equals(seq.getAppKey())){
				user = u;
				break;
			}
		
		if(user == null || ApplicationRegistery.getInstance().getSession(user) == null)
			return Response.status(200).entity(res).build();
		
		SessionCache cache = ApplicationRegistery.getInstance().getSession(user);
		if(cache.getSequences() == null)
			cache.setSequences(new ArrayList<SScreensequence>());
		
		for(SScreensequence s : cache.getSequences()){
			if(s.getId() == seq.getId()){
				s.setTitle(seq.getTitle());
				s.setDescription(seq.getDescription());
			}
		}
		
		res = "{\"status\":\"true\"}";
		return Response.status(200).entity(res).build();
	}
	
	@Path("/newmodule/delete")
	@POST
	@Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response deleteModule(InputStream is){
		StringBuilder jsBuilder = new StringBuilder();
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			String line = null;
			while ((line = in.readLine()) != null) {
				jsBuilder.append(line);
			}
		} catch (Exception e) {
			System.out.println("Error Parsing: - ");
		}
		System.out.println("Data Received: " + jsBuilder.toString());
		String jsonText = jsBuilder.toString();
		
		SScreensequence seq = new JSONDeserializer<SScreensequence>().deserialize(jsonText);
		CCHUser user = null;
		String res = "{status:false}";
		for(CCHUser u : GUserSession.getInstance().getOnlineUsers())
			if(u.getUid().equals(seq.getAppKey())){
				user = u;
				break;
			}
		
		if(user == null || ApplicationRegistery.getInstance().getSession(user) == null)
			return Response.status(200).entity(res).build();
		
		SessionCache cache = ApplicationRegistery.getInstance().getSession(user);
		if(cache.getSequences() == null)
			cache.setSequences(new ArrayList<SScreensequence>());
		
		SScreensequence toDelete = null;
		for(SScreensequence s : cache.getSequences()){
			if(s.getId() == seq.getId()){
				toDelete = s;
				break;
			}
		}
		if(toDelete!= null)
			cache.getSequences().remove(toDelete);
		
		res = "{\"status\":\"true\"}";
		return Response.status(200).entity(res).build();
	}
	
	@Path("/modules/liste")
	@POST
	@Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response loadModules(InputStream is){
		StringBuilder jsBuilder = new StringBuilder();
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			String line = null;
			while ((line = in.readLine()) != null) {
				jsBuilder.append(line);
			}
		} catch (Exception e) {
			System.out.println("Error Parsing: - ");
		}
		System.out.println("Data Received: " + jsBuilder.toString());
		String jsonText = jsBuilder.toString();
		
		SApplication a = new JSONDeserializer<SApplication>().deserialize(jsonText);
		CCHUser user = null;
		for(CCHUser u : GUserSession.getInstance().getOnlineUsers())
			if(u.getUid().equals(a.getAppKey())){
				user = u;
				break;
			}
		SessionCache cache = ApplicationRegistery.getInstance().getSession(user);
		String res = new JSONSerializer().serialize(cache.getSequences());
		return Response.status(200).entity(res).build();
	}
	
	
	
	
	
	/*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$
	 * 	Data 
	 *$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*/
	@Path("/newtable/add")
	@POST
	@Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response addTable(InputStream is){
		StringBuilder jsBuilder = new StringBuilder();
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			String line = null;
			while ((line = in.readLine()) != null) {
				jsBuilder.append(line);
			}
		} catch (Exception e) {
			System.out.println("Error Parsing: - ");
		}
		System.out.println("Data Received: " + jsBuilder.toString());
		String jsonText = jsBuilder.toString();
		
		CBusinessClass table = new JSONDeserializer<CBusinessClass>().deserialize(jsonText);
		CCHUser user = null;
		String res = "{status:false}";
		for(CCHUser u : GUserSession.getInstance().getOnlineUsers())
			if(u.getUid().equals(table.getAppKey())){
				user = u;
				break;
			}
		
		if(user == null || ApplicationRegistery.getInstance().getSession(user) == null)
			return Response.status(200).entity(res).build();
		
		SessionCache cache = ApplicationRegistery.getInstance().getSession(user);
		if(cache.getEntities() == null)
			cache.setEntities(new ArrayList<CBusinessClass>());
		
		table.setDataReference(StringFormat.getInstance().tableDataReferenceFormat(table.getName()));
		CAttributetype atype = new CAttributetype(1, table.getName());
		CAttribute attribute = new CAttribute();
		attribute.setAttribute("ID "+table.getName());
		attribute.setCAttributetype(atype);
		attribute.setDataReference("pk_"+table.getDataReference());
		attribute.setEntity(table);
		attribute.setKeyAttribute(false);
		attribute.setMandatory(false);
		attribute.setReference(false);
		attribute.setTypeLabel(atype.getType());
		attribute.setDescription("Clé primaire");
		
		table.setAttributes(new ArrayList<CAttribute>());
		table.getAttributes().add(attribute);
		cache.getEntities().add(table);
		
		
		res = "{\"status\":\"true\"}";
		return Response.status(200).entity(res).build();
	}
	
	@Path("/newtable/rename")
	@POST
	@Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response renameTable(InputStream is){
		StringBuilder jsBuilder = new StringBuilder();
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			String line = null;
			while ((line = in.readLine()) != null) {
				jsBuilder.append(line);
			}
		} catch (Exception e) {
			System.out.println("Error Parsing: - ");
		}
		System.out.println("Data Received: " + jsBuilder.toString());
		String jsonText = jsBuilder.toString();
		
		CBusinessClass table = new JSONDeserializer<CBusinessClass>().deserialize(jsonText);
		CCHUser user = null;
		String res = "{status:false}";
		for(CCHUser u : GUserSession.getInstance().getOnlineUsers())
			if(u.getUid().equals(table.getAppKey())){
				user = u;
				break;
			}
		
		if(user == null || ApplicationRegistery.getInstance().getSession(user) == null)
			return Response.status(200).entity(res).build();
		
		SessionCache cache = ApplicationRegistery.getInstance().getSession(user);
		if(cache.getEntities() == null)
			cache.setEntities(new ArrayList<CBusinessClass>());
		
		for(CBusinessClass e : cache.getEntities())
			if(e.getId() == table.getId()){
				e.setName(table.getName());
				e.setDataReference(StringFormat.getInstance().tableDataReferenceFormat(table.getName()));
				CAttribute attribute = e.getAttributes().get(0);
				attribute.setAttribute("ID "+e.getName());
				attribute.setDataReference("pk_"+e.getDataReference());
			}
		
		res = "{\"status\":\"true\"}";
		return Response.status(200).entity(res).build();
	}
	
	@Path("/newattribute/add")
	@POST
	@Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response addAttribute(InputStream is){
		StringBuilder jsBuilder = new StringBuilder();
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			String line = null;
			while ((line = in.readLine()) != null) {
				jsBuilder.append(line);
			}
		} catch (Exception e) {
			System.out.println("Error Parsing: - ");
		}
		System.out.println("Data Received: " + jsBuilder.toString());
		String jsonText = jsBuilder.toString();
		
		CAttribute attribute = new JSONDeserializer<CAttribute>().deserialize(jsonText);
		CBusinessClass table = attribute.getEntity();
		CCHUser user = null;
		String res = "{status:false}";
		for(CCHUser u : GUserSession.getInstance().getOnlineUsers())
			if(u.getUid().equals(table.getAppKey())){
				user = u;
				break;
			}
		
		if(user == null || ApplicationRegistery.getInstance().getSession(user) == null)
			return Response.status(200).entity(res).build();
		
		SessionCache cache = ApplicationRegistery.getInstance().getSession(user);
		
		for(CBusinessClass e : cache.getEntities())
			if(e.getId() == table.getId()){
				table = e;
				break;
			}
		
		CAttributetype atype = new CAttributetype(2, "Nombre");
		attribute.setCAttributetype(atype);
		attribute.setDataReference(StringFormat.getInstance().attributeDataReferenceFormat(attribute.getAttribute()));
		attribute.setEntity(table);
		attribute.setKeyAttribute(false);
		attribute.setMandatory(false);
		attribute.setReference(false);
		attribute.setTypeLabel(atype.getType());
		attribute.setDescription("");
		
		table.getAttributes().add(attribute);
		
		
		res = "{\"status\":\"true\"}";
		return Response.status(200).entity(res).build();
	}
	
	@Path("/newattribute/rename")
	@POST
	@Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response renameAttribute(InputStream is){
		StringBuilder jsBuilder = new StringBuilder();
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			String line = null;
			while ((line = in.readLine()) != null) {
				jsBuilder.append(line);
			}
		} catch (Exception e) {
			System.out.println("Error Parsing: - ");
		}
		System.out.println("Data Received: " + jsBuilder.toString());
		String jsonText = jsBuilder.toString();
		
		CAttribute attribute = new JSONDeserializer<CAttribute>().deserialize(jsonText);
		CBusinessClass table = attribute.getEntity();
		CCHUser user = null;
		String res = "{status:false}";
		for(CCHUser u : GUserSession.getInstance().getOnlineUsers())
			if(u.getUid().equals(table.getAppKey())){
				user = u;
				break;
			}
		
		if(user == null || ApplicationRegistery.getInstance().getSession(user) == null)
			return Response.status(200).entity(res).build();
		
		SessionCache cache = ApplicationRegistery.getInstance().getSession(user);
		
		for(CBusinessClass e : cache.getEntities())
			if(e.getId() == table.getId()){
				table = e;
				break;
			}
		
		DAL dal = new DAL();
		CAttributetype atype = dal.getType(attribute.getCAttributetype().getType());
		attribute.setCAttributetype(atype);
		attribute.setDataReference(StringFormat.getInstance().attributeDataReferenceFormat(attribute.getAttribute()));
		attribute.setEntity(table);
		attribute.setTypeLabel(atype.getType());
		attribute.setDescription("");
		int index = 0;
		for(CAttribute a : table.getAttributes()){
			if(a.getId()!=attribute.getId())
				continue;
			
			index = table.getAttributes().indexOf(a);
		}
		
		table.getAttributes().set(index, attribute);
		
		res = "{\"status\":\"true\"}";
		return Response.status(200).entity(res).build();
	}
	
	@Path("/newassociation/add")
	@POST
	@Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response addAssociation(InputStream is){
		StringBuilder jsBuilder = new StringBuilder();
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			String line = null;
			while ((line = in.readLine()) != null) {
				jsBuilder.append(line);
			}
		} catch (Exception e) {
			System.out.println("Error Parsing: - ");
		}
		System.out.println("Data Received: " + jsBuilder.toString());
		String jsonText = jsBuilder.toString();
		
		CAttribute attribute = new JSONDeserializer<CAttribute>().deserialize(jsonText);
		CBusinessClass table = attribute.getEntity();
		CCHUser user = null;
		String res = "{status:false}";
		for(CCHUser u : GUserSession.getInstance().getOnlineUsers())
			if(u.getUid().equals(table.getAppKey())){
				user = u;
				break;
			}
		
		if(user == null || ApplicationRegistery.getInstance().getSession(user) == null)
			return Response.status(200).entity(res).build();
		
		SessionCache cache = ApplicationRegistery.getInstance().getSession(user);
		
		
		String attributeName = attribute.getAttribute();
		String t2 = attributeName.split("__")[1];
		CBusinessClass refEntity = null;
		for(CBusinessClass e : cache.getEntities()){
			if(e.getId() == table.getId()){
				table = e;
			}
			if(e.getName().equals(t2)){
				refEntity = e;
			}
		}
		
		
		CAttributetype atype = new CAttributetype(1, t2);
		attribute.setCAttributetype(atype);
		attribute.setDataReference("fk_"+refEntity.getDataReference());
		attribute.setEntity(table);
		attribute.setReference(true);
		attribute.setMultiple(false);
		attribute.setTypeLabel(atype.getType());
		
		table.getAttributes().add(attribute);
		
		
		res = "{\"status\":\"true\"}";
		return Response.status(200).entity(res).build();
	}
	
	@Path("/newassociation/rename")
	@POST
	@Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response renameAssociation(InputStream is){
		StringBuilder jsBuilder = new StringBuilder();
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			String line = null;
			while ((line = in.readLine()) != null) {
				jsBuilder.append(line);
			}
		} catch (Exception e) {
			System.out.println("Error Parsing: - ");
		}
		System.out.println("Data Received: " + jsBuilder.toString());
		String jsonText = jsBuilder.toString();
		
		CAttribute attribute = new JSONDeserializer<CAttribute>().deserialize(jsonText);
		CBusinessClass table = attribute.getEntity();
		CCHUser user = null;
		String res = "{status:false}";
		for(CCHUser u : GUserSession.getInstance().getOnlineUsers())
			if(u.getUid().equals(table.getAppKey())){
				user = u;
				break;
			}
		
		if(user == null || ApplicationRegistery.getInstance().getSession(user) == null)
			return Response.status(200).entity(res).build();
		
		SessionCache cache = ApplicationRegistery.getInstance().getSession(user);
		
		for(CBusinessClass e : cache.getEntities()){
			if(e.getId() == table.getId()){
				table = e;
			}
		}
		
		for(CAttribute a : table.getAttributes()){
			if(a.getId() == attribute.getId()){
				String reftable = a.getDataReference().substring(3);
				a.setAttribute(attribute.getAttribute());
				a.setReference(attribute.isReference());
				a.setMultiple(attribute.isMultiple());
				a.setMandatory(attribute.isMandatory());
				a.setKeyAttribute(attribute.isKeyAttribute());
				
				if(a.isMultiple()){
					a.setDataReference("fk_"+reftable+"__"+table.getDataReference());
				}
			}
		}
		
		res = "{\"status\":\"true\"}";
		return Response.status(200).entity(res).build();
	}
	
	@Path("/data/save")
	@POST
	@Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response saveData(InputStream is){
		StringBuilder jsBuilder = new StringBuilder();
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			String line = null;
			while ((line = in.readLine()) != null) {
				jsBuilder.append(line);
			}
		} catch (Exception e) {
			System.out.println("Error Parsing: - ");
		}
		System.out.println("Data Received: " + jsBuilder.toString());
		String jsonText = jsBuilder.toString();
		
		CBusinessClass table = new JSONDeserializer<CBusinessClass>().deserialize(jsonText);
		CCHUser user = null;
		String res = "{status:false}";
		for(CCHUser u : GUserSession.getInstance().getOnlineUsers())
			if(u.getUid().equals(table.getAppKey())){
				user = u;
				break;
			}
		SessionCache cache = ApplicationRegistery.getInstance().getSession(user);
		GenerationService service = new GenerationService();
		service.createDBSchema(cache.getEntities());
		
		res = "{\"status\":\"true\"}";
		return Response.status(200).entity(res).build();
	}
	
	@Path("/data/liste")
	@POST
	@Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response loadTables(InputStream is){
		StringBuilder jsBuilder = new StringBuilder();
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			String line = null;
			while ((line = in.readLine()) != null) {
				jsBuilder.append(line);
			}
		} catch (Exception e) {
			System.out.println("Error Parsing: - ");
		}
		System.out.println("Data Received: " + jsBuilder.toString());
		String jsonText = jsBuilder.toString();
		
		SApplication a = new JSONDeserializer<SApplication>().deserialize(jsonText);
		CCHUser user = null;
		for(CCHUser u : GUserSession.getInstance().getOnlineUsers())
			if(u.getUid().equals(a.getAppKey())){
				user = u;
				break;
			}
		SessionCache cache = ApplicationRegistery.getInstance().getSession(user);
		if(cache.getEntities() == null)
			cache.setEntities(new ArrayList<CBusinessClass>());
		String res = new JSONSerializer().serialize(cache.getEntities());
		return Response.status(200).entity(res).build();
	}
	
	@Path("/data/attributes")
	@POST
	@Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response loadAttributes(InputStream is){
		StringBuilder jsBuilder = new StringBuilder();
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			String line = null;
			while ((line = in.readLine()) != null) {
				jsBuilder.append(line);
			}
		} catch (Exception e) {
			System.out.println("Error Parsing: - ");
		}
		System.out.println("Data Received: " + jsBuilder.toString());
		String jsonText = jsBuilder.toString();
		
		CBusinessClass table = new JSONDeserializer<CBusinessClass>().deserialize(jsonText);
		CCHUser user = null;
		for(CCHUser u : GUserSession.getInstance().getOnlineUsers())
			if(u.getUid().equals(table.getAppKey())){
				user = u;
				break;
			}
		SessionCache cache = ApplicationRegistery.getInstance().getSession(user);
		List<CAttribute> resultats = new ArrayList<CAttribute>();
		for(CBusinessClass e : cache.getEntities())
			if(e.getDataReference().equals(table.getDataReference())){
				resultats = e.getAttributes();
				break;
			}
		
		String res = new JSONSerializer().serialize(resultats);
		return Response.status(200).entity(res).build();
	}
	
	/*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$
	 * 	Screens 
	 *$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*/
	@Path("/ecrans/liste")
	@POST
	@Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response loadScreens(InputStream is){
		StringBuilder jsBuilder = new StringBuilder();
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			String line = null;
			while ((line = in.readLine()) != null) {
				jsBuilder.append(line);
			}
		} catch (Exception e) {
			System.out.println("Error Parsing: - ");
		}
		System.out.println("Data Received: " + jsBuilder.toString());
		String jsonText = jsBuilder.toString();
		
		SApplication a = new JSONDeserializer<SApplication>().deserialize(jsonText);
		CCHUser user = null;
		for(CCHUser u : GUserSession.getInstance().getOnlineUsers())
			if(u.getUid().equals(a.getAppKey())){
				user = u;
				break;
			}
		SessionCache cache = ApplicationRegistery.getInstance().getSession(user);
		if(cache.getWindows() == null)
			cache.setWindows(new ArrayList<CWindow>());
		String res = new JSONSerializer().serialize(cache.getWindows());
		return Response.status(200).entity(res).build();
	}
	
	@Path("/window/savenew")
	@POST
	@Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response saveNewWindow(InputStream is){
		StringBuilder jsBuilder = new StringBuilder();
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			String line = null;
			while ((line = in.readLine()) != null) {
				jsBuilder.append(line);
			}
		} catch (Exception e) {
			System.out.println("Error Parsing: - ");
		}
		System.out.println("Data Received: " + jsBuilder.toString());
		String jsonText = jsBuilder.toString();
		
		CWindow w = new JSONDeserializer<CWindow>().deserialize(jsonText);
		CCHUser user = null;
		for(CCHUser u : GUserSession.getInstance().getOnlineUsers())
			if(u.getUid().equals(w.getAppKey())){
				user = u;
				break;
			}
		SessionCache cache = ApplicationRegistery.getInstance().getSession(user);
		
		int size = cache.getWindows().size();
		w.setId(size+1);
		
		for(SScreensequence s : cache.getSequences())
			if(s.getId() == w.getFunction().getId()){
				w.setFunction(s);
				break;
			}
		
		cache.getWindows().add(w);
		
		String res = new JSONSerializer().serialize(w);
		return Response.status(200).entity(res).build();
	}
	
	/*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$
	 * 	Roles 
	 *$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*/
	@Path("/roles/liste")
	@POST
	@Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response loadRoles(InputStream is){
		StringBuilder jsBuilder = new StringBuilder();
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			String line = null;
			while ((line = in.readLine()) != null) {
				jsBuilder.append(line);
			}
		} catch (Exception e) {
			System.out.println("Error Parsing: - ");
		}
		System.out.println("Data Received: " + jsBuilder.toString());
		String jsonText = jsBuilder.toString();
		
		SApplication a = new JSONDeserializer<SApplication>().deserialize(jsonText);
		CCHUser user = null;
		for(CCHUser u : GUserSession.getInstance().getOnlineUsers())
			if(u.getUid().equals(a.getAppKey())){
				user = u;
				break;
			}
		SessionCache cache = ApplicationRegistery.getInstance().getSession(user);
		String res = new JSONSerializer().serialize(cache.getRoles());
		return Response.status(200).entity(res).build();
	}
	
	@Path("/roles/add")
	@POST
	@Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response addRole(InputStream is){
		StringBuilder jsBuilder = new StringBuilder();
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			String line = null;
			while ((line = in.readLine()) != null) {
				jsBuilder.append(line);
			}
		} catch (Exception e) {
			System.out.println("Error Parsing: - ");
		}
		System.out.println("Data Received: " + jsBuilder.toString());
		String jsonText = jsBuilder.toString();
		
		CoreRole r = new JSONDeserializer<CoreRole>().deserialize(jsonText);
		CCHUser user = null;
		for(CCHUser u : GUserSession.getInstance().getOnlineUsers())
			if(u.getUid().equals(r.getsWindows())){
				user = u;
				break;
			}
		SessionCache cache = ApplicationRegistery.getInstance().getSession(user);
		int id = cache.getRoles().size()+1;
		r.setId(id);
		cache.getRoles().add(r);
		
		String res = new JSONSerializer().serialize(r);
		return Response.status(200).entity(res).build();
	}
	
	/*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$
	 * 	Workflows 
	 *$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*/
	@Path("/workflow/add")
	@POST
	@Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response addWorkflow(InputStream is){
		StringBuilder jsBuilder = new StringBuilder();
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			String line = null;
			while ((line = in.readLine()) != null) {
				jsBuilder.append(line);
			}
		} catch (Exception e) {
			System.out.println("Error Parsing: - ");
		}
		System.out.println("Data Received: " + jsBuilder.toString());
		String jsonText = jsBuilder.toString();
		
		WorkflowDefinition a = new JSONDeserializer<WorkflowDefinition>().deserialize(jsonText);
		CCHUser user = null;
		for(CCHUser u : GUserSession.getInstance().getOnlineUsers())
			if(u.getUid().equals(a.getAppKey())){
				user = u;
				break;
			}
		SessionCache cache = ApplicationRegistery.getInstance().getSession(user);
		
		int id = cache.getWorkflows().size()+1;
		a.setId(id);
		for(CWindow w : cache.getWindows())
			if(w.getId() == a.getScreen().getId()){
				a.setScreen(w);
			}
		cache.getWorkflows().add(a);
		
		String res = new JSONSerializer().serialize(a);
		return Response.status(200).entity(res).build();
	}
	
	@Path("/workflow/liste")
	@POST
	@Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response loadWorkflows(InputStream is){
		StringBuilder jsBuilder = new StringBuilder();
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			String line = null;
			while ((line = in.readLine()) != null) {
				jsBuilder.append(line);
			}
		} catch (Exception e) {
			System.out.println("Error Parsing: - ");
		}
		System.out.println("Data Received: " + jsBuilder.toString());
		String jsonText = jsBuilder.toString();
		
		SApplication a = new JSONDeserializer<SApplication>().deserialize(jsonText);
		CCHUser user = null;
		for(CCHUser u : GUserSession.getInstance().getOnlineUsers())
			if(u.getUid().equals(a.getAppKey())){
				user = u;
				break;
			}
		SessionCache cache = ApplicationRegistery.getInstance().getSession(user);
		String res = new JSONSerializer().serialize(cache.getWorkflows());
		return Response.status(200).entity(res).build();
	}
	
	/*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$
	 * 	Menus 
	 *$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*$*/
	@Path("/menu/save")
	@POST
	@Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response saveMenu(InputStream is){
		StringBuilder jsBuilder = new StringBuilder();
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			String line = null;
			while ((line = in.readLine()) != null) {
				jsBuilder.append(line);
			}
		} catch (Exception e) {
			System.out.println("Error Parsing: - ");
		}
		System.out.println("Data Received: " + jsBuilder.toString());
		String jsonText = jsBuilder.toString();
		
		List<SRubrique> a = new JSONDeserializer<List<SRubrique>>().deserialize(jsonText);
		String res = "{status : false}";
		if(a.size()>0){
			CCHUser user = null;
			for(CCHUser u : GUserSession.getInstance().getOnlineUsers())
				if(u.getUid().equals(a.get(0).getDescription())){
					user = u;
					break;
				}
			SessionCache cache = ApplicationRegistery.getInstance().getSession(user);
			
			cache.setRubriques(a);
			for(SRubrique r : a)
				cache.getMenu().addAll(r.getItems());
		}
		
		
		res = "{status : true}";
		return Response.status(200).entity(res).build();
	}
}
