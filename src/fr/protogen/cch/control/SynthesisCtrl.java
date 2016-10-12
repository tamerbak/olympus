package fr.protogen.cch.control;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

import java.util.ArrayList;
import java.util.List;

import fr.protogen.cch.cache.ApplicationRegistery;
import fr.protogen.cch.cache.SessionCache;
import fr.protogen.cch.configuration.Parameters;
import fr.protogen.cch.genration.GenerationService;
import fr.protogen.masterdata.model.*;

@ManagedBean
@SessionScoped
public class SynthesisCtrl {
	
	private String applogo;
	private String appname;
	private String appversion;
	private String appdescription;
	private List<CBusinessClass> entities;
	private List<CWindow> screens;
	private List<SProcess> processes;
	private int progressMonitor;
	
	@PostConstruct
	public void initialize(){
		CCHUser user = (CCHUser)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(Parameters.USER_TOKEN);
    	SessionCache cache = ApplicationRegistery.getInstance().getSession(user);
    	applogo = Parameters.LOGO_PATH;
    	appname = cache.getApplication().getProjectName();
    	appdescription = cache.getApplication().getDescription();
    	appversion = cache.getApplication().getVersion();
    	entities = cache.getEntities();
    	screens = cache.getWindows();
    	
    	// add actions
    	for(CWindow w : screens){
    		w.setCActionbuttons(new ArrayList<CActionbutton>());
    	}
    	for(CActionbutton a : cache.getActions()){
    		for(CWindow w : screens){
    			
    			if(a.getCWindow().getTitle().equals(w.getTitle())){
    				w.getCActionbuttons().add(a);
    				break;
    			}
    		}
    	}
    	
    	// add documents
    	for(CWindow w : screens){
    		w.setCDocumentbuttons(new ArrayList<CDocumentbutton>());
    	}
    	for(CDocumentbutton a : cache.getDocuments()){
    		for(CWindow w : screens){
    			
    			if(a.getCWindow().getTitle().equals(w.getTitle())){
    				w.getCDocumentbuttons().add(a);
    				break;
    			}
    		}
    	}
    	
    	processes = cache.getProcesses();
	}
	
	
	
	public void createApplication(){
		GenerationService service = new GenerationService();
		CCHUser user = (CCHUser)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(Parameters.USER_TOKEN);
    	SessionCache session = ApplicationRegistery.getInstance().getSession(user);
    	
    	service.generateNewApplication(session);
    	service.generateNewSequences(session);
    	progressMonitor=10;
		
		service.generateEntities(session);
		progressMonitor = 60;
		
		
		service.generateScreens(session);
		service.generateActions(session);
		service.generateDocuments(session);
		service.generateProcesses(session);
		progressMonitor=90;

		service.generateMenu(session);
		service.generateSecurity(session);
		service.generateWidgets(session);
		progressMonitor=100;
	}
	
	
	/*
	 * 	Getters and setters
	 */
	
	public String getApplogo() {
		return applogo;
	}

	public void setApplogo(String applogo) {
		this.applogo = applogo;
	}

	public String getAppname() {
		return appname;
	}

	public void setAppname(String appname) {
		this.appname = appname;
	}

	public String getAppversion() {
		return appversion;
	}

	public void setAppversion(String appversion) {
		this.appversion = appversion;
	}

	public String getAppdescription() {
		return appdescription;
	}

	public void setAppdescription(String appdescription) {
		this.appdescription = appdescription;
	}

	public List<CBusinessClass> getEntities() {
		return entities;
	}

	public void setEntities(List<CBusinessClass> entities) {
		this.entities = entities;
	}

	public List<CWindow> getScreens() {
		return screens;
	}

	public void setScreens(List<CWindow> screens) {
		this.screens = screens;
	}

	public List<SProcess> getProcesses() {
		return processes;
	}

	public void setProcesses(List<SProcess> processes) {
		this.processes = processes;
	}



	public int getProgressMonitor() {
		return progressMonitor;
	}



	public void setProgressMonitor(int progressMonitor) {
		this.progressMonitor = progressMonitor;
	}
}
