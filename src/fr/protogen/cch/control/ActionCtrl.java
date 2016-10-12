package fr.protogen.cch.control;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.primefaces.event.RowEditEvent;



import fr.protogen.cch.cache.ApplicationRegistery;
import fr.protogen.cch.cache.SessionCache;
import fr.protogen.cch.configuration.Parameters;
import fr.protogen.cch.configuration.StringFormat;
import fr.protogen.cch.control.ui.beans.DocumentParameter;
import fr.protogen.cch.genration.GenerationService;
import fr.protogen.masterdata.model.*;

@ManagedBean
@SessionScoped
public class ActionCtrl implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<CWindow> windows = new ArrayList<CWindow>();
	private List<CAttribute> attributes = new ArrayList<CAttribute>();
	private List<CBusinessClass> entities = new ArrayList<CBusinessClass>();
	private List<CActionbutton> actions = new ArrayList<CActionbutton>();
	private String selectedEntity;
	private CBusinessClass paEntity = new CBusinessClass();
	private MPostAction postAction = new MPostAction();
	private String selectedType;
	private CAttribute selectedAttribute = new CAttribute();
	private String selectedScreen;
	private String plcScript;
	private String actionTitle;
	private String actionDescription;
	private CAttribute target;
	private SessionCache cache;
	private List<CBusinessClass> mtmTables = new ArrayList<CBusinessClass>();
	
	//	Parameters
	private String parameterLabel;
	private String parameterType;
	private List<DocumentParameter> parameters;
	private Boolean maintainmode;
	
	//	Bound document
	private int selectedDocument=0;
	private List<CDocumentbutton> documents=new ArrayList<CDocumentbutton>();
	
	//	Maintenance mode
	private CActionbutton toDelete;
	
	@PostConstruct
	public void construction(){
		CCHUser user = (CCHUser)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(Parameters.USER_TOKEN);
    	cache = ApplicationRegistery.getInstance().getSession(user);
    	windows = cache.getWindows();
    	entities = cache.getEntities();
    	documents = cache.getDocuments();
    	attributes = windows.get(0).getCAttributes();
    	paEntity = entities.get(0);
    	
    	maintainmode = (Boolean)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(Parameters.MAINTAIN_MODE);
		if(maintainmode){
			actions = cache.getActions();
		}
	}
	
	public void doDelete(){
		for(CWindow w : windows)
			if(w.getCActionbuttons().contains(toDelete))
				w.getCActionbuttons().remove(toDelete);
		
		cache.getActions().remove(toDelete);
		actions.remove(toDelete);
		
		if(maintainmode){
			GenerationService service = new GenerationService();
			ArrayList<CActionbutton> idActions = new ArrayList<CActionbutton>();
			idActions.add(toDelete);
			service.deleteActions(idActions);
		}
	}
	public void cancelDelete(){
		
	}
	public String constructDependencies(){
		return "";
	}
	
	public void addParameter(){
		if(parameters == null)
			parameters = new ArrayList<DocumentParameter>();
		
		DocumentParameter p = new DocumentParameter();
		p.setLabel(parameterLabel);
		p.setT(parameterType);
		if(parameterType.length()==1){
			switch(parameterType.charAt(0)){
			case 'I': p.setType("Entier");break;
			case 'F': p.setType("Nombre décimal");break;
			case 'D': p.setType("Date");break;
			}
		} else {
			//	reference to another entity
			p.setT(parameterType);
		}
		parameters.add(p);
		
		plcScript = plcScript+" <<"+p.getLabel()+">> ";
	}
	
	public String next(){
		CCHUser user = (CCHUser)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(Parameters.USER_TOKEN);
    	SessionCache cache = ApplicationRegistery.getInstance().getSession(user);
    	cache.setActions(actions);
		return "documents";
	}

	public void saveAction(ActionEvent evt){
		
		List<String> defaultvalues = new ArrayList<String>();
		for(CAttribute a : paEntity.getAttributes())
			defaultvalues.add(a.getDtoValue());
		for(CBusinessClass e : mtmTables)
			for(CAttribute a : e.getAttributes())
				defaultvalues.add(a.getDtoValue());
		
		MAction action = new MAction(0, actionTitle, plcScript);
		MPostactionType type = new MPostactionType();
		type.setId(Integer.parseInt(selectedType));
		MPostAction postaction = new MPostAction();
		postaction.setAttributes(paEntity.getAttributes());
		for(CBusinessClass e : mtmTables)
			postaction.getAttributes().addAll(e.getAttributes());
		postaction.setPostAction(actionTitle);
		postaction.setType(type);
		postaction.setDefaultValues(defaultvalues);
		action.setPostAction(postaction);
		
		
		
		CWindow window = new CWindow();
		for(CWindow w : windows){
			if(w.getTitle().equals(selectedScreen))
				window = w;
		}
		CActionbutton btn = new CActionbutton(0, window, action, actionTitle);
		btn.setDescription(actionDescription);
		actions.add(btn);
		btn.setParameters("");
		CCHUser user = (CCHUser)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(Parameters.USER_TOKEN);
    	SessionCache cache = ApplicationRegistery.getInstance().getSession(user);
    	if(cache.getParameters() == null)
    		cache.setParameters(new ArrayList<DocumentParameter>());
    	if(parameters == null)
    		parameters= new ArrayList<DocumentParameter>();
		for(DocumentParameter p : parameters){
			if(plcScript.contains("<<"+p.getLabel()+">>")){
				cache.getParameters().add(p);
				btn.setParameters(btn.getParameters()+";"+StringFormat.getInstance().parameterFormat(p.getLabel())+":"+p.getT()+":"+p.getLabel());
			}
		}
		if(btn.getParameters() != null && btn.getParameters().length()>0)
			btn.setParameters(btn.getParameters().substring(1));
		for(CAttribute a : paEntity.getAttributes()){
			
			if(a.getDtoValue()==null || a.getDtoValue().length()==0)
				continue;
			DocumentParameter p = new DocumentParameter();
			p.setLabel(a.getDtoValue());
			switch(a.getCAttributetype().getId()){
				case 1:p.setT("I");p.setType("Entier");break;
				case 3:p.setT("D");p.setType("Date");break;
				case 4:p.setT("F");p.setType("Nombre à décimale");break;
			}
			cache.getParameters().add(p);
		}
		
		//		Binding a document
		if(selectedDocument>0){
			btn.setBound(true);
			for(CDocumentbutton b: documents)
				if(b.getId() == selectedDocument){
					btn.setBoundDocument(b);
					break;
				}
		}
		
		if(maintainmode){
			GenerationService service = new GenerationService();
			service.addNewAction(btn,cache);
		}
		
		parameters = new ArrayList<DocumentParameter>();
		parameterLabel="";
		actionTitle = "";
		plcScript="";
		target = null;
	}
	
	

	public void addAttribute(ActionEvent evt){
		if(selectedAttribute == null)
			return;
		String arg = "<<"+selectedAttribute.getAttribute()+">>";
		plcScript = plcScript+" "+arg+" ";
	}
	
	public void screenChanged(){
		String newValue=selectedScreen;
		for(CWindow w : windows){
			if(w.getTitle().equals(newValue)){
				attributes = w.getCAttributes();
				break;
			}
		}
	}
	
	
	
	public void paEntityChanged(){
		
		for(CBusinessClass e : entities){
			
			if(e.getDataReference().equals(selectedEntity)){

				paEntity = e;
				mtmTables = new ArrayList<CBusinessClass>();
				for(CAttribute a : paEntity.getAttributes()){
					if(a.isMultiple()){
						String dref = a.getDataReference().split("__")[0].substring(3);
						for(CBusinessClass en : cache.getEntities())
							if(en.getDataReference().equals(dref)){
								mtmTables.add(en);
								break;
							}
					}
				}
				
				return;
			}
		}
	}
	
	public void onRowEdit(RowEditEvent evt){
		
	}
	
	/*
	 * 	Getters and setters
	 */
	public String getActionTitle() {
		return actionTitle;
	}

	public void setActionTitle(String actionTitle) {
		this.actionTitle = actionTitle;
	}
	public List<CWindow> getWindows() {
		return windows;
	}

	public void setWindows(List<CWindow> windows) {
		this.windows = windows;
	}

	public List<CAttribute> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<CAttribute> attributes) {
		this.attributes = attributes;
	}

	public List<CBusinessClass> getEntities() {
		return entities;
	}

	public void setEntities(List<CBusinessClass> entities) {
		this.entities = entities;
	}

	public String getSelectedScreen() {
		return selectedScreen;
	}

	public void setSelectedScreen(String selectedScreen) {
		this.selectedScreen = selectedScreen;
	}

	public String getPlcScript() {
		return plcScript;
	}

	public void setPlcScript(String plcScript) {
		this.plcScript = plcScript;
	}

	public CAttribute getSelectedAttribute() {
		return selectedAttribute;
	}

	public void setSelectedAttribute(CAttribute selectedAttribute) {
		this.selectedAttribute = selectedAttribute;
	}

	public MPostAction getPostAction() {
		return postAction;
	}

	public void setPostAction(MPostAction postAction) {
		this.postAction = postAction;
	}

	public String getSelectedEntity() {
		return selectedEntity;
	}

	public void setSelectedEntity(String selectedEntity) {
		this.selectedEntity = selectedEntity;
	}

	public String getSelectedType() {
		return selectedType;
	}

	public void setSelectedType(String selectedType) {
		this.selectedType = selectedType;
	}

	public CBusinessClass getPaEntity() {
		return paEntity;
	}

	public void setPaEntity(CBusinessClass paEntity) {
		this.paEntity = paEntity;
	}

	public List<CActionbutton> getActions() {
		return actions;
	}

	public void setActions(List<CActionbutton> actions) {
		this.actions = actions;
	}

	public String getParameterLabel() {
		return parameterLabel;
	}

	public void setParameterLabel(String parameterLabel) {
		this.parameterLabel = parameterLabel;
	}

	public String getParameterType() {
		return parameterType;
	}

	public void setParameterType(String parameterType) {
		this.parameterType = parameterType;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public List<DocumentParameter> getParameters() {
		return parameters;
	}

	public void setParameters(List<DocumentParameter> parameters) {
		this.parameters = parameters;
	}

	public CAttribute getTarget() {
		return target;
	}

	public void setTarget(CAttribute target) {
		this.target = target;
	}

	public CActionbutton getToDelete() {
		return toDelete;
	}

	public void setToDelete(CActionbutton toDelete) {
		this.toDelete = toDelete;
	}

	public List<CBusinessClass> getMtmTables() {
		return mtmTables;
	}

	public void setMtmTables(List<CBusinessClass> mtmTables) {
		this.mtmTables = mtmTables;
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

	public int getSelectedDocument() {
		return selectedDocument;
	}

	public void setSelectedDocument(int selectedDocument) {
		this.selectedDocument = selectedDocument;
	}

	public List<CDocumentbutton> getDocuments() {
		return documents;
	}

	public void setDocuments(List<CDocumentbutton> documents) {
		this.documents = documents;
	}

	public String getActionDescription() {
		return actionDescription;
	}

	public void setActionDescription(String actionDescription) {
		this.actionDescription = actionDescription;
	}
}
