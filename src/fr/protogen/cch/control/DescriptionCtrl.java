package fr.protogen.cch.control;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import fr.protogen.cch.cache.ApplicationRegistery;
import fr.protogen.cch.cache.SessionCache;
import fr.protogen.cch.configuration.Parameters;
import fr.protogen.cch.genration.GenerationService;
import fr.protogen.masterdata.model.CActionbutton;
import fr.protogen.masterdata.model.CCHUser;
import fr.protogen.masterdata.model.CDocumentbutton;
import fr.protogen.masterdata.model.CWindow;
import fr.protogen.masterdata.model.SProcess;
import fr.protogen.masterdata.model.SScreensequence;

@ManagedBean
@SessionScoped
public class DescriptionCtrl {
	/**
	 * 
	 */
	
	private SessionCache cache;

	//	form
	private String title;
	private String description;
	
	//	table
	private List<SScreensequence> sequences = new ArrayList<SScreensequence>();
	
	//	Maintenance mode
	private SScreensequence selectedSequence;
	private List<String> windows;
	private List<String> actions;
	private List<String> documents;
	private List<String> processes;
	private Boolean maintainmode;
	
		@PostConstruct
	public void postload(){
		maintainmode = (Boolean)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(Parameters.MAINTAIN_MODE);
		if(maintainmode){
			CCHUser user = (CCHUser)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(Parameters.USER_TOKEN);
	    	cache = ApplicationRegistery.getInstance().getSession(user);
			sequences = cache.getSequences();
		}
	}
	
	public void add(ActionEvent evt){
		SScreensequence sequence = new SScreensequence();
		sequence.setDescription(description);
		sequence.setTitle(title);
		sequences.add(sequence);
		
		if(maintainmode){
			GenerationService service = new GenerationService();
			service.addNewFunction(sequence,cache.getAppKey());
		}
		
	    title="";
		description="";
	}
	
	public void constructDependencies(){
		windows =new ArrayList<String>();
		documents = new ArrayList<String>();
		actions = new ArrayList<String>();
		processes = new ArrayList<String>();
		
		for(CWindow w : cache.getWindows()){
			if(w.getFunction().getId() == selectedSequence.getId()){
				windows.add(w.getTitle());
				for(CActionbutton a : w.getCActionbuttons()){
					actions.add(a.getTitle());
				}
				for(CDocumentbutton d : w.getCDocumentbuttons()){
					documents.add(d.getTitle());
				}
				for(SProcess p : cache.getProcesses()){
					for(CWindow pw : p.getWindows())
						if(pw.getId() == w.getId())
							processes.add(p.getTitle());
				}
			}
		}
		
	}
	
	public void doDelete(){
		
		List<CWindow> idWindows = new ArrayList<CWindow>();
		List<CActionbutton> idActions = new ArrayList<CActionbutton>();
		List<CDocumentbutton> idDocuments = new ArrayList<CDocumentbutton>();
		List<SProcess> idProcesses = new ArrayList<SProcess>();
		
		
		cache.getSequences().remove(selectedSequence);
		
		for(CWindow w : cache.getWindows()){
			if(w.getFunction().getId() == selectedSequence.getId()){
				idWindows.add(w);
				cache.getWindows().remove(w);
				for(CActionbutton a : w.getCActionbuttons()){
					cache.getActions().remove(a);
					idActions.add(a);
				}
				for(CDocumentbutton d : w.getCDocumentbuttons()){
					cache.getDocuments().remove(d);
					idDocuments.add(d);
				}
				for(SProcess p : cache.getProcesses()){
					for(CWindow pw : p.getWindows())
						if(pw.getId() == w.getId()){
							cache.getProcesses().remove(p);
							idProcesses.add(p);
						}
				}
			}
		}
		
		//	DB Manipulation
		if(maintainmode){
			GenerationService service = new GenerationService();
			service.deleteActions(idActions);
			service.deleteDocuments(idDocuments);
			service.deleteProcesses(idProcesses);
			service.deleteWindows(idWindows);
		}
	}
	public void cancelDelete(){
		String dummy="";
		dummy = dummy+"";
	}
	
	public String suivant(){
		CCHUser user = (CCHUser)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(Parameters.USER_TOKEN);
    	cache = ApplicationRegistery.getInstance().getSession(user);
    	cache.setSequences(sequences);
		return "entities";
	}
	
	/*
	 * 		Getters and setters
	 */
	public SessionCache getCache() {
		return cache;
	}
	public void setCache(SessionCache cache) {
		this.cache = cache;
	}
	
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

	public List<SScreensequence> getSequences() {
		return sequences;
	}

	public void setSequences(List<SScreensequence> sequences) {
		this.sequences = sequences;
	}

	public SScreensequence getSelectedSequence() {
		return selectedSequence;
	}

	public void setSelectedSequence(SScreensequence selectedSequence) {
		this.selectedSequence = selectedSequence;
	}

	public List<String> getWindows() {
		return windows;
	}

	public void setWindows(List<String> windows) {
		this.windows = windows;
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

	public Boolean getMaintainmode() {
		return maintainmode;
	}

	public void setMaintainmode(Boolean maintainmode) {
		this.maintainmode = maintainmode;
	}
}
