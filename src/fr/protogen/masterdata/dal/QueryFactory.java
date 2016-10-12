package fr.protogen.masterdata.dal;

import fr.protogen.masterdata.model.CCHUser;

public class QueryFactory {
	public static String ATTRIBUTE_TYPE = "select * from c_attributetype where id=";
	public static String INSERT_USER = "insert into cch_user (id_user, user_name, password, nom, prenom) values ('%id_user%', '%user_name%', '%password%', '%nom%', '%prenom%')";
	
	
	private static QueryFactory instance = null;
	public static synchronized QueryFactory getInstance(){
		if(instance == null)
			instance  = new QueryFactory();
		return instance;
	}
	private QueryFactory(){}
	
	public String getUserByExemple(CCHUser user) {
		// TODO Auto-generated method stub
		//	if there is an id
		if(user.getUid() != null && user.getUid().length()>0)
			return "select id_user, user_name, password, nom, prenom from cch_user where id_user='"+user.getUid()+"'";
		
		String query = "select id_user, user_name, password, nom, prenom from cch_user ";
		if((user.getUserName() == null ||user.getUserName().length()==0) && (user.getPassword() == null ||user.getPassword().length()==0) && (user.getNom() == null ||user.getNom().length()==0) && (user.getPrenom() == null ||user.getPrenom().length()==0))
			return query;
		
		query = query+" where ";
		if(user.getUserName() != null && user.getUserName().length()>0)
			query = query + "user_name='"+user.getUserName()+"' AND ";
		if(user.getPassword() != null && user.getPassword().length()>0)
			query = query + "password='"+user.getPassword()+"' AND ";
		if(user.getNom() != null && user.getNom().length()>0)
			query = query + "nom='"+user.getNom()+"' AND ";
		if(user.getPrenom() != null && user.getPrenom().length()>0)
			query = query + "prenom='"+user.getPrenom()+"' AND ";
		
		query = query.substring(0, query.length()-4);
		
		return query;
	}
}
