package fr.protogen.cch.control;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

import fr.protogen.cch.cache.ApplicationRegistery;
import fr.protogen.cch.cache.SessionCache;
import fr.protogen.cch.configuration.Parameters;
import fr.protogen.cch.genration.GenerationService;
import fr.protogen.masterdata.model.CCHUser;
import fr.protogen.masterdata.model.CWindow;
import fr.protogen.masterdata.model.CoreRole;
import fr.protogen.masterdata.model.SAlert;

@ManagedBean
@SessionScoped
public class AlertsCtrl {
	
	private List<SAlert> alerts = new ArrayList<SAlert>();
	private SAlert toDelete;
	private SessionCache cache;
	private Boolean maintainmode;
	private SAlert current = new SAlert();
	private String selectedWindow;
	private List<CWindow> windows = new ArrayList<CWindow>();
	private String selectedRole;
	private List<CoreRole> roles = new ArrayList<CoreRole>();
    
	@PostConstruct
	public void energize(){
		CCHUser user = (CCHUser)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(Parameters.USER_TOKEN);
    	cache = ApplicationRegistery.getInstance().getSession(user);
    	windows = cache.getWindows();
    	roles = cache.getRoles();
    	
    	maintainmode = (Boolean)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(Parameters.MAINTAIN_MODE);
		if(maintainmode){
			alerts = cache.getAlerts();
		}
	}
	
	public String saveAlert(){
		current.setAppKey(cache.getAppKey());
		for(CWindow w : windows)
			if(w.getTitle().equals(selectedWindow)){
				current.setWindow(w);
				break;
			}
		
		for(CoreRole w : roles)
			if(w.getRole().equals(selectedRole)){
				current.setRole(w);
				break;
			}
		
		if(cache.getAlerts() == null)
			cache.setAlerts(new ArrayList<SAlert>());
		cache.getAlerts().add(current);
		
		alerts = cache.getAlerts();
		
		if(maintainmode){
			GenerationService srv = new GenerationService();
			srv.generateAlert(current);
		}
		
		current = new SAlert();
		return "";
	}
	
	public String next(){
		return "workspace";
	}
	
	public String constructDependencies(){
		return "";
	}
	
	public void doDelete(){
		
	}
	
	public void cancelDelete(){
		
	}

	public List<SAlert> getAlerts() {
		return alerts;
	}

	public void setAlerts(List<SAlert> alerts) {
		this.alerts = alerts;
	}

	public SAlert getToDelete() {
		return toDelete;
	}

	public void setToDelete(SAlert toDelete) {
		this.toDelete = toDelete;
	}

	public SessionCache getCache() {
		return cache;
	}

	public void setCache(SessionCache cache) {
		this.cache = cache;
	}

	public Boolean getMaintainmode() {
		return maintainmode;
	}

	public void setMaintainmode(Boolean maintainmode) {
		this.maintainmode = maintainmode;
	}

	public SAlert getCurrent() {
		return current;
	}

	public void setCurrent(SAlert current) {
		this.current = current;
	}

	public String getSelectedWindow() {
		return selectedWindow;
	}

	public void setSelectedWindow(String selectedWindow) {
		this.selectedWindow = selectedWindow;
	}

	public List<CWindow> getWindows() {
		return windows;
	}

	public void setWindows(List<CWindow> windows) {
		this.windows = windows;
	}

	public String getSelectedRole() {
		return selectedRole;
	}

	public void setSelectedRole(String selectedRole) {
		this.selectedRole = selectedRole;
	}

	public List<CoreRole> getRoles() {
		return roles;
	}

	public void setRoles(List<CoreRole> roles) {
		this.roles = roles;
	}
}
