package fr.protogen.cch.control;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

import fr.protogen.cch.configuration.Parameters;
import fr.protogen.cch.service.UserManager;
import fr.protogen.masterdata.model.CCHUser;

@ManagedBean
@SessionScoped
public class LoginCtrl {
	private String userName;
	private String password;
	
	public String goLogin(){
		
		CCHUser user = new CCHUser();
		UserManager manager = new UserManager();
		user = manager.authenitcate(userName, password);
		
		if(user == null){
	        FacesContext context = FacesContext.getCurrentInstance();  
	        context.getExternalContext().getFlash().setKeepMessages(true);
	        context.addMessage(null, new FacesMessage("Erreur d'authentification", "Veuillez verifier votre nom d'utilisateur / mot de passe"));
	        return "";
		}
		
		FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(Parameters.USER_TOKEN, user);
		
		return "allapps";
	}
	
	/*
	 *	Getters and setters
	 */
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	
}
