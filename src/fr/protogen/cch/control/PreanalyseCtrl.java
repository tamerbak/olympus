package fr.protogen.cch.control;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import fr.protogen.cch.cache.ApplicationRegistery;
import fr.protogen.cch.cache.SessionCache;
import fr.protogen.cch.configuration.Parameters;
import fr.protogen.cch.control.ui.beans.PreAnalysisElement;
import fr.protogen.cch.control.ui.beans.UITreeNode;
import fr.protogen.masterdata.model.CActionbutton;
import fr.protogen.masterdata.model.CAttribute;
import fr.protogen.masterdata.model.CBusinessClass;
import fr.protogen.masterdata.model.CCHUser;
import fr.protogen.masterdata.model.CDocumentbutton;
import fr.protogen.masterdata.model.CWindow;
import fr.protogen.masterdata.model.CoreRole;
import fr.protogen.masterdata.model.SMenuitem;
import fr.protogen.masterdata.model.SProcedure;
import fr.protogen.masterdata.model.SScreensequence;
import fr.protogen.masterdata.model.SWidget;

@ManagedBean
@SessionScoped
public class PreanalyseCtrl {

	private String applicationTitle;
	private String functionTitle;
	private String functionDescription;
	private List<PreAnalysisElement> functions=new ArrayList<PreAnalysisElement>();
	private String entityTitle;
	private String entityDescription;
	private List<PreAnalysisElement> entities=new ArrayList<PreAnalysisElement>();
	private String windowTitle;
	private String windowDescription;
	private List<PreAnalysisElement> windows=new ArrayList<PreAnalysisElement>();
	private String actionTitle;
	private String actionDescription;
	private List<PreAnalysisElement> actions=new ArrayList<PreAnalysisElement>();
	private String documentTitle;
	private String documentDescription;
	private List<PreAnalysisElement> documents=new ArrayList<PreAnalysisElement>();
	private String processTitle;
	private String processDescription;
	private List<PreAnalysisElement> processes=new ArrayList<PreAnalysisElement>();
	private String roleTitle;
	private String roleDescription;
	private List<PreAnalysisElement> roles=new ArrayList<PreAnalysisElement>();
	private String descriptionToShow;
	
	private SessionCache cache;
	private TreeNode root;
	
	@PostConstruct
	public void initialize(){
		CCHUser user = (CCHUser)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(Parameters.USER_TOKEN);
    	cache = ApplicationRegistery.getInstance().getSession(user);
	}
	
	public String next(){
		return "application";
	}
	
	public void addFunctionTitle(){
		PreAnalysisElement e = new PreAnalysisElement(functionTitle,functionDescription);
		functions.add(e);
	}
	public void addEntity(){
		PreAnalysisElement e = new PreAnalysisElement(entityTitle,entityDescription);
		entities.add(e);
	}
	public void addWindow(){
		PreAnalysisElement e = new PreAnalysisElement(windowTitle,windowDescription);
		windows.add(e);
	}
	public void addAction(){
		PreAnalysisElement e = new PreAnalysisElement(actionTitle,actionDescription);
		actions.add(e);
	}
	public void addDocument(){
		PreAnalysisElement e = new PreAnalysisElement(documentTitle,documentDescription);
		documents.add(e);
	}
	public void addProcess(){
		PreAnalysisElement e = new PreAnalysisElement(processTitle,processDescription);
		processes.add(e);
	}
	public void addRole(){
		PreAnalysisElement e = new PreAnalysisElement(roleTitle,roleDescription);
		roles.add(e);
	}
	
	
	@SuppressWarnings("unused")
	public void loadRoadMap(){
		root = new DefaultTreeNode(new UITreeNode(cache.getApplication().getProjectName(),"ui-icon-power"),null);
		
		TreeNode rf = new DefaultTreeNode(new UITreeNode("Rubriques fonctionnelles","ui-icon-folder-collapsed"),root);
		for(SScreensequence f : cache.getSequences()){
			TreeNode n = new DefaultTreeNode(new UITreeNode(f.getTitle(),"ui-icon-document"),rf);
		}
		
		TreeNode en = new DefaultTreeNode(new UITreeNode("Entités et champs","ui-icon-disk"),root);
		for(CBusinessClass c : cache.getEntities()){
			TreeNode e = new DefaultTreeNode(new UITreeNode(c.getName(),"ui-icon-disk"),en);
			for(CAttribute a : c.getAttributes()){
				TreeNode f = new DefaultTreeNode(new UITreeNode(a.getAttribute(),"ui-icon-document-b"),e);
			}
		}
		
		TreeNode win = new DefaultTreeNode(new UITreeNode("Ecrans","ui-icon-newwin"),root);
		for(CWindow w : cache.getWindows()){
			TreeNode n = new DefaultTreeNode(new UITreeNode(w.getTitle(),"ui-icon-newwin"),win);
			for(CActionbutton b : w.getCActionbuttons()){
				TreeNode ab = new DefaultTreeNode(new UITreeNode(b.getTitle(),"ui-icon-gear"),n);
			}
			for(CDocumentbutton b : w.getCDocumentbuttons()){
				TreeNode ab = new DefaultTreeNode(new UITreeNode(b.getTitle(),"ui-icon-print"),n);
			}
		}
		
		TreeNode prc = new DefaultTreeNode(new UITreeNode("Procédures","ui-icon-gear"),root);
		for(SProcedure p : cache.getProcedures()){
			TreeNode n = new DefaultTreeNode(new UITreeNode(p.getTitle(),"ui-icon-gear"),prc);
		}
		
		TreeNode menu = new DefaultTreeNode(new UITreeNode("Menu","ui-icon-arrow-4"),root);
		for(SMenuitem p : cache.getMenu()){
			TreeNode n = new DefaultTreeNode(new UITreeNode(p.getTitle(),"ui-icon-extlink"),menu);
		}
		
		TreeNode roles = new DefaultTreeNode(new UITreeNode("Rôles","ui-icon-locked"),root);
		for(CoreRole p : cache.getRoles()){
			TreeNode n = new DefaultTreeNode(new UITreeNode(p.getRole(),"ui-icon-person"),roles);
		}
		
		TreeNode widgets = new DefaultTreeNode(new UITreeNode("Tableau de bord","ui-icon-lightbulb"),root);
		for(SWidget p : cache.getWidgets()){
			TreeNode n = new DefaultTreeNode(new UITreeNode(p.getTitle(),"ui-icon-lightbulb"),widgets);
		}
		
	}
	
	public String logout(){
		HttpSession session = (HttpSession)FacesContext.getCurrentInstance().getExternalContext().getSession(true);
		session.invalidate();
		
		return "login.xhtml";
	}
	
	/*
	 * 	Getters and setters
	 */

	public String getApplicationTitle() {
		return applicationTitle;
	}

	public void setApplicationTitle(String applicationTitle) {
		this.applicationTitle = applicationTitle;
	}

	public String getFunctionTitle() {
		return functionTitle;
	}

	public void setFunctionTitle(String functionTitle) {
		this.functionTitle = functionTitle;
	}

	public String getFunctionDescription() {
		return functionDescription;
	}

	public void setFunctionDescription(String functionDescription) {
		this.functionDescription = functionDescription;
	}

	public List<PreAnalysisElement> getFunctions() {
		return functions;
	}

	public void setFunctions(List<PreAnalysisElement> functions) {
		this.functions = functions;
	}

	public String getEntityTitle() {
		return entityTitle;
	}

	public void setEntityTitle(String entityTitle) {
		this.entityTitle = entityTitle;
	}

	public String getEntityDescription() {
		return entityDescription;
	}

	public void setEntityDescription(String entityDescription) {
		this.entityDescription = entityDescription;
	}

	public List<PreAnalysisElement> getEntities() {
		return entities;
	}

	public void setEntities(List<PreAnalysisElement> entities) {
		this.entities = entities;
	}

	public String getWindowTitle() {
		return windowTitle;
	}

	public void setWindowTitle(String windowTitle) {
		this.windowTitle = windowTitle;
	}

	public String getWindowDescription() {
		return windowDescription;
	}

	public void setWindowDescription(String windowDescription) {
		this.windowDescription = windowDescription;
	}

	public List<PreAnalysisElement> getWindows() {
		return windows;
	}

	public void setWindows(List<PreAnalysisElement> windows) {
		this.windows = windows;
	}

	public String getActionTitle() {
		return actionTitle;
	}

	public void setActionTitle(String actionTitle) {
		this.actionTitle = actionTitle;
	}

	public String getActionDescription() {
		return actionDescription;
	}

	public void setActionDescription(String actionDescription) {
		this.actionDescription = actionDescription;
	}

	public List<PreAnalysisElement> getActions() {
		return actions;
	}

	public void setActions(List<PreAnalysisElement> actions) {
		this.actions = actions;
	}

	public String getDocumentTitle() {
		return documentTitle;
	}

	public void setDocumentTitle(String documentTitle) {
		this.documentTitle = documentTitle;
	}

	public String getDocumentDescription() {
		return documentDescription;
	}

	public void setDocumentDescription(String documentDescription) {
		this.documentDescription = documentDescription;
	}

	public List<PreAnalysisElement> getDocuments() {
		return documents;
	}

	public void setDocuments(List<PreAnalysisElement> documents) {
		this.documents = documents;
	}

	public String getProcessTitle() {
		return processTitle;
	}

	public void setProcessTitle(String processTitle) {
		this.processTitle = processTitle;
	}

	public String getProcessDescription() {
		return processDescription;
	}

	public void setProcessDescription(String processDescription) {
		this.processDescription = processDescription;
	}

	public List<PreAnalysisElement> getProcesses() {
		return processes;
	}

	public void setProcesses(List<PreAnalysisElement> processes) {
		this.processes = processes;
	}

	public String getRoleTitle() {
		return roleTitle;
	}

	public void setRoleTitle(String roleTitle) {
		this.roleTitle = roleTitle;
	}

	public String getRoleDescription() {
		return roleDescription;
	}

	public void setRoleDescription(String roleDescription) {
		this.roleDescription = roleDescription;
	}

	public List<PreAnalysisElement> getRoles() {
		return roles;
	}

	public void setRoles(List<PreAnalysisElement> roles) {
		this.roles = roles;
	}

	public String getDescriptionToShow() {
		return descriptionToShow;
	}

	public void setDescriptionToShow(String descriptionToShow) {
		this.descriptionToShow = descriptionToShow;
	}

	public SessionCache getCache() {
		return cache;
	}

	public void setCache(SessionCache cache) {
		this.cache = cache;
	}

	public TreeNode getRoot() {
		return root;
	}

	public void setRoot(TreeNode root) {
		this.root = root;
	}
}
