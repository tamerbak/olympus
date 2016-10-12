package fr.protogen.cch.control;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import fr.protogen.cch.cache.ApplicationRegistery;
import fr.protogen.cch.cache.SessionCache;
import fr.protogen.cch.configuration.Parameters;
import fr.protogen.cch.genration.GenerationService;
import fr.protogen.masterdata.model.CBusinessClass;
import fr.protogen.masterdata.model.CCHUser;
import fr.protogen.masterdata.model.COrganization;
import fr.protogen.masterdata.model.CParameterMetamodel;

@ManagedBean
@SessionScoped
public class ParamsCtrl {

	private List<CParameterMetamodel> models = new ArrayList<CParameterMetamodel>();
	private int selectedOrg;
	private List<COrganization> organizations = new ArrayList<COrganization>();
	private CParameterMetamodel tocreate = new CParameterMetamodel();
	private List<CBusinessClass> entities = new ArrayList<CBusinessClass>();
	private int[] selectedEntities;
	
	private boolean maintainmode = false;
	private SessionCache cache;
	private boolean update=false;
	
	@PostConstruct
	public void energize(){
		CCHUser user = (CCHUser)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(Parameters.USER_TOKEN);
    	cache = ApplicationRegistery.getInstance().getSession(user);
    	
    	maintainmode = ((Boolean)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(Parameters.MAINTAIN_MODE)).booleanValue();
		if(maintainmode){
			models = cache.getParametersModels();
			
		}
		
		organizations = cache.getOrganizations();
		entities = cache.getEntities();
	}
	
	public String doUpdate(){
		
		update = true;
		
		selectedEntities = new int[tocreate.getMappedEntities().size()];
		int i = 0;
		for(CBusinessClass c : tocreate.getMappedEntities()){
			selectedEntities[i] = c.getId();
			i++;
		}
			
		
		return "newparametersmodel";
	}
	
	public void doDelete(){
		String sid = FacesContext.getCurrentInstance().getExternalContext().getRequestMap().get("modtodel").toString();
		int id = Integer.parseInt(sid);
		
		GenerationService service = new GenerationService();
		service.deleteParametersModel(id);
	}

	public void saveModel(){
		for(COrganization o : organizations)
			if(selectedOrg == o.getId()){
				tocreate.setOrganization(o);
			}
		
		tocreate.setMappedEntities(new ArrayList<CBusinessClass>());
		for(int id : selectedEntities){
			for(CBusinessClass e : entities)
				if(e.getId()==id){
					tocreate.getMappedEntities().add(e);
					break;
				}
		}
		
		cache.getParametersModels().add(tocreate);
		
		if(maintainmode){
			GenerationService service = new GenerationService();
			if(!update)
				service.addNewParameterModel(tocreate, cache.getAppKey());
			else
				service.saveParameterModel(tocreate, cache.getAppKey());
		}
		tocreate = new CParameterMetamodel();
		update = false;
	}
	
	/*
	 * 	GETTERS AND SETTERS
	 */
	public List<CParameterMetamodel> getModels() {
		return models;
	}

	public void setModels(List<CParameterMetamodel> models) {
		this.models = models;
	}

	public boolean isMaintainmode() {
		return maintainmode;
	}

	public void setMaintainmode(boolean maintainmode) {
		this.maintainmode = maintainmode;
	}

	public SessionCache getCache() {
		return cache;
	}

	public void setCache(SessionCache cache) {
		this.cache = cache;
	}

	public int getSelectedOrg() {
		return selectedOrg;
	}

	public void setSelectedOrg(int selectedOrg) {
		this.selectedOrg = selectedOrg;
	}

	public List<COrganization> getOrganizations() {
		return organizations;
	}

	public void setOrganizations(List<COrganization> organizations) {
		this.organizations = organizations;
	}

	public CParameterMetamodel getTocreate() {
		return tocreate;
	}

	public void setTocreate(CParameterMetamodel tocreate) {
		this.tocreate = tocreate;
	}

	public List<CBusinessClass> getEntities() {
		return entities;
	}

	public void setEntities(List<CBusinessClass> entities) {
		this.entities = entities;
	}

	public int[] getSelectedEntities() {
		return selectedEntities;
	}

	public void setSelectedEntities(int[] selectedEntities) {
		this.selectedEntities = selectedEntities;
	}
}
