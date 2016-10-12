package fr.protogen.cch.batch;

import java.util.UUID;

import fr.protogen.masterdata.dal.dao.CCHUserDAO;
import fr.protogen.masterdata.model.CCHUser;
import fr.protogen.security.Md5;

public class NewUser {

	public static void main(String[] args) {
		newUser("j.benabbes","Re$pub1","Benabbes", "Jamal");
		newUser("a.baghazou","Re$pub1","Baghazou", "Abdallah");
		newUser("d.essabbar","Re$pub1","Essabbar", "Driss");
		newUser("a.rochd","Re$pub1","Rochd", "Amal");
		newUser("h.maristani","Re$pub1","Maristani", "Hamza");
	}
	public static void newUser(String login, String password, String nom, String prenom){
		
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
