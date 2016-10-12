package fr.protogen.cch.cache;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import fr.protogen.cch.control.ui.beans.DocumentParameter;
import fr.protogen.masterdata.model.CActionbutton;
import fr.protogen.masterdata.model.CAttribute;
import fr.protogen.masterdata.model.CBusinessClass;
import fr.protogen.masterdata.model.CCallout;
import fr.protogen.masterdata.model.CDocumentbutton;
import fr.protogen.masterdata.model.CGlobalValue;
import fr.protogen.masterdata.model.COrganization;
import fr.protogen.masterdata.model.CParameterMetamodel;
import fr.protogen.masterdata.model.CUIParameter;
import fr.protogen.masterdata.model.CWindow;
import fr.protogen.masterdata.model.CWindowCallout;
import fr.protogen.masterdata.model.CoreRole;
import fr.protogen.masterdata.model.GOrganization;
import fr.protogen.masterdata.model.GParametersPackage;
import fr.protogen.masterdata.model.SAlert;
import fr.protogen.masterdata.model.SApplication;
import fr.protogen.masterdata.model.SMenuitem;
import fr.protogen.masterdata.model.SProcedure;
import fr.protogen.masterdata.model.SProcess;
import fr.protogen.masterdata.model.SResource;
import fr.protogen.masterdata.model.SRubrique;
import fr.protogen.masterdata.model.SScreensequence;
import fr.protogen.masterdata.model.SWidget;
import fr.protogen.masterdata.model.StepType;
import fr.protogen.masterdata.model.WorkflowDefinition;

public class SessionCache implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7682128108524599121L;
	private SApplication application;
	private List<SScreensequence> sequences = new ArrayList<SScreensequence>();
	private List<CBusinessClass> entities = new ArrayList<CBusinessClass>();
	private List<CAttribute> attributes = new ArrayList<CAttribute>();
	private List<CWindow> windows  = new ArrayList<CWindow>();
	private List<CActionbutton> actions  = new ArrayList<CActionbutton>();
	private List<CDocumentbutton> documents = new ArrayList<CDocumentbutton>();
	private List<DocumentParameter> parameters = new ArrayList<DocumentParameter>();
	private List<SProcess> processes = new ArrayList<SProcess>();
	private List<SMenuitem> menu = new ArrayList<SMenuitem>();
	private List<CoreRole> roles = new ArrayList<CoreRole>();
	private List<SWidget> widgets = new ArrayList<SWidget>();
	private List<CUIParameter> applicationParameters = new ArrayList<CUIParameter>();
	private List<SProcedure> procedures = new ArrayList<SProcedure>();
	private List<SResource> resources = new ArrayList<SResource>();
	private List<CGlobalValue> globalValues = new ArrayList<CGlobalValue>();
	private List<SAlert> alerts=new ArrayList<SAlert>();
	private List<SRubrique> rubriques = new ArrayList<SRubrique>();
	private String AppKey;
	
	private List<COrganization> organizations = new ArrayList<COrganization>();
	private List<CParameterMetamodel> parametersModels = new ArrayList<CParameterMetamodel>();
	
	private List<GOrganization> gorganizations = new ArrayList<GOrganization>();
	private List<GParametersPackage> models = new ArrayList<GParametersPackage>();
	private List<WorkflowDefinition> workflows = new ArrayList<WorkflowDefinition>();
	
	private List<CCallout> callouts = new ArrayList<CCallout>();
	private List<CWindowCallout> windowCallouts = new ArrayList<CWindowCallout>();

	public SApplication getApplication() {
		return application;
	}

	public void setApplication(SApplication application) {
		this.application = application;
	}

	public List<CBusinessClass> getEntities() {
		return entities;
	}

	public void setEntities(List<CBusinessClass> entities) {
		this.entities = entities;
	}

	public List<CAttribute> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<CAttribute> attributes) {
		this.attributes = attributes;
	}

	public List<SScreensequence> getSequences() {
		return sequences;
	}

	public void setSequences(List<SScreensequence> sequences) {
		this.sequences = sequences;
	}

	public List<CWindow> getWindows() {
		return windows;
	}

	public void setWindows(List<CWindow> windows) {
		this.windows = windows;
	}

	public List<CActionbutton> getActions() {
		return actions;
	}

	public void setActions(List<CActionbutton> actions) {
		this.actions = actions;
	}

	public List<CDocumentbutton> getDocuments() {
		return documents;
	}

	public void setDocuments(List<CDocumentbutton> documents) {
		this.documents = documents;
	}

	public List<DocumentParameter> getParameters() {
		return parameters;
	}

	public void setParameters(List<DocumentParameter> parameters) {
		this.parameters = parameters;
	}

	public List<SProcess> getProcesses() {
		return processes;
	}

	public void setProcesses(List<SProcess> processes) {
		this.processes = processes;
	}

	public String getAppKey() {
		return AppKey;
	}

	public void setAppKey(String appKey) {
		AppKey = appKey;
	}

	public List<SMenuitem> getMenu() {
		return menu;
	}

	public void setMenu(List<SMenuitem> menu) {
		this.menu = menu;
	}

	public List<CoreRole> getRoles() {
		return roles;
	}

	public void setRoles(List<CoreRole> roles) {
		this.roles = roles;
	}

	public List<SWidget> getWidgets() {
		return widgets;
	}

	public void setWidgets(List<SWidget> widgets) {
		this.widgets = widgets;
	}

	public List<StepType> getStepTypes() {
		// TODO Auto-generated method stub
		List<StepType> t = new ArrayList<StepType>();
		
		t.add(new StepType(1, "Fenêtre"));
		t.add(new StepType(2, "Saisie de paramétres"));
		t.add(new StepType(3, "Chargement d'un fichier par l'utilisateur"));
		t.add(new StepType(4, "Téléchargement d'un fichier"));
		t.add(new StepType(5, "Envoi d'un EMail"));
		t.add(new StepType(6, "Envoi d'un Fax"));
		t.add(new StepType(7, "Appel téléphonique"));
		t.add(new StepType(8, "Appel visio-conférence"));
				
		return t;
	}

	public List<CBusinessClass> getIndirectReferences(CBusinessClass mainEntity) {
		// TODO Auto-generated method stub
		ArrayList<CBusinessClass> entities = new ArrayList<CBusinessClass>();
		for(CAttribute a : attributes){
			if(a.getDataReference().equals("fk_"+mainEntity.getDataReference()) && a.isReference()){
				entities.add(a.getEntity());
			}
		}
		return entities;
	}
	
	public List<CUIParameter> getApplicationParameters() {
		return applicationParameters;
	}

	public void setApplicationParameters(List<CUIParameter> applicationParameters) {
		this.applicationParameters = applicationParameters;
	}

	public List<SProcedure> getProcedures() {
		return procedures;
	}

	public void setProcedures(List<SProcedure> procedures) {
		this.procedures = procedures;
	}

	public List<SResource> getResources() {
		return resources;
	}

	public void setResources(List<SResource> resources) {
		this.resources = resources;
	}

	public List<CGlobalValue> getGlobalValues() {
		return globalValues;
	}

	public void setGlobalValues(List<CGlobalValue> globalValues) {
		this.globalValues = globalValues;
	}

	public List<SAlert> getAlerts() {
		return alerts;
	}

	public void setAlerts(List<SAlert> alerts) {
		this.alerts = alerts;
	}

	public List<COrganization> getOrganizations() {
		return organizations;
	}

	public void setOrganizations(List<COrganization> organizations) {
		this.organizations = organizations;
	}

	public List<CParameterMetamodel> getParametersModels() {
		return parametersModels;
	}

	public void setParametersModels(List<CParameterMetamodel> parametersModels) {
		this.parametersModels = parametersModels;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public List<SRubrique> getRubriques() {
		return rubriques;
	}

	public void setRubriques(List<SRubrique> rubriques) {
		this.rubriques = rubriques;
	}

	public List<GOrganization> getGorganizations() {
		return gorganizations;
	}

	public void setGorganizations(List<GOrganization> gorganizations) {
		this.gorganizations = gorganizations;
	}

	public List<GParametersPackage> getModels() {
		return models;
	}

	public void setModels(List<GParametersPackage> models) {
		this.models = models;
	}

	public List<WorkflowDefinition> getWorkflows() {
		return workflows;
	}

	public void setWorkflows(List<WorkflowDefinition> workflows) {
		this.workflows = workflows;
	}

	public List<CCallout> getCallouts() {
		return callouts;
	}

	public void setCallouts(List<CCallout> callouts) {
		this.callouts = callouts;
	}

	public List<CWindowCallout> getWindowCallouts() {
		return windowCallouts;
	}

	public void setWindowCallouts(List<CWindowCallout> windowCallouts) {
		this.windowCallouts = windowCallouts;
	}

	

	
}
