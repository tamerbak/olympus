package fr.protogen.cch.control;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.primefaces.event.FileUploadEvent;

import fr.protogen.cch.cache.ApplicationRegistery;
import fr.protogen.cch.cache.SessionCache;
import fr.protogen.cch.configuration.Parameters;
import fr.protogen.cch.genration.GenerationService;
import fr.protogen.masterdata.model.CCHUser;
import fr.protogen.masterdata.model.SApplication;

@ManagedBean
@ViewScoped
public class NewApplicationCtrl implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2181766903904054918L;
	
	private SessionCache cache;

	
	private String appName;
	private String version;
	private String author;
	private String description;
	private String logoPath;
	private String copyright;
	private InputStream logo;
	private Boolean maintainmode;
	
	@PostConstruct
	public void postLoad(){
		
		maintainmode = (Boolean)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(Parameters.MAINTAIN_MODE);
		if(maintainmode == null){
			FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(Parameters.MAINTAIN_MODE, new Boolean(false));
			maintainmode = new Boolean(false);
		}
		if(maintainmode){
			CCHUser user = (CCHUser)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(Parameters.USER_TOKEN);
	    	cache = ApplicationRegistery.getInstance().getSession(user);
	    	SApplication a = cache.getApplication();
			copyright = a.getLicence();
			author = a.getAuthor();
			description = a.getDescription();
			version = a.getVersion();
			appName = a.getProjectName();
		}
	}
	
   public String saveToDB(){
	   
	   SApplication application = cache.getApplication();
	   application.setLicence(copyright);
	   application.setAuthor(author);
	   application.setDescription(description);
	   application.setProjectName(appName);
	   application.setVersion(version);
	   
	   GenerationService service = new GenerationService();
	   service.updateApplication(cache.getApplication());
	   return "";
   }
    
    public String suivant(){
		
    	SApplication application = new SApplication();
    	application.setLicence(copyright);
    	application.setAuthor(author);
    	application.setDescription(description);
    	application.setProjectName(appName);
    	application.setVersion(version);
    	
    	CCHUser user = (CCHUser)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(Parameters.USER_TOKEN);
    	cache = ApplicationRegistery.getInstance().getSession(user);
    	
    	cache.setApplication(application);
    	
    	return "description";
    }
    
    @SuppressWarnings("unused")
   	public void handleFileUpload(FileUploadEvent event) {
       	try {
       		FacesContext fc = FacesContext.getCurrentInstance();
       	    ExternalContext ec = fc.getExternalContext();
   			logo = event.getFile().getInputstream();
   			File f = new File(ec.getRealPath(Parameters.LOGO_PATH));
   			if(!f.exists())
   				f.createNewFile();
   			OutputStream writer = new FileOutputStream(ec.getRealPath(Parameters.LOGO_PATH));
   			logoPath = ec.getRealPath(Parameters.LOGO_PATH);
   			byte[] bytes = new byte[1024];
   			int read = 0;
   			while((read = logo.read(bytes))!=-1){
   				writer.write(bytes);
   			}
   			
   			logo.close();
   			writer.flush();
   			writer.close();
   			
   		} catch (IOException e) {
   			// TODO Auto-generated catch block
   			e.printStackTrace();
   		}
       }
    
    /*
     * 	Getters and setters
     * */
	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getLogoPath() {
		return logoPath;
	}

	public void setLogoPath(String logoPath) {
		this.logoPath = logoPath;
	}

	public String getCopyright() {
		return copyright;
	}

	public void setCopyright(String copyright) {
		this.copyright = copyright;
	}


	public InputStream getLogo() {
		return logo;
	}

	public void setLogo(InputStream logo) {
		this.logo = logo;
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

	
}
