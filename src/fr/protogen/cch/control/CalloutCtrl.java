package fr.protogen.cch.control;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

import org.apache.commons.io.IOUtils;
import org.primefaces.event.FileUploadEvent;

import fr.protogen.cch.cache.ApplicationRegistery;
import fr.protogen.cch.cache.SessionCache;
import fr.protogen.cch.configuration.Parameters;
import fr.protogen.cch.genration.GenerationService;
import fr.protogen.masterdata.model.CAttribute;
import fr.protogen.masterdata.model.CCHUser;
import fr.protogen.masterdata.model.CCallout;
import fr.protogen.masterdata.model.CCalloutArguments;
import fr.protogen.masterdata.model.CWindow;
import fr.protogen.masterdata.model.CWindowCallout;
import fr.protogen.masterdata.model.CWindowCalloutArgument;

@SuppressWarnings("serial")
@ManagedBean
@SessionScoped
public class CalloutCtrl implements Serializable {
	private List<CCallout> callouts = new ArrayList<CCallout>();
	private CCallout toDelete = new CCallout();
	private Boolean maintainmode = false;;
	private SessionCache cache;
	private CCallout toCreate = new CCallout();
	private CCalloutArguments newArg = new CCalloutArguments();
	
	private List<CWindowCallout> windowCallouts = new ArrayList<CWindowCallout>();
	private List<CWindowCallout> windowCalloutsFiltres;
	
	private int selectedWindowId;
	private int selectedCalloutId;
	private List<CWindow> windows = new ArrayList<CWindow>();
	private List<CCalloutArguments> arguments = new ArrayList<CCalloutArguments>();
	private List<CAttribute> attributes = new ArrayList<CAttribute>();
	
	public CalloutCtrl(){
		maintainmode = (Boolean)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(Parameters.MAINTAIN_MODE);
		if(maintainmode){
			CCHUser user = (CCHUser)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(Parameters.USER_TOKEN);
	    	cache = ApplicationRegistery.getInstance().getSession(user);
	    	callouts = cache.getCallouts();
	    	windowCallouts = cache.getWindowCallouts();
	    	windows = cache.getWindows();
	    	
	    	if(callouts.size()>0)
	    		selectedCalloutId = callouts.get(0).getId();
	    	if(windows.size()>0)
	    		selectedWindowId = windows.get(0).getId();
		}
	}
	
	public void doDelete(){
		
	}

	public String saveCallout(){
		
		if(!maintainmode){
			cache.getCallouts().add(toCreate);
			toCreate = new CCallout();
			return null;
		}
		GenerationService engine = new GenerationService();
		toCreate = engine.addNewCallout(toCreate, cache);
		cache.getCallouts().add(toCreate);
		if(!callouts.contains(toCreate))
			callouts.add(toCreate);
		toCreate = new CCallout();
		return null;
	}
	
	public void handleFileUpload(FileUploadEvent event) {
		try {
    	    InputStream jarFile = event.getFile().getInputstream();
    	    toCreate.setFile(IOUtils.toByteArray(jarFile));
    	    jarFile.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void addArg(){
		toCreate.getArgs().add(newArg);
		newArg = new CCalloutArguments();
	}
	
	/*
	 * Window
	 */
	public String saveWindowCallout(){
		
		CWindowCallout cw = new CWindowCallout();
		
		for(CWindow w : windows)
			if(w.getId() == selectedWindowId){
				cw.setWindow(w);
				break;
			}
		
		for(CCallout c : callouts)
			if(c.getId() == selectedCalloutId){
				cw.setCallout(c);
				break;
			}
		
		cw.setArguments(new ArrayList<CWindowCalloutArgument>());
		for(CCalloutArguments a : arguments){
			CWindowCalloutArgument arg = new CWindowCalloutArgument();
			arg.setArgument(a);
			arg.setPrompt(false);
			arg.setCreated(false);
			arg.setSelection(false);
			if(a.getValue().equals("prompt")){
				arg.setPrompt(true);
				arg.setCreated(false);
				arg.setSelection(false);
			}
			for(CAttribute att : attributes)
				if(att.getDataReference().equals(a.getValue())){
					arg.setAttribute(att);
					if(cw.getWindow().getCWindowtype().getId() == 1)
						arg.setSelection(true);
					if(cw.getWindow().getCWindowtype().getId() == 2)
						arg.setCreated(true);
					break;
				}
			
			cw.getArguments().add(arg);
		}
		
		if(maintainmode){
			GenerationService service = new GenerationService();
			service.addNewWindowCallout(cw);
		}
		cache.getWindowCallouts().add(cw);
		if(!windowCallouts.contains(cw))
			windowCallouts.add(cw);
		
		if(callouts.size()>0)
    		selectedCalloutId = callouts.get(0).getId();
    	if(windows.size()>0)
    		selectedWindowId = windows.get(0).getId();
		return null;
	}
	
	public void selectedWindowChange(){
		for(CWindow w : windows)
			if(w.getId() == selectedWindowId){
				attributes = w.getCAttributes();
				break;
			}
	}
	
	public void selectedCalloutChange(){
		for(CCallout c : callouts)
			if(c.getId() == selectedCalloutId){
				arguments = c.getArgs();
				break;
			}
	}
	
	/*
	 * GETTERS AND SETTERS
	 */
	public List<CCallout> getCallouts() {
		return callouts;
	}

	public void setCallouts(List<CCallout> callouts) {
		this.callouts = callouts;
	}

	public CCallout getToDelete() {
		return toDelete;
	}

	public void setToDelete(CCallout toDelete) {
		this.toDelete = toDelete;
	}

	public CCallout getToCreate() {
		return toCreate;
	}

	public void setToCreate(CCallout toCreate) {
		this.toCreate = toCreate;
	}

	public CCalloutArguments getNewArg() {
		return newArg;
	}

	public void setNewArg(CCalloutArguments newArg) {
		this.newArg = newArg;
	}

	public List<CWindowCallout> getWindowCallouts() {
		return windowCallouts;
	}

	public void setWindowCallouts(List<CWindowCallout> windowCallouts) {
		this.windowCallouts = windowCallouts;
	}

	public List<CWindowCallout> getWindowCalloutsFiltres() {
		return windowCalloutsFiltres;
	}

	public void setWindowCalloutsFiltres(List<CWindowCallout> windowCalloutsFiltres) {
		this.windowCalloutsFiltres = windowCalloutsFiltres;
	}

	public int getSelectedWindowId() {
		return selectedWindowId;
	}

	public void setSelectedWindowId(int selectedWindowId) {
		this.selectedWindowId = selectedWindowId;
	}

	public int getSelectedCalloutId() {
		return selectedCalloutId;
	}

	public void setSelectedCalloutId(int selectedCalloutId) {
		this.selectedCalloutId = selectedCalloutId;
	}

	public List<CWindow> getWindows() {
		return windows;
	}

	public void setWindows(List<CWindow> windows) {
		this.windows = windows;
	}

	public List<CCalloutArguments> getArguments() {
		return arguments;
	}

	public void setArguments(List<CCalloutArguments> arguments) {
		this.arguments = arguments;
	}

	public List<CAttribute> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<CAttribute> attributes) {
		this.attributes = attributes;
	}
}
