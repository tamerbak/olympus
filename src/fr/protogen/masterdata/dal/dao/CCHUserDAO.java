package fr.protogen.masterdata.dal.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.UUID;

import fr.protogen.masterdata.dal.DBUtils;
import fr.protogen.masterdata.dal.QueryFactory;
import fr.protogen.masterdata.model.CCHUser;

public class CCHUserDAO {
	public void createUser(CCHUser user){
		UUID uid = UUID.randomUUID();
		user.setUid(uid.toString());
		
		String query = QueryFactory.INSERT_USER;
		query = query.replaceAll("%id_user%", user.getUid());
		query = query.replaceAll("%user_name%", user.getUserName());
		query = query.replaceAll("%password%", user.getPassword());
		query = query.replaceAll("%nom%", user.getNom());
		query = query.replaceAll("%prenom%", user.getPrenom());
		try{
			Class.forName("org.postgresql.Driver");

		    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
		    
			Statement st = cnx.createStatement();
			st.execute(query);
			st.close();
			cnx.close();
		} catch(Exception exc){
			exc.printStackTrace();
		}
	}
	
	public CCHUser getUser(CCHUser user){
		
		String query = QueryFactory.getInstance().getUserByExemple(user);
		try{
			Class.forName("org.postgresql.Driver");

		    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
		    
			Statement st = cnx.createStatement();
			ResultSet rs = st.executeQuery(query);
			CCHUser result = null;
			if(rs.next()){
				result = new CCHUser();
				result.setUid(rs.getString("id_user"));
				result.setNom(rs.getString("nom"));
				result.setPassword(rs.getString("password"));
				result.setPrenom(rs.getString("prenom"));
				result.setUserName(rs.getString("user_name"));
			}
			
			rs.close();
			st.close();
			cnx.close();
			return result;
		} catch(Exception exc){
			exc.printStackTrace();
		}

		
		return null;
	}
}
