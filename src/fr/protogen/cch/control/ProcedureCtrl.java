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
import fr.protogen.cch.configuration.StringFormat;
import fr.protogen.cch.genration.GenerationService;
import fr.protogen.masterdata.model.CCHUser;
import fr.protogen.masterdata.model.CUIParameter;
import fr.protogen.masterdata.model.CWindow;
import fr.protogen.masterdata.model.SAtom;
import fr.protogen.masterdata.model.SProcedure;
import fr.protogen.masterdata.model.SResource;
import fr.protogen.masterdata.model.SScheduledCom;
import fr.protogen.masterdata.model.SStep;
import fr.protogen.masterdata.model.StepType;

@ManagedBean
@ViewScoped
public class ProcedureCtrl implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3829895478513768432L;
	private String title;
	private String description;
	private String keywords;
	private String stepTitle;
	private String stepDescription;
	private List<SStep> steps = new ArrayList<SStep>();
	private String atomTitle;
	private String atomDescription;
	private String atomStep;
	private int atomType=1;
	private List<StepType> types = new ArrayList<StepType>();
	private String atomWindow;
	private List<CWindow> windows = new ArrayList<CWindow>();
	private String parameterLabel;
	private String parameterType;
	private String resinTitle;
	private String resinDescription;
	private String resinFile;
	private String comTitle;
	private String comDescription;
	private List<CUIParameter> parametersTable = new ArrayList<CUIParameter>();
	private CUIParameter[] selectedParameters;
	private List<SResource> resourcesTable = new ArrayList<SResource>();
	private SResource selectedResource;
	private List<SAtom> atoms = new ArrayList<SAtom>();
	
	private List<SProcedure> procedures = new ArrayList<SProcedure>();
	
	private Boolean maintainmode;
	private SessionCache cache;
	private SProcedure toDelete;
	
	@PostConstruct
	public void initialize(){
		maintainmode = (Boolean)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(Parameters.MAINTAIN_MODE);
		CCHUser user = (CCHUser)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(Parameters.USER_TOKEN);
    	cache = ApplicationRegistery.getInstance().getSession(user);
    	types = cache.getStepTypes();
    	windows = cache.getWindows();
    	parametersTable = cache.getApplicationParameters();
    	resourcesTable = cache.getResources();
		if(maintainmode){
			procedures = cache.getProcedures();
		}
		
		steps = new ArrayList<SStep>();
		atoms = new ArrayList<SAtom>();
		
	}
	
	public void save(){
		SProcedure p = new SProcedure();
		p.setTitle(title);
		p.setDescription(description);
		p.setKeyWords(new ArrayList<String>());
		String[] kw = keywords.split(",");
		for(String s : kw)
			p.getKeyWords().add(s);
		p.setEtapes(steps);
		procedures.add(p);
		
		if(maintainmode){
			GenerationService service = new GenerationService();
			service.addNewProcedure(p,cache.getAppKey(),keywords);
		}
		
		title="";
		description="";
		keywords="";
		stepTitle="";
		stepDescription="";
		steps = new ArrayList<SStep>();
		atomTitle="";
		atomDescription="";
		atomStep="";
		atomType=1;
		atomWindow="";
		parameterLabel="";
		parameterType="";
		resinTitle="";
		resinDescription="";
		resinFile="";
		comTitle="";
		comDescription="";
		selectedParameters=null;
		selectedResource=null;
		atoms = new ArrayList<SAtom>();
	}
	public String next(){
		
		cache.setProcedures(procedures);
		
		return "menuitem";
	}
	public void addStep(){
		SStep s = new SStep();
		s.setTitle(stepTitle);
		s.setDescription(stepDescription);
		s.setActions(new ArrayList<SAtom>());
		steps.add(s);
		
		stepTitle="";
		stepDescription="";
	}
	public void typeChangeListener(){
		String aaa = "";
		aaa = aaa+"";
	}
	public void addParameter(){
		CUIParameter p = new CUIParameter();
		p.setParameterLabel(parameterLabel);
		p.setParameterType(parameterType.charAt(0));
		p.setParameterKey(StringFormat.getInstance().parameterFormat(parameterLabel));
		p.setCtrlDate((parameterType.charAt(0) == 'D'));
		parametersTable.add(p);
		
	}
	public void addRes(){
		SResource r = new SResource();
		r.setTitle(resinTitle);
		r.setDescription(resinDescription);
		r.setFileName(resinFile);
		resourcesTable.add(r);
	}
	public void addAtom(){
		SAtom a = new SAtom();
		a.setTitle(atomTitle);
		a.setDescription(atomDescription);
		StepType t = new StepType();
		for(StepType st : types){
			if(st.getId()==atomType){
				t = st;
				break;
			}
		}
		
		for(SStep s : steps){
			if(s.getTitle().equals(atomStep)){
				a.setStep(s);
				if(s.getActions() == null)
					s.setActions(new ArrayList<SAtom>());
				s.getActions().add(a);
				break;
			}
				
		}
		
		a.setType(t);
		
		switch(atomType){
		case 1: {
			//	Window
			for(CWindow sw : windows){
				if(sw.getTitle().equals(atomWindow)){
					a.setWindow(sw);
					break;
				}
			}
			break;
		}
		
		case 2:{
			//	Parameters
			a.setParameters(parametersTable);
			break;
		}
		case 3:case 4:{
			//	Resources
			a.setResource(selectedResource);
			selectedResource=null;
			break;
		}
		case 5:case 6:case 7:case 8:{
			//	Communication
			SScheduledCom c = new SScheduledCom();
			c.setTitle(comTitle);
			c.setDescription(comDescription);
			if(selectedResource!=null)
				c.setAttachement(selectedResource);
			selectedResource=null;
			a.setCommunication(c);
			break;
		}
		}
		
		atoms.add(a);
		
		
		//	INIT
		atomTitle="";
		atomDescription="";
		atomStep="";
		atomType=1;
		atomWindow="";
		parameterLabel="";
		parameterType="";
		resinTitle="";
		resinDescription="";
		resinFile="";
		comTitle="";
		comDescription="";
		selectedParameters=null;
		selectedResource=null;
	}

	public void doDelete(){
		procedures.remove(toDelete);
		cache.getProcedures().remove(toDelete);
		if(maintainmode){
			GenerationService service = new GenerationService();
			service.deleteProcedure(toDelete);
		}
	}
	public void cancelDelete(){
		
	}
	public void constructDependencies(){
		
	}
	
	/*
	 * 	Getters and setters
	 */
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getKeywords() {
		return keywords;
	}

	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	public String getStepTitle() {
		return stepTitle;
	}

	public void setStepTitle(String stepTitle) {
		this.stepTitle = stepTitle;
	}

	public String getStepDescription() {
		return stepDescription;
	}

	public void setStepDescription(String stepDescription) {
		this.stepDescription = stepDescription;
	}

	public List<SStep> getSteps() {
		return steps;
	}

	public void setSteps(List<SStep> steps) {
		this.steps = steps;
	}

	public String getAtomTitle() {
		return atomTitle;
	}

	public void setAtomTitle(String atomTitle) {
		this.atomTitle = atomTitle;
	}

	public String getAtomDescription() {
		return atomDescription;
	}

	public void setAtomDescription(String atomDescription) {
		this.atomDescription = atomDescription;
	}

	public String getAtomStep() {
		return atomStep;
	}

	public void setAtomStep(String atomStep) {
		this.atomStep = atomStep;
	}

	public int getAtomType() {
		return atomType;
	}

	public void setAtomType(int atomType) {
		this.atomType = atomType;
	}

	public List<StepType> getTypes() {
		return types;
	}

	public void setTypes(List<StepType> types) {
		this.types = types;
	}

	public String getAtomWindow() {
		return atomWindow;
	}

	public void setAtomWindow(String atomWindow) {
		this.atomWindow = atomWindow;
	}

	public List<CWindow> getWindows() {
		return windows;
	}

	public void setWindows(List<CWindow> windows) {
		this.windows = windows;
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

	public String getResinTitle() {
		return resinTitle;
	}

	public void setResinTitle(String resinTitle) {
		this.resinTitle = resinTitle;
	}

	public String getResinDescription() {
		return resinDescription;
	}

	public void setResinDescription(String resinDescription) {
		this.resinDescription = resinDescription;
	}

	public String getComTitle() {
		return comTitle;
	}

	public void setComTitle(String comTitle) {
		this.comTitle = comTitle;
	}

	public String getComDescription() {
		return comDescription;
	}

	public void setComDescription(String comDescription) {
		this.comDescription = comDescription;
	}

	public List<CUIParameter> getParametersTable() {
		return parametersTable;
	}

	public void setParametersTable(List<CUIParameter> parametersTable) {
		this.parametersTable = parametersTable;
	}

	public CUIParameter[] getSelectedParameters() {
		return selectedParameters;
	}

	public void setSelectedParameters(CUIParameter[] selectedParameters) {
		this.selectedParameters = selectedParameters;
	}

	public List<SResource> getResourcesTable() {
		return resourcesTable;
	}

	public void setResourcesTable(List<SResource> resourcesTable) {
		this.resourcesTable = resourcesTable;
	}

	public SResource getSelectedResource() {
		return selectedResource;
	}

	public void setSelectedResource(SResource selectedResource) {
		this.selectedResource = selectedResource;
	}

	public List<SAtom> getAtoms() {
		return atoms;
	}

	public void setAtoms(List<SAtom> atoms) {
		this.atoms = atoms;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public List<SProcedure> getProcedures() {
		return procedures;
	}

	public void setProcedures(List<SProcedure> procedures) {
		this.procedures = procedures;
	}

	public SProcedure getToDelete() {
		return toDelete;
	}

	public void setToDelete(SProcedure toDelete) {
		this.toDelete = toDelete;
	}

	public String getResinFile() {
		return resinFile;
	}

	public void setResinFile(String resinFile) {
		this.resinFile = resinFile;
	}
}
