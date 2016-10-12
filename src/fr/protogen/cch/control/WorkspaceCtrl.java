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
import fr.protogen.cch.genration.MetadataQueryFactory;
import fr.protogen.masterdata.model.*;

@ManagedBean
@SessionScoped
public class WorkspaceCtrl {
	
	private String title;
	private String selectedType;
	private String selectedEntity;
	private List<CBusinessClass> entities;
	private String selectedLabel;
	private List<CAttribute> pieLabels;
	private String selectedValue;
	private List<CAttribute> pieValues;
	private CBusinessClass sEntity;
	private List<SWidget> widgets; 
	private CAttribute[] selectedEntities;
	private boolean table=true;
	
	private boolean maintainmode=false;
	private SessionCache cache; 
	
	@PostConstruct
	public void initialize(){
		CCHUser user = (CCHUser)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(Parameters.USER_TOKEN);
    	cache = ApplicationRegistery.getInstance().getSession(user);
    	entities = cache.getEntities();
    	sEntity = entities.get(0);
    	pieLabels = sEntity.getAttributes();
    	pieValues = sEntity.getAttributes();
    	Boolean b = (Boolean)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(Parameters.MAINTAIN_MODE);
    	maintainmode = b.booleanValue();
    	if(maintainmode)
    		widgets = cache.getWidgets();
	}
	
	public void saveWidget(){
		if(widgets == null)
			widgets = new ArrayList<SWidget>();
		
		SWidget widget = new SWidget();
		widget.setTitle(title);
		widget.setType(selectedType);
		if(selectedType.equals("P")){
			
			CAttribute labelAttribute = new CAttribute();
			CAttribute valueAttribute = new CAttribute();
			for(CAttribute a : sEntity.getAttributes()){
				if(a.getDataReference().equals(selectedLabel))
					labelAttribute = a;
				if(a.getDataReference().equals(selectedValue))
					valueAttribute = a;
			}
			
			widget.setLabel(labelAttribute.getAttribute());
			widget.setLvalues(valueAttribute.getAttribute());
			MetadataQueryFactory factory = new MetadataQueryFactory(); 
			String query = factory.generatePieWidgetQuery(sEntity, labelAttribute, valueAttribute);
			widget.setQuery(query);
		} else if(selectedType.equals("T")){
			MetadataQueryFactory factory = new MetadataQueryFactory(); 
			String query = factory.generateTableWidgetQuery(sEntity, selectedEntities);
			widget.setQuery(query);
			String labels="";
			String values="";
			for(CAttribute a :selectedEntities){
				labels = labels+a.getAttribute()+";";
				values = values+a.getDataReference()+";";
			}
			labels = labels.substring(0,labels.length()-1);
			values = values.substring(0,values.length()-1);
			widget.setLabel(labels);
			widget.setLvalues(values);
		} else if(selectedType.equals("C")){
			
			CAttribute labelAttribute = new CAttribute();
			CAttribute valueAttribute = new CAttribute();
			for(CAttribute a : sEntity.getAttributes()){
				if(a.getDataReference().equals(selectedLabel))
					labelAttribute = a;
				if(a.getDataReference().equals(selectedValue))
					valueAttribute = a;
			}
			
			widget.setLabel(labelAttribute.getAttribute());
			widget.setLvalues(valueAttribute.getAttribute());
			MetadataQueryFactory factory = new MetadataQueryFactory(); 
			String query = factory.generateCourbesWidgetQuery(sEntity, labelAttribute, valueAttribute);
			widget.setQuery(query);
		}
		title="";
		widgets.add(widget);
		
		if(maintainmode) {
			GenerationService srv = new GenerationService();
			srv.generateWidgets(widget, cache.getAppKey());
		}
		
	}
	public void entityChanged(){
		for(CBusinessClass e : entities){
			if(e.getDataReference().equals(selectedEntity)){
				sEntity = e;
				break;
			}
		}
		
		pieLabels = sEntity.getAttributes() ;
		pieValues = sEntity.getAttributes();
	}
	public void typeChanged(){
		if(selectedType.equals("T"))
			table=true;
		else
			table=false;
	}
	
	public String next(){
		CCHUser user = (CCHUser)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(Parameters.USER_TOKEN);
    	SessionCache cache = ApplicationRegistery.getInstance().getSession(user);
    	cache.setWidgets(widgets);
		return "synthesis";
	}
	
	/*
	 * 		GETTERS AND SETTERS
	 */

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSelectedType() {
		return selectedType;
	}

	public void setSelectedType(String selectedType) {
		this.selectedType = selectedType;
	}

	public String getSelectedEntity() {
		return selectedEntity;
	}

	public void setSelectedEntity(String selectedEntity) {
		this.selectedEntity = selectedEntity;
	}

	public List<CBusinessClass> getEntities() {
		return entities;
	}

	public void setEntities(List<CBusinessClass> entities) {
		this.entities = entities;
	}

	public String getSelectedLabel() {
		return selectedLabel;
	}

	public void setSelectedLabel(String selectedLabel) {
		this.selectedLabel = selectedLabel;
	}

	public List<CAttribute> getPieLabels() {
		return pieLabels;
	}

	public void setPieLabels(List<CAttribute> pieLabels) {
		this.pieLabels = pieLabels;
	}

	public String getSelectedValue() {
		return selectedValue;
	}

	public void setSelectedValue(String selectedValue) {
		this.selectedValue = selectedValue;
	}

	public List<CAttribute> getPieValues() {
		return pieValues;
	}

	public void setPieValues(List<CAttribute> pieValues) {
		this.pieValues = pieValues;
	}

	public CBusinessClass getsEntity() {
		return sEntity;
	}

	public void setsEntity(CBusinessClass sEntity) {
		this.sEntity = sEntity;
	}
	
	public List<SWidget> getwidgets(){
		return widgets;
	}
	
	public void setWidgets(List<SWidget> widgets){
		this.widgets = widgets;
	}
	
	public boolean isTable(){
		return table;
	}
	
	public void setTable(boolean table){
		this.table = table;
	}
	
	public CAttribute[] getSelectedEntities(){
		return selectedEntities;
	}
	
	public void setSelectedEntities(CAttribute[] selectedEntities){
		this.selectedEntities = selectedEntities;
	}

	public boolean isMaintainmode() {
		return maintainmode;
	}

	public void setMaintainmode(boolean maintainmode) {
		this.maintainmode = maintainmode;
	}
}
