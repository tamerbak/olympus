package fr.protogen.cch.control;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.primefaces.event.FileUploadEvent;

import fr.protogen.cch.cache.ApplicationRegistery;
import fr.protogen.cch.cache.SessionCache;
import fr.protogen.cch.configuration.Parameters;
import fr.protogen.cch.configuration.StringFormat;
import fr.protogen.cch.control.ui.beans.DocumentParameter;
import fr.protogen.cch.genration.GenerationService;
import fr.protogen.masterdata.model.*;

@ManagedBean
@SessionScoped
public class DocumentCtrl {

	private List<CWindow> windows;
	private List<CDocumentbutton> documents = new ArrayList<CDocumentbutton>();
	private String selectedScreen;
	private String documentTitle;
	private String documentDescription;
	private InputStream jrxmlFile;
	private List<DocumentParameter> parameters;
	private String parameterLabel;
	private String parameterType;
	private String filePath;
	private String parameterMode;

	private CDocumentbutton toDelete;
	private SessionCache cache;
	private Boolean maintainmode;
	
	@PostConstruct
	public void construction(){
		CCHUser user = (CCHUser)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(Parameters.USER_TOKEN);
    	cache = ApplicationRegistery.getInstance().getSession(user);
    	windows = cache.getWindows();
    	if(cache.getParameters() != null)
    		parameters = cache.getParameters();
    	else
    		parameters = new ArrayList<DocumentParameter>();
    	
    	maintainmode = (Boolean)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(Parameters.MAINTAIN_MODE);
		if(maintainmode){
			documents = cache.getDocuments();
		}
	}
	
	public void doDelete(){
		for(CWindow w : windows)
			if(w.getCDocumentbuttons().contains(toDelete))
				w.getCDocumentbuttons().remove(toDelete);
		
		cache.getDocuments().remove(toDelete);
		documents.remove(toDelete);
		if(maintainmode){
    		GenerationService service = new GenerationService();
    		List<CDocumentbutton> docs = new ArrayList<CDocumentbutton>();
    		docs.add(toDelete);
    		service.deleteDocuments(docs);
		}
	}
	public void cancelDelete(){
		
	}
	public String constructDependencies(){
		return "";
	}
	
	public void saveDocument(){
		MDocument doc = new MDocument(0, documentTitle, filePath);
		doc.setParameterMode(parameterMode.charAt(0));
		CWindow window = new CWindow();
		for(CWindow w : windows){
			if(w.getTitle().equals(selectedScreen)){
				window = w;
				break;
			}
		}
		
		String _parameters = "";
		for(DocumentParameter p : parameters){
			String param = StringFormat.getInstance().parameterFormat(p.getLabel());
			_parameters = _parameters+param+":"+p.getT()+":"+p.getLabel()+";";
		}
		if(_parameters.length()>0)
			_parameters = _parameters.substring(0,_parameters.length()-1);
		
		CDocumentbutton btn = new CDocumentbutton(0, window, doc, documentTitle, _parameters) ;
		btn.setDescription(documentDescription);
		documents.add(btn);
		
		CCHUser user = (CCHUser)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(Parameters.USER_TOKEN);
    	SessionCache cache = ApplicationRegistery.getInstance().getSession(user);
    	if(cache.getParameters() == null)
    		cache.setParameters(parameters);
    	else
    		for(DocumentParameter p : parameters){
    			if(isDuplicated(p, cache.getParameters()))
    				continue;
    			cache.getParameters().add(p);
    		}
    	
    	if(maintainmode){
    		GenerationService service = new GenerationService();
			service.addNewDocument(btn,cache);
    	}
    	
    	documentTitle = "";
    	documentDescription = "";
    	parameterLabel = "";
	}
	
	private boolean isDuplicated(DocumentParameter p,
			List<DocumentParameter> parameters2) {
		// TODO Auto-generated method stub
		for(DocumentParameter cp : parameters2)
			if(p.getLabel().equals(cp.getLabel()))
				return true;
		return false;
	}

	public void addParam(ActionEvent evt){
		DocumentParameter p = new DocumentParameter();
		p.setLabel(parameterLabel);
		p.setT(parameterType);
		if(p.getT().equals("I"))
			p.setType("Entier");
		if(p.getT().equals("F"))
			p.setType("Nombre à décimale");
		if(p.getT().equals("D"))
			p.setType("Date");
		
		parameters.add(p);
	}
	
	public void handleFileUpload(FileUploadEvent event) {
		try {
			FacesContext fc = FacesContext.getCurrentInstance();
    	    ExternalContext ec = fc.getExternalContext();
    	    jrxmlFile = event.getFile().getInputstream();
    	    filePath=ec.getRealPath(Parameters.JRXML_PATH)+event.getFile().getFileName();
			OutputStream writer = new FileOutputStream(filePath);
			
			byte[] bytes = new byte[1024];
			@SuppressWarnings("unused")
			int read = 0;
			while((read = jrxmlFile.read(bytes))!=-1){
				writer.write(bytes);
			}
			
			jrxmlFile.close();
			writer.flush();
			writer.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String next(){
		CCHUser user = (CCHUser)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(Parameters.USER_TOKEN);
    	SessionCache cache = ApplicationRegistery.getInstance().getSession(user);
    	cache.setDocuments(documents);
    	return "proceduresynthesis";
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

	public String getDocumentTitle() {
		return documentTitle;
	}

	public void setDocumentTitle(String documentTitle) {
		this.documentTitle = documentTitle;
	}

	public List<DocumentParameter> getParameters() {
		return parameters;
	}

	public void setParameters(List<DocumentParameter> parameters) {
		this.parameters = parameters;
	}

	public InputStream getJrxmlFile() {
		return jrxmlFile;
	}

	public void setJrxmlFile(InputStream jrxmlFile) {
		this.jrxmlFile = jrxmlFile;
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
	
	public List<CDocumentbutton> getDocuments() {
		return documents;
	}

	public void setDocuments(List<CDocumentbutton> documents) {
		this.documents = documents;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public CDocumentbutton getToDelete() {
		return toDelete;
	}

	public void setToDelete(CDocumentbutton toDelete) {
		this.toDelete = toDelete;
	}

	public String getParameterMode() {
		return parameterMode;
	}

	public void setParameterMode(String parameterMode) {
		this.parameterMode = parameterMode;
	}

	public String getDocumentDescription() {
		return documentDescription;
	}

	public void setDocumentDescription(String documentDescription) {
		this.documentDescription = documentDescription;
	}

}
