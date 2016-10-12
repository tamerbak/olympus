package fr.protogen.cch.control;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import fr.protogen.cch.cache.ApplicationRegistery;
import fr.protogen.cch.cache.SessionCache;
import fr.protogen.cch.configuration.Parameters;
import fr.protogen.masterdata.dal.DAL;
import fr.protogen.masterdata.model.*;

@ManagedBean
@ViewScoped
public class HomeCtrl implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3995843377517656877L;

	private List<SApplication> applications;
	private SApplication selectedApp;
	private SessionCache cache;
	
	@PostConstruct
	public void initialize(){
		DAL dal = new DAL();
		applications = dal.selectAllApplications();
    	FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(Parameters.MAINTAIN_MODE, new Boolean(false));

	}
	
	public String next(){
		CCHUser user = (CCHUser)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(Parameters.USER_TOKEN);
    	cache = ApplicationRegistery.getInstance().getSession(user);
    	
    	cache.setApplication(selectedApp);
    	cache.setAppKey(selectedApp.getAppKey());
    	DAL dal = new DAL();
    	
    	//	load entities
    	List<CBusinessClass> entities = dal.getEntities(selectedApp);
    	cache.setEntities(entities);
    	cache.setAttributes(new ArrayList<CAttribute>());
    	for(CBusinessClass e : entities){
    		cache.getAttributes().addAll(e.getAttributes());
    	}
    	
    	//	load screen sequences
    	List<SScreensequence> functions = dal.getSequences(selectedApp);
    	cache.setSequences(functions);
    	
    	//	load screens, actions and documents
    	List<CWindow> screens = dal.getScreens(functions,entities);
    	cache.setWindows(screens);
    	cache.setActions(new ArrayList<CActionbutton>());
    	cache.setDocuments(new ArrayList<CDocumentbutton>());
    	for(CWindow w : screens){
    		cache.getActions().addAll(w.getCActionbuttons());
    		cache.getDocuments().addAll(w.getCDocumentbuttons());
    	}
    	
    	//	load processes
    	List<SProcess> processes = dal.getProcesses(cache);
    	cache.setProcesses(processes);
    	
    	//	load procedures
    	List<SProcedure> procedures = dal.getProcedures(cache);
    	cache.setProcedures(procedures);
    	
    	//	load resources
    	List<SResource> resources = dal.getResources(cache);
    	cache.setResources(resources);
    	
    	//	Load global values
    	List<CGlobalValue> values = dal.getGlobalValues(cache);
    	cache.setGlobalValues(values);
    	
    	//	load menu
    	List<SMenuitem> menu = dal.getMenu(selectedApp, screens, cache); 
    	cache.setMenu(menu);
    	
    	//	load roles 
    	List<CoreRole> roles = dal.getRoles(cache);
    	cache.setRoles(roles);
    	
    	//	load widgets
    	List<SWidget> widgets = dal.getWidgets(cache);
    	cache.setWidgets(widgets);
    	
    	//	load alerts
    	List<SAlert> alerts = dal.getAlerts(cache);
    	cache.setAlerts(alerts);
    	
    	//	load organizations
    	List<COrganization> organizations = dal.getOrgs(cache);
    	cache.setOrganizations(organizations);
    	
    	//	Load GOrgs
    	List<GOrganization> orgs = dal.loadGorgs(cache);
    	cache.setGorganizations(orgs);
    	
    	//	Load new parameters models
    	List<GParametersPackage> pkgs = dal.getPkgs(cache);
    	cache.setModels(pkgs);
    	
    	//	load parameters models
    	List<CParameterMetamodel> metamodels = dal.getParameterModels(cache);
    	cache.setParametersModels(metamodels);
    	
    	//	load callouts
    	List<CCallout> callouts = dal.getCallouts(cache);
    	cache.setCallouts(callouts);
    	cache.setWindowCallouts(dal.getWindowCallouts(cache));
    	
    	FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(Parameters.MAINTAIN_MODE, new Boolean(true));
    	
		return "preanalysis";
	}
	
	

	public List<SApplication> getApplications() {
		return applications;
	}

	public void setApplications(List<SApplication> applications) {
		this.applications = applications;
	}

	public SApplication getSelectedApp() {
		return selectedApp;
	}

	public void setSelectedApp(SApplication selectedApp) {
		this.selectedApp = selectedApp;
	}

	
}
