package fr.protogen.masterdata.dal;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import flexjson.JSONDeserializer;
import fr.protogen.cch.cache.SessionCache;
import fr.protogen.masterdata.model.CActionbutton;
import fr.protogen.masterdata.model.CAttribute;
import fr.protogen.masterdata.model.CAttributetype;
import fr.protogen.masterdata.model.CBusinessClass;
import fr.protogen.masterdata.model.CCHUser;
import fr.protogen.masterdata.model.CCallout;
import fr.protogen.masterdata.model.CCalloutArguments;
import fr.protogen.masterdata.model.CDocumentbutton;
import fr.protogen.masterdata.model.CGlobalValue;
import fr.protogen.masterdata.model.COrganization;
import fr.protogen.masterdata.model.CParameterMetamodel;
import fr.protogen.masterdata.model.CParametersWindow;
import fr.protogen.masterdata.model.CUIParameter;
import fr.protogen.masterdata.model.CWindow;
import fr.protogen.masterdata.model.CWindowCallout;
import fr.protogen.masterdata.model.CWindowtype;
import fr.protogen.masterdata.model.CoreRole;
import fr.protogen.masterdata.model.GOrganization;
import fr.protogen.masterdata.model.GParametersPackage;
import fr.protogen.masterdata.model.MAction;
import fr.protogen.masterdata.model.MDocument;
import fr.protogen.masterdata.model.MPostAction;
import fr.protogen.masterdata.model.MPostactionType;
import fr.protogen.masterdata.model.SAlert;
import fr.protogen.masterdata.model.SApplication;
import fr.protogen.masterdata.model.SAtom;
import fr.protogen.masterdata.model.SMenuitem;
import fr.protogen.masterdata.model.SProcedure;
import fr.protogen.masterdata.model.SProcess;
import fr.protogen.masterdata.model.SResource;
import fr.protogen.masterdata.model.SRubrique;
import fr.protogen.masterdata.model.SScheduledCom;
import fr.protogen.masterdata.model.SScreensequence;
import fr.protogen.masterdata.model.SStep;
import fr.protogen.masterdata.model.SWidget;
import fr.protogen.masterdata.model.StepType;

public class DAL {
	public CAttributetype getType(String reference){
		
		int index = 0;
		char t = reference.charAt(0);
		switch(t){
		case 'N':index=4;break;
		case 'D':index=3;break;
		case 'T':index=2;break;
		case 'H':index=5;break;
		case 'F':index=6;break;
		case 'U':index=7;break;
		case 'M':index=8;break;
		case 'V':index=9;break;
		case 'E':index=10;break;
		case 'R':index=11;break;
		case 'X':index=12;break;
		}
		
		String query = QueryFactory.ATTRIBUTE_TYPE+index;
		
		try{
			Class.forName("org.postgresql.Driver");

		    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
		    
			Statement st = cnx.createStatement();
			
			ResultSet rs = st.executeQuery(query);
			CAttributetype type = new CAttributetype(); 

			if(rs.next()){
				type.setId(index);
				type.setType(rs.getString("type"));
				
			}
			
			rs.close();
			st.close();
			
			
			return type;
			
		} catch(Exception exc){
			
		}
		
		return null;
	}
	public List<CWindow> populateWindowsIds(List<CWindow> windows){
		
		try{
			Class.forName("org.postgresql.Driver");

		    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
		    
			Statement st = cnx.createStatement();
			
			for(CWindow w : windows){
				String wq = "select id from c_window where title='"+w.getTitle()+"' and appkey='"+w.getAppKey()+"' order by id desc";
				ResultSet rs = st.executeQuery(wq);
				int idWindow=0;
				if(rs.next()){
					idWindow = rs.getInt(1);
				}
				w.setId(idWindow);
				rs.close();
			}
			
		} catch(Exception exc){
			
		}
		
		return windows;
	}

	public List<CActionbutton> populateActionssIds(List<CActionbutton> actions, String appkey){
		
		try{
			Class.forName("org.postgresql.Driver");
	
		    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
		    
			Statement st = cnx.createStatement();
			
			if(actions == null)
				return null;
			
			for(CActionbutton w : actions){
				String wq = "select a.id from c_actionbutton a,c_window w where a.title='"+w.getTitle()+"' and w.appkey='"+appkey+"' and a.id_window=w.id order by a.id desc";
				ResultSet rs = st.executeQuery(wq);
				int id=0;
				if(rs.next()){
					id = rs.getInt(1);
				}
				w.setId(id);
				rs.close();
			}
			
		} catch(Exception exc){
			
		}
		
		return actions;
	}
	
	public List<CDocumentbutton> populateDocumentsIds(List<CDocumentbutton> actions, String appkey){
		
		try{
			Class.forName("org.postgresql.Driver");
	
		    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
		    
			Statement st = cnx.createStatement();
			
			if(actions == null)
				return null;
			
			for(CDocumentbutton w : actions){
				String wq = "select a.id from c_documentbutton a,c_window w where a.title='"+w.getTitle()+"' and w.appkey='"+appkey+"' and a.id_window=w.id order by a.id desc";
				ResultSet rs = st.executeQuery(wq);
				int id=0;
				if(rs.next()){
					id = rs.getInt(1);
				}
				w.setId(id);
				rs.close();
			}
			
		} catch(Exception exc){
			
		}
		
		return actions;
	}
	public List<SProcess> populateProcessIds(List<SProcess> processes, String appkey){
		
		try{
			Class.forName("org.postgresql.Driver");

		    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
		    
			Statement st = cnx.createStatement();
			
			if(processes == null)
				return null;
			
			for(SProcess w : processes){
				String wq = "select id from s_process where title='"+w.getTitle()+"' and appkey='"+appkey+"' order by id desc";
				ResultSet rs = st.executeQuery(wq);
				int id=0;
				if(rs.next()){
					id = rs.getInt(1);
				}
				w.setId(id);
				rs.close();
			}
			
		} catch(Exception exc){
			
		}
		
		return processes;
	}
	public List<SApplication> selectAllApplications() {
		// TODO Auto-generated method stub
		
		String query = "select * from s_application";
		List<SApplication> applications = new ArrayList<SApplication>();
		try{
			Class.forName("org.postgresql.Driver");

		    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
		    
			Statement st = cnx.createStatement();
			
			ResultSet rs = st.executeQuery(query);
			while(rs.next()){
				SApplication a = new SApplication();
				a.setId(rs.getInt("id"));
				a.setProjectName(rs.getString("project_name"));
				a.setAppKey(rs.getString("appkey"));
				a.setAuthor(rs.getString("author"));
				a.setLicence(rs.getString("licence_text"));
				a.setDescription(rs.getString("description"));
				a.setVersion(rs.getString("version"));
				a.setUser(new CCHUser());
				applications.add(a);
			}
		}catch(Exception exc){
			
		}
		return applications;
	}
	public List<CBusinessClass> getEntities(SApplication selectedApp) {
		// TODO Auto-generated method stub
		
		String entQuery = "select * from c_businessclass where appkey='"+selectedApp.getAppKey()+"'";
		List<CBusinessClass> entities = new ArrayList<CBusinessClass>();
		try{
			Class.forName("org.postgresql.Driver");

		    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
		    
			Statement st = cnx.createStatement();
			
			ResultSet rs = st.executeQuery(entQuery);
			while(rs.next()){
				CBusinessClass e = new CBusinessClass();
				e.setId(rs.getInt("id"));
				e.setDataReference(rs.getString("data_reference"));
				e.setName(rs.getString("name"));
				e.setAppKey(selectedApp.getAppKey());
				entities.add(e);
			}
			rs.close();
			
			for(CBusinessClass e : entities){
				String query = "select * from c_attribute where id_class="+e.getId();
				rs = st.executeQuery(query);
				e.setAttributes(new ArrayList<CAttribute>());
				while(rs.next()){
					CAttribute a = new CAttribute();
					a.setId(rs.getInt("id"));
					a.setDataReference(rs.getString("data_reference"));
					a.setAttribute(rs.getString("attribute"));
					a.setCalculated(rs.getString("is_calculated").equals("Y"));
					a.setCAttributetype(new CAttributetype(rs.getInt("id_attributetype"),""));
					a.setEntity(e);
					a.setFormula(rs.getString("formula"));
					a.setKeyAttribute(rs.getInt("key_attribute")==1);
					a.setMandatory(rs.getString("mandatory").equals("Y"));
					a.setMultiple(rs.getString("multiple").equals("Y"));
					a.setReference(rs.getString("reference").equals("Y"));
					a.setRequiresValidation(rs.getString("requires_validation").equals("Y"));
					a.setValidationFormula(rs.getString("validation_formula"));
					
					e.getAttributes().add(a);
				}
			}
			
		}catch(Exception exc){
			
		}
		return entities;
	}
	public List<SScreensequence> getSequences(SApplication selectedApp) {
		// TODO Auto-generated method stub
		String query = "select * from s_screensequence where appkey='"+selectedApp.getAppKey()+"'";
		List<SScreensequence> functions = new ArrayList<SScreensequence>();
		
		try{
			Class.forName("org.postgresql.Driver");

		    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
		    
			Statement st = cnx.createStatement();
			
			ResultSet rs = st.executeQuery(query);
			while(rs.next()){
				SScreensequence f  = new SScreensequence();
				f.setAppKey(selectedApp.getAppKey());
				f.setDescription(rs.getString("description"));
				f.setId(rs.getInt("id"));
				f.setTitle(rs.getString("title"));
				functions.add(f);
			}
		}catch(Exception exc){
			
		}
		return functions;
	}
	public List<CWindow> getScreens(List<SScreensequence> functions,
			List<CBusinessClass> entities) {
		// TODO Auto-generated method stub
		if(functions.size()==0 || entities.size()==0)
			return new ArrayList<CWindow>();
		String query = "select * from c_window where appkey='"+functions.get(0).getAppKey()+"' order by id asc";
		List<CAttribute> attributes = new ArrayList<CAttribute>();
		for(CBusinessClass e : entities){
			attributes.addAll(e.getAttributes());
		}
		List<CWindow> screens = new ArrayList<CWindow>();
		
		try{
			Class.forName("org.postgresql.Driver");

		    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
		    
			Statement st = cnx.createStatement();
			
			ResultSet rs = st.executeQuery(query);
			while(rs.next()){
				CWindow w = new CWindow();
				w.setId(rs.getInt("id"));
				w.setTitle(rs.getString("title"));
				w.setStepDescription(rs.getString("stepDescription"));
				w.setCWindowtype(new CWindowtype(rs.getInt("id_windowtype"), ""));
				w.setFunction(fetchSequence(rs.getInt("id_screen_sequence"),functions));
				w.setMainEntity(fetchEntity(rs.getInt("id_entity"),entities).getDataReference());
				w.setAppKey(functions.get(0).getAppKey());
				screens.add(w);
			}
			rs.close();
			
			//	Get window attributes
			for(CWindow w : screens){
				query = "select id_attribute from c_window_windowattribute where id_window="+w.getId();
				w.setCAttributes(new ArrayList<CAttribute>());
				rs = st.executeQuery(query);
				while(rs.next()){
					int idattribute = rs.getInt(1);
					for(CAttribute a : attributes)
						if(a.getId()==idattribute){
							w.getCAttributes().add(a);
							break;
						}
				}
				rs.close();
			}
			
			//	Get action buttons
			for(CWindow w : screens){
				query="SELECT c_actionbutton.id AS id_button, c_actionbutton.title, c_actionbutton.parameters AS bparameters, c_actionbutton.id_action, " +
						"c_actionbutton.id_window, m_action.code, m_action.id_postaction, m_action.parameters, m_postaction.idtype " +
						"" +
						"FROM public.c_actionbutton, public.m_action, public.m_postaction WHERE c_actionbutton.id_action = m_action.id" +
						" AND m_postaction.id = m_action.id_postaction AND c_actionbutton.id_window="+w.getId();
				rs = st.executeQuery(query);
				w.setCActionbuttons(new ArrayList<CActionbutton>());
				while(rs.next()){
					MPostAction p = new MPostAction();
					MPostactionType pt = new MPostactionType();
					MAction a = new MAction();
					CActionbutton b = new CActionbutton();
					
					b.setId(rs.getInt(1));
					b.setTitle(rs.getString(2));
					a.setTitle(rs.getString(2));
					b.setParameters(rs.getString(3));
					a.setId(rs.getInt(4));
					b.setCWindow(w);
					a.setCode(rs.getString(6));
					p.setId(rs.getInt(7));
					pt.setId(rs.getInt(9));
					
					p.setType(pt);
					a.setPostAction(p);
					b.setMAction(a);
					w.getCActionbuttons().add(b);
				}
				rs.close();
				
				//	load postaction attributes
				for(CActionbutton a : w.getCActionbuttons()){
					MPostAction p = a.getMAction().getPostAction();
					query="select * from m_postaction_attribute where id_postaction="+p.getId();
					p.setAttributes(new ArrayList<CAttribute>());
					p.setDefaultValues(new ArrayList<String>());
					p.setParametersValues(new ArrayList<String>());
					rs = st.executeQuery(query);
					while(rs.next()){
						int idAttribute = rs.getInt(2);
						for(CAttribute aa : attributes)
							if(aa.getId()==idAttribute){
								p.getAttributes().add(aa);
								break;
							}
						p.getDefaultValues().add(rs.getString(3));
						p.getParametersValues().add(rs.getString(4));
					}
					rs.close();
					
				}
			}
			
			//	Get document buttons
			for(CWindow w : screens){
				query = "SELECT c_documentbutton.id, c_documentbutton.title, c_documentbutton.parameters, c_documentbutton.id_document, m_document.id, m_document.title, m_document.stream FROM public.m_document, public.c_documentbutton WHERE c_documentbutton.id_document = m_document.id AND c_documentbutton.id_window="+w.getId();
				rs = st.executeQuery(query);
				w.setCDocumentbuttons(new ArrayList<CDocumentbutton>());
				while(rs.next()){
					CDocumentbutton b = new CDocumentbutton();
					MDocument d = new MDocument();
					b.setCWindow(w);
					b.setId(rs.getInt(1));
					b.setMDocument(d);
					b.setParameters(rs.getString(3));
					b.setTitle(rs.getString(2));
					d.setId(rs.getInt(4));
					d.setTitle(rs.getString(6));
					d.setStream(rs.getString(7));
					w.getCDocumentbuttons().add(b);
				}
				rs.close();
			}
			
			//	Get windows links
			for(CWindow w : screens){
				query = "select destination from c_window_link where source=?";
				PreparedStatement ps= cnx.prepareStatement(query);
				ps.setInt(1, w.getId());
				rs = ps.executeQuery();
				
				w.setLinks(new ArrayList<CWindow>());
				while(rs.next()){
					int id=rs.getInt(1);
					for(CWindow s : screens)
						if(s.getId()==id){
							w.getLinks().add(s);
							break;
						}
				}
				rs.close();
				ps.close();
			}
			
			st.close();
			
		}catch(Exception exc){
			String dummy="";
			
			dummy = dummy+exc.getMessage();
		}
		
		return screens;
	}
	private CBusinessClass fetchEntity(int id, List<CBusinessClass> entities) {
		// TODO Auto-generated method stub
		for(CBusinessClass e : entities)
			if(e.getId()==id)
				return e;
		return null;
	}
	private SScreensequence fetchSequence(int id,
			List<SScreensequence> functions) {
		// TODO Auto-generated method stub
		for(SScreensequence f : functions)
			if(f.getId()==id)
				return f;
		return null;
	}
	public List<SProcess> getProcesses(SessionCache cache) {
		// TODO Auto-generated method stub
		List<SProcess> processes = new ArrayList<SProcess>();
		try{
			Class.forName("org.postgresql.Driver");

		    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
		    
			Statement st = cnx.createStatement();
			
			//	get processes
			String query = "select id, title, description from s_process where appkey='"+cache.getAppKey()+"'";
			ResultSet rs = st.executeQuery(query);
			while(rs.next()){
				SProcess p = new SProcess();
				p.setId(rs.getInt(1));
				p.setTitle(rs.getString(2));
				p.setDescription(rs.getString(3));
				p.setWindows(new ArrayList<CWindow>());
				p.setPwindows(new ArrayList<CParametersWindow>());
				processes.add(p);
			}
			rs.close();
			
			// select all paramters windows to start construction
			query="select id, title, \"stepDescription\" from c_parameters_window where appkey='"+cache.getAppKey()+"'";
			rs = st.executeQuery(query);
			List<CParametersWindow> paramWindows = new ArrayList<CParametersWindow>();
			while(rs.next()){
				CParametersWindow w = new CParametersWindow();
				w.setId(rs.getInt(1));
				w.setTitle(rs.getString(2));
				w.setStepDescription(rs.getString(3));
				paramWindows.add(w);
			}
			rs.close();
			
			//	get all parameters controls
			for(CParametersWindow w : paramWindows){
				query = "select id, parameter_key, parameter_type, parameter_label from c_parameter_ctrl where id_window="+w.getId();
				rs = st.executeQuery(query);
				w.setUiParameters(new ArrayList<CUIParameter>());
				while(rs.next()){
					CUIParameter c = new CUIParameter();
					c.setId(rs.getInt(1));
					c.setParameterKey(rs.getString(2));
					c.setParameterType(rs.getString(3).charAt(0));
					c.setParameterLabel(rs.getString(4));
					c.setCtrlDate(c.getParameterType()=='D');
					w.getUiParameters().add(c);
				}
				rs.close();
			}
			
			//	Associate screens to processes
			for(SProcess p : processes){
				query = "select id_c_window, order, is_parameter from s_process_window where id_s_process="+p.getId()+" order by order asc";
				rs = st.executeQuery(query);
				while(rs.next()){
					int idW = rs.getInt(1);
					int isP = rs.getInt(3);
					
					if(isP==0)
						for(CWindow w : cache.getWindows()){
							if(w.getId()==idW){
								p.getWindows().add(w);
								break;
							}
						}
					else
						for(CParametersWindow w : paramWindows){
							if(w.getId()==idW){
								p.getWindows().add(w);
								p.getPwindows().add(w);
								break;
							}
						}
				}
				rs.close();				
			}
			
			st.close();
			
		}catch(Exception exc){
			
		}
		return processes;
	}
	public List<SMenuitem> getMenu(SApplication selectedApp, List<CWindow> windows, SessionCache cache) {
		// TODO Auto-generated method stub
		List<SRubrique> rubriques = new ArrayList<SRubrique>();
		List<SMenuitem> menu = new ArrayList<SMenuitem>();
		try{
			Class.forName("org.postgresql.Driver");

		    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
		    
			Statement st = cnx.createStatement();
			
			String query = "select id, title, \"isParent\", id_parent, window_id, rubrique_id from s_menuitem where appkey='"+selectedApp.getAppKey()+"' order by id asc";
			ResultSet rs = st.executeQuery(query);
			
			while(rs.next()){
				SMenuitem m = new SMenuitem();
				m.setId(rs.getInt(1));
				m.setTitle(rs.getString(2));
				m.setIsParent(rs.getBoolean(3));
				m.setAppKey(selectedApp.getAppKey());
				m.setIdParent(new Integer(rs.getInt(4)));
				int idw = rs.getInt(5);
				for(CWindow w : windows)
					if(w.getId()==idw){
						m.setWindow(w);
						break;
					}
				m.setRubrique(rs.getInt("rubrique_id"));
				menu.add(m);
				
			}
			
			for(SMenuitem m : menu){
				if(!m.isParent()){
					for(SMenuitem p : menu)
						if(p.isIsParent() && p.getId()==m.getIdParent().intValue()){
							m.setParent(p);
							break;
						}
				}
			}
			
			cache.setRubriques(rubriques);
			
			rs.close();
			
			
			st.close();
			
			
			query="select id, title from s_rubrique where appkey=?";
			PreparedStatement ps = cnx.prepareStatement(query);
			ps.setString(1, selectedApp.getAppKey());
			
			rs = ps.executeQuery();
			while(rs.next()){
				SRubrique r = new SRubrique();
				r.setId(rs.getInt(1));
				r.setTitre(rs.getString(2));
				rubriques.add(r);
			}
			rs.close();
			ps.close();
			for(SRubrique r : rubriques){
				r.setItems(new ArrayList<SMenuitem>());
				for(SMenuitem m : menu){
					if(m.getRubrique() == r.getId())
						r.getItems().add(m);
				}
			}
			
			
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		return menu;
	}
	public List<CoreRole> getRoles(SessionCache cache) {
		// TODO Auto-generated method stub
		List<CoreRole> roles = new ArrayList<CoreRole>();
		
		try{
			Class.forName("org.postgresql.Driver");

		    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
		    
			Statement st = cnx.createStatement();
			
			String query = "select id, role, screens, actions, documents, processes, options, user_bound, superadmin, form_mode, description from core_role where appkey='"+cache.getAppKey()+"'";
			
			ResultSet rs = st.executeQuery(query);
			while (rs.next()){
				CoreRole r = new CoreRole(rs.getInt(1), rs.getString(2));
				r.setsWindows(rs.getString(3));
				r.setsActions(rs.getString(4));
				r.setsDocuments(rs.getString(5));
				r.setsProcesses(rs.getString(6));
				r.setSoptions(rs.getString(7));
				r.setBoundEntity(rs.getInt(8));
				r.setSuperAdmin(rs.getString(9).equals("Y"));
				r.setVision(rs.getString(10));
				r.setDescription(rs.getString(11));
				roles.add(r);
			}
			
			for(CoreRole r : roles){
				String[] wid = r.getsWindows().split(";");
				r.setWindows(new ArrayList<CWindow>());
				for(String id : wid){
					for(CWindow w : cache.getWindows())
						if(w.getId()==Integer.parseInt(id)){
							r.getWindows().add(w);
							break;
						}
				}
				
				String[] aid = r.getsActions().split(";");
				r.setActions(new ArrayList<CActionbutton>());
				for(String id : aid){
					for(CActionbutton w : cache.getActions())
						if(w.getId()==Integer.parseInt(id)){
							r.getActions().add(w);
							break;
						}
				}
				
				String[] did = r.getsDocuments().split(";");
				r.setDocuments(new ArrayList<CDocumentbutton>());
				for(String id : did){
					for(CDocumentbutton w : cache.getDocuments())
						if(w.getId()==Integer.parseInt(id)){
							r.getDocuments().add(w);
							break;
						}
				}
				
				if(r.getsProcesses() == null)
					r.setsProcesses("");
				String[] pid = r.getsProcesses().split(";");
				r.setProcesses(new ArrayList<SProcess>());
				for(String id : pid){
					for(SProcess w : cache.getProcesses())
						if(w.getId()==Integer.parseInt(id)){
							r.getProcesses().add(w);
							break;
						}
				}
				
				if(r.getSoptions()==null)
					r.setSoptions("");
				String[] mid = r.getSoptions().split(";");
				r.setOptions(new ArrayList<SMenuitem>());
				for(String id : mid){
					for(SMenuitem i : cache.getMenu()){
						if(id.equals(""+i.getId())){
							r.getOptions().add(i);
							break;
						}
					}
				}
			}
						
			st.close();
			
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		return roles;
	}
	public List<SProcedure> getProcedures(SessionCache cache) {
		// TODO Auto-generated method stub
		
		List<SProcedure> procedures = new ArrayList<SProcedure>();
		
		try {
		    Class.forName("org.postgresql.Driver");

		    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);//DBUtils.ds.getConnection();
		    Statement st = cnx.createStatement();
		    
			//	Select procedures		    
		    String sql = "select * from s_procedure where app_key='"+cache.getAppKey()+"'"; 
		    ResultSet rs = st.executeQuery(sql);
		    
		    while (rs.next()){
		    	SProcedure p = new SProcedure();
		    	p.setId(rs.getInt("id"));
		    	p.setTitle(rs.getString("title"));
		    	p.setDescription(rs.getString("description"));
		    	p.setAppKey(cache.getAppKey());
		    	String[] ts = rs.getString("key_words").split(",");
		    	p.setKeyWords(new ArrayList<String>());
		    	for(String k : ts)
		    		p.getKeyWords().add(k);
		    	procedures.add(p);
		    }
		    rs.close();
		    
		    for(SProcedure p : procedures){
		    	//	Now get steps
		    	sql = "select id, title, description from s_procedure_step where procedure_id="+p.getId();
		    	rs = st.executeQuery(sql);
		    	p.setEtapes(new ArrayList<SStep>());
		    	while(rs.next()){
		    		SStep s = new SStep();
		    		s.setId(rs.getInt(1));
		    		s.setTitle(rs.getString(2));
		    		s.setDescription(rs.getString(3));
		    		p.getEtapes().add(s);
		    	}
		    	rs.close();
		    	
		    	for(SStep s : p.getEtapes()){
		    		sql = "select id, title, description, id_type, id_window, id_scheduled_com, id_resource  from s_atom where id_step="+s.getId();
		    		rs = st.executeQuery(sql);
		    		s.setActions(new ArrayList<SAtom>());
		    		while (rs.next()){
		    			SAtom a = new SAtom();
		    			a.setId(rs.getInt(1));
		    			a.setTitle(rs.getString(2));
		    			a.setDescription(rs.getString(3));
		    			a.setType(new StepType(rs.getInt(4), ""));
		    			switch(a.getType().getId()){
		    			case 1:
		    				a.setWindow(loadWindowAtom(rs.getInt(5),cnx,cache));
		    				a.getWindow().setAppKey(cache.getAppKey());
		    				break;
		    			case 2:
		    				a.setParameters(loadParametersAtom(a.getId(),cnx));
		    				break;
		    			case 3:case 4:
		    				a.setResource(loadResourceAtom(rs.getInt(7),cnx));
		    				break;
		    			case 5:case 6:case 7:case 8:
		    				a.setCommunication(loadComAtom(rs.getInt(6),cnx));
		    				break;
		    				
		    			}
		    			s.getActions().add(a);
		    		}
		    	}
		    }
		    
		    
		    
		    
		}catch(Exception exc){
			String dummy="";
			dummy=dummy+exc.getMessage();
		}
		
		return procedures;
	}
	private List<CUIParameter> loadParametersAtom(int id, Connection cnx) throws Exception {
		List<CUIParameter> parameters = new ArrayList<CUIParameter>();
		
		String query = "select p.id, p.parameter_key, p.parameter_type, p.parameter_label from c_parameter_ctrl p, s_procedure_s_parameter j where p.id=j.parameter_id and j.procedure_id="+id;
		Statement st = cnx.createStatement();
		ResultSet rs = st.executeQuery(query);
		
		while(rs.next()){
			CUIParameter p = new CUIParameter();
			p.setId(rs.getInt(1));
			p.setParameterKey(rs.getString(2));
			p.setParameterType(rs.getString(3).charAt(0));
			p.setParameterLabel(rs.getString(4));
			parameters.add(p);
		}
		rs.close();
		
		return parameters;
	}

	private SResource loadResourceAtom(int resId, Connection cnx) throws Exception {
		SResource r = new SResource();
		
		String query = "select id, title, description from s_resource where id="+resId;
		Statement st = cnx.createStatement();
		ResultSet rs = st.executeQuery(query);
		if(rs.next()) {
			r.setId(rs.getInt(1));
			r.setTitle(rs.getString(2));
			r.setDescription(rs.getString(3));
		}
		rs.close();
		
		return r;
	}

	private SScheduledCom loadComAtom(int comId, Connection cnx) throws Exception {
		SScheduledCom c = new SScheduledCom();
		
		String query = "select id, title, description from s_scheduled_com where id="+comId;
		
		Statement st = cnx.createStatement();
		ResultSet rs = st.executeQuery(query);
		if(rs.next()) {
			c.setId(rs.getInt(1));
			c.setTitle(rs.getString(2));
			c.setDescription(rs.getString(3));
		}
		
		return c;
	}

	private CWindow loadWindowAtom(int id, Connection cnx, SessionCache cache) throws Exception {
		
		
		for(CWindow w : cache.getWindows())
			if(w.getId() == id)
				return w;
		
		return (new CWindow());
	}
	public List<SResource> getResources(SessionCache cache) {
		// TODO Auto-generated method stub
		
		List<SResource> resources = new ArrayList<SResource>();
		
		try{
			Class.forName("org.postgresql.Driver");

		    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);//DBUtils.ds.getConnection();
		    Statement st = cnx.createStatement();
			String query = "select id, title, description from s_resource where appkey='"+cache.getAppKey()+"'";
			ResultSet rs = st.executeQuery(query);
			while(rs.next()) {
				SResource r = new SResource();
				r.setId(rs.getInt(1));
				r.setTitle(rs.getString(2));
				r.setDescription(rs.getString(3));
				resources.add(r);
			}
			rs.close();
			st.close();
			
		}catch(Exception exc){
			
		}
		
		
		return resources;
	}
	public List<CGlobalValue> getGlobalValues(SessionCache cache) {
		
		List<CGlobalValue> values = new ArrayList<CGlobalValue>();
		try{
			Class.forName("org.postgresql.Driver");

		    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);//DBUtils.ds.getConnection();
		    
		    
		    String query = "select id, \"key\", \"label\", \"value\", appkey from c_globalvalue where appkey=?";
		    PreparedStatement ps = cnx.prepareStatement(query);
		    ps.setString(1, cache.getAppKey());
		    
		    ResultSet rs = ps.executeQuery();
		    while(rs.next()){
		    	CGlobalValue v = new CGlobalValue();
		    	v.setId(rs.getInt(1));
		    	v.setKey(rs.getString(2));
		    	v.setLabel(rs.getString(3));
		    	v.setValue(rs.getString(4));
		    	v.setAppKey(rs.getString(5));
		    	values.add(v);
		    }
		    
		    rs.close();
		    ps.close();
		    
		}catch(Exception e){
			
		}
		
		return values;
	}
	public List<SWidget> getWidgets(SessionCache cache) {
		List<SWidget> widgets = new ArrayList<SWidget>();
		try{
			Class.forName("org.postgresql.Driver");

		    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);//DBUtils.ds.getConnection();
		    
		    String sql = "select " +
		    				" id,title,type " +
		    			 " from " +
		    			 	" s_widget" +
		    			 " where " +
		    			 	" appkey=?";
		    PreparedStatement ps = cnx.prepareStatement(sql);
		    ps.setString(1, cache.getAppKey());
		    
		    ResultSet rs = ps.executeQuery();
		    while(rs.next()){
		    	SWidget w = new SWidget();
		    	w.setId(rs.getInt(1));
		    	w.setTitle(rs.getString(2));
		    	w.setType(rs.getString(3));
		    	
		    	widgets.add(w);
		    }
		    
		    rs.close();
		    ps.close();
		    
		}catch(Exception e){
			e.printStackTrace();
		}
		return widgets;
	}
	public List<SAlert> getAlerts(SessionCache cache) {
		List<SAlert> alerts = new ArrayList<SAlert>(); 
		try{
			Class.forName("org.postgresql.Driver");
		    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
		    String sql = "select s_alert_id, titre, description, type, role, appkey, s_window from s_alert where appkey=?";
		    PreparedStatement ps = cnx.prepareStatement(sql);
		    ps.setString(1, cache.getAppKey());
		    
		    ResultSet rs = ps.executeQuery();
		    
		    while(rs.next()){
		    	SAlert a=new SAlert();
		    	a.setId(rs.getInt(1));
		    	a.setTitre(rs.getString(2));
		    	a.setDescription(rs.getString(3));
		    	a.setInsert(rs.getInt(4)==0);
		    	
		    	for(CoreRole r : cache.getRoles())
		    		if(r.getId()==rs.getInt(5)){
		    			a.setRole(r);
		    			break;
		    		}
		    	
		    	a.setAppKey(rs.getString(6));
		    	
		    	for(CWindow r : cache.getWindows())
		    		if(r.getId()==rs.getInt(7)){
		    			a.setWindow(r);
		    			break;
		    		}
		    	alerts.add(a);
		    	
		    }
		    
		    rs.close();
		    ps.close();
		    
		}catch(Exception e){
			e.printStackTrace();
		}
		return alerts;
	}
	
	/*
	 * 	ORGANISATION ET HABILITATIONS
	 */
	public List<COrganization> getOrgs(SessionCache cache) {
		List<COrganization> results = new ArrayList<COrganization>();
		try{
			Class.forName("org.postgresql.Driver");
		    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
		    
		    String sql = "select id, label, representative from c_organization where appkey=?";
		    PreparedStatement ps = cnx.prepareStatement(sql);
		    ps.setString(1, cache.getAppKey());
		    
		    ResultSet rs = ps.executeQuery();
		    while(rs.next()){
		    	COrganization org = new COrganization();
		    	org.setId(rs.getInt(1));
		    	org.setLabel(rs.getString(2));
		    	int idEnt = rs.getInt(3);
		    	
		    	for(CBusinessClass e : cache.getEntities()){
		    		if(e.getId() == idEnt){
		    			org.setRepresentative(e);
		    			break;
		    		}
		    	}
		    	
		    	results.add(org);
		    }
		    rs.close();
		    ps.close();
		    
		    
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return results;
	}
	public List<CParameterMetamodel> getParameterModels(SessionCache cache) {
		
		List<CParameterMetamodel> metamodels = new ArrayList<CParameterMetamodel>();
		
		try{
			Class.forName("org.postgresql.Driver");
		    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
		    String sql = "select id, label, description, organization from c_metaparameters where appkey=?";
		    PreparedStatement ps = cnx.prepareStatement(sql);
		    ps.setString(1, cache.getAppKey());
		    
		    ResultSet rs = ps.executeQuery();
		    while(rs.next()){
		    	CParameterMetamodel p = new CParameterMetamodel();
		    	p.setId(rs.getInt(1));
		    	p.setLabel(rs.getString(2));
		    	p.setDescription(rs.getString(3));
		    	int orgid = rs.getInt(4);
		    	
		    	for(COrganization o : cache.getOrganizations())
		    		if(orgid == o.getId()){
		    			p.setOrganization(o);
		    			break;
		    		}
		    	metamodels.add(p);
		    }
		    
		    rs.close();
		    ps.close();
		    
		    for(CParameterMetamodel m : metamodels){
		    	m.setMappedEntities(new ArrayList<CBusinessClass>());
		    	sql = "select businessclass from c_meta_entity_mapping where metaparameters=?";
		    	ps = cnx.prepareStatement(sql);
		    	ps.setInt(1, m.getId());
		    	rs = ps.executeQuery();
		    	
		    	while(rs.next()){
		    		int id = rs.getInt(1);
		    		for(CBusinessClass e : cache.getEntities())
		    			if(e.getId() == id){
		    				m.getMappedEntities().add(e);
		    				break;
		    			}
		    	}
		    	
		    	rs.close();
		    	ps.close();
		    }
		    
		}catch(Exception e){
			e.printStackTrace();
		} 
		return metamodels;
	}
	public List<GOrganization> loadGorgs(SessionCache cache) {
		List<GOrganization> orgs = new ArrayList<GOrganization>();
		
		String sql = "select id, nom, id_parent, root_organization, representative  from g_organization where appkey=?";
		
		
		try{
			Class.forName("org.postgresql.Driver");
		    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
		    
		    PreparedStatement ps = cnx.prepareStatement(sql);
		    
		    ps.setString(1, cache.getAppKey());
		    ResultSet rs = ps.executeQuery();
		    
		    while(rs.next()){
		    	GOrganization o = new GOrganization();
		    	o.setId(rs.getInt(1));
		    	o.setName(rs.getString(2));
		    	GOrganization parent = new GOrganization();
		    	parent.setId(rs.getInt(3));
		    	o.setParent(parent);
		    	o.setRoot(rs.getString(4).equals("Y"));
		    	
		    	for(CBusinessClass c : cache.getEntities())
		    		if(c.getId() == rs.getInt(5)){
		    			o.setRepresentativeEntity(c);
		    			break;
		    		}
		    	orgs.add(o);
		    }
		    
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		return orgs;
	}
	public List<GParametersPackage> getPkgs(SessionCache cache) {
		List<GParametersPackage> pkgs = new ArrayList<GParametersPackage>();
		String sql = "select id, nom, representative  from g_parameters_pkg where appkey=?";
		
		
		try{
			Class.forName("org.postgresql.Driver");
		    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
		    
		    PreparedStatement ps = cnx.prepareStatement(sql);
		    ps.setString(1, cache.getAppKey());
		    
		    ResultSet rs = ps.executeQuery();
		    
		    while(rs.next()){
		    	GParametersPackage p = new GParametersPackage();
		    	p.setId(rs.getInt(1));
		    	p.setNom(rs.getString(2));
		    	for(CBusinessClass e : cache.getEntities())
		    		if(e.getId() == rs.getInt(3)){
		    			p.setEntity(e);
		    			break;
		    		}
		    	pkgs.add(p);
		    }
		    rs.close();
		    ps.close();
		    
		    for(GParametersPackage pk :pkgs){
		    	sql = "select business_class from g_parameters_entities where parameters_pkg=?";
		    	ps = cnx.prepareStatement(sql);
		    	ps.setInt(1, pk.getId());
		    	rs = ps.executeQuery();
		    	pk.setImplicatedEntities(new ArrayList<CBusinessClass>());
		    	while(rs.next()){
		    		CBusinessClass e = new CBusinessClass();
		    		e.setId(rs.getInt(1));
		    		pk.getImplicatedEntities().add(e);
		    	}
		    	
		    	rs.close();
		    	ps.close();
		    }
		    
		    
		    cnx.close();
		    
		}catch(Exception exc){
			exc.printStackTrace();
		}
		return pkgs;
	}
	
	public List<CCallout> getCallouts(SessionCache cache){
		List<CCallout> results = new ArrayList<CCallout>();
		String sql = "select id, nom, arguments from c_callout where appkey=?";
		
		try{
			Class.forName("org.postgresql.Driver");
		    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
		    
		    PreparedStatement ps = cnx.prepareStatement(sql);
		    ps.setString(1, cache.getAppKey());
		    
		    ResultSet rs = ps.executeQuery();
		    
		    while(rs.next()){
		    	CCallout c = new CCallout();
		    	c.setId(rs.getInt(1));
		    	c.setNom(rs.getString(2));
		    	c.setJsonArguments(rs.getString(3));
		    	if(c.getJsonArguments() != null && c.getJsonArguments().length()>0){
			    	List<CCalloutArguments> args = new JSONDeserializer<List<CCalloutArguments>>().deserialize(c.getJsonArguments());
			    	c.setArgs(args);
		    	}
		    	results.add(c);
		    }
		    
		    rs.close();
		    ps.close();
		    cnx.close();
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		return results;
	}
	
	public List<CWindowCallout> getWindowCallouts(SessionCache cache){
		List<CWindowCallout> results = new ArrayList<CWindowCallout>();
		String sql = "select id, window_id, callout_id from c_window_callout where "
				+ "callout_id in (select id from c_callout where appkey=?)";
				
		try{
			Class.forName("org.postgresql.Driver");
		    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
		    
		    PreparedStatement ps = cnx.prepareStatement(sql);
		    ps.setString(1, cache.getAppKey());
		    
		    ResultSet rs = ps.executeQuery();
		    
		    while(rs.next()){
		    	CWindowCallout c = new CWindowCallout();
		    	c.setId(rs.getInt(1));
		    	int win = rs.getInt(2);
		    	int call= rs.getInt(3);
		    	
		    	for(CWindow w : cache.getWindows())
		    		if(w.getId() == win){
		    			c.setWindow(w);
		    			break;
		    		}
		    	for(CCallout ca : cache.getCallouts())
		    		if(ca.getId() == call){
		    			c.setCallout(ca);
		    			break;
		    		}
		    	
		    	results.add(c);
		    }
		    
		    rs.close();
		    ps.close();
		    cnx.close();
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		return results;
	}
}
