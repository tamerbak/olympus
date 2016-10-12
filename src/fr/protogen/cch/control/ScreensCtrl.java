package fr.protogen.cch.control;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.primefaces.event.FlowEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import fr.protogen.cch.cache.ApplicationRegistery;
import fr.protogen.cch.cache.SessionCache;
import fr.protogen.cch.configuration.Parameters;
import fr.protogen.cch.control.ui.beans.UITreeNode;
import fr.protogen.cch.genration.GenerationService;
import fr.protogen.masterdata.model.CActionbutton;
import fr.protogen.masterdata.model.CAttribute;
import fr.protogen.masterdata.model.CBusinessClass;
import fr.protogen.masterdata.model.CCHUser;
import fr.protogen.masterdata.model.CDocumentbutton;
import fr.protogen.masterdata.model.CGlobalValue;
import fr.protogen.masterdata.model.CWindow;
import fr.protogen.masterdata.model.CWindowtype;
import fr.protogen.masterdata.model.SMenuitem;
import fr.protogen.masterdata.model.SProcess;
import fr.protogen.masterdata.model.SScreensequence;

@ManagedBean
@SessionScoped
public class ScreensCtrl {
	private List<CBusinessClass> entities;
	private List<SScreensequence> functions;
	private List<CAttribute> attributes;
	private List<CWindow> screens;
	private String screenTitle;
	private String screenDescription;
	private String selectedEntity;
	private String selectedFunction;
	private String screenType;
	private List<UITreeNode> nodes;
	private List<UITreeNode> crNodes;
	private TreeNode root;
	private TreeNode crRoot;
	private List<String> visibleFields=new ArrayList<String>();
	
	//	Global values
	private List<CGlobalValue> globalValues;
	private CGlobalValue[] selectedGValues;
	private String gvKey;
	private String gvLabel;
	private String gvValue;
	
	//	Recall fields
	private String selectedRappelReference;
	private List<CAttribute> attributeReferences = new ArrayList<CAttribute>();
	
	//	Maintnenance mode
	private CWindow toDelete = new CWindow();
	private CWindow toLoad = new CWindow();
	private List<String> actions;
	private List<String> documents;
	private List<String> processes;
    private SessionCache cache;
	private Boolean maintainmode;
    
	//	Options
	private boolean deleteBtn=true;
	private boolean updateBtn=true;
	
	//	Forced filters
	private boolean screenLock;
	private List<String> selectedFilters;
	private List<String> filters;
	
	//	Constraints
	private String selectConstraint="";
	
	@PostConstruct
	public void construction(){
		CCHUser user = (CCHUser)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(Parameters.USER_TOKEN);
    	cache = ApplicationRegistery.getInstance().getSession(user);
   	
		//	Load entities
		entities = cache.getEntities();
		setAttributes(cache.getAttributes());
		
		//	Load sequences
		functions = cache.getSequences();
		
		screens = new ArrayList<CWindow>();
		nodes = new ArrayList<UITreeNode>();
		
		maintainmode = (Boolean)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(Parameters.MAINTAIN_MODE);
		if(maintainmode){
			screens = cache.getWindows();
			globalValues = cache.getGlobalValues();
		}
	}
	
	public void addGlobalValue(){
		CGlobalValue v = new CGlobalValue();
		v.setKey(gvKey);
		v.setLabel(gvLabel);
		v.setValue(gvValue);
		cache.getGlobalValues().add(v);
		if(maintainmode){
			GenerationService engine = new GenerationService();
			v.setAppKey(cache.getAppKey());
			engine.addNewGValue(v);
		}
	}
	
	public String loadWindow(){
		selectedFunction = toLoad.getFunction().getTitle();
		screenType =""+ toLoad.getCWindowtype().getId();
		selectedEntity = toLoad.getMainEntity();
		screenDescription = toLoad.getStepDescription();
		screenTitle = toLoad.getTitle();
		return "newscreen";
	}
	
	public void doLoadWindow(){
		
		if(toLoad==null)
			return;
		
		for(CWindow w : screens)
			if(w.getId()==toLoad.getId()){
				toLoad = w;
				break;
			}
		
		try {
			FacesContext.getCurrentInstance().getExternalContext().redirect(loadWindow()+".xhtml");
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}
	
	public String constructDependencies(){
		documents = new ArrayList<String>();
		actions = new ArrayList<String>();
		processes = new ArrayList<String>();
		
		for(CActionbutton a : toDelete.getCActionbuttons()){
			actions.add(a.getTitle());
		}
		for(CDocumentbutton d : toDelete.getCDocumentbuttons()){
			documents.add(d.getTitle());
		}
		for(SProcess p : cache.getProcesses()){
			for(CWindow pw : p.getWindows())
				if(pw.getId() == toDelete.getId())
					processes.add(p.getTitle());
		}
		return "";
		
	}
	
	public void doDelete(){
		screens.remove(toDelete);
		CWindow w = toDelete;
		
		for(CActionbutton a : w.getCActionbuttons()){
			cache.getActions().remove(a);
		}
		for(CDocumentbutton d : w.getCDocumentbuttons()){
			cache.getDocuments().remove(d);
		}
		for(SProcess p : cache.getProcesses()){
			for(CWindow pw : p.getWindows())
				if(pw.getId() == w.getId())
					cache.getProcesses().remove(p);
		}
		cache.getWindows().remove(toDelete);
		if(maintainmode){
			GenerationService service = new GenerationService();
			List<CWindow> idWindows = new ArrayList<CWindow>();
			idWindows.add(toDelete);
			service.deleteWindows(idWindows);
		}
	}
	
	public void cancelDelete(){
		
	}
	
	@SuppressWarnings("unused")
	public String initialize(FlowEvent event){
		
		if(event == null){
			return "generalscreen";
		}
		if(event.getNewStep().equals("orderfields")){
			visibleFields = new ArrayList<String>();
			for(int i = 1 ; i < nodes.size() ; i++) {
				
				UITreeNode n = nodes.get(i);
				if(!n.isVisible())
					continue;
				CAttribute attribute = n.getAttribute();
				
				visibleFields.add(attribute.getAttribute());
			}
			for(int i = 1 ; i < crNodes.size() ; i++) {
				
				UITreeNode n = crNodes.get(i);
				if(!n.isVisible())
					continue;
				CAttribute attribute = n.getAttribute();
				
				visibleFields.add(attribute.getAttribute());
			}
			return event.getNewStep();
		}
		
		if(event.getOldStep().equals("entities") && event.getNewStep().equals("crossreference")){	//	Cross reference tables
			crRoot=new DefaultTreeNode("root",null);
			crNodes = new ArrayList<UITreeNode>();
			CBusinessClass mainEntity = getMainEntity(selectedEntity, entities, attributes);
			List<CBusinessClass> indirectEntities = cache.getIndirectReferences(mainEntity);
			for(CBusinessClass e : indirectEntities){
				UITreeNode n = new UITreeNode(e);
				crNodes.add(n);
				TreeNode mainnode = new DefaultTreeNode(n, crRoot);
				
				for(CAttribute ca : e.getAttributes()){
					if(!ca.isMultiple()){
						UITreeNode subnode = new UITreeNode(ca);
						crNodes.add(subnode);
						TreeNode sn = new DefaultTreeNode(subnode,mainnode);
					} else {
						CBusinessClass subentity = null;
						String datareference;
						datareference = ca.getDataReference().split("__")[0].substring(3);
						subentity = getSubEntity(datareference,entities,attributes);
						UITreeNode cn = new UITreeNode(subentity);
						cn.setAttribute(ca);
						crNodes.add(cn);
						cn.setMtm(true);
						TreeNode child = new DefaultTreeNode(cn, mainnode);
						for(CAttribute cca : subentity.getAttributes()){
							UITreeNode subnode = new UITreeNode(cca);
							subnode.setMtm(true);
							crNodes.add(subnode);
							TreeNode sn = new DefaultTreeNode(subnode,child);
						}
					}
				}
			}
			return event.getNewStep();
		}
		
		
		if(event.getOldStep().equals("crossreference") && event.getNewStep().equals("locktab")){
			filters = new ArrayList<String>();
			for(int i = 1 ; i < nodes.size() ; i++) {
				UITreeNode n = nodes.get(i);
				if(n.isFilterEnabled()){
					filters.add(n.getAttribute().getAttribute());					
				}
			}
			for(int i = 1 ; i < crNodes.size() ; i++) {
				UITreeNode n = crNodes.get(i);
				if(n.isFilterEnabled()){
					filters.add(n.getAttribute().getAttribute());					
				}
			}
		}
		
		
		if(!event.getOldStep().equals("generalscreen") || !event.getNewStep().equals("entities"))
			return event.getNewStep();
		//	Construct tree nodes
		try{
			root = new DefaultTreeNode("root",null);
			CBusinessClass mainEntity = getMainEntity(selectedEntity, entities, attributes);
			UITreeNode node = new UITreeNode(mainEntity);
			node.setVisible(true);
			nodes = new ArrayList<UITreeNode>();
			nodes.add(node);
			TreeNode mainnode = new DefaultTreeNode(node, root);
			attributeReferences = new ArrayList<CAttribute>();
			for(CAttribute a : mainEntity.getAttributes()){
				if(!a.getDataReference().startsWith("fk_")){
					UITreeNode n = new UITreeNode(a);
					n.setVisible(true);
					nodes.add(n);
					TreeNode child = new DefaultTreeNode(n, mainnode);
				} else {
					attributeReferences.add(a);
					CBusinessClass subentity = null;
					String datareference;
					if(!a.isMultiple())
						datareference = a.getDataReference().substring(3);
					else
						datareference = a.getDataReference().split("__")[0].substring(3);
					subentity = getSubEntity(datareference,entities,attributes);
					UITreeNode n = new UITreeNode(subentity);
					n.setAttribute(a);
					n.setVisible(true);
					n.setReference(a.isReference());
					nodes.add(n);
					TreeNode child = new DefaultTreeNode(n, mainnode);
					for(CAttribute ca : subentity.getAttributes()){
						UITreeNode subnode = new UITreeNode(ca);
						nodes.add(subnode);
						TreeNode sn = new DefaultTreeNode(subnode,child);
					}
				}
			}
		}catch(Exception exc){
			exc.printStackTrace();
		}
		return event.getNewStep();
	}
	
	private CBusinessClass getSubEntity(String selectedEntity,
			List<CBusinessClass> entities, List<CAttribute> attributes) {
		
		CBusinessClass mainEntity = null;
		
		//	Select entity
		for(CBusinessClass e : entities){
			if(e.getDataReference().equals(selectedEntity)){
				mainEntity = e;
				break;
			}
		}
		
		if(mainEntity == null)
			return null;
		
		//	Load with attributes 
		mainEntity.setAttributes(new ArrayList<CAttribute>());
		for(CAttribute a : attributes){
			if(a.getEntity().getDataReference().equals(selectedEntity))
				mainEntity.getAttributes().add(a);
		}
		return mainEntity;	
	}

	private CBusinessClass getMainEntity(String selectedEntity,
			List<CBusinessClass> entities, List<CAttribute> attributes) {
		
		
		CBusinessClass mainEntity = null;
		
		//	Select entity
		for(CBusinessClass e : entities){
			if(e.getDataReference().equals(selectedEntity)){
				mainEntity = e;
				break;
			}
		}
		
		if(mainEntity == null)
			return null;
		
		//	Load with attributes 
		mainEntity.setAttributes(new ArrayList<CAttribute>());
		for(CAttribute a : attributes){
			if(a.getEntity().getDataReference().equals(selectedEntity))
				mainEntity.getAttributes().add(a);
		}
		return mainEntity;
	}

	public void saveScreen(ActionEvent event){
		
		CWindow screen = new CWindow();
		
		SScreensequence sequence = new SScreensequence();
		for(SScreensequence s : functions){
			if(s.getTitle().equals(selectedFunction)){
				sequence = s;
				break;
			}
		}
		
		CBusinessClass entity = new CBusinessClass();
		for(CBusinessClass e : cache.getEntities()){
			if(e.getDataReference().equals(selectedEntity)){
				entity = e;
				break;
			}
		}
		
		screen.setFunction(sequence);
		screen.setCWindowtype(new CWindowtype(Integer.parseInt(screenType),""));
		screen.setMainEntity(selectedEntity);
		screen.setStepDescription(screenDescription);
		screen.setTitle(screenTitle);
		screen.setCAttributes(new ArrayList<CAttribute>());
		
		for(int i = 1 ; i < nodes.size() ; i++) {
			UITreeNode n = nodes.get(i);
			CAttribute attribute = cloneAttribute(n.getAttribute());
			attribute.setReference(n.isReference());
			attribute.setVisible(n.isVisible());
			attribute.setRappel(n.isRappel());
			attribute.setFilterEnabled(n.isFilterEnabled());
			screen.getCAttributes().add(attribute);
			
			if(selectedFilters != null && selectedFilters.size()>0 && screenLock){
				attribute.setFilterMandatory(false);
				for(String f : selectedFilters){
					if(f.equals(attribute.getAttribute())){
						attribute.setFilterMandatory(true);
						break;
					}
				}
			}
			
			screen.setForceFilter(screenLock);
			
			for(int j = 0; j < visibleFields.size();j++){
				String dr = visibleFields.get(j);
				if(attribute.getAttribute().equals(dr)){
					attribute.setDispalyOrder(j+1);
					break;
				}
			}
		}
		
		for(int i=0 ; i< crNodes.size() ; i++){
			
			UITreeNode n = crNodes.get(i);
			
			if(!n.isVisible() || n.getAttribute() == null)
				continue;
			CAttribute attribute = cloneAttribute(n.getAttribute());
			attribute.setVisible(n.isVisible());
			attribute.setIndirectFunction(n.getIndirectFunction());
			attribute.setFilterEnabled(n.isFilterEnabled());
			if(attribute.getIndirectFunction()>1){
				attribute.setIndirectMtmKey(n.isReference());
			}
			
			attribute.setIndirectReference(true);
			if(n.isMtm()){
				if(n.isReference())
					attribute.setIndirectMtmKey(true);
				else
					attribute.setIndirectMtmValue(true);
			}
			
			if(selectedFilters != null && selectedFilters.size()>0 && screenLock){
				attribute.setFilterMandatory(false);
				for(String f : selectedFilters){
					if(f.equals(attribute.getAttribute())){
						attribute.setFilterMandatory(true);
						break;
					}
				}
			}
			
			screen.getCAttributes().add(attribute);
			
			
			for(int j = 0; j < visibleFields.size();j++){
				String dr = visibleFields.get(j);
				if(attribute.getAttribute().equals(dr)){
					attribute.setDispalyOrder(j+1);
					break;
				}
			}
		}
		
		//	Global values
		if(selectedGValues!= null && selectedGValues.length>0){
			screen.setGlobalValues(new ArrayList<CGlobalValue>());
			for(CGlobalValue v : selectedGValues)
				screen.getGlobalValues().add(v);
		}
		
		//	Recall
		if(!selectedRappelReference.equals("NONE")){
			
			for(CAttribute a : screen.getCAttributes()){
				if(a.getDataReference().equals(selectedRappelReference)){
					screen.setRappelReference(a);
					break;
				}
			}
				
		}
		screen.setSelectConstraints(selectConstraint);
		screen.setUpdateBtn(updateBtn);
		screen.setDeleteBtn(deleteBtn);
		if(toLoad==null || toLoad.getTitle()==null)
			screens.add(screen);
		
		if(maintainmode){
			GenerationService service = new GenerationService();
			if(toLoad==null || toLoad.getTitle()==null){
				service.addNewScreen(screen,cache,entity);
			}
			else
				service.updateScreen(screen,toLoad,cache,entity);
		}
		
		toLoad=new CWindow();
		
		screenTitle="";
		screenDescription = "";
		updateBtn=true;
		deleteBtn=true;
        
	}
	
	private CAttribute cloneAttribute(CAttribute attribute) {
		
		CAttribute a = new CAttribute();
		a.setAttribute(attribute.getAttribute());
		a.setCalculated(attribute.isCalculated());
		a.setCAttributetype(attribute.getCAttributetype());
		a.setDataReference(attribute.getDataReference());
		a.setDescription(attribute.getDescription());
		a.setEntity(attribute.getEntity());
		a.setFormula(attribute.getFormula());
		a.setMandatory(attribute.isMandatory());
		a.setTypeLabel(attribute.getTypeLabel());
		a.setId(attribute.getId());
		return a;
	}

	public String next(){
		CCHUser user = (CCHUser)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(Parameters.USER_TOKEN);
    	SessionCache cache = ApplicationRegistery.getInstance().getSession(user);
    	cache.setWindows(screens);
		return "actions";
	}
	
	/*
	 * 	Getters and setters
	 */

	public List<CWindow> getWindows() {
		return screens;
	}

	public void setWindows(List<CWindow> windows) {
		this.screens = windows;
	}

	public String getSelectedFunction() {
		return selectedFunction;
	}

	public void setSelectedFunction(String selectedFunction) {
		this.selectedFunction = selectedFunction;
	}

	public List<SScreensequence> getFunctions() {
		return functions;
	}

	public void setFunctions(List<SScreensequence> functions) {
		this.functions = functions;
	}

	public List<CBusinessClass> getEntities() {
		return entities;
	}

	public void setEntities(List<CBusinessClass> entities) {
		this.entities = entities;
	}

	public String getScreenTitle() {
		return screenTitle;
	}

	public void setScreenTitle(String screenTitle) {
		this.screenTitle = screenTitle;
	}

	public String getScreenDescription() {
		return screenDescription;
	}

	public void setScreenDescription(String screenDescription) {
		this.screenDescription = screenDescription;
	}

	public String getSelectedEntity() {
		return selectedEntity;
	}

	public void setSelectedEntity(String selectedEntity) {
		this.selectedEntity = selectedEntity;
	}

	public String getScreenType() {
		return screenType;
	}

	public void setScreenType(String screenType) {
		this.screenType = screenType;
	}

	public List<CAttribute> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<CAttribute> attributes) {
		this.attributes = attributes;
	}

	public List<CWindow> getScreens() {
		return screens;
	}

	public void setScreens(List<CWindow> screens) {
		this.screens = screens;
	}
	
	public List<UITreeNode> getNodes() {
		return nodes;
	}

	public void setNodes(List<UITreeNode> nodes) {
		this.nodes = nodes;
	}

	public TreeNode getRoot() {
		return root;
	}

	public void setRoot(TreeNode root) {
		this.root = root;
	}

	public CWindow getToDelete() {
		return toDelete;
	}

	public void setToDelete(CWindow toDelete) {
		this.toDelete = toDelete;
	}

	public List<String> getActions() {
		return actions;
	}

	public void setActions(List<String> actions) {
		this.actions = actions;
	}

	public List<String> getDocuments() {
		return documents;
	}

	public void setDocuments(List<String> documents) {
		this.documents = documents;
	}

	public List<String> getProcesses() {
		return processes;
	}

	public void setProcesses(List<String> processes) {
		this.processes = processes;
	}

	public SessionCache getCache() {
		return cache;
	}

	public void setCache(SessionCache cache) {
		this.cache = cache;
	}

	public CWindow getToLoad() {
		return toLoad;
	}

	public void setToLoad(CWindow toLoad) {
		this.toLoad = toLoad;
	}

	public List<String> getVisibleFields() {
		return visibleFields;
	}

	public void setVisibleFields(List<String> visibleFields) {
		this.visibleFields = visibleFields;
	}

	public List<CGlobalValue> getGlobalValues() {
		return globalValues;
	}

	public void setGlobalValues(List<CGlobalValue> globalValues) {
		this.globalValues = globalValues;
	}

	public CGlobalValue[] getSelectedGValues() {
		return selectedGValues;
	}

	public void setSelectedGValues(CGlobalValue[] selectedGValues) {
		this.selectedGValues = selectedGValues;
	}

	public String getGvKey() {
		return gvKey;
	}

	public void setGvKey(String gvKey) {
		this.gvKey = gvKey;
	}

	public String getGvLabel() {
		return gvLabel;
	}

	public void setGvLabel(String gvLabel) {
		this.gvLabel = gvLabel;
	}

	public String getGvValue() {
		return gvValue;
	}

	public void setGvValue(String gvValue) {
		this.gvValue = gvValue;
	}

	public Boolean getMaintainmode() {
		return maintainmode;
	}

	public void setMaintainmode(Boolean maintainmode) {
		this.maintainmode = maintainmode;
	}

	public String getSelectedRappelReference() {
		return selectedRappelReference;
	}

	public void setSelectedRappelReference(String selectedRappelReference) {
		this.selectedRappelReference = selectedRappelReference;
	}

	public List<CAttribute> getAttributeReferences() {
		return attributeReferences;
	}

	public void setAttributeReferences(List<CAttribute> attributeReferences) {
		this.attributeReferences = attributeReferences;
	}

	public TreeNode getCrRoot() {
		return crRoot;
	}

	public void setCrRoot(TreeNode crRoot) {
		this.crRoot = crRoot;
	}

	public List<UITreeNode> getCrNodes() {
		return crNodes;
	}

	public void setCrNodes(List<UITreeNode> crNodes) {
		this.crNodes = crNodes;
	}

	public boolean isDeleteBtn() {
		return deleteBtn;
	}

	public void setDeleteBtn(boolean deleteBtn) {
		this.deleteBtn = deleteBtn;
	}

	public boolean isUpdateBtn() {
		return updateBtn;
	}

	public void setUpdateBtn(boolean updateBtn) {
		this.updateBtn = updateBtn;
	}

	public boolean isScreenLock() {
		return screenLock;
	}

	public void setScreenLock(boolean screenLock) {
		this.screenLock = screenLock;
	}

	public List<String> getSelectedFilters() {
		return selectedFilters;
	}

	public void setSelectedFilters(List<String> selectedFilters) {
		this.selectedFilters = selectedFilters;
	}

	public List<String> getFilters() {
		return filters;
	}

	public void setFilters(List<String> filters) {
		this.filters = filters;
	}

	public String getSelectConstraint() {
		return selectConstraint;
	}

	public void setSelectConstraint(String selectConstraint) {
		this.selectConstraint = selectConstraint;
	}
}
