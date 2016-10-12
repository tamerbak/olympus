package fr.protogen.cch.control;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.primefaces.event.FileUploadEvent;

import fr.protogen.cch.cache.ApplicationRegistery;
import fr.protogen.cch.cache.SessionCache;
import fr.protogen.cch.configuration.Parameters;
import fr.protogen.cch.genration.GenerationService;
import fr.protogen.masterdata.model.*;

@ManagedBean
@SessionScoped
public class RolesCtrl implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3093619913982450453L;

	private String roleTitle;
	private Map<String,String> screens;
	private List<String> selectedScreens;
	private Map<String,String> actions;
	private List<String> selectedActions;
	private Map<String,String> documents;
	private List<String> selectedDocuments;
	private Map<String,String> processes;
	private List<String> selectedProcesses;
	private List<CoreRole> roles; 
	private SessionCache cache;
	
	private boolean maintenancemode = false;
	private boolean modifyFlag = false;
	private CoreRole toUpdate = new CoreRole();
	
	//	Options
	private List<String> menus;
	private List<String> superMenus;
	
	private int entityId;
	private List<CBusinessClass> entities;
	
	private boolean superAdmin;
	private String vision;
	
	//	LOGO
	private InputStream logo = null;
	private String filename;
	
	//	Role description
	private String roleDescription="";
	
	@PostConstruct
	public void initialize(){
		
		logo = null;
		
		//	Get screens
		CCHUser user = (CCHUser)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(Parameters.USER_TOKEN);
    	cache = ApplicationRegistery.getInstance().getSession(user);
    	
    	screens = new HashMap<String, String>();
    	for(CWindow w : cache.getWindows()){
    		screens.put(w.getTitle(), w.getTitle());
    	}
    	selectedScreens = new ArrayList<String>();
    	
    	actions = new HashMap<String, String>();
    	if(cache.getActions() != null){
	    	for(CActionbutton w : cache.getActions()){
	    		actions.put(w.getTitle(), w.getTitle());
	    	}
    	}
    	selectedActions = new ArrayList<String>();
    	
    	documents = new HashMap<String, String>();
    	if(cache.getDocuments() != null){
	    	for(CDocumentbutton w : cache.getDocuments()){
	    		documents.put(w.getTitle(), w.getTitle());
	    	}
    	}
    	selectedDocuments = new ArrayList<String>();
    	
    	processes = new HashMap<String, String>();
    	if(cache.getProcesses() != null){
	    	for(SProcess w : cache.getProcesses()){
	    		processes.put(w.getTitle(), w.getTitle());
	    	}
    	}
    	selectedProcesses = new ArrayList<String>();
    	
    	menus = new ArrayList<String>();
    	if(cache.getMenu() != null){
    		for(SMenuitem i : cache.getMenu()){
    			if(i.isParent())
    				menus.add(i.getTitle());
    		}
    	}
    	
		maintenancemode = ((Boolean)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(Parameters.MAINTAIN_MODE));
		if(maintenancemode){
			roles = cache.getRoles();
			entities = cache.getEntities();
		}
		
		
	}
	
	public String updateRole(){
		int id = Integer.parseInt(FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("roleid"));
		modifyFlag = true;
		
		for(CoreRole r : roles){
			if(r.getId() == id){
				setToUpdate(r);
				break;
			}
		}
		
		selectedScreens = new ArrayList<String>();
		
		for(String sid : toUpdate.getsWindows().split(";")){
			id = Integer.parseInt(sid);
			for(CWindow w : cache.getWindows())
				if(w.getId() == id){
					selectedScreens.add(w.getTitle());
					break;
				}
		}
		
		superMenus = new ArrayList<String>();
		
		if( toUpdate.getSoptions().length()>0){
			for(String sid : toUpdate.getSoptions().split(";")){
				id=Integer.parseInt(sid);
				for(SMenuitem i : cache.getMenu()){
					superMenus.add(i.getTitle());
					break;
				}
			}
		}
		superAdmin = toUpdate.isSuperAdmin();
		roleTitle = toUpdate.getRole();
		entityId = toUpdate.getBoundEntity();
		vision = toUpdate.getVision();
		return "security";
	}
	
	public void saveRole(){
		if(roles == null)
			roles = new ArrayList<CoreRole>();
		CoreRole role = new CoreRole();
		role.setRole(roleTitle);
		role.setDescription(roleDescription);
		role.setWindows(new ArrayList<CWindow>());
		for(String ss : selectedScreens){
			for(CWindow w : cache.getWindows()){
				if(w.getTitle().equals(ss)){
					role.getWindows().add(w);
					break;
				}
			}
		}
		
		role.setOptions(new ArrayList<SMenuitem>());
		for(String s : superMenus){
			for(SMenuitem i : cache.getMenu())
				if(i.getTitle().equals(s)){
					role.getOptions().add(i);
				}
		}
			
		
		role.setActions(new ArrayList<CActionbutton>());
		for(String ss : selectedActions){
			for(CActionbutton w : cache.getActions()){
				if(w.getTitle().equals(ss)){
					role.getActions().add(w);
					break;
				}
			}
		}
		
		role.setDocuments(new ArrayList<CDocumentbutton>());
		for(String ss : selectedDocuments){
			for(CDocumentbutton w : cache.getDocuments()){
				if(w.getTitle().equals(ss)){
					role.getDocuments().add(w);
					break;
				}
			}
		}
		
		role.setProcesses(new ArrayList<SProcess>());
		for(String ss : selectedProcesses){
			for(SProcess w : cache.getProcesses()){
				if(w.getTitle().equals(ss)){
					role.getProcesses().add(w);
					break;
				}
			}
		}
		role.setBoundEntity(entityId);
		role.setSuperAdmin(superAdmin);
		role.setVision(vision);
		if(logo != null){
			role.setLogo(logo);
			role.setLogoResKey(UUID.randomUUID().toString());
			role.setFileName(filename);
		}
		
		if(!modifyFlag)
			roles.add(role);
		
		if(maintenancemode){
			
			GenerationService service = new GenerationService();
			if(!modifyFlag){
				service.generateRole(role, cache.getAppKey());
			} else {
				role.setId(toUpdate.getId());
				service.updateRole(role, cache.getAppKey());
				modifyFlag = false;
			}
		}
		
		roleTitle = "";
		selectedActions = new ArrayList<String>();
		selectedDocuments = new ArrayList<String>();
		selectedProcesses = new ArrayList<String>();
		selectedScreens = new ArrayList<String>();
	}
	
	public String next(){
		cache.setRoles(roles);
		return "alerts";
	}

	public void handleFileUpload(FileUploadEvent event) {
       	try {
       		logo = event.getFile().getInputstream();
   			filename = event.getFile().getFileName();
   		} catch (IOException e) {
   			e.printStackTrace();
   		}
       }
	
	/*
	 * GETTERS AND SETTERS
	 */
	public String getRoleTitle() {
		return roleTitle;
	}

	public void setRoleTitle(String roleTitle) {
		this.roleTitle = roleTitle;
	}

	public Map<String, String> getScreens() {
		return screens;
	}

	public void setScreens(Map<String, String> screens) {
		this.screens = screens;
	}

	public List<String> getSelectedScreens() {
		return selectedScreens;
	}

	public void setSelectedScreens(List<String> selectedScreens) {
		this.selectedScreens = selectedScreens;
	}

	public Map<String, String> getActions() {
		return actions;
	}

	public void setActions(Map<String, String> actions) {
		this.actions = actions;
	}

	public List<String> getSelectedActions() {
		return selectedActions;
	}

	public void setSelectedActions(List<String> selectedActions) {
		this.selectedActions = selectedActions;
	}

	public Map<String, String> getDocuments() {
		return documents;
	}

	public void setDocuments(Map<String, String> documents) {
		this.documents = documents;
	}

	public List<String> getSelectedDocuments() {
		return selectedDocuments;
	}

	public void setSelectedDocuments(List<String> selectedDocuments) {
		this.selectedDocuments = selectedDocuments;
	}

	public Map<String, String> getProcesses() {
		return processes;
	}

	public void setProcesses(Map<String, String> processes) {
		this.processes = processes;
	}

	public List<String> getSelectedProcesses() {
		return selectedProcesses;
	}

	public void setSelectedProcesses(List<String> selectedProcesses) {
		this.selectedProcesses = selectedProcesses;
	}

	public List<CoreRole> getRoles() {
		return roles;
	}

	public void setRoles(List<CoreRole> roles) {
		this.roles = roles;
	}

	public boolean isMaintenancemode() {
		return maintenancemode;
	}

	public void setMaintenancemode(boolean maintenancemode) {
		this.maintenancemode = maintenancemode;
	}

	public boolean isModifyFlag() {
		return modifyFlag;
	}

	public void setModifyFlag(boolean modifyFlag) {
		this.modifyFlag = modifyFlag;
	}

	public CoreRole getToUpdate() {
		return toUpdate;
	}

	public void setToUpdate(CoreRole toUpdate) {
		this.toUpdate = toUpdate;
	}

	public SessionCache getCache() {
		return cache;
	}

	public void setCache(SessionCache cache) {
		this.cache = cache;
	}

	public List<String> getMenus() {
		return menus;
	}

	public void setMenus(List<String> menus) {
		this.menus = menus;
	}

	public List<String> getSuperMenus() {
		return superMenus;
	}

	public void setSuperMenus(List<String> superMenus) {
		this.superMenus = superMenus;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public int getEntityId() {
		return entityId;
	}

	public void setEntityId(int entityId) {
		this.entityId = entityId;
	}

	public List<CBusinessClass> getEntities() {
		return entities;
	}

	public void setEntities(List<CBusinessClass> entities) {
		this.entities = entities;
	}

	public boolean isSuperAdmin() {
		return superAdmin;
	}

	public void setSuperAdmin(boolean superAdmin) {
		this.superAdmin = superAdmin;
	}

	public String getVision() {
		return vision;
	}

	public void setVision(String vision) {
		this.vision = vision;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getRoleDescription() {
		return roleDescription;
	}

	public void setRoleDescription(String roleDescription) {
		this.roleDescription = roleDescription;
	}

}
