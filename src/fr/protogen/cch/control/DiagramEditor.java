/**
 * 
 */
package fr.protogen.cch.control;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import fr.protogen.cch.cache.ApplicationRegistery;
import fr.protogen.cch.cache.SessionCache;
import fr.protogen.cch.configuration.Parameters;
import fr.protogen.cch.genration.GenerationService;
import fr.protogen.masterdata.model.CAttribute;
import fr.protogen.masterdata.model.CBusinessClass;
import fr.protogen.masterdata.model.CCHUser;
import fr.protogen.masterdata.model.CWindow;
import fr.protogen.masterdata.model.CoreRole;

/**
 * @author mohamed
 *
 */
@SuppressWarnings("serial")
@ManagedBean
@RequestScoped
public class DiagramEditor implements Serializable {
    //	Maintenance mode
    private CBusinessClass maintenanceEntity;
    private CAttribute attToDelete;
    private List<String> satts;
	private List<String> actions;
	private List<String> documents;
	private List<String> processes;
    private SessionCache cache;
	private Boolean maintainmode;
	private List<CoreRole> roles = new ArrayList<CoreRole>();
	private List<CWindow> windows = new ArrayList<CWindow>();
	
	
	private String getReqParam(String param) {
		Object obj = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get(param);
		if(obj instanceof String) {
			return (String)obj;
		}
		return null;
	}
	@PostConstruct
	public void postLoad(){
		maintainmode = (Boolean)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(Parameters.MAINTAIN_MODE);
		if(maintainmode){
			CCHUser user = (CCHUser)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(Parameters.USER_TOKEN);
	    	cache = ApplicationRegistery.getInstance().getSession(user);
	    	setRoles(cache.getRoles());
			setWindows(cache.getWindows());
		}
		
		String action = getReqParam("source");
		if ("fromDC".equals(action)) {
			String diagram = getReqParam("diagram");
			String title = getReqParam("wf_title");
			String description = getReqParam("message");
			String window = getReqParam("wf_windows");
			System.out.println(diagram);
			if (diagram != null && diagram != "") {
				parseDiagram(title, description==null?"":description, window, diagram);
			}
		}
		
	}

	@SuppressWarnings("rawtypes")
	private void parseDiagram(String title, String description, String window, String diagram) {
		try{
			JSONObject jsonDiagram = JSONObject.fromObject(diagram);
			Integer defId = addNewDefinition(title, description, Integer.parseInt(window), diagram);
			
			JSONObject nodes = jsonDiagram.getJSONObject("nodes");
			JSONObject transitions = jsonDiagram.getJSONObject("transitions");
			Set nodesKeys = nodes.keySet();
			for(Iterator _it = nodesKeys.iterator(); _it.hasNext();) {
				String key = (String) _it.next();
				JSONObject node = nodes.getJSONObject(key);
				int nodeId = storeNode(node, defId);
				node.put("nodeId", nodeId);
			}
			Set transKeys = transitions.keySet();
			for(Iterator _it = transKeys.iterator(); _it.hasNext();) {
				String key = (String) _it.next();
				JSONObject trans = transitions.getJSONObject(key);
				String keyA = trans.getString ("nodeAKey");
				String keyB = trans.getString ("nodeBKey");
				JSONObject nodeA = nodes.getJSONObject(keyA);
				JSONObject nodeB = nodes.getJSONObject(keyB);
				Integer nodeAId = nodeA.getInt("nodeId");
				Integer nodeBId = nodeB.getInt("nodeId");
				Integer transId = storeTransition(trans, defId, nodeAId, nodeBId);
				if("answer".equals(nodeB.getString("type"))) {
					updateNode("answer", nodeBId, null, null, nodeAId);
				}
				if("request".equals(nodeA.getString("type"))){
					if("yes".equals(trans.getString("yes_no"))){
						updateNode("request", nodeAId, nodeBId, null, null);
					} else {
						updateNode("request", nodeAId, null, nodeBId, null);
					}
				}
				if("choice".equals(nodeA.getString("type"))) {
					storeChoice(nodeAId, transId, nodeB);
				}
			}
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	private Integer storeChoice(Integer nodeAId,Integer transId, JSONObject node) {
		String title = node.getString("title");
		String nodeId = node.getString("nodeId");
		
		GenerationService service = new GenerationService();
		return service.addNewChoice(nodeAId, transId, title, nodeId);
		
	}
	private Integer storeTransition(JSONObject trans, Integer defId, Integer nodeAId, Integer nodeBId) {
		String title = trans.getString("title");
		Boolean auto = trans.getBoolean("auto");
		GenerationService service = new GenerationService();
		return service.addNewWFTransition(title, auto, nodeAId, nodeBId, defId);
	}
	private Integer storeNode(JSONObject node, Integer defId) {
		String title = node.getString("title");
		String message = node.getString("message") !=null?node.getString("message"):"";
		String type = node.getString("type");
		String typeId = node.getString("type_id");
		String role = node.getString("role");
		String window = node.getString("window");
		
		GenerationService service = new GenerationService();
		return service.addNewWFNode(title, message,type,  Integer.parseInt(typeId), Integer.parseInt(role), window, defId);
		
	}
	private Integer addNewDefinition(String title, String description, Integer window, String diagram){
		GenerationService service = new GenerationService();
		return service.addNewWFDefintion(title, description, window, diagram, cache);
	}
	private void updateNode(String type, Integer cNode, Integer yesNode, Integer noNode, Integer requestNode) {
		
		GenerationService service = new GenerationService();
		service.updateWFNode(type, cNode, yesNode, noNode, requestNode);
	}
	public String workflowEditor(){
		CCHUser user = (CCHUser)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(Parameters.USER_TOKEN);
    	cache = ApplicationRegistery.getInstance().getSession(user);
    	setRoles(cache.getRoles());
		setWindows(cache.getWindows());
		return "wf";
	}
	
	
	public CBusinessClass getMaintenanceEntity() {
		return maintenanceEntity;
	}
	public void setMaintenanceEntity(CBusinessClass maintenanceEntity) {
		this.maintenanceEntity = maintenanceEntity;
	}
	public CAttribute getAttToDelete() {
		return attToDelete;
	}
	public void setAttToDelete(CAttribute attToDelete) {
		this.attToDelete = attToDelete;
	}
	public List<String> getSatts() {
		return satts;
	}
	public void setSatts(List<String> satts) {
		this.satts = satts;
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
	public Boolean getMaintainmode() {
		return maintainmode;
	}
	public void setMaintainmode(Boolean maintainmode) {
		this.maintainmode = maintainmode;
	}

	public List<CoreRole> getRoles() {
		return roles;
	}

	public void setRoles(List<CoreRole> roles) {
		this.roles = roles;
	}

	public List<CWindow> getWindows() {
		return windows;
	}

	public void setWindows(List<CWindow> windows) {
		this.windows = windows;
	}

}
