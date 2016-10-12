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
import fr.protogen.cch.genration.GenerationService;
import fr.protogen.masterdata.model.CBusinessClass;
import fr.protogen.masterdata.model.CCHUser;
import fr.protogen.masterdata.model.GOrganization;
import fr.protogen.masterdata.model.GParametersPackage;

@SuppressWarnings("serial")
@ManagedBean
@ViewScoped
public class ParameterPackageCtrl implements Serializable {
	private String nom;
	private int selectedEntity;
	private List<CBusinessClass> entities;
	private List<CBusinessClass> implicatedEntities;
	private List<Integer> implicatedEntitiesId;
	private boolean maintainmode;
	private List<GParametersPackage> models;
	private int selectedOrg;
	private List<GOrganization> organizations;
	private List<Integer> selectedModelsId;
	
	private int idUpdate=0;
	private boolean updateFlag=false;
	private GParametersPackage modToUpdate;
	
	@PostConstruct
	public void energize(){
		//	Get appllication cache
		CCHUser user = (CCHUser)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(Parameters.USER_TOKEN);
    	SessionCache cache = ApplicationRegistery.getInstance().getSession(user);
    	entities = cache.getEntities();
    	models = cache.getModels();
    	organizations = cache.getGorganizations();
    	setMaintainmode((Boolean)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(Parameters.MAINTAIN_MODE));
    	
    	updateFlag = false;
		String sid = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("modtoup");
		if(sid == null || sid.length()==0)
			return;
		
		updateFlag = true;
		int id = Integer.parseInt(sid);
		
		modToUpdate = null;
		for(GParametersPackage m : models){
			if(m.getId() == id){
				modToUpdate = m;
				updateFlag = true;
				break;
			}
		}
		
		nom = modToUpdate.getNom();
		selectedEntity = modToUpdate.getId();
		implicatedEntitiesId = new ArrayList<Integer>();
		for(CBusinessClass e : modToUpdate.getImplicatedEntities()){
			implicatedEntitiesId.add(new Integer(e.getId()));
		}
	}

	public void saveModel(){
		GParametersPackage o = new GParametersPackage();
		for( CBusinessClass e : entities){
			if(e.getId() == selectedEntity){
				o.setEntity(e);
				break;
			}
		}
		
		implicatedEntities = new ArrayList<CBusinessClass>();
		for(Integer id : implicatedEntitiesId){
			int i = id.intValue();
			for(CBusinessClass e : entities)
				if(i == e.getId()){
					implicatedEntities.add(e);
					break;
				}
		}
		
		o.setNom(nom);
		o.setImplicatedEntities(implicatedEntities);
		
		if(maintainmode){
			CCHUser user = (CCHUser)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(Parameters.USER_TOKEN);
	    	SessionCache cache = ApplicationRegistery.getInstance().getSession(user);
			GenerationService srv = new GenerationService();
			if(!updateFlag)
				srv.persistParametersModel(o, cache.getAppKey());
			else
				srv.updateParametersModel(o, modToUpdate.getId());
		}
	}

	public void saveMapping(){
		if(maintainmode){
			GenerationService srv = new GenerationService();
			srv.updateOrgParametersMapping(selectedOrg, selectedModelsId);
		}
		
	}
	
	public String updateModel(){
		
		
		return "newparpkg";
	}
	
	public void deleteModel(){
		
	}
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


	public boolean isMaintainmode() {
		return maintainmode;
	}


	public void setMaintainmode(boolean maintainmode) {
		this.maintainmode = maintainmode;
	}

	public List<CBusinessClass> getImplicatedEntities() {
		return implicatedEntities;
	}

	public void setImplicatedEntities(List<CBusinessClass> implicatedEntities) {
		this.implicatedEntities = implicatedEntities;
	}

	public List<Integer> getImplicatedEntitiesId() {
		return implicatedEntitiesId;
	}

	public void setImplicatedEntitiesId(List<Integer> implicatedEntitiesId) {
		this.implicatedEntitiesId = implicatedEntitiesId;
	}

	public List<GParametersPackage> getModels() {
		return models;
	}

	public void setModels(List<GParametersPackage> models) {
		this.models = models;
	}

	public int getSelectedOrg() {
		return selectedOrg;
	}

	public void setSelectedOrg(int selectedOrg) {
		this.selectedOrg = selectedOrg;
	}

	public List<GOrganization> getOrganizations() {
		return organizations;
	}

	public void setOrganizations(List<GOrganization> organizations) {
		this.organizations = organizations;
	}

	public List<Integer> getSelectedModelsId() {
		return selectedModelsId;
	}

	public void setSelectedModelsId(List<Integer> selectedModelsId) {
		this.selectedModelsId = selectedModelsId;
	}

	public int getIdUpdate() {
		return idUpdate;
	}

	public void setIdUpdate(int idUpdate) {
		this.idUpdate = idUpdate;
	}

	public boolean isUpdateFlag() {
		return updateFlag;
	}

	public void setUpdateFlag(boolean updateFlag) {
		this.updateFlag = updateFlag;
	}

	public GParametersPackage getModToUpdate() {
		return modToUpdate;
	}

	public void setModToUpdate(GParametersPackage modToUpdate) {
		this.modToUpdate = modToUpdate;
	}
}
