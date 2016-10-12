package fr.protogen.cch.control;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import fr.protogen.cch.cache.ApplicationRegistery;
import fr.protogen.cch.cache.SessionCache;
import fr.protogen.cch.configuration.Parameters;
import fr.protogen.cch.genration.GenerationService;
import fr.protogen.masterdata.model.CBusinessClass;
import fr.protogen.masterdata.model.CCHUser;
import fr.protogen.masterdata.model.GOrganization;

@SuppressWarnings("serial")
@ManagedBean
@ViewScoped
public class OrgsCtrl implements Serializable {
	private String nom;
	private int selectedEntity;
	private List<CBusinessClass> entities;
	private boolean rootOrg;
	private int parentOrg;
	private List<GOrganization> organizations;
	private boolean maintainmode;
	private List<Integer> implicatedEntitiesId;
	
	@PostConstruct
	public void energize(){
		//	Get appllication cache
		CCHUser user = (CCHUser)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(Parameters.USER_TOKEN);
    	SessionCache cache = ApplicationRegistery.getInstance().getSession(user);
    	entities = cache.getEntities();
    	organizations = cache.getGorganizations();
    	setMaintainmode((Boolean)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(Parameters.MAINTAIN_MODE));
	}
	
	public void saveOrg(){
		
		GOrganization o = new GOrganization();
		for( CBusinessClass e : entities){
			if(e.getId() == selectedEntity){
				o.setRepresentativeEntity(e);
				break;
			}
		}
		for(GOrganization or : organizations){
			if(rootOrg){
				GOrganization p = new GOrganization();
				p.setId(0);;
				o.setParent(p);
				break;
			}
			if(or.getId() == parentOrg){
				o.setParent(or);
			}
		}
		
		o.setRoot(rootOrg);
		o.setName(nom);
		
		
		if(maintainmode){
			CCHUser user = (CCHUser)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(Parameters.USER_TOKEN);
	    	SessionCache cache = ApplicationRegistery.getInstance().getSession(user);
	    	
			GenerationService srv = new GenerationService();
			srv.persistGOrganization(o, cache, implicatedEntitiesId);
			organizations.add(o);
		}
		
		selectedEntity = entities.get(0).getId();
		nom = "";
		rootOrg=false;
	}

	public void deleteOrg(){
		int id;
		String sid = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("modtodel");
		if(sid == null || sid.equals("null")){
			return;
		}
		
		id = Integer.parseInt(sid);
		GOrganization todel = new GOrganization();
		for(GOrganization o : organizations)
			if(o.getId() == id){
				todel = o;
				break;
			}
		
		organizations.remove(todel);
		
		if(maintainmode){
			GenerationService srv = new GenerationService();
			srv.deleteOrganization(todel);
		}
	}
	
	/*
	 * Getters and setters
	 */
	public String getNom() {
		return nom;
	}

	public void setNom(String nom) {
		this.nom = nom;
	}

	public int getSelectedEntity() {
		return selectedEntity;
	}

	public void setSelectedEntity(int selectedEntity) {
		this.selectedEntity = selectedEntity;
	}

	public List<CBusinessClass> getEntities() {
		return entities;
	}

	public void setEntities(List<CBusinessClass> entities) {
		this.entities = entities;
	}

	public boolean isRootOrg() {
		return rootOrg;
	}

	public void setRootOrg(boolean rootOrg) {
		this.rootOrg = rootOrg;
	}

	public int getParentOrg() {
		return parentOrg;
	}

	public void setParentOrg(int parentOrg) {
		this.parentOrg = parentOrg;
	}

	public List<GOrganization> getOrganizations() {
		return organizations;
	}

	public void setOrganizations(List<GOrganization> organizations) {
		this.organizations = organizations;
	}

	public boolean isMaintainmode() {
		return maintainmode;
	}

	public void setMaintainmode(boolean maintainmode) {
		this.maintainmode = maintainmode;
	}

	public List<Integer> getImplicatedEntitiesId() {
		return implicatedEntitiesId;
	}

	public void setImplicatedEntitiesId(List<Integer> implicatedEntitiesId) {
		this.implicatedEntitiesId = implicatedEntitiesId;
	}
}
