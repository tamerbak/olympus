package fr.protogen.cch.service;

import java.util.UUID;

import fr.protogen.cch.cache.ApplicationRegistery;
import fr.protogen.masterdata.dal.dao.CCHUserDAO;
import fr.protogen.masterdata.model.CCHUser;
import fr.protogen.security.Md5;

public class UserManager {
public CCHUser authenitcate(String login, String password){
		
		//	Hash
		String hashedPassword = Md5.encode(password);
		
		CCHUser user = new CCHUser();
		user.setUserName(login);
		user.setPassword(hashedPassword);
		CCHUserDAO dao = new CCHUserDAO(); 
		
		user = dao.getUser(user);
		
		//	Add new session cache
		ApplicationRegistery.getInstance().newInstance(user);
		
		return user;
		
	}

	public void newUser(String login, String password, String nom, String prenom){
	
		//	Hash
		String hashedPassword = Md5.encode(password);
		
		CCHUser user = new CCHUser();
		user.setUserName(login);
		user.setPassword(hashedPassword);
		user.setNom(nom);
		user.setPrenom(prenom);
		user.setUid(UUID.randomUUID().toString());
		CCHUserDAO dao = new CCHUserDAO(); 
		
		dao.createUser(user);
		
	}
}
