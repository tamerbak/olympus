package fr.protogen.cch.control;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.primefaces.event.SelectEvent;
import org.primefaces.model.mindmap.DefaultMindmapNode;  
import org.primefaces.model.mindmap.MindmapNode;

import fr.protogen.cch.cache.ApplicationRegistery;
import fr.protogen.cch.cache.SessionCache;
import fr.protogen.cch.configuration.Parameters;
import fr.protogen.cch.configuration.StringFormat;
import fr.protogen.cch.control.ui.beans.DocumentParameter;
import fr.protogen.cch.genration.GenerationService;
import fr.protogen.masterdata.model.*;

@ManagedBean
@SessionScoped
public class ProcessCtrl {
	private List<CWindow> windows;
	private String selectedScreen;
	private MindmapNode root;
	private MindmapNode selectedNode;
	private String partitle;
	private String processtitle;
	private String pardesc;
	private List<DocumentParameter> parameters;
	private DocumentParameter[] selectedParameters = new DocumentParameter[1];
	private List<String> selectedParametersLabels;
	private List<SProcess> processes;
	
	
	//	Maintenance mode
	private Boolean maintainmode;
	private SessionCache cache;
	private SProcess toDelete;
	
	@PostConstruct
	public void construction(){
		CCHUser user = (CCHUser)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(Parameters.USER_TOKEN);
    	cache = ApplicationRegistery.getInstance().getSession(user);
    	windows = cache.getWindows();
    	root = new DefaultMindmapNode("Depart", "Debut", "FFFFFF", true);
    	parameters = cache.getParameters();
    	maintainmode = (Boolean)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(Parameters.MAINTAIN_MODE);
		if(maintainmode){
			processes = cache.getProcesses();
		}
	}
	
	public void doDelete(){
		processes.remove(toDelete);
		cache.getProcesses().remove(toDelete);
		
		if(maintainmode){
			GenerationService service = new GenerationService();
			List<SProcess> processes = new ArrayList<SProcess>();
			processes.add(toDelete);
			service.deleteProcesses(processes);
		}
	}
	
	public void cancelDelete(){
		
	}
	
	public String constructDependencies(){
		return "";
	}
	
	public void save(ActionEvent evt){
		if(processes == null)
			processes = new ArrayList<SProcess>();
		
		SProcess process = new SProcess();
		process.setTitle(processtitle);
		process.setDescription("");
		List<CWindow> steps = new ArrayList<CWindow>();
		MindmapNode current = root;
		while(current.getChildren().size()>0){
			MindmapNode n = current.getChildren().get(0);
			CWindow w = (CWindow)n.getData();
			steps.add(w);
			current = n;
		}
		process.setWindows(steps);
		processes.add(process);
		
		root = new DefaultMindmapNode("Depart", "Debut", "FFFFFF", true);
		this.pardesc="";
		this.partitle="";
		this.processtitle="";
		
		if(maintainmode){
			GenerationService service = new GenerationService();
			service.addNewProcess(process, cache);
		}
		
	}
	
	public void addNode(ActionEvent evt){
		if(selectedNode == null || selectedNode.getChildren()==null || selectedNode.getChildren().size()>0)
			return;
		
		CWindow window = new CWindow();
		for(CWindow w : windows){
			if(w.getTitle().equals(selectedScreen)){
				window = w;
				break;
			}
		}
		MindmapNode node = new DefaultMindmapNode(selectedScreen, window, "C67B40", true);
		selectedNode.addNode(node);
	}
	public void addPNode(ActionEvent evt){
		if(selectedNode == null || selectedNode.getChildren()==null || selectedNode.getChildren().size()>0)
			return;
		
		CParametersWindow window = new CParametersWindow();
		window.setTitle(partitle);
		window.setDescription(pardesc);
		window.setUiParameters(new ArrayList<CUIParameter>());
		for(DocumentParameter p : selectedParameters){
			CUIParameter param = new CUIParameter();
			param.setParameterLabel(p.getLabel());
			param.setParameterKey(StringFormat.getInstance().parameterFormat(p.getLabel()));
			param.setParameterType(p.getT().charAt(0));
			window.getUiParameters().add(param);
		}
		
		MindmapNode node = new DefaultMindmapNode(partitle, window, "FFFACC", true);
		selectedNode.addNode(node);
	}
	
	public String next(){
		CCHUser user = (CCHUser)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(Parameters.USER_TOKEN);
    	SessionCache cache = ApplicationRegistery.getInstance().getSession(user);
    	cache.setProcesses(processes);
    	
		return "menuitem";
	}
	
    public void onNodeSelect(SelectEvent event) {  
        selectedNode = (MindmapNode) event.getObject();  
    }

    /*
     * 	Getters and setters
     */
	public List<CWindow> getWindows() {
		return windows;
	}

	public void setWindows(List<CWindow> windows) {
		this.windows = windows;
	}

	public String getSelectedScreen() {
		return selectedScreen;
	}

	public void setSelectedScreen(String selectedScreen) {
		this.selectedScreen = selectedScreen;
	}

	public MindmapNode getRoot() {
		return root;
	}

	public void setRoot(MindmapNode root) {
		this.root = root;
	}

	public MindmapNode getSelectedNode() {
		return selectedNode;
	}

	public void setSelectedNode(MindmapNode selectedNode) {
		this.selectedNode = selectedNode;
	}

	public List<String> getSelectedParametersLabels() {
		return selectedParametersLabels;
	}

	public void setSelectedParametersLabels(List<String> selectedParametersLabels) {
		this.selectedParametersLabels = selectedParametersLabels;
	}
	
	public String getPartitle() {
		return partitle;
	}

	public void setPartitle(String partitle) {
		this.partitle = partitle;
	}

	public String getPardesc() {
		return pardesc;
	}

	public void setPardesc(String pardesc) {
		this.pardesc = pardesc;
	}

	public List<DocumentParameter> getParameters() {
		return parameters;
	}

	public void setParameters(List<DocumentParameter> parameters) {
		this.parameters = parameters;
	}

	public DocumentParameter[] getSelectedParameters() {
		return selectedParameters;
	}

	public void setSelectedParameters(DocumentParameter[] selectedParameters) {
		this.selectedParameters = selectedParameters;
	}

	public String getProcesstitle() {
		return processtitle;
	}

	public void setProcesstitle(String processtitle) {
		this.processtitle = processtitle;
	}

	public List<SProcess> getProcesses() {
		return processes;
	}

	public void setProcesses(List<SProcess> processes) {
		this.processes = processes;
	}

	public SProcess getToDelete() {
		return toDelete;
	}

	public void setToDelete(SProcess toDelete) {
		this.toDelete = toDelete;
	}
}
