package fr.protogen.cch.genration;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.apache.commons.io.IOUtils;
import org.primefaces.json.JSONObject;

import flexjson.JSONSerializer;
import fr.protogen.cch.cache.SessionCache;
import fr.protogen.cch.configuration.Parameters;
import fr.protogen.cch.configuration.StringFormat;
import fr.protogen.masterdata.dal.DAL;
import fr.protogen.masterdata.dal.DBUtils;
import fr.protogen.masterdata.model.*;
import fr.protogen.security.Md5;

public class GenerationService {

	public void createDBSchema(List<CBusinessClass> entities) {

		DBSchemaGenerator generator = new DBSchemaGenerator();
		String script = generator.createSchema(entities);
		System.out.println("--------------------------------------------\nCreation script\n--------------------------------------------");
		System.out.println(script);
		System.out.println("--------------------------------------------\nEnd\n--------------------------------------------");
		try {
			Class.forName("org.postgresql.Driver");

			Connection cnx = DriverManager.getConnection(DBUtils.url,
					DBUtils.username, DBUtils.password);
			String[] commands = script.split(";");

			Statement st = cnx.createStatement();
			for (String c : commands)
				st.execute(c);

			st.close();

		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	public void generateNewApplication(SessionCache session) {
		SApplication app = session.getApplication();

		app.setAppKey(UUID.randomUUID().toString());
		session.setAppKey(app.getAppKey());

		MetadataQueryFactory factory = new MetadataQueryFactory();
		String query = factory.InsertNewApplication(app);

		try {
			Class.forName("org.postgresql.Driver");

			Connection cnx = DriverManager.getConnection(DBUtils.url,
					DBUtils.username, DBUtils.password);

			Statement st = cnx.createStatement();

			int index = 0;
			String q = "select nextval('s_application_seq')";
			ResultSet rs = st.executeQuery(q);
			if (rs.next())
				index = rs.getInt(1) + 1;
			rs.close();

			st.execute(query);

			String mwquery = "insert into s_mainwindow (application_title,s_application_id,appkey) values (?,?,?)";
			PreparedStatement ps = cnx.prepareStatement(mwquery);
			ps.setString(1, app.getProjectName());
			ps.setInt(2, index);
			ps.setString(3, app.getAppKey());

			ps.executeUpdate();

			ps.close();
			st.close();

		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	public void generateNewSequences(SessionCache session) {
		List<SScreensequence> functions = session.getSequences();
		for (SScreensequence f : functions)
			f.setAppKey(session.getAppKey());

		MetadataQueryFactory factory = new MetadataQueryFactory();
		List<String> queries = factory.InsertNewFunctions(functions);
		try {
			Class.forName("org.postgresql.Driver");

			Connection cnx = DriverManager.getConnection(DBUtils.url,
					DBUtils.username, DBUtils.password);

			Statement st = cnx.createStatement();
			for (String query : queries)
				st.execute(query);

			st.close();

		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	public void generateEntities(SessionCache session) {
		List<CBusinessClass> entities = session.getEntities();
		for (CBusinessClass e : entities)
			e.setAppKey(session.getAppKey());

		MetadataQueryFactory factory = new MetadataQueryFactory();
		Map<Object, String> queries = factory.InsertNewEntities(entities);
		try {
			Class.forName("org.postgresql.Driver");

			Connection cnx = DriverManager.getConnection(DBUtils.url,
					DBUtils.username, DBUtils.password);

			Statement st = cnx.createStatement();
			for (Object o : queries.keySet()) {
				if (o instanceof CAttribute)
					continue;
				String query = queries.get(o);
				System.out.println("DATA MODEL CREATION \t"+query);
				st.execute(query);
			}

			for (Object o : queries.keySet()) {
				if (o instanceof CBusinessClass)
					continue;
				CAttribute a = (CAttribute) o;
				int id = 0;
				String q = "select id from c_businessclass where data_reference='"
						+ StringFormat.getInstance().formatQuery(
								a.getEntity().getDataReference())
						+ "' order by id desc";
				ResultSet rs = st.executeQuery(q);
				if (rs.next())
					id = rs.getInt(1);
				rs.close();
				String query = queries.get(o);
				query = query.replaceAll("___eid___", id + "");
				st.execute(query);
			}

			st.close();

		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	public void generateScreens(SessionCache session) {
		List<CWindow> windows = session.getWindows();

		try {
			Class.forName("org.postgresql.Driver");

			Connection cnx = DriverManager.getConnection(DBUtils.url,
					DBUtils.username, DBUtils.password);

			Statement st = cnx.createStatement();

			for (CWindow w : windows) {

				int idEntity = 0;
				int idSequence = 0;
				String q = "select id from c_businessclass where data_reference='"
						+ StringFormat.getInstance().formatQuery(
								w.getMainEntity()) + "' order by id desc";
				ResultSet rs = st.executeQuery(q);
				if (rs.next()) {
					idEntity = rs.getInt(1);
				}
				rs.close();

				q = "select id from s_screensequence where title='"
						+ StringFormat.getInstance().formatQuery(
								w.getFunction().getTitle())
						+ "' and appkey='"
						+ StringFormat.getInstance().formatQuery(
								session.getAppKey()) + "'";
				rs = st.executeQuery(q);
				if (rs.next()) {
					idSequence = rs.getInt(1);
				}
				rs.close();

				if (w.getRappelReference() == null) {
					CAttribute a = new CAttribute();
					a.setId(0);
					w.setRappelReference(a);
				} else {
					int idAttribute = 0;
					q = "select c_attribute.id from c_attribute, c_businessclass  where c_attribute.data_reference='"
							+ StringFormat.getInstance().formatQuery(
									w.getRappelReference().getDataReference())
							+ "' and c_attribute.attribute='"
							+ StringFormat.getInstance().formatQuery(
									w.getRappelReference().getAttribute())
							+ "' and c_businessclass.appkey='"
							+ StringFormat.getInstance().formatQuery(
									session.getAppKey())
							+ "' and c_businessclass.data_reference='"
							+ w.getRappelReference().getEntity()
									.getDataReference()
							+ "' and c_businessclass.id=c_attribute.id_class order by c_attribute.id desc";
					rs = st.executeQuery(q);
					if (rs.next())
						idAttribute = rs.getInt(1);
					rs.close();
					w.getRappelReference().setId(idAttribute);
				}

				MetadataQueryFactory factory = new MetadataQueryFactory();
				String query = factory.InsertNewWindow(w, idEntity, idSequence,
						session.getAppKey());
				w.setAppKey(session.getAppKey());
				st.execute(query);

				// Windows and attributes
				q = "select id from c_window where title='"
						+ StringFormat.getInstance().formatQuery(w.getTitle())
						+ "' and appkey='"
						+ StringFormat.getInstance().formatQuery(
								session.getAppKey()) + "' order by id desc";
				rs = st.executeQuery(q);
				int idWindow = 0;
				if (rs.next())
					idWindow = rs.getInt(1);
				rs.close();
				for (CAttribute a : w.getCAttributes()) {
					if (!a.isVisible())
						continue;
					String visible = a.isVisible() ? "TRUE" : "FALSE";
					String reference = a.isReference() ? "1" : "0";
					int idAttribute = 0;
					q = "select c_attribute.id from c_attribute, c_businessclass  where c_attribute.data_reference='"
							+ StringFormat.getInstance().formatQuery(
									a.getDataReference())
							+ "' and c_attribute.attribute='"
							+ StringFormat.getInstance().formatQuery(
									a.getAttribute())
							+ "' and c_businessclass.appkey='"
							+ StringFormat.getInstance().formatQuery(
									session.getAppKey())
							+ "' and c_businessclass.data_reference='"
							+ a.getEntity().getDataReference()
							+ "' and c_businessclass.id=c_attribute.id_class order by c_attribute.id desc";
					rs = st.executeQuery(q);
					if (rs.next())
						idAttribute = rs.getInt(1);
					rs.close();
					query = "insert into c_window_windowattribute (id_window, id_attribute, visible, is_reference,display_order,rappel,indirect_reference,indirect_mtm_key,indirect_mtm_value,indirect_function,filter_enabled) values "
							+ "("
							+ idWindow
							+ ","
							+ idAttribute
							+ ",'"
							+ visible
							+ "',"
							+ reference
							+ ","
							+ a.getDisplayOrder()
							+ ",'"
							+ (a.isRappel() ? 'Y' : 'N')
							+ "'"
							+ ",'"
							+ (a.isIndirectReference() ? 'Y' : 'N')
							+ "','"
							+ (a.isIndirectMtmKey() ? 'Y' : 'N')
							+ "','"
							+ (a.isIndirectMtmValue() ? 'Y' : 'N')
							+ "',"
							+ ""
							+ a.getIndirectFunction()
							+ ",'"
							+ (a.isFilterEnabled() ? 'Y' : 'N') + "')";
					st.execute(query);
				}

				for (CGlobalValue v : w.getGlobalValues()) {
					for (CGlobalValue cv : session.getGlobalValues())
						if (cv.getKey().equals(v.getKey()))
							v.setId(cv.getId());

					query = "insert into c_window_globalvalue (id_window,id_global) values ("
							+ w.getId() + "," + v.getId() + ")";
					st.execute(query);
				}

			}
			st.close();

		} catch (Exception exc) {
			exc.printStackTrace();
		}

	}

	public void generateActions(SessionCache session) {
		List<CActionbutton> buttons = session.getActions();
		try {
			Class.forName("org.postgresql.Driver");

			Connection cnx = DriverManager.getConnection(DBUtils.url,
					DBUtils.username, DBUtils.password);

			Statement st = cnx.createStatement();

			for (CActionbutton b : buttons) {
				int idWindow = 0;
				int idAction = 0;
				int idPostaction = 0;

				// Insert postaction
				MPostAction p = b.getMAction().getPostAction();
				String pquery = "insert into m_postaction (idtype) values ('"
						+ p.getType().getId() + "')";
				st.execute(pquery);
				pquery = "select id from m_postaction order by id desc";
				ResultSet rs = st.executeQuery(pquery);
				if (rs.next())
					idPostaction = rs.getInt(1);
				rs.close();
				for (int i = 0; i < p.getAttributes().size(); i++) {
					CAttribute a = p.getAttributes().get(i);
					int idAttribute = 0;
					if (a.getDtoValue() == null)
						a.setDtoValue("");
					String atq = "select id from c_attribute where data_reference='"
							+ a.getDataReference() + "' order by id desc";
					rs = st.executeQuery(atq);
					if (rs.next())
						idAttribute = rs.getInt(1);
					String aq = "insert into m_postaction_attribute (id_postaction,id_attribute,default_value,parameter) values "
							+ "('"
							+ idPostaction
							+ "','"
							+ idAttribute
							+ "','','"
							+ StringFormat.getInstance().parameterFormat(
									a.getDtoValue()) + "')";
					st.execute(aq);
				}

				// Insert action
				String aquery = "insert into m_action (title, code, id_postaction) values ("
						+ "'"
						+ StringFormat.getInstance().formatQuery(
								b.getMAction().getTitle())
						+ "','"
						+ b.getMAction().getCode()
						+ "','"
						+ idPostaction
						+ "')";
				st.execute(aquery);

				aquery = "select id from m_action where title='"
						+ StringFormat.getInstance().formatQuery(
								b.getMAction().getTitle())
						+ "' and id_postaction='" + idPostaction + "'";
				rs = st.executeQuery(aquery);
				if (rs.next())
					idAction = rs.getInt(1);
				rs.close();

				// insert arguments tags
				List<String> tags = new ArrayList<String>();
				String code = b.getMAction().getCode();
				for (int i = 0; i < code.length(); i++) {
					int index = code.indexOf("<<", i);
					if (index < 0)
						break;
					index = index + 2;
					int endindex = code.indexOf(">>", index);
					String tag = code.substring(index, endindex);
					tags.add(tag);
					i = endindex;
				}

				for (String t : tags) {
					aquery = "insert into m_arguments (tag, id_action) values ('"
							+ t + "'," + idAction + ")";
					st.execute(aquery);
				}

				// Insert action button
				String wq = "select id from c_window where title='"
						+ StringFormat.getInstance().formatQuery(
								b.getCWindow().getTitle()) + "' and appkey='"
						+ session.getAppKey() + "'";
				rs = st.executeQuery(wq);
				if (rs.next())
					idWindow = rs.getInt(1);
				rs.close();
				String bquery = "insert into c_actionbutton (title, description, id_action, id_window,parameters) values ("
						+ "'"
						+ StringFormat.getInstance().formatQuery(b.getTitle())
						+ "', '"
						+ StringFormat.getInstance().formatQuery(
								b.getDescription())
						+ "', '"
						+ idAction
						+ "', '" + idWindow + "','" + b.getParameters() + "')";
				st.execute(bquery);

				st.close();

			}
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	public void generateDocuments(SessionCache session) {
		List<CDocumentbutton> buttons = session.getDocuments();
		try {

			Class.forName("org.postgresql.Driver");

			Connection cnx = DriverManager.getConnection(DBUtils.url,
					DBUtils.username, DBUtils.password);

			Statement st = cnx.createStatement();

			for (CDocumentbutton b : buttons) {
				int idWindow = 0;
				int idDocument = 0;

				// Insert mdocument
				String truefullpath = b.getMDocument().getStream();
				String fullPath = truefullpath.replace('\\', '_');
				fullPath = fullPath.replace('/', '_');
				String[] sp = fullPath.split("_");
				String fileName = "";
				if (sp.length > 0)
					fileName = sp[sp.length - 1];

				String mdq = "insert into m_document (title, stream, parameter_mode) values ('"
						+ StringFormat.getInstance().formatQuery(b.getTitle())
						+ "', '"
						+ fileName
						+ "', '"
						+ fileName
						+ "','"
						+ b.getMDocument().getParameterMode() + "')";
				st.execute(mdq);

				mdq = "select nextval('m_document_seq')";
				ResultSet rs = st.executeQuery(mdq);
				if (rs.next())
					idDocument = rs.getInt(1) - 1;
				rs.close();

				File file = new File(truefullpath);
				FileInputStream fis = new FileInputStream(file);

				String mwquery = "update m_document set zip_content=? where id="
						+ idDocument;
				PreparedStatement ps = cnx.prepareStatement(mwquery);
				ps.setBinaryStream(1, fis, (int) file.length());
				ps.executeUpdate();

				// Insert cdocumentbutton
				String wq = "select id from c_window where title='"
						+ StringFormat.getInstance().formatQuery(
								b.getCWindow().getTitle()) + "' and appkey='"
						+ session.getAppKey() + "' order by id desc";
				rs = st.executeQuery(wq);
				if (rs.next())
					idWindow = rs.getInt(1);
				rs.close();

				String dbq = "insert into c_documentbutton (title, description, parameters, id_document, id_window) values ('"
						+ StringFormat.getInstance().formatQuery(b.getTitle())
						+ "', '"
						+ StringFormat.getInstance().formatQuery(
								b.getDescription())
						+ "', '"
						+ b.getParameters()
						+ "', "
						+ idDocument
						+ ", "
						+ idWindow + ")";
				st.execute(dbq);

				st.close();

			}

		} catch (Exception exc) {
			String dummy = "";
			dummy = dummy + exc.getMessage();
		}
	}

	public void generateProcesses(SessionCache session) {
		List<SProcess> processes = session.getProcesses();

		try {

			Class.forName("org.postgresql.Driver");
			Connection cnx = DriverManager.getConnection(DBUtils.url,
					DBUtils.username, DBUtils.password);
			Statement st = cnx.createStatement();

			for (SProcess p : processes) {

				// Insert parameters windows
				for (CWindow w : p.getWindows()) {
					if (w instanceof CParametersWindow) {
						CParametersWindow pw = (CParametersWindow) w;
						String pwq = "insert into c_parameters_window (title, \"stepDescription\", id_windowtype, appkey) values ( '"
								+ StringFormat.getInstance().formatQuery(
										pw.getTitle())
								+ "','"
								+ StringFormat.getInstance().formatQuery(
										pw.getStepDescription())
								+ "',5,'"
								+ session.getAppKey() + "')";
						st.execute(pwq);

						// insert window ctrls
						int windowID = 0;
						pwq = "select id from c_parameters_window where appkey='"
								+ session.getAppKey()
								+ "' and title='"
								+ StringFormat.getInstance().formatQuery(
										pw.getTitle()) + "' order by id desc";
						ResultSet rs = st.executeQuery(pwq);
						if (rs.next())
							windowID = rs.getInt(1);
						rs.close();

						List<CUIParameter> ctrls = pw.getUiParameters();
						for (CUIParameter c : ctrls) {
							String cq = "insert into c_parameter_ctrl (id_window, parameter_key, parameter_type, parameter_label) values "
									+ "("
									+ windowID
									+ ",'"
									+ c.getParameterKey()
									+ "', '"
									+ c.getParameterType()
									+ "','"
									+ StringFormat.getInstance().formatQuery(
											c.getParameterLabel()) + "')";
							st.execute(cq);
						}
					}
				}

				// Insert process
				String pq = "insert into s_process (title, description, appkey) values"
						+ "('"
						+ StringFormat.getInstance().formatQuery(p.getTitle())
						+ "', '"
						+ StringFormat.getInstance().formatQuery(
								p.getDescription())
						+ "', '"
						+ session.getAppKey() + "')";
				st.execute(pq);
				int processID = 0;
				pq = "select id from s_process where title='"
						+ StringFormat.getInstance().formatQuery(p.getTitle())
						+ "' and appkey='" + session.getAppKey()
						+ "' order by id desc";
				ResultSet rs = st.executeQuery(pq);
				if (rs.next())
					processID = rs.getInt(1);

				// Map windows to process
				for (int i = 0; i < p.getWindows().size(); i++) {
					CWindow w = p.getWindows().get(i);
					String wq = "select id from c_window where title='"
							+ StringFormat.getInstance().formatQuery(
									w.getTitle()) + "' and appkey='"
							+ session.getAppKey() + "' order by id desc";
					rs = st.executeQuery(wq);
					int windowID = 0;
					if (rs.next())
						windowID = rs.getInt(1);
					rs.close();
					int isparameter = (w instanceof CParametersWindow) ? 0 : 1;
					wq = "insert into s_process_window (id_s_process, id_c_window, \"order\", is_parameter) values "
							+ "("
							+ processID
							+ ","
							+ windowID
							+ ","
							+ (i + 1)
							+ "," + isparameter + ")";
					st.execute(wq);
				}
			}

			st.close();

		} catch (Exception exc) {
			String dummy = "";
			dummy = dummy + exc.getMessage();
		}
	}

	public void generateMenu(SessionCache session) {
		// TODO Auto-generated method stub
		List<SMenuitem> menu = session.getMenu();
		try {

			Class.forName("org.postgresql.Driver");
			Connection cnx = DriverManager.getConnection(DBUtils.url,
					DBUtils.username, DBUtils.password);

			List<SRubrique> rubriques = session.getRubriques();
			for(SRubrique r : rubriques){
				String query = "insert into s_rubrique (title, appkey) values (?,?)";
				PreparedStatement ps = cnx.prepareStatement(query);
				ps.setString(1, r.getTitre());
				ps.setString(2, session.getAppKey());
				ps.execute();
				ps.close();
				query = "select nextval('s_rubrique_seq')";
				ps = cnx.prepareStatement(query);
				ResultSet res = ps.executeQuery();
				int id = r.getId();
				if(res.next())
					id=(res.getInt(1)-1);
				res.close();
				ps.close();
				
				for(SMenuitem i : menu)
					if(i.getRubrique() == r.getId()){
						i.setRubrique(id);
					}
			}
			
			//	Menu
			Statement st = cnx.createStatement();
			
			int index = 0;
			String query = "select nextval('s_menuitem_seq')";
			ResultSet rs = st.executeQuery(query);
			if (rs.next())
				index = rs.getInt(1);
			rs.close();

			for (SMenuitem i : menu) {
				if (i.isIsParent()) {
					query = "insert into s_menuitem (id, title, \"isParent\", appkey,rubrique_id) values ("
							+ index
							+ ",'"
							+ StringFormat.getInstance().formatQuery(
									i.getTitle())
							+ "',TRUE,'"
							+ session.getAppKey() + "',"+i.getRubrique()+")";
					i.setId(index);
					st.execute(query);
					index++;
				}
			}

			for (SMenuitem i : menu) {
				if (!i.isIsParent()) {
					String wq = "select id from c_window where title='"
							+ StringFormat.getInstance().formatQuery(
									i.getWindow().getTitle())
							+ "' and appkey='" + session.getAppKey()
							+ "' order by id desc";
					rs = st.executeQuery(wq);
					int idWindow = 0;
					if (rs.next()) {
						idWindow = rs.getInt(1);
					}
					rs.close();

					query = "insert into s_menuitem (id, title, \"isParent\", window_id, id_parent ,appkey) values ("
							+ index
							+ ",'"
							+ StringFormat.getInstance().formatQuery(
									i.getTitle())
							+ "',FALSE,"
							+ idWindow
							+ ","
							+ i.getParent().getId()
							+ ",'"
							+ session.getAppKey() + "')";
					i.setId(index);
					st.execute(query);
					index++;
				}
			}

			query = "select setval('s_menuitem_seq'," + index + ",false)";
			st.executeQuery(query);

			st.close();

		} catch (Exception exc) {
			exc.printStackTrace();
		}

	}

	public void generateSecurity(SessionCache session) {
		List<CoreRole> roles = session.getRoles();
		try {

			Class.forName("org.postgresql.Driver");
			Connection cnx = DriverManager.getConnection(DBUtils.url,
					DBUtils.username, DBUtils.password);
			Statement st = cnx.createStatement();

			int index = 0;
			String query = "select nextval('core_role_seq')";
			ResultSet rs = st.executeQuery(query);
			if (rs.next())
				index = rs.getInt(1);
			rs.close();

			for (CoreRole r : roles) {

				DAL dal = new DAL();
				// Get windows Ids
				List<CWindow> windows = dal.populateWindowsIds(r.getWindows());
				r.setsWindows("");
				for (CWindow w : windows) {
					r.setsWindows(r.getsWindows() + w.getId() + ";");
				}
				if (r.getsWindows().length() > 0)
					r.setsWindows(r.getsWindows().substring(0,
							r.getsWindows().length() - 1));

				// Get actions Ids
				List<CActionbutton> actions = dal.populateActionssIds(
						r.getActions(), session.getAppKey());
				r.setsActions("");
				for (CActionbutton w : actions) {
					r.setsActions(r.getsActions() + w.getId() + ";");
				}
				if (r.getsActions().length() > 0)
					r.setsActions(r.getsActions().substring(0,
							r.getsActions().length() - 1));

				// Get documents Ids
				List<CDocumentbutton> docs = dal.populateDocumentsIds(
						r.getDocuments(), session.getAppKey());
				r.setsDocuments("");
				for (CDocumentbutton w : docs) {
					r.setsDocuments(r.getsDocuments() + w.getId() + ";");
				}
				if (r.getsDocuments().length() > 0)
					r.setsDocuments(r.getsDocuments().substring(0,
							r.getsDocuments().length() - 1));

				// Get processes Ids
				List<SProcess> processes = dal.populateProcessIds(
						r.getProcesses(), session.getAppKey());
				r.setsProcesses("");
				for (SProcess w : processes) {
					r.setsProcesses(r.getsProcesses() + w.getId() + ";");
				}
				if (r.getsProcesses().length() > 0)
					r.setsProcesses(r.getsProcesses().substring(0,
							r.getsProcesses().length() - 1));

				query = "insert into core_role (id, role, screens, actions, documents, processes, appkey, description) values ("
						+ index
						+ ",'"
						+ StringFormat.getInstance().formatQuery(r.getRole())
						+ "','"
						+ r.getsWindows()
						+ "','"
						+ r.getsActions()
						+ "','"
						+ r.getsDocuments()
						+ "','"
						+ r.getsProcesses()
						+ "','"
						+ session.getAppKey()
						+ "','"
						+ r.getDescription() + "')";

				st.execute(query);

				// add default user
				
				query = "insert into core_user (\"firstName\",\"lastName\",login,password,\"idRole\",appkey,user_theme,theme_color,theme_style,lang) "
						+ "values (?,?,?,?,?,?,?,?,?,?)";
				PreparedStatement ps = cnx.prepareStatement(query);
				ps.setString(1, r.getRole());
				ps.setString(2, r.getRole());
				ps.setString(3,
						StringFormat.getInstance().parameterFormat(r.getRole()));
				ps.setString(4, Md5.encode("1234"));
				ps.setInt(5, index);
				ps.setString(6, session.getAppKey());
				ps.setString(7, "THEME:DEVELOPR");
				ps.setString(8, "css/style.css?v=1");
				ps.setString(9, "css/colors.css?v=1");
				ps.setString(10, "fr");
				ps.execute();

				ps.close();
				
				index++;
			}
			query = "select setval('core_role_seq'," + index + ",false)";
			st.executeQuery(query);
			st.close();

		} catch (Exception exc) {
			String dummy = exc.getLocalizedMessage();
			dummy = dummy + "";
		}
	}

	public void generateWidgets(SessionCache session) {
		// TODO Auto-generated method stub
		List<SWidget> widgets = session.getWidgets();
		try {

			Class.forName("org.postgresql.Driver");
			Connection cnx = DriverManager.getConnection(DBUtils.url,
					DBUtils.username, DBUtils.password);
			Statement st = cnx.createStatement();
			for (SWidget w : widgets) {
				String query = "insert into s_widget (title,label,lvalue,type,wquery,appkey) values ('"
						+ w.getTitle()
						+ "','"
						+ w.getLabel()
						+ "','"
						+ w.getLvalues()
						+ "','"
						+ w.getType()
						+ "','"
						+ w.getQuery() + "','" + session.getAppKey() + "')";
				st.execute(query);
			}

			st.close();

		} catch (Exception exc) {

		}
	}

	/************************************************************************************************************************************
	 * * Maintainance * *
	 ************************************************************************************************************************************/

	public void updateApplication(SApplication a) {
		try {

			Class.forName("org.postgresql.Driver");
			Connection cnx = DriverManager.getConnection(DBUtils.url,
					DBUtils.username, DBUtils.password);
			Statement st = cnx.createStatement();

			String query = "update s_application set project_name='"
					+ StringFormat.getInstance()
							.formatQuery(a.getProjectName())
					+ "', version='"
					+ StringFormat.getInstance().formatQuery(a.getVersion())
					+ "',"
					+ " author='"
					+ StringFormat.getInstance().formatQuery(a.getAuthor())
					+ "', description='"
					+ StringFormat.getInstance()
							.formatQuery(a.getDescription()) + "',"
					+ " licence_text='"
					+ StringFormat.getInstance().formatQuery(a.getLicence())
					+ "' where id=" + a.getId();
			st.execute(query);

			FacesContext fc = FacesContext.getCurrentInstance();
			ExternalContext ec = fc.getExternalContext();
			File file = new File(ec.getRealPath(Parameters.LOGO_PATH));
			FileInputStream fis = new FileInputStream(file);

			String mwquery = "update s_mainwindow set application_title=?, logo=? where s_application_id="
					+ a.getId();
			PreparedStatement ps = cnx.prepareStatement(mwquery);
			ps.setString(1, a.getProjectName());
			ps.setBinaryStream(2, fis, (int) file.length());

			ps.executeUpdate();

			st.close();

		} catch (Exception exc) {
			String dummy = "";
			dummy = dummy + exc.getLocalizedMessage();
		}
	}

	public void addNewFunction(SScreensequence s, String appkey) {
		try {

			Class.forName("org.postgresql.Driver");
			Connection cnx = DriverManager.getConnection(DBUtils.url,
					DBUtils.username, DBUtils.password);
			Statement st = cnx.createStatement();

			String query = "select nextval('s_screensequence_seq')";
			ResultSet rs = st.executeQuery(query);
			int index = 0;
			if (rs.next())
				index = rs.getInt(1);
			s.setId(index);

			query = "insert into s_screensequence (title,appkey,description) values ("
					+ "'"
					+ StringFormat.getInstance().formatQuery(s.getTitle())
					+ "','"
					+ appkey
					+ "','"
					+ StringFormat.getInstance()
							.formatQuery(s.getDescription()) + "')";
			st.execute(query);

			st.close();

		} catch (Exception exc) {
			String dummy = "";
			dummy = dummy + exc.getLocalizedMessage();
		}

	}

	public void deleteActions(List<CActionbutton> idActions) {
		// TODO Auto-generated method stub
		try {

			Class.forName("org.postgresql.Driver");
			Connection cnx = DriverManager.getConnection(DBUtils.url,
					DBUtils.username, DBUtils.password);
			Statement st = cnx.createStatement();

			for (CActionbutton i : idActions) {
				String query = "delete from c_actionbutton where id="
						+ i.getId();
				st.execute(query);

				query = "delete from m_arguments where id_action="
						+ i.getMAction().getId();
				st.execute(query);

				query = "delete from m_action where id="
						+ i.getMAction().getId();
				st.execute(query);

				query = "delete from m_postaction_attribute where id_postaction="
						+ i.getMAction().getPostAction().getId();
				st.execute(query);

				query = "delete from m_postaction where id="
						+ i.getMAction().getPostAction().getId();
				st.execute(query);

			}

			st.close();

		} catch (Exception exc) {
			String dummy = "";
			dummy = dummy + exc.getLocalizedMessage();
		}
	}

	public void deleteDocuments(List<CDocumentbutton> idDocuments) {

		try {

			Class.forName("org.postgresql.Driver");
			Connection cnx = DriverManager.getConnection(DBUtils.url,
					DBUtils.username, DBUtils.password);
			Statement st = cnx.createStatement();

			for (CDocumentbutton i : idDocuments) {
				String query = "delete from c_documentbutton where id="
						+ i.getId();
				st.execute(query);

				query = "delete from m_document where id="
						+ i.getMDocument().getId();
				st.execute(query);

			}

			st.close();

		} catch (Exception exc) {
			String dummy = "";
			dummy = dummy + exc.getLocalizedMessage();
		}
	}

	public void deleteProcesses(List<SProcess> idProcesses) {
		// TODO Auto-generated method stub
		try {

			Class.forName("org.postgresql.Driver");
			Connection cnx = DriverManager.getConnection(DBUtils.url,
					DBUtils.username, DBUtils.password);
			Statement st = cnx.createStatement();

			for (SProcess i : idProcesses) {
				String query = "delete from s_process_window where id_s_process="
						+ i.getId();
				for (CParametersWindow w : i.getPwindows()) {
					query = "delete from c_parameter_ctrl where id_window="
							+ w.getId();
					st.execute(query);
					query = "delete from c_parameters_window where id="
							+ w.getId();
					st.execute(query);
				}
				query = "delete from s_process where id=" + i.getId();
				st.execute(query);

			}

			st.close();

		} catch (Exception exc) {
			String dummy = "";
			dummy = dummy + exc.getLocalizedMessage();
		}
	}

	public void deleteWindows(List<CWindow> idWindows) {
		try {

			Class.forName("org.postgresql.Driver");
			Connection cnx = DriverManager.getConnection(DBUtils.url,
					DBUtils.username, DBUtils.password);
			Statement st = cnx.createStatement();

			for (CWindow i : idWindows) {
				String query = "delete from c_window_windowattribute where id_window="
						+ i.getId();
				st.execute(query);

				query = "delete from c_window_globalvalue where id_window="
						+ i.getId();
				st.execute(query);

				query = "delete from s_menuitem where window_id=" + i.getId();
				st.execute(query);

				query = "delete from c_window_link where source=" + i.getId()
						+ " or destination=" + i.getId();
				st.execute(query);

				query = "delete from c_window where id=" + i.getId();
				st.execute(query);
			}

			st.close();

		} catch (Exception exc) {
			exc.printStackTrace();
		}

	}

	public void addNewEntity(CBusinessClass entity, SessionCache cache) {
		// TODO Auto-generated method stub
		List<CBusinessClass> es = new ArrayList<CBusinessClass>();
		es.add(entity);
		createDBSchema(es);

		// Get id
		try {

			Class.forName("org.postgresql.Driver");
			Connection cnx = DriverManager.getConnection(DBUtils.url,
					DBUtils.username, DBUtils.password);
			Statement st = cnx.createStatement();

			String query = "insert into c_businessclass (data_reference, name, description,appkey,user_restrict) values ("
					+ "'"
					+ entity.getDataReference()
					+ "','"
					+ StringFormat.getInstance().formatQuery(entity.getName())
					+ "','"
					+ StringFormat.getInstance().formatQuery(
							entity.getDescription())
					+ "','"
					+ cache.getAppKey()
					+ "','"
					+ entity.getUserRestrict()
					+ "')";
			// System.out.println(query);
			st.execute(query);

			query = "select nextval('c_businessclass_seq')";
			ResultSet rs = st.executeQuery(query);
			int index = 0;
			if (rs.next())
				index = rs.getInt(1) - 1;
			rs.close();

			entity.setId(index);

			// primary key
			query = "insert into c_attribute (data_reference, id_attributetype, attribute, id_class, key_attribute, formula, is_calculated, mandatory, reference, multiple) values ("
					+ "'pk_"
					+ entity.getDataReference()
					+ "',1,'"
					+ StringFormat.getInstance().formatQuery(
							"ID " + entity.getName())
					+ "',"
					+ entity.getId()
					+ ",0,'','N','Y','N','N')";
			st.execute(query);
			query = "select nextval('c_businessclass_seq')";
			rs = st.executeQuery(query);
			index = 0;
			if (rs.next())
				index = rs.getInt(1) - 1;
			rs.close();

			entity.getAttributes().get(0).setId(index);

			st.close();

		} catch (Exception exc) {
			exc.printStackTrace();
		}

	}

	public void deleteEntity(CBusinessClass e) {
		try {

			Class.forName("org.postgresql.Driver");
			Connection cnx = DriverManager.getConnection(DBUtils.url,
					DBUtils.username, DBUtils.password);
			Statement st = cnx.createStatement();

			String query = "delete from c_attribute where id_class="
					+ e.getId();
			st.execute(query);

			query = "delete from c_businessclass where id=" + e.getId();
			st.execute(query);

			query = "drop table if exists " + e.getDataReference();
			st.execute(query);

			query = "drop sequence if exists " + e.getDataReference() + "_seq";
			st.execute(query);

			st.close();

		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	public void deleteAttributes(List<CAttribute> idAttributes) {
		try {

			Class.forName("org.postgresql.Driver");
			Connection cnx = DriverManager.getConnection(DBUtils.url,
					DBUtils.username, DBUtils.password);
			Statement st = cnx.createStatement();
			String query = "";
			for (CAttribute a : idAttributes) {
				if (a.getDataReference().startsWith("fk_") && !a.isMultiple()) {
					try {
						query = "alter table "
								+ a.getEntity().getDataReference()
								+ " drop constraint " + a.getDataReference()
								+ " cascade";
						st.execute(query);
						query = "alter table "
								+ a.getEntity().getDataReference()
								+ " drop column " + a.getDataReference()
								+ " cascade";
						st.execute(query);
					} catch (Exception e1) {
					}
				} else if (a.getDataReference().startsWith("fk_")
						&& a.isMultiple()) {
					String table = a.getDataReference().split("__")[0]
							.substring(3);
					try {
						query = "alter table " + table + " drop constraint "
								+ table + "_fk_" + table + "_fkey cascade";
						st.execute(query);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					query = "alter table " + table + " drop column "
							+ a.getDataReference() + " cascade";
					st.execute(query);
				} else {
					try {
						query = "alter table "
								+ a.getEntity().getDataReference()
								+ " drop column " + a.getDataReference()
								+ " cascade";
					} catch (Exception e1) {
					}
					st.execute(query);
				}

				query = "delete from c_window_windowattribute where id_attribute="
						+ a.getId();
				st.execute(query);

				query = "delete from m_postaction_attribute where id_attribute="
						+ a.getId();
				st.execute(query);

				query = "delete from c_attribute where id=" + a.getId();
				st.execute(query);
			}

			st.close();

		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	public void addNewAttribute(CAttribute attribute, CBusinessClass sentity) {
		try {

			Class.forName("org.postgresql.Driver");
			Connection cnx = DriverManager.getConnection(DBUtils.url,
					DBUtils.username, DBUtils.password);
			Statement st = cnx.createStatement();

			// in case of autovalue create sequence
			if (attribute.isAutoValue()) {

				try {
					String sql = "DROP SEQUENCE autoseq_"
							+ sentity.getDataReference() + "_"
							+ attribute.getDataReference() + "";
					st.execute(sql);
				} catch (Exception exc) {
					exc.printStackTrace();
				}

				try {
					String sequery = "CREATE SEQUENCE autoseq_"
							+ sentity.getDataReference()
							+ "_"
							+ attribute.getDataReference()
							+ "  INCREMENT 1  MINVALUE 10  MAXVALUE 9223372036854775807  START 21  CACHE 1";
					st.execute(sequery);

					sequery = "ALTER TABLE autoseq_"
							+ sentity.getDataReference() + "_"
							+ attribute.getDataReference()
							+ "  OWNER TO jakj";
					st.execute(sequery);
				} catch (Exception exc) {
					exc.printStackTrace();
				}
			}

			// Add column to table
			String defaultvalue = "";
			if (attribute.getDefaultValue() != null
					&& attribute.getDefaultValue().length() > 0) {

				switch (attribute.getCAttributetype().getId()) {
				case 1:
				case 4:
				case 5:
				case 8:
					defaultvalue = "DEFAULT " + attribute.getDefaultValue();
					break;
				case 2:
					defaultvalue = "DEFAULT '" + attribute.getDefaultValue()
							+ "'";
					break;
				case 3:
					defaultvalue = "DEFAULT '" + attribute.getDefaultValue()
							+ " 00:00:00+00'";
					break;
				case 9:
					defaultvalue = "DEFAULT '"
							+ (attribute.getDefaultValue().toLowerCase()
									.equals("oui") ? "Oui" : "Non") + "'";
					break;
				case 12:
					defaultvalue = "DEFAULT 'Non'";
					break;
				}
			}
			String query = "alter table " + sentity.getDataReference()
					+ " add column " + attribute.getDataReference();
			String type = "";

			if (attribute.isAutoValue()) {
				type = " integer ";
			} else {
				switch (attribute.getCAttributetype().getId()) {
				case 1:
				case 5:
				case 7:
				case 10:
				case 11:
					type = " integer ";
					break;
				case 2:
					type = " text ";
					break;
				case 3:
					type = " timestamp with time zone ";
					break;
				case 4:
				case 8:
					type = " double precision ";
					break;
				case 6:
					type = " bytea ";
					break;
				case 9:
					type = " character(3) ";
					break;
				case 12:
					type = " character(3) ";
					break;

				}
			}
			query = query + type + defaultvalue;

			if (attribute.isAutoValue()) {
				query = query + " DEFAULT nextval('autoseq_"
						+ sentity.getDataReference() + "_"
						+ attribute.getDataReference() + "'::regclass)";
			}

			System.out.println("NEW ATTRIBUTE QUERY\n\t" + query);
			try{
				st.execute(query);
			}catch(Exception exc ){
				System.out.println("Most likely the attribute is already in the table");
			}
			// Add c_attribute row
			query = "insert into c_attribute (data_reference, id_attributetype, attribute, id_class, key_attribute, formula, is_calculated, mandatory, reference, multiple,autovalue,requires_validation,validation_formula,file_title,file_extension, conditional_layout, default_value, lock_attribute, lock_label, unlock_label, meta_table_ref, textarea, field_width) values ("
					+ "'"
					+ attribute.getDataReference()
					+ "',"
					+ attribute.getCAttributetype().getId()
					+ ",'"
					+ StringFormat.getInstance().formatQuery(
							attribute.getAttribute())
					+ "',"
					+ sentity.getId()
					+ ","
					+ (attribute.isKeyAttribute() ? 1 : 0)
					+ ",'"
					+ StringFormat.getInstance().formatQuery(
							attribute.getFormula())
					+ "','"
					+ (attribute.isCalculated() ? 'Y' : 'N')
					+ "','"
					+ (attribute.isMandatory() ? 'Y' : 'N')
					+ "','"
					+ (attribute.isReference() ? 'Y' : 'N')
					+ "','"
					+ (attribute.isMultiple() ? 'Y' : 'N')
					+ "', '"
					+ (attribute.isAutoValue() ? 'Y' : 'N')
					+ "',"
					+ "'"
					+ (attribute.isRequiresValidation() ? 'Y' : 'N')
					+ "','"
					+ StringFormat.getInstance().formatQuery(
							attribute.getValidationFormula())
					+ "','"
					+ attribute.getFileName()
					+ "','"
					+ attribute.getFileExtension()
					+ "','"
					+ (attribute.isConditionalLayout() ? 'Y' : 'N')
					+ "','"
					+ attribute.getDefaultValue()
					+ "',"
					+ "'"
					+ ((attribute.getCAttributetype().getId() == 9) ? 'Y' : 'N')
					+ "', '"
					+ attribute.getLockLabel()
					+ "','"
					+ attribute.getUnlockLabel()
					+ "'"
					+ ",'"
					+ attribute.getMetatableReference()
					+ "','"
					+ (attribute.isTextarea() ? 'Y' : 'N') + "'"
					+ ","+attribute.getFieldWidth()+")";

			st.execute(query);

			// Get ID
			query = "select nextval('c_businessclass_seq')";
			ResultSet rs = st.executeQuery(query);
			int index = 0;
			if (rs.next())
				index = rs.getInt(1) - 1;
			attribute.setId(index);

			st.close();

		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	public void addNewReference(CAttribute attribute, CBusinessClass sentity,
			CBusinessClass rentity) {

		try {

			Class.forName("org.postgresql.Driver");
			Connection cnx = DriverManager.getConnection(DBUtils.url,
					DBUtils.username, DBUtils.password);
			Statement st = cnx.createStatement();

			// Add column to table
			String query = "alter table " + sentity.getDataReference()
					+ " add column " + attribute.getDataReference() + " ";
			String type = "";
			switch (attribute.getCAttributetype().getId()) {
			case 1:
				type = "integer ";
				break;
			case 2:
				type = "text ";
				break;
			case 3:
				type = "timestamp with time zone ";
				break;
			case 4:
				type = "double precision ";
				break;
			case 5:
				type = "integer ";
				break;
			}
			query = query + type;

			String defaultValue = "";
			if (attribute.getDefaultValue() != null
					&& attribute.getDefaultValue().length() > 0)
				defaultValue = " default " + attribute.getDefaultValue() + " ";

			query = query + defaultValue;

			st.execute(query);

			// Add foreign key constraint

			query = "alter table " + sentity.getDataReference()
					+ " add foreign key (" + attribute.getDataReference()
					+ ") references " + rentity.getDataReference() + " (pk_"
					+ rentity.getDataReference() + ")";
			st.execute(query);

			// Add c_attribute row
			query = "insert into c_attribute (data_reference, id_attributetype, attribute, id_class, key_attribute, formula, is_calculated, mandatory, reference, multiple, conditional_layout,default_value) values ("
					+ "'"
					+ attribute.getDataReference()
					+ "',"
					+ attribute.getCAttributetype().getId()
					+ ",'"
					+ StringFormat.getInstance().formatQuery(
							attribute.getAttribute())
					+ "',"
					+ sentity.getId()
					+ ","
					+ (attribute.isKeyAttribute() ? 1 : 0)
					+ ",'"
					+ StringFormat.getInstance().formatQuery(
							attribute.getFormula())
					+ "','"
					+ (attribute.isCalculated() ? 'Y' : 'N')
					+ "','"
					+ (attribute.isMandatory() ? 'Y' : 'N')
					+ "','"
					+ (attribute.isReference() ? 'Y' : 'N')
					+ "','"
					+ (attribute.isMultiple() ? 'Y' : 'N')
					+ "'"
					+ ",'"
					+ (attribute.isConditionalLayout() ? 'Y' : 'N')
					+ "','"
					+ attribute.getDefaultValue() + "')";

			st.execute(query);

			// Get ID
			query = "select nextval('c_businessclass_seq')";
			ResultSet rs = st.executeQuery(query);
			int index = 0;
			if (rs.next())
				index = rs.getInt(1) - 1;
			attribute.setId(index);

			rs.close();
			st.close();

			// History
			if (!sentity.isHistory())
				return;

			String sql = "insert into c_data_history (entity, attribute) "
					+ " values (?,?)";
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, sentity.getId());
			ps.setInt(2, index);

			ps.execute();
			ps.close();
			cnx.close();
		} catch (Exception exc) {
			exc.printStackTrace();
		}

	}

	public void addNewMultipleReference(CAttribute attribute,
			CBusinessClass sentity, CBusinessClass rentity) {
		try {

			Class.forName("org.postgresql.Driver");
			Connection cnx = DriverManager.getConnection(DBUtils.url,
					DBUtils.username, DBUtils.password);
			Statement st = cnx.createStatement();

			// Add column to table
			String query = "alter table " + rentity.getDataReference()
					+ " add column " + attribute.getDataReference() + " ";
			String type = "";
			switch (attribute.getCAttributetype().getId()) {
			case 1:
				type = "integer ";
				break;
			case 2:
				type = "text ";
				break;
			case 3:
				type = "timestamp with time zone ";
				break;
			case 4:
				type = "double precision ";
				break;
			case 5:
				type = "integer ";
				break;
			}
			query = query + type;

			st.execute(query);

			// Add foreign key constraint
			query = "alter table " + rentity.getDataReference()
					+ " add foreign key (" + attribute.getDataReference()
					+ ") references " + sentity.getDataReference() + " (pk_"
					+ sentity.getDataReference() + ")";
			st.execute(query);

			// Add c_attribute row
			query = "insert into c_attribute (data_reference, id_attributetype, attribute, id_class, key_attribute, formula, is_calculated, mandatory, reference, multiple) values ("
					+ "'"
					+ attribute.getDataReference()
					+ "',"
					+ attribute.getCAttributetype().getId()
					+ ",'"
					+ StringFormat.getInstance().formatQuery(
							attribute.getAttribute())
					+ "',"
					+ sentity.getId()
					+ ","
					+ (attribute.isKeyAttribute() ? 1 : 0)
					+ ",'"
					+ attribute.getFormula()
					+ "','"
					+ (attribute.isCalculated() ? 'Y' : 'N')
					+ "','"
					+ (attribute.isMandatory() ? 'Y' : 'N')
					+ "','"
					+ (attribute.isReference() ? 'Y' : 'N')
					+ "','"
					+ (attribute.isMultiple() ? 'Y' : 'N') + "')";

			st.execute(query);

			// Get ID
			query = "select nextval('c_businessclass_seq')";
			ResultSet rs = st.executeQuery(query);
			int index = 0;
			if (rs.next())
				index = rs.getInt(1) - 1;
			attribute.setId(index);

			st.close();

		} catch (Exception exc) {
			exc.printStackTrace();
			
		}

	}

	public void addNewScreen(CWindow screen, SessionCache cache,
			CBusinessClass e) {

		try {

			Class.forName("org.postgresql.Driver");
			Connection cnx = DriverManager.getConnection(DBUtils.url,
					DBUtils.username, DBUtils.password);
			Statement st = cnx.createStatement();

			String query = "insert into c_window (title,\"stepDescription\",id_windowtype,id_screen_sequence,id_entity,appkey,rappel_reference, drop_option, update_option,mandatory_filter,selection_constraint) values ("
					+ "'"
					+ StringFormat.getInstance().formatQuery(screen.getTitle())
					+ "','"
					+ StringFormat.getInstance().formatQuery(
							screen.getStepDescription())
					+ "',"
					+ screen.getCWindowtype().getId()
					+ ","
					+ screen.getFunction().getId()
					+ ","
					+ e.getId()
					+ ",'"
					+ cache.getAppKey()
					+ "',"
					+ (screen.getRappelReference() == null ? 0 : screen
							.getRappelReference().getId())
					+ ",'"
					+ (screen.isDeleteBtn() ? "Y" : "N")
					+ "','"
					+ (screen.isUpdateBtn() ? "Y" : "N")
					+ "','"
					+ (screen.isForceFilter() ? "Y" : "N")
					+ "','"
					+ screen.getSelectConstraints()
					+ "')";
			System.out.println("SCREEN INSERT QUERY\n\t"+query);
			st.execute(query);

			query = "select nextval('c_window_seq')";
			ResultSet rs = st.executeQuery(query);
			int index = 0;
			if (rs.next())
				index = rs.getInt(1) - 1;
			screen.setId(index);
			rs.close();

			for (CAttribute a : screen.getCAttributes()) {
				String visible = a.isVisible() ? "TRUE" : "FALSE";
				String reference = a.isReference() ? "1" : "0";
				query = "insert into c_window_windowattribute (id_window, id_attribute, visible, is_reference,display_order,rappel,indirect_reference,indirect_mtm_key,indirect_mtm_value,indirect_function,filter_enabled, forced_filter) values ("
						+ ""
						+ screen.getId()
						+ ","
						+ a.getId()
						+ ","
						+ visible
						+ ","
						+ reference
						+ ","
						+ a.getDisplayOrder()
						+ ",'"
						+ (a.isRappel() ? 'Y' : 'N')
						+ "'"
						+ ",'"
						+ (a.isIndirectReference() ? 'Y' : 'N')
						+ "','"
						+ (a.isIndirectMtmKey() ? 'Y' : 'N')
						+ "','"
						+ (a.isIndirectMtmValue() ? 'Y' : 'N')
						+ "',"
						+ ""
						+ a.getIndirectFunction()
						+ ",'"
						+ (a.isFilterEnabled() ? 'Y' : 'N')
						+ "','"
						+ (a.isFilterMandatory() ? 'Y' : 'N') + "')";
				st.execute(query);
			}

			for (CGlobalValue v : screen.getGlobalValues()) {
				query = "insert into c_window_globalvalue (id_window,id_global) values ("
						+ screen.getId() + "," + v.getId() + ")";
				st.execute(query);
			}

			st.close();

		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	public void updateScreen(CWindow screen, CWindow toLoad,
			SessionCache cache, CBusinessClass entity) {
		try {

			Class.forName("org.postgresql.Driver");
			Connection cnx = DriverManager.getConnection(DBUtils.url,
					DBUtils.username, DBUtils.password);
			Statement st = cnx.createStatement();

			String query = "update c_window set title='"
					+ StringFormat.getInstance().formatQuery(screen.getTitle())
					+ "', \"stepDescription\"='"
					+ StringFormat.getInstance().formatQuery(
							screen.getStepDescription())
					+ "', "
					+ " id_windowtype="
					+ screen.getCWindowtype().getId()
					+ ", id_screen_sequence="
					+ screen.getFunction().getId()
					+ ", id_entity="
					+ entity.getId()
					+ ", rappel_reference="
					+ (screen.getRappelReference() == null ? 0 : screen
							.getRappelReference().getId()) + ", drop_option='"
					+ (screen.isDeleteBtn() ? "Y" : "N") + "', update_option='"
					+ (screen.isUpdateBtn() ? "Y" : "N")
					+ "',  mandatory_filter='"
					+ (screen.isForceFilter() ? "Y" : "N") + "'" 
					+", selection_constraint='"+screen.getSelectConstraints()+"' "
					+ "WHERE id="
					+ toLoad.getId();

			System.out.println("SCREEN UPDATE QUERY\n\t"+query);
			
			st.execute(query);

			query = "delete from c_window_windowattribute where id_window="
					+ toLoad.getId();
			st.execute(query);

			query = "delete from c_window_globalvalue where id_window="
					+ toLoad.getId();
			st.execute(query);

			screen.setId(toLoad.getId());
			for (CAttribute a : screen.getCAttributes()) {
				if (!a.isVisible())
					continue;
				String visible = a.isVisible() ? "TRUE" : "FALSE";
				String reference = a.isReference() ? "1" : "0";
				query = "insert into c_window_windowattribute (id_window, id_attribute, visible, is_reference,display_order, rappel, indirect_reference,indirect_mtm_key,indirect_mtm_value,indirect_function,filter_enabled,forced_filter) values ("
						+ ""
						+ screen.getId()
						+ ","
						+ a.getId()
						+ ","
						+ visible
						+ ","
						+ reference
						+ ","
						+ a.getDisplayOrder()
						+ ",'"
						+ (a.isRappel() ? 'Y' : 'N')
						+ "'"
						+ ",'"
						+ (a.isIndirectReference() ? 'Y' : 'N')
						+ "','"
						+ (a.isIndirectMtmKey() ? 'Y' : 'N')
						+ "','"
						+ (a.isIndirectMtmValue() ? 'Y' : 'N')
						+ "',"
						+ a.getIndirectFunction()
						+ ",'"
						+ (a.isFilterEnabled() ? 'Y' : 'N')
						+ "','"
						+ (a.isFilterMandatory() ? 'Y' : 'N') + "')";
				try {
					st.execute(query);
				} catch (Exception ec) { // On vient de rencontrer un attribut
											// reflexif alors on l'ignore
					String msg = ec.getLocalizedMessage();
					msg = msg + "";
					continue;
				}
			}

			for (CGlobalValue v : screen.getGlobalValues()) {
				query = "insert into c_window_globalvalue (id_window,id_global) values ("
						+ screen.getId() + "," + v.getId() + ")";
				st.execute(query);
			}

			st.close();

		} catch (Exception exc) {
			exc.printStackTrace();
		}

	}

	public void addNewAction(CActionbutton b, SessionCache cache) {
		// TODO Auto-generated method stub
		try {
			Class.forName("org.postgresql.Driver");

			Connection cnx = DriverManager.getConnection(DBUtils.url,
					DBUtils.username, DBUtils.password);

			Statement st = cnx.createStatement();

			int idWindow = 0;
			int idAction = 0;
			int idPostaction = 0;

			// Insert postaction
			MPostAction p = b.getMAction().getPostAction();
			String pquery = "insert into m_postaction (idtype) values ('"
					+ p.getType().getId() + "')";
			st.execute(pquery);
			pquery = "select id from m_postaction order by id desc";
			ResultSet rs = st.executeQuery(pquery);
			if (rs.next())
				idPostaction = rs.getInt(1);
			rs.close();
			for (int i = 0; i < p.getAttributes().size(); i++) {
				CAttribute a = p.getAttributes().get(i);

				int idAttribute = a.getId();
				if (idAttribute == 0) {
					if (a.getDtoValue() == null)
						a.setDtoValue("");
					String atq = "select id from c_attribute where data_reference='"
							+ a.getDataReference() + "' order by id desc";
					rs = st.executeQuery(atq);
					if (rs.next())
						idAttribute = rs.getInt(1);
				}
				String aq = "insert into m_postaction_attribute (id_postaction,id_attribute,default_value,parameter) values "
						+ "('"
						+ idPostaction
						+ "','"
						+ idAttribute
						+ "','','"
						+ StringFormat.getInstance().parameterFormat(
								a.getDtoValue()) + "')";
				st.execute(aq);
			}

			// Insert action
			String aquery = "insert into m_action (title, code, id_postaction) values ("
					+ "'"
					+ StringFormat.getInstance().formatQuery(
							b.getMAction().getTitle())
					+ "','"
					+ b.getMAction().getCode() + "','" + idPostaction + "')";
			System.out.println("CREATE NEW ACTION\n\t" + aquery);
			st.execute(aquery);

			aquery = "select id from m_action where title='"
					+ StringFormat.getInstance().formatQuery(
							b.getMAction().getTitle())
					+ "' and id_postaction='" + idPostaction + "'";
			rs = st.executeQuery(aquery);
			if (rs.next())
				idAction = rs.getInt(1);
			rs.close();

			List<String> tags = new ArrayList<String>();
			String code = b.getMAction().getCode();
			for (int i = 0; i < code.length(); i++) {
				int index = code.indexOf("<<", i);
				if (index < 0)
					break;
				index = index + 2;
				int endindex = code.indexOf(">>", index);
				String tag = code.substring(index, endindex);
				tags.add(tag);
				i = endindex;
			}

			for (String t : tags) {
				aquery = "insert into m_arguments (tag, id_action) values ('"
						+ t + "'," + idAction + ")";
				st.execute(aquery);
			}

			// Insert action button
			idWindow = b.getCWindow().getId();

			String bquery = "insert into c_actionbutton (title, description, id_action, id_window,parameters,document_bound,id_document) values ("
					+ "'"
					+ StringFormat.getInstance().formatQuery(b.getTitle())
					+ "', '"
					+ StringFormat.getInstance()
							.formatQuery(b.getDescription())
					+ "','"
					+ idAction
					+ "', '"
					+ idWindow
					+ "','"
					+ b.getParameters()
					+ "','"
					+ (b.isBound() ? 'Y' : 'N')
					+ "',"
					+ (b.isBound() ? b.getBoundDocument().getId() : 0) + ")";
			st.execute(bquery);

			String query = "select nextval('c_actionbutton_seq')";
			rs = st.executeQuery(query);
			int index = 0;
			if (rs.next())
				index = rs.getInt(1) - 1;
			b.setId(index);
			rs.close();

			st.close();

		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	public void addNewDocument(CDocumentbutton b, SessionCache cache) {
		try {

			Class.forName("org.postgresql.Driver");

			Connection cnx = DriverManager.getConnection(DBUtils.url,
					DBUtils.username, DBUtils.password);

			Statement st = cnx.createStatement();

			int idWindow = 0;
			int idDocument = 0;

			// Insert mdocument
			String truefullpath = b.getMDocument().getStream();
			String fullPath = truefullpath.replace('\\', '_');
			fullPath = fullPath.replace('/', '_');
			String[] sp = fullPath.split("_");
			String fileName = "";
			if (sp.length > 0)
				fileName = sp[sp.length - 1];

			String mdq = "insert into m_document (title, stream, parameter_mode) values ('"
					+ StringFormat.getInstance().formatQuery(b.getTitle())
					+ "', '"
					+ fileName
					+ "','"
					+ b.getMDocument().getParameterMode() + "')";

			st.execute(mdq);

			mdq = "select nextval('m_document_seq')";
			ResultSet rs = st.executeQuery(mdq);
			if (rs.next())
				idDocument = rs.getInt(1) - 1;
			rs.close();

			File file = new File(truefullpath);
			FileInputStream fis = new FileInputStream(file);

			String mwquery = "update m_document set zip_content=? where id="
					+ idDocument;
			PreparedStatement ps = cnx.prepareStatement(mwquery);
			ps.setBinaryStream(1, fis, (int) file.length());
			ps.executeUpdate();

			// Insert cdocumentbutton
			idWindow = b.getCWindow().getId();

			String dbq = "insert into c_documentbutton (title,description, parameters, id_document, id_window) values ('"
					+ StringFormat.getInstance().formatQuery(b.getTitle())
					+ "', '"
					+ StringFormat.getInstance()
							.formatQuery(b.getDescription())
					+ "', '"
					+ b.getParameters()
					+ "', "
					+ idDocument
					+ ", "
					+ idWindow
					+ ")";
			st.execute(dbq);

			String query = "select nextval('c_documentbutton_seq')";
			rs = st.executeQuery(query);
			int index = 0;
			if (rs.next())
				index = rs.getInt(1) - 1;
			b.setId(index);
			rs.close();

			st.close();

		} catch (Exception exc) {
			exc.printStackTrace();
		}

	}

	public void addNewProcess(SProcess p, SessionCache cache) {
		// TODO Auto-generated method stub
		try {

			Class.forName("org.postgresql.Driver");
			Connection cnx = DriverManager.getConnection(DBUtils.url,
					DBUtils.username, DBUtils.password);
			Statement st = cnx.createStatement();

			// Insert parameters windows
			for (CWindow w : p.getWindows()) {
				if (w instanceof CParametersWindow) {
					CParametersWindow pw = (CParametersWindow) w;
					String pwq = "insert into c_parameters_window (title, \"stepDescription\", id_windowtype, appkey) values ( '"
							+ StringFormat.getInstance().formatQuery(
									pw.getTitle())
							+ "','"
							+ StringFormat.getInstance().formatQuery(
									pw.getStepDescription())
							+ "',5,'"
							+ cache.getAppKey() + "')";
					st.execute(pwq);

					// insert window ctrls
					int windowID = 0;
					pwq = "select id from c_parameters_window where appkey='"
							+ cache.getAppKey()
							+ "' and title='"
							+ StringFormat.getInstance().formatQuery(
									pw.getTitle()) + "' order by id desc";
					ResultSet rs = st.executeQuery(pwq);
					if (rs.next())
						windowID = rs.getInt(1);
					rs.close();

					List<CUIParameter> ctrls = pw.getUiParameters();
					for (CUIParameter c : ctrls) {
						String cq = "insert into c_parameter_ctrl (id_window, parameter_key, parameter_type, parameter_label) values "
								+ "("
								+ windowID
								+ ",'"
								+ c.getParameterKey()
								+ "', '"
								+ c.getParameterType()
								+ "','"
								+ StringFormat.getInstance().formatQuery(
										c.getParameterLabel()) + "')";
						st.execute(cq);
					}
				}
			}

			// Insert process
			String pq = "insert into s_process (title, description, appkey) values"
					+ "('"
					+ StringFormat.getInstance().formatQuery(p.getTitle())
					+ "', '"
					+ StringFormat.getInstance()
							.formatQuery(p.getDescription())
					+ "', '"
					+ cache.getAppKey() + "')";
			st.execute(pq);
			int processID = 0;
			pq = "select id from s_process where title='"
					+ StringFormat.getInstance().formatQuery(p.getTitle())
					+ "' and appkey='" + cache.getAppKey()
					+ "' order by id desc";
			ResultSet rs = st.executeQuery(pq);
			if (rs.next())
				processID = rs.getInt(1);
			p.setId(processID);
			// Map windows to process
			for (int i = 0; i < p.getWindows().size(); i++) {
				CWindow w = p.getWindows().get(i);

				int windowID = w.getId();
				int isparameter = (w instanceof CParametersWindow) ? 1 : 0;
				String wq = "insert into s_process_window (id_s_process, id_c_window, \"order\", is_parameter) values "
						+ "("
						+ processID
						+ ","
						+ windowID
						+ ","
						+ (i + 1)
						+ "," + isparameter + ")";
				st.execute(wq);
			}

			st.close();

		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	public void deleteMenuEntry(SMenuitem m) {

		try {

			Class.forName("org.postgresql.Driver");
			Connection cnx = DriverManager.getConnection(DBUtils.url,
					DBUtils.username, DBUtils.password);
			Statement st = cnx.createStatement();

			String query = "delete from s_menuitem where id_parent="
					+ m.getId();
			st.execute(query);
			query = "delete from s_menuitem where id=" + m.getId();
			st.execute(query);

			st.close();

		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	public void addNewMenu(SMenuitem i, SessionCache cache) {
		// TODO Auto-generated method stub
		try {

			Class.forName("org.postgresql.Driver");
			Connection cnx = DriverManager.getConnection(DBUtils.url,
					DBUtils.username, DBUtils.password);
			Statement st = cnx.createStatement();

			String query = "";
			if (i.isParent()) {
				query = "insert into s_menuitem (title, \"isParent\", appkey, rubrique_id) values ('"
						+ StringFormat.getInstance().formatQuery(i.getTitle())
						+ "', TRUE, '"
						+ cache.getAppKey()
						+ "',"
						+ i.getRubrique() + ")";
			} else {
				query = "insert into s_menuitem (title, \"isParent\", id_parent, appkey, window_id) values ('"
						+ StringFormat.getInstance().formatQuery(i.getTitle())
						+ "', FALSE,"
						+ i.getParent().getId()
						+ ",'"
						+ cache.getAppKey()
						+ "',"
						+ i.getWindow().getId()
						+ ")";
			}

			st.execute(query);

			query = "select nextval('s_menuitem_seq')";
			ResultSet rs = st.executeQuery(query);
			int index = 0;
			if (rs.next())
				index = rs.getInt(1) - 1;

			i.setId(index);

			st.close();

		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	public void addNewProcedure(SProcedure p, String appkey, String keywords) {
		// TODO Auto-generated method stub
		try {

			Class.forName("org.postgresql.Driver");
			Connection cnx = DriverManager.getConnection(DBUtils.url,
					DBUtils.username, DBUtils.password);
			Statement st = cnx.createStatement();

			// insert s_procedure
			String query = "insert into s_procedure (title,description,app_key,key_words) values (?,?,?,?)";
			PreparedStatement ps = cnx.prepareStatement(query);
			ps.setString(1, p.getTitle());
			ps.setString(2, p.getDescription());
			ps.setString(3, appkey);
			ps.setString(4, keywords);

			ps.executeUpdate();

			query = "select nextval('s_procedure_seq')";
			ResultSet rs = st.executeQuery(query);
			int index = 0;
			if (rs.next())
				index = rs.getInt(1) - 1;
			p.setId(index);
			rs.close();
			ps.close();

			// insert s_step
			for (SStep s : p.getEtapes()) {
				query = "insert into s_procedure_step (title, description, procedure_id) values (?,?,?)";
				ps = cnx.prepareStatement(query);
				ps.setString(1, s.getTitle());
				ps.setString(2, s.getDescription());
				ps.setInt(3, p.getId());
				ps.executeUpdate();

				query = "select nextval('s_procedure_step_seq')";
				rs = st.executeQuery(query);
				index = 0;
				if (rs.next())
					index = rs.getInt(1) - 1;
				s.setId(index);

				// insert s_atom
				for (SAtom a : s.getActions()) {
					query = "insert into s_atom (title, id_type, description, id_step";
					String vals = " values (?,?,?,?";

					switch (a.getType().getId()) {
					case 1:
						query = query + ",id_window)";
						vals = vals + ",?)";
						break;
					case 2:
						query = query + ")";
						vals = vals + ")";
						break;
					case 3:
					case 4:
						query = query + ",id_resource)";
						vals = vals + ",?)";
						break;
					case 5:
					case 6:
					case 7:
					case 8:
						query = query + ",id_scheduled_com)";
						vals = vals + ",?)";
						break;
					}

					query = query + vals;

					ps = cnx.prepareStatement(query);

					ps.setString(1, a.getTitle());
					ps.setInt(2, a.getType().getId());
					ps.setString(3, a.getDescription());
					ps.setInt(4, s.getId());

					switch (a.getType().getId()) {
					case 1:
						// if window exists in db
						if (a.getWindow().getId() > 0)
							ps.setInt(5, a.getWindow().getId());
						else {
							String wq = "select id from c_window where title='"
									+ StringFormat.getInstance()
											.attributeDataReferenceFormat(
													a.getWindow().getTitle())
									+ "' and appkey='" + appkey + "'";
							rs = st.executeQuery(wq);
							index = 0;
							if (rs.next())
								index = rs.getInt(1);
							rs.close();
							ps.setInt(5, index);
						}
						break;
					case 2:
						for (CUIParameter par : a.getParameters()) {
							String pq = "";
							if (par.getId() > 0) {
								pq = "insert into s_procedure_s_parameter (procedure_id, parameter_id) values ("
										+ p.getId() + "," + par.getId() + ")";
								st.execute(pq);
							} else {
								pq = "select id from c_parameter_ctrl where parameter_key='"
										+ par.getParameterKey()
										+ "' and parameter_type='"
										+ par.getParameterType()
										+ "' and parameter_label='"
										+ StringFormat
												.getInstance()
												.parameterFormat(
														par.getParameterLabel())
										+ "' order by id desc";
								rs = st.executeQuery(pq);
								index = 0;
								boolean flag = false;
								if (rs.next()) {
									index = rs.getInt(1);
									flag = true;
								}
								rs.close();
								if (!flag) {
									pq = "insert into c_parameter_ctrl (parameter_key, parameter_type, parameter_label) "
											+ "values ('"
											+ par.getParameterKey()
											+ "', '"
											+ par.getParameterType()
											+ "', '"
											+ StringFormat
													.getInstance()
													.parameterFormat(
															par.getParameterLabel())
											+ "')";
									st.execute(pq);
									pq = "select nextval('c_parameter_ctrl_seq')";
									rs = st.executeQuery(pq);
									index = 0;
									if (rs.next()) {
										index = rs.getInt(1) - 1;
									}
									par.setId(index);
								}
								pq = "insert into s_procedure_s_parameter (procedure_id, parameter_id) values ("
										+ p.getId() + "," + index + ")";
								st.execute(pq);
							}

						}
						break;
					case 3:
					case 4:
						SResource r = a.getResource();
						if (r.getId() > 0) {
							ps.setInt(5, r.getId());
						} else {
							String rq = "select id from s_resource where title='"
									+ StringFormat.getInstance().formatQuery(
											r.getTitle())
									+ "' and appkey='"
									+ appkey + "'";
							rs = st.executeQuery(rq);
							index = 0;
							boolean flag = false;
							if (rs.next()) {
								index = rs.getInt(1);
								r.setId(index);
								flag = true;
							}
							rs.close();
							if (!flag) {
								rq = "insert into s_resource (title, description, filename, appkey) values ("
										+ "'"
										+ StringFormat.getInstance()
												.formatQuery(r.getTitle())
										+ "',"
										+ "'"
										+ StringFormat
												.getInstance()
												.formatQuery(r.getDescription())
										+ "',"
										+ "'"
										+ r.getFileName()
										+ "','"
										+ appkey + "')";
								st.execute(rq);
								rq = "select nextval('s_resource_seq')";
								rs = st.executeQuery(rq);
								index = 0;
								if (rs.next()) {
									index = rs.getInt(1) - 1;
								}
								r.setId(index);
							}
							ps.setInt(5, r.getId());
						}
						break;
					case 5:
					case 6:
					case 7:
					case 8:
						String scq = "";
						if (a.getCommunication().getAttachement() == null)
							scq = "insert into s_scheduled_com (title, description) values ('"
									+ StringFormat.getInstance().formatQuery(
											a.getCommunication().getTitle())
									+ "',"
									+ "'"
									+ StringFormat.getInstance().formatQuery(
											a.getCommunication()
													.getDescription()) + "')";
						else {
							String rq = "select id from s_resource where title='"
									+ StringFormat.getInstance().formatQuery(
											a.getCommunication()
													.getAttachement()
													.getTitle())
									+ "' and appkey='" + appkey + "'";
							rs = st.executeQuery(rq);
							index = 0;
							if (rs.next()) {
								index = rs.getInt(1);
							}
							rs.close();
							scq = "insert into s_scheduled_com (title, description, attachement_id) values ('"
									+ StringFormat.getInstance().formatQuery(
											a.getCommunication().getTitle())
									+ "',"
									+ "'"
									+ StringFormat.getInstance().formatQuery(
											a.getCommunication()
													.getDescription())
									+ "',"
									+ index + ")";
						}
						st.execute(scq);
						scq = "select nextval('s_scheduled_com_seq')";
						rs = st.executeQuery(scq);
						index = 0;
						if (rs.next()) {
							index = rs.getInt(1) - 1;
						}
						a.getCommunication().setId(index);
						ps.setInt(5, index);

						break;
					}

					ps.executeUpdate();
					ps.close();

				}

			}

			st.close();

		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	public void deleteProcedure(SProcedure toDelete) {
		// TODO Auto-generated method stub
		try {

			Class.forName("org.postgresql.Driver");
			Connection cnx = DriverManager.getConnection(DBUtils.url,
					DBUtils.username, DBUtils.password);
			Statement st = cnx.createStatement();

			String query = "";

			for (SStep s : toDelete.getEtapes()) {
				query = "delete from s_atom where id_step=" + s.getId();
				st.execute(query);

				query = "delete from s_procedure_step where id=" + s.getId();
				st.execute(query);
			}

			query = "delete from s_procedure where id=" + toDelete.getId();
			st.execute(query);

			st.close();

		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	public void addNewGValue(CGlobalValue v) {
		// TODO Auto-generated method stub
		try {

			Class.forName("org.postgresql.Driver");
			Connection cnx = DriverManager.getConnection(DBUtils.url,
					DBUtils.username, DBUtils.password);
			String sql = "insert into c_globalvalue (\"key\",\"label\",\"value\",appkey) values (?,?,?,?) ";
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setString(1, v.getKey());
			ps.setString(2, v.getLabel());
			ps.setString(3, v.getValue());
			ps.setString(4, v.getAppKey());

			ps.execute();

			sql = "select nextval('c_globalvalue_seq')";
			Statement st = cnx.createStatement();
			ResultSet rs = st.executeQuery(sql);
			int id = 0;
			if (rs.next())
				id = rs.getInt(1) - 1;
			rs.close();

			v.setId(id);

			st.close();
			ps.close();

		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	public void saveLinks(CWindow source) {

		try {

			Class.forName("org.postgresql.Driver");
			Connection cnx = DriverManager.getConnection(DBUtils.url,
					DBUtils.username, DBUtils.password);
			String sql = "delete from c_window_link where source=?";
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, source.getId());

			ps.execute();
			ps.close();

			for (CWindow w : source.getLinks()) {
				sql = "insert into c_window_link (source,destination) values (?,?)";
				ps = cnx.prepareStatement(sql);
				ps.setInt(1, source.getId());
				ps.setInt(2, w.getId());

				ps.execute();
				ps.close();
			}

		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	public void generateRole(CoreRole role, String appkey) {
		try {

			Class.forName("org.postgresql.Driver");
			Connection cnx = DriverManager.getConnection(DBUtils.url,
					DBUtils.username, DBUtils.password);

			String screens = "";
			String actions = "";
			String documents = "";

			for (CWindow w : role.getWindows())
				screens = screens + w.getId() + ";";
			if (screens.length() > 0)
				screens = screens.substring(0, screens.length() - 1);

			for (CActionbutton w : role.getActions())
				actions = actions + w.getId() + ";";
			if (actions.length() > 0)
				actions = actions.substring(0, actions.length() - 1);

			for (CDocumentbutton w : role.getDocuments())
				documents = documents + w.getId() + ";";
			if (documents.length() > 0)
				documents = documents.substring(0, documents.length() - 1);

			String options = "";
			for (SMenuitem i : role.getOptions())
				options = options + i.getId() + ";";
			if (options.length() > 0)
				options = options.substring(0, options.length() - 1);

			role.setsActions(actions);
			role.setsDocuments(documents);
			role.setsWindows(screens);
			role.setsProcesses("");

			/*
			 * Logo insert
			 */
			try {
				if (role.getLogo() != null) {
					byte[] lgbytes = IOUtils.toByteArray(role.getLogo());
					role.getLogo().close();
					role.getLogo();
					String rsql = "insert into s_resource_table (key,filename, content) values (?,?,?)";
					PreparedStatement rps = cnx.prepareStatement(rsql);
					rps.setString(1, role.getLogoResKey());
					rps.setString(2, role.getFileName());
					rps.setBytes(3, lgbytes);
					rps.execute();
				}
			} catch (Exception e) {
				System.out.println("RESSOURCES");
				e.printStackTrace();
			}

			String sql = "insert into core_role (role,screens,actions,documents,appkey, options,user_bound,superadmin,form_mode,logo, description) values (?,?,?,?,?,?,?,?,?,?,?)";
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setString(1, role.getRole());
			ps.setString(2, role.getsWindows());
			ps.setString(3, role.getsActions());
			ps.setString(4, role.getsDocuments());
			ps.setString(5, appkey);
			ps.setString(6, options);
			ps.setInt(7, role.getBoundEntity());
			ps.setString(8, role.isSuperAdmin() ? "Y" : "N");
			ps.setString(9, role.getVision());
			ps.setString(10, role.getLogoResKey());
			ps.setString(11, role.getDescription());

			ps.execute();
			ps.close();

			sql = "select nextval('core_role_seq')";
			;
			ps = cnx.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			int id = 0;
			if (rs.next())
				id = rs.getInt(1) - 1;
			rs.close();
			ps.close();

			if (!role.isSuperAdmin())
				return;

			sql = "insert into core_user (\"firstName\",\"lastName\",login,password,\"idRole\",appkey,user_theme,theme_color,theme_style,lang) "
					+ "values (?,?,?,?,?,?,?,?,?,?)";
			ps = cnx.prepareStatement(sql);
			ps.setString(1, role.getRole());
			ps.setString(2, role.getRole());
			ps.setString(3,
					StringFormat.getInstance().parameterFormat(role.getRole()));
			ps.setString(4, Md5.encode("1234"));
			ps.setInt(5, id);
			ps.setString(6, appkey);
			ps.setString(7, "THEME:DEVELOPR");
			ps.setString(8, "css/style.css?v=1");
			ps.setString(9, "css/colors.css?v=1");
			ps.setString(10, "fr");
			ps.execute();

			ps.close();

		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	public void updateRole(CoreRole role, String appkey) {
		try {

			Class.forName("org.postgresql.Driver");
			Connection cnx = DriverManager.getConnection(DBUtils.url,
					DBUtils.username, DBUtils.password);

			String screens = "";
			String actions = "";
			String documents = "";

			for (CWindow w : role.getWindows())
				screens = screens + w.getId() + ";";
			if (screens.length() > 0)
				screens = screens.substring(0, screens.length() - 1);

			for (CActionbutton w : role.getActions())
				actions = actions + w.getId() + ";";
			if (actions.length() > 0)
				actions = actions.substring(0, actions.length() - 1);

			for (CDocumentbutton w : role.getDocuments())
				documents = documents + w.getId() + ";";
			if (documents.length() > 0)
				documents = documents.substring(0, documents.length() - 1);
			String options = "";
			for (SMenuitem i : role.getOptions())
				options = options + i.getId() + ";";
			if (options.length() > 0)
				options = options.substring(0, options.length() - 1);
			role.setsActions(actions);
			role.setsDocuments(documents);
			role.setsWindows(screens);
			role.setsProcesses("");

			/*
			 * Logo insert
			 */
			try {
				if (role.getLogo() != null) {
					byte[] lgbytes = IOUtils.toByteArray(role.getLogo());
					role.getLogo().close();
					role.getLogo();
					String rsql = "insert into s_resource_table (key,filename, content) values (?,?,?)";
					PreparedStatement rps = cnx.prepareStatement(rsql);
					rps.setString(1, role.getLogoResKey());
					rps.setString(2, role.getFileName());
					rps.setBytes(3, lgbytes);
					rps.execute();
				}
			} catch (Exception e) {
				System.out.println("RESSOURCES");
				e.printStackTrace();
			}

			String sql = "update core_role set role=?, screens=?, actions=?, documents=?, options=?, user_bound=?, superadmin=?, form_mode=?, logo=?, description=?  where id=?";
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setString(1, role.getRole());
			ps.setString(2, role.getsWindows());
			ps.setString(3, role.getsActions());
			ps.setString(4, role.getsDocuments());
			ps.setString(5, options);
			ps.setInt(6, role.getBoundEntity());
			ps.setString(7, role.isSuperAdmin() ? "Y" : "N");
			ps.setString(8, role.getVision());
			ps.setString(9, role.getLogoResKey());
			ps.setString(10, role.getDescription());
			ps.setInt(11, role.getId());

			ps.execute();
			ps.close();

		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	public void generateWidgets(SWidget w, String appkey) {
		try {

			Class.forName("org.postgresql.Driver");
			Connection cnx = DriverManager.getConnection(DBUtils.url,
					DBUtils.username, DBUtils.password);
			Statement st = cnx.createStatement();

			String query = "insert into s_widget (title,label,lvalue,type,wquery,appkey) values ('"
					+ w.getTitle()
					+ "','"
					+ w.getLabel()
					+ "','"
					+ w.getLvalues()
					+ "','"
					+ w.getType()
					+ "','"
					+ w.getQuery() + "','" + appkey + "')";
			st.execute(query);

			st.close();

		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	public void generateAlert(SAlert a) {
		try {

			Class.forName("org.postgresql.Driver");
			Connection cnx = DriverManager.getConnection(DBUtils.url,
					DBUtils.username, DBUtils.password);
			String sql = "insert into s_alert (titre,description,type,role,appkey,s_window)"
					+ " values " + "(?,?,?,?,?,?)";

			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setString(1, a.getTitre());
			ps.setString(2, a.getDescription());
			ps.setInt(3, a.isInsert() ? 0 : 1);
			ps.setInt(4, a.getRole().getId());
			ps.setString(5, a.getAppKey());
			ps.setInt(6, a.getWindow().getId());

			ps.execute();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void addNewParameterModel(CParameterMetamodel tocreate, String appkey) {
		try {

			Class.forName("org.postgresql.Driver");
			Connection cnx = DriverManager.getConnection(DBUtils.url,
					DBUtils.username, DBUtils.password);

			String sql = "insert into c_metaparameters (label,description,appkey,organization) "
					+ "values (?,?,?,?)";
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setString(1, tocreate.getLabel());
			ps.setString(2, tocreate.getDescription());
			ps.setString(3, appkey);
			ps.setInt(4, tocreate.getOrganization().getId());

			ps.execute();
			ps.close();

			int id = 0;
			sql = "select nextval('c_metaparameters_seq')";
			ps = cnx.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			if (rs.next())
				id = rs.getInt(1) - 1;
			rs.close();
			ps.close();

			for (CBusinessClass e : tocreate.getMappedEntities()) {
				sql = "insert into c_meta_entity_mapping (metaparameters,businessclass) values (?,?)";
				ps = cnx.prepareStatement(sql);
				ps.setInt(1, id);
				ps.setInt(2, e.getId());

				ps.execute();
				ps.close();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void deleteParametersModel(int id) {
		try {

			Class.forName("org.postgresql.Driver");
			Connection cnx = DriverManager.getConnection(DBUtils.url,
					DBUtils.username, DBUtils.password);

			String sql = "delete from c_meta_entity_mapping where metaparameters=?";
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, id);

			ps.execute();
			ps.close();

			sql = "delete from c_metaparameters where id=?";
			ps = cnx.prepareStatement(sql);
			ps.setInt(1, id);

			ps.execute();
			ps.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void saveParameterModel(CParameterMetamodel tocreate, String appkey) {
		try {

			Class.forName("org.postgresql.Driver");
			Connection cnx = DriverManager.getConnection(DBUtils.url,
					DBUtils.username, DBUtils.password);

			String sql = "update c_metaparameters set label=?,description=?,appkey=?,organization=? where id=?";
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setString(1, tocreate.getLabel());
			ps.setString(2, tocreate.getDescription());
			ps.setString(3, appkey);
			ps.setInt(4, tocreate.getOrganization().getId());
			ps.setInt(5, tocreate.getId());

			ps.execute();
			ps.close();

			int id = tocreate.getId();
			sql = "delete from c_meta_entity_mapping where metaparameters=?";
			ps = cnx.prepareStatement(sql);
			ps.setInt(1, id);

			ps.execute();

			for (CBusinessClass e : tocreate.getMappedEntities()) {
				sql = "insert into c_meta_entity_mapping (metaparameters,businessclass) values (?,?)";
				ps = cnx.prepareStatement(sql);
				ps.setInt(1, id);
				ps.setInt(2, e.getId());

				ps.execute();
				ps.close();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void addNewRubrique(SRubrique r, SessionCache cache) {
		String sql = "insert into s_rubrique (title,appkey,technical) values (?,?,?)";
		try {
			Class.forName("org.postgresql.Driver");
			Connection cnx = DriverManager.getConnection(DBUtils.url,
					DBUtils.username, DBUtils.password);
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setString(1, r.getTitre());
			ps.setString(2, cache.getAppKey());
			ps.setString(3, r.isTechnical() ? "Y" : "N");
			ps.execute();
			ps.close();

			sql = "select nextval('s_rubrique_seq')";
			ps = cnx.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			if (rs.next())
				r.setId(rs.getInt(1) - 1);
			rs.close();
			ps.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void generateWindowFormLink(CWindow src) {
		String sql = "update c_window set form_window=? where id=?";
		try {
			Class.forName("org.postgresql.Driver");
			Connection cnx = DriverManager.getConnection(DBUtils.url,
					DBUtils.username, DBUtils.password);
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, src.getFormId());
			ps.setInt(2, src.getId());

			ps.execute();
			ps.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void persistGOrganization(GOrganization o, SessionCache cache,
			List<Integer> implicatedEntitiesId) {
		String sql = "insert into g_organization (nom, id_parent, root_organization,representative,appkey) values (?,?,?,?,?)";
		try {
			Class.forName("org.postgresql.Driver");
			Connection cnx = DriverManager.getConnection(DBUtils.url,
					DBUtils.username, DBUtils.password);
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setString(1, o.getName());
			ps.setInt(2, o.getParent() != null ? o.getParent().getId() : 0);
			ps.setString(3, (o.isRoot() ? "Y" : "N"));
			ps.setInt(4, o.getRepresentativeEntity().getId());
			ps.setString(5, cache.getAppKey());

			ps.execute();
			ps.close();

			sql = "select nextval('g_organization_seq')";
			ps = cnx.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				o.setId(rs.getInt(1) - 1);
			}
			rs.close();
			ps.close();

			sql = "insert into g_org_independant_data (org_id, ent_id) values (?,?)";
			for (Integer ii : implicatedEntitiesId) {
				int i = ii.intValue();
				ps = cnx.prepareStatement(sql);
				ps.setInt(1, o.getId());
				ps.setInt(2, i);
				ps.execute();
				ps.close();

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void deleteOrganization(GOrganization todel) {
		String sql = "delete from g_organization where id=?";
		try {
			Class.forName("org.postgresql.Driver");
			Connection cnx = DriverManager.getConnection(DBUtils.url,
					DBUtils.username, DBUtils.password);
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setString(1, todel.getName());
			ps.execute();
			ps.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void updateParametersModel(GParametersPackage o, int id) {

		try {
			Class.forName("org.postgresql.Driver");
			Connection cnx = DriverManager.getConnection(DBUtils.url,
					DBUtils.username, DBUtils.password);
			String sql = "update g_parameters_pkg set nom=?, representative=? where id=?";
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setString(1, o.getNom());
			ps.setInt(2, o.getEntity().getId());
			ps.setInt(3, id);

			ps.execute();

			sql = "delete from g_parameters_entities where parameters_pkg=?";
			ps = cnx.prepareStatement(sql);
			ps.setInt(1, id);
			ps.execute();

			// Now we should map the package with the implicated entities
			for (CBusinessClass e : o.getImplicatedEntities()) {
				sql = "insert into g_parameters_entities (parameters_pkg, business_class) values (?,?)";
				ps = cnx.prepareStatement(sql);
				ps.setInt(1, id);
				ps.setInt(2, e.getId());

				ps.execute();
				ps.close();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void persistParametersModel(GParametersPackage o, String appKey) {
		String sql = "insert into g_parameters_pkg (nom, representative,appkey) values (?,?,?)";
		try {
			Class.forName("org.postgresql.Driver");
			Connection cnx = DriverManager.getConnection(DBUtils.url,
					DBUtils.username, DBUtils.password);
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setString(1, o.getNom());
			ps.setInt(2, o.getEntity().getId());
			ps.setString(3, appKey);

			ps.execute();
			ps.close();

			sql = "select nextval('g_parameters_pkg_seq')";
			ps = cnx.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				o.setId(rs.getInt(1) - 1);
			}
			rs.close();
			ps.close();

			// Now we should map the package with the implicated entities
			for (CBusinessClass e : o.getImplicatedEntities()) {
				sql = "insert into g_parameters_entities (parameters_pkg, business_class) values (?,?)";
				ps = cnx.prepareStatement(sql);
				ps.setInt(1, o.getId());
				ps.setInt(2, e.getId());

				ps.execute();
				ps.close();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void updateOrgParametersMapping(int selectedOrg,
			List<Integer> selectedModelsId) {

		String sql = "delete from g_org_package_association where id_org=?";
		try {
			Class.forName("org.postgresql.Driver");
			Connection cnx = DriverManager.getConnection(DBUtils.url,
					DBUtils.username, DBUtils.password);
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, selectedOrg);

			ps.execute();
			ps.close();
			sql = "insert into g_org_package_association (id_org, id_pkg) values (?,?)";
			for (Integer ii : selectedModelsId) {
				int i = ii.intValue();
				ps = cnx.prepareStatement(sql);
				ps.setInt(1, selectedOrg);
				ps.setInt(2, i);

				ps.execute();
				ps.close();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void saveDiagram(String diagram, SessionCache cache) {
		String query = "update s_application set json_diagram='" + diagram
				+ "' where appkey='" + cache.getAppKey() + "'";
		try {
			Class.forName("org.postgresql.Driver");
			Connection cnx = DriverManager.getConnection(DBUtils.url,
					DBUtils.username, DBUtils.password);
			Statement st = cnx.createStatement();
			st.execute(query);
			st.close();
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	public String retrieveDiagram(SessionCache cache) {
		String query = "select json_diagram from s_application where appkey='"
				+ cache.getAppKey() + "'";
		String diagram = null;
		try {
			Class.forName("org.postgresql.Driver");
			Connection cnx = DriverManager.getConnection(DBUtils.url,
					DBUtils.username, DBUtils.password);
			Statement st = cnx.createStatement();
			ResultSet rs = st.executeQuery(query);
			if (rs.next())
				diagram = rs.getString(1);
			rs.close();
			st.close();
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		return diagram;
	}

	public Integer addNewWFDefintion(String title, String description,
			Integer window, String diagram, SessionCache cache) {
		String query = "INSERT INTO s_wf_definition(title, description, \"window\", appKey, json_diagram) ";
		query += "VALUES('" + title + "','" + description + "'," + window
				+ ",'" + cache.getAppKey() + "','" + diagram + "')";
		Integer definitionId = null;
		try {
			Class.forName("org.postgresql.Driver");
			Connection cnx = DriverManager.getConnection(DBUtils.url,
					DBUtils.username, DBUtils.password);
			Statement st = cnx.createStatement();
			st.execute(query);
			st.close();

			String sql = "select nextval('s_wf_definition_seq')";
			Statement ps = cnx.createStatement();
			ResultSet rs = ps.executeQuery(sql);
			if (rs.next()) {
				definitionId = new Integer(rs.getInt(1) - 1);
			}
			rs.close();
			ps.close();

		} catch (Exception exc) {
			exc.printStackTrace();
		} finally {

		}
		return definitionId;
	}

	public Integer addNewWFNode(String title, String message, String type,
			Integer typeId, Integer role, String window, Integer defId) {
		String query = "Insert into s_wf_node(label, description, type, definition, responsible)";
		query += "VALUES('" + title + "','" + message + "'," + typeId + ","
				+ defId + "," + role + ")";
		Integer nodeId = null;
		try {
			Class.forName("org.postgresql.Driver");
			Connection cnx = DriverManager.getConnection(DBUtils.url,
					DBUtils.username, DBUtils.password);
			Statement st = cnx.createStatement();
			st.execute(query);
			st.close();
			String sql = "select nextval('s_wf_node_seq')";
			Statement ps = cnx.createStatement();
			ResultSet rs = ps.executeQuery(sql);
			if (rs.next()) {
				nodeId = new Integer(rs.getInt(1) - 1);
			}
			rs.close();
			ps.close();

			storeTypeNode(type, nodeId, window);

		} catch (Exception exc) {
			exc.printStackTrace();
		}
		return nodeId;
	}

	private void storeTypeNode(String type, Integer nodeId, String window) {
		String q = "Insert into s_wf";
		if (type.equals("infos")) {
			q += "_info (node) values(" + nodeId + ")";
		} else if (type.equals("screen")) {
			q += "_screen(node,\"window\") values(" + nodeId + ","
					+ Integer.parseInt(window) + ")";
		} else if (type.equals("request")) {
			q += "_request( yes_label, no_label, node) values('Oui','No',"
					+ nodeId + ")";
		} else if (type.equals("answer")) {
			q += "_answer( node) values(" + nodeId + ")";
		} else if (type.equals("choice")) {
			q += "_choice_node( node) values(" + nodeId + ")";
		}

		try {
			Class.forName("org.postgresql.Driver");
			Connection cnx = DriverManager.getConnection(DBUtils.url,
					DBUtils.username, DBUtils.password);
			Statement st = cnx.createStatement();
			st.execute(q);
			st.close();

		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	public void updateWFNode(String type, Integer node, Integer yesNode,
			Integer noNode, Integer requestNode) {
		String query = "";
		if (type.equals("request")) {
			if (yesNode != null) {
				query = "UPDATE s_wf_request SET yes_node=" + yesNode
						+ " WHERE node=" + node;
			} else if (noNode != node) {
				query = "UPDATE s_wf_request SET no_node=" + noNode
						+ " WHERE node=" + node;
			}
		} else if (type.equals("answer")) {
			query = "UPDATE s_wf_answer SET request=" + requestNode
					+ " WHERE node=" + node;
		}
		try {
			Class.forName("org.postgresql.Driver");
			Connection cnx = DriverManager.getConnection(DBUtils.url,
					DBUtils.username, DBUtils.password);
			Statement st = cnx.createStatement();
			st.execute(query);
			st.close();

		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	public Integer addNewChoice(Integer nodeAId, Integer transId, String title,
			String nodeId) {
		String query = "INSERT INTO s_wf_choice(node, transition, ref_node, title) ";
		query += "VALUES (" + nodeAId + "," + transId + "," + nodeId + ",'"
				+ title + "')";
		Integer choiceId = null;
		try {
			Class.forName("org.postgresql.Driver");
			Connection cnx = DriverManager.getConnection(DBUtils.url,
					DBUtils.username, DBUtils.password);
			Statement st = cnx.createStatement();
			st.execute(query);
			st.close();

			String sql = "select nextval('s_wf_choice_seq')";
			Statement ps = cnx.createStatement();
			ResultSet rs = ps.executeQuery(sql);
			if (rs.next()) {
				choiceId = new Integer(rs.getInt(1) - 1);
			}
			rs.close();
			ps.close();

		} catch (Exception exc) {
			exc.printStackTrace();
		}
		return choiceId;
	}

	public Integer addNewWFTransition(String title, Boolean auto,
			Integer nodeAId, Integer nodeBId, Integer defId) {
		String query = "INSERT INTO s_wf_transition(\"from\", \"to\", definition) ";
		query += "VALUES(" + nodeAId + "," + nodeBId + "," + defId + ")";
		Integer transId = null;
		try {
			Class.forName("org.postgresql.Driver");
			Connection cnx = DriverManager.getConnection(DBUtils.url,
					DBUtils.username, DBUtils.password);
			Statement st = cnx.createStatement();
			st.execute(query);
			st.close();
			String sql = "select nextval('s_wf_transition_seq')";
			Statement ps = cnx.createStatement();
			ResultSet rs = ps.executeQuery(sql);
			if (rs.next()) {
				transId = new Integer(rs.getInt(1) - 1);
			}
			rs.close();
			ps.close();

		} catch (Exception exc) {
			exc.printStackTrace();
		}
		return transId;
	}

	public void updateIndependentEntities(List<CBusinessClass> selent,
			String appKey) {

		String sql = "delete from g_org_independant_data where org_id=6";
		try {
			Class.forName("org.postgresql.Driver");
			Connection cnx = DriverManager.getConnection(DBUtils.url,
					DBUtils.username, DBUtils.password);
			Statement st = cnx.createStatement();
			st.execute(sql);
			st.close();

			sql = "insert into g_org_independant_data (org_id, ent_id) values (?,?)";
			for (CBusinessClass e : selent) {
				PreparedStatement ps = cnx.prepareStatement(sql);
				ps.setInt(1, 6);
				ps.setInt(2, e.getId());
				ps.execute();
				ps.close();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void addNewFactTable(CFactTable factTable) {
		try {
			Class.forName("org.postgresql.Driver");
			Connection cnx = DriverManager.getConnection(DBUtils.url,
					DBUtils.username, DBUtils.password);
			// insert data within c_fact_table
			String insertQuery = "INSERT INTO c_fact_table(tableName, query, appKey, view_name) VALUES(?,?,?,?)";
			String viewName = createViewForFactTable(factTable.getTableName(), factTable.getQuery());
			PreparedStatement ps = cnx.prepareStatement(insertQuery);
			ps.setString(1, factTable.getTableName());
			ps.setString(2, factTable.getQuery());
			ps.setString(3, factTable.getAppKey());
			ps.setString(4, viewName);
			ps.execute();
			ps.close();
			
			// retrieve the id of the last insert
			int factTableId = 0;
			String sql = "SELECT nextval('c_fact_table_seq')";
			Statement statement = cnx.createStatement();
			ResultSet rs = statement.executeQuery(sql);
			if (rs.next()) {
				factTableId = new Integer(rs.getInt(1) - 1);
			}
			rs.close();
			statement.close();
			
			// insert into 
			for(CoreRole role : factTable.getRoles()) {
				String rft = "INSERT INTO c_role__fact_table(role_id, fact_table_id) VALUES(?,?)";
				PreparedStatement rps = cnx.prepareStatement(rft);
				rps.setInt(1, role.getId());
				rps.setInt(2, factTableId);
				rps.execute();
				rps.close();
			}

			for (CFactTableAttribute attr : factTable.getAttributes()) {
				// some thing
				String query = "INSERT INTO c_fact_table_attribute(attribute_id, labelattribute, isindex, isxdimension, isydimension, isgroupby, aggregationfct, constraints, fact_table_id)"
						+ " VALUES(?,?,?,?,?,?,?,?,?)";
				String constraintJS = "{";
				for (Iterator<String> it = attr.getConstraints().keySet().iterator(); it.hasNext();) {
					String key = it.next();
					constraintJS += "\"" + key + "\":";
					constraintJS += "\"" + attr.getConstraints().get(key) + "\"";
					if (it.hasNext()) {
						constraintJS += ",";
					}

				}
				constraintJS += "}";
				PreparedStatement attrPs = cnx.prepareStatement(query);
				attrPs.setInt(1, attr.getAttributeId());
				attrPs.setString(2, attr.getLabelAttribute());
				attrPs.setString(3, attr.isIndex() ? "Y" : "N");
				attrPs.setString(4, attr.isxDimension() ? "Y" : "N");
				attrPs.setString(5, attr.isyDimension() ? "Y" : "N");
				attrPs.setString(6, attr.isGroupBy() ? "Y" : "N");
				attrPs.setString(7, attr.getAggregationFct());
				attrPs.setString(8, constraintJS);
				attrPs.setInt(9, factTableId);
				attrPs.execute();
				attrPs.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	private String  createViewForFactTable(String tableName, String query) {
		tableName = tableName.replaceAll("[^a-zA-Z0-9_]", "_");
		tableName += "_"+new Date().getTime();
		String qCreateView = "CREATE VIEW " + tableName + " AS "+query;
		try{
			Connection cnx = DriverManager.getConnection(DBUtils.url,
					DBUtils.username, DBUtils.password);
			PreparedStatement ps2 = cnx.prepareStatement(qCreateView);
			ps2.execute();
			ps2.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
		return tableName;
	}

	public List<CFactTable> retrieveFactTables(String appKey) {
		List<CFactTable> factTables = new ArrayList<CFactTable>();
		try {
			Class.forName("org.postgresql.Driver");
			Connection cnx = DriverManager.getConnection(DBUtils.url,
					DBUtils.username, DBUtils.password);
			String sql = "SELECT id,tableName, query,view_name FROM c_fact_table WHERE appkey = '"+appKey+"'";
			Statement statement = cnx.createStatement();
			ResultSet rs = statement.executeQuery(sql);
			while (rs.next()) {
				CFactTable factTable = new CFactTable();
				factTable.setTableName(rs.getString("tableName"));
				factTable.setQuery(rs.getString("query"));
				factTable.setAppKey(appKey);
				factTable.setId(rs.getInt("id"));
				factTable.setViewName(rs.getString("view_name"));
				
				factTables.add(factTable);
			}
			rs.close();
			statement.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return factTables;
	}

	public CFactTable retrieveFactTable(CFactTable factTable) {
		try {
			Class.forName("org.postgresql.Driver");
			Connection cnx = DriverManager.getConnection(DBUtils.url,
					DBUtils.username, DBUtils.password);
			String sql = "SELECT id,attribute_id, labelattribute, isindex, isxdimension, isydimension, isgroupby, aggregationfct, constraints FROM c_fact_table_attribute WHERE fact_table_id="
					+ factTable.getId();
			Statement statement = cnx.createStatement();
			ResultSet rs = statement.executeQuery(sql);
			factTable.setAttributes(new ArrayList<CFactTableAttribute>());
			while (rs.next()) {
				CFactTableAttribute attribute = new CFactTableAttribute();
				attribute.setId(rs.getInt("id"));
				long randInt = (int) Math.random() * 1000000;
				attribute.setTempId(new Date().getTime() + randInt);
				attribute.setAttributeId(rs.getInt("attribute_id"));
				attribute.setLabelAttribute(rs.getString("labelattribute"));
				attribute
						.setIndex("Y".equalsIgnoreCase(rs.getString("isindex")) ? true
								: false);
				attribute.setxDimension("Y".equalsIgnoreCase(rs
						.getString("isxdimension")) ? true : false);
				attribute.setyDimension("Y".equalsIgnoreCase(rs
						.getString("isydimension")) ? true : false);
				attribute.setGroupBy("Y".equalsIgnoreCase(rs
						.getString("isgroupby")) ? true : false);
				attribute.setAggregationFct(rs.getString("aggregationfct"));
				String stCons = rs.getString("constraints");
				attribute.setConstraints(new HashMap<String, String>());
				if (!"".equals(stCons)) {
					try {
						JSONObject jConst = new JSONObject(stCons);
						for (Iterator<String> it = jConst.keys(); it.hasNext();) {
							String key = it.next();
							attribute.getConstraints().put(key,
									jConst.getString(key));
						}
					} catch (Exception e) {
						attribute.setConstraints(new HashMap<String, String>());
					}
				}
				factTable.getAttributes().add(attribute);
			}
			rs.close();
			statement.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return factTable;
	}

	public boolean deleteFactTable(int factId, String nameView) {
		String query = "DELETE FROM c_fact_table WHERE id=" + factId;
		String attrquery = "DELETE FROM c_fact_table_attribute WHERE fact_table_id=" + factId;
		/*String rolequery = "DELETE FROM c_role__fact_table where fact_table_id=" + factId;
		String dropView = "DROP VIEW "+nameView;*/
		try {
			Class.forName("org.postgresql.Driver");
			Connection cnx = DriverManager.getConnection(DBUtils.url, DBUtils.username, DBUtils.password);
			// delete fact table attribute
			PreparedStatement ps2 = cnx.prepareStatement(attrquery);
			ps2.execute();
			ps2.close();
			// delete fact table
			PreparedStatement ps = cnx.prepareStatement(query);
			ps.execute();
			ps.close();
			cnx.close();
			return true;
			
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}

	public CCallout addNewCallout(CCallout toCreate, SessionCache cache) {
		String json = new JSONSerializer().serialize(toCreate.getArgs());
		toCreate.setJsonArguments(json);
		String sql = "insert into c_callout (nom, fichier, arguments, appkey, callout_key) values (?, ?, ?, ?, ?) returning id";
		try{
			Class.forName("org.postgresql.Driver");
			Connection cnx = DriverManager.getConnection(DBUtils.url, DBUtils.username, DBUtils.password);
			PreparedStatement ps = cnx.prepareStatement(sql);
			
			ps.setString(1, toCreate.getNom());
			ps.setBytes(2, toCreate.getFile());
			ps.setString(3, json);
			ps.setString(4, cache.getAppKey());
			ps.setInt(5, toCreate.getKey());
			
			ResultSet rs = ps.executeQuery();
			if(rs.next())
				toCreate.setId(rs.getInt(1));
			
			rs.close();
			ps.close();
			cnx.close();
			
		}catch(Exception exc){
			exc.printStackTrace();
		}
		return toCreate;
	}

	public void addNewWindowCallout(CWindowCallout cw) {
		String sql = "insert into c_window_callout (window_id, callout_id) values (?, ?) returning id;";
		try{
			Class.forName("org.postgresql.Driver");
			Connection cnx = DriverManager.getConnection(DBUtils.url, DBUtils.username, DBUtils.password);
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, cw.getWindow().getId());
			ps.setInt(2, cw.getCallout().getId());
			ResultSet rs = ps.executeQuery();
			if(rs.next())
				cw.setId(rs.getInt(1));
			rs.close();
			ps.close();
			cnx.close();
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		
		for(CWindowCalloutArgument a : cw.getArguments()){
			if(a.getAttribute() == null){
				sql = "insert into c_win_call_argument (window_callout_id, callout_arg, prompt_opt, selection_opt, created_opt)  "
						+ " value (?, ?, ?, ?, ?)";
				try{
					Class.forName("org.postgresql.Driver");
					Connection cnx = DriverManager.getConnection(DBUtils.url, DBUtils.username, DBUtils.password);
					PreparedStatement ps = cnx.prepareStatement(sql);
					ps.setInt(1, cw.getId());
					ps.setString(2, a.getArgument().getLibelle());
					ps.setString(3, a.isPrompt()?"Y":"N");
					ps.setString(4, a.isSelection()?"Y":"N");
					ps.setString(5, a.isCreated()?"Y":"N");
					ps.close();
					cnx.close();
				}catch(Exception exc){
					exc.printStackTrace();
				}
			} else {
				sql = "insert into c_win_call_argument (window_callout_id, callout_arg, prompt_opt, selection_opt, created_opt,attribute_id)  "
						+ " value (?, ?, ?, ?, ?,?)";
				try{
					Class.forName("org.postgresql.Driver");
					Connection cnx = DriverManager.getConnection(DBUtils.url, DBUtils.username, DBUtils.password);
					PreparedStatement ps = cnx.prepareStatement(sql);
					ps.setInt(1, cw.getId());
					ps.setString(2, a.getArgument().getLibelle());
					ps.setString(3, a.isPrompt()?"Y":"N");
					ps.setString(4, a.isSelection()?"Y":"N");
					ps.setString(5, a.isCreated()?"Y":"N");
					ps.setInt(6, a.getAttribute().getId());
					ps.execute();
					ps.close();
					cnx.close();
				}catch(Exception exc){
					exc.printStackTrace();
				}
			}
		}
	}

}
