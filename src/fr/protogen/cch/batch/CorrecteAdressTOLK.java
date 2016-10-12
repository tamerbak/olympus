package fr.protogen.cch.batch;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.protogen.masterdata.dal.DBUtils;

public class CorrecteAdressTOLK {

	public static void main(String[] args) {
		Map<String, Integer>  CPS = new HashMap<String, Integer>();
		List<String> codes = new ArrayList<String>();
		Map<String, Integer>  VS = new HashMap<String, Integer>();
		List<String> villes = new ArrayList<String>();
		
		//	Codes postaux
		String sql = "select cp from user_adresse where fk_user_code_postal is null group by cp ";
		try{
			Class.forName("org.postgresql.Driver");

		    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
		    PreparedStatement ps = cnx.prepareStatement(sql);
		    ResultSet rs = ps.executeQuery();
		    while(rs.next())
		    	codes.add(rs.getString(1));
		    rs.close();
		    ps.close();
		    cnx.close();
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		System.out.println("[DEBUG] SELECTION DES CODES POSTAUX "+codes.size());
		
		sql = "select pk_user_code_postal, libelle from user_code_postal";
		try{
			Class.forName("org.postgresql.Driver");
			
		    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
		    PreparedStatement ps = cnx.prepareStatement(sql);
		    ResultSet rs = ps.executeQuery();
		    while(rs.next())
		    	CPS.put(rs.getString(2), new Integer(rs.getInt(1)));
		    cnx.close();
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		sql = "insert into user_code_postal (libelle) values (?) returning pk_user_code_postal";
		for(String c : codes){
			if(CPS.containsKey(c))
				continue;
			
			try{
				Class.forName("org.postgresql.Driver");
				
			    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
			    PreparedStatement ps = cnx.prepareStatement(sql);
			    ps.setString(1, c);
			    ResultSet rs = ps.executeQuery();
			    if(rs.next())
			    	CPS.put(c, new Integer(rs.getInt(1)));
			    rs.close();
			    ps.close();
			    cnx.close();
				System.out.println("[DEBUG] INSERTION CP "+(codes.indexOf(c)+1)+"/"+codes.size());
			}catch(Exception exc){
				exc.printStackTrace();
			}
		}
		
		sql = "update user_adresse set fk_user_code_postal = ? where cp=?";
		for(String c : codes){
			try{
				Class.forName("org.postgresql.Driver");

			    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
			    PreparedStatement ps = cnx.prepareStatement(sql);
			    ps.setInt(1, CPS.get(c));
			    ps.setString(2, c);
			    ps.execute();
			    ps.close();
			    cnx.close();
				System.out.println("[DEBUG] MAJ des Adresses "+(codes.indexOf(c)+1)+"/"+codes.size());
			}catch(Exception exc){
				exc.printStackTrace();
			}
		}
		
		//	Villes
		sql = "select district from user_adresse where fk_user_ville is null group by district ";
		try{
			Class.forName("org.postgresql.Driver");

		    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
		    PreparedStatement ps = cnx.prepareStatement(sql);
		    ResultSet rs = ps.executeQuery();
		    while(rs.next())
		    	villes.add(rs.getString(1));
		    rs.close();
		    ps.close();
		    cnx.close();
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		System.out.println("[DEBUG] SELECTION DES VILLES "+villes.size());
		
		sql = "select pk_user_ville, libelle from user_ville";
		try{
			Class.forName("org.postgresql.Driver");
			
		    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
		    PreparedStatement ps = cnx.prepareStatement(sql);
		    ResultSet rs = ps.executeQuery();
		    while(rs.next())
		    	VS.put(rs.getString(2), new Integer(rs.getInt(1)));
		    cnx.close();
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		sql = "insert into user_ville (libelle) values (?) returning pk_user_ville";
		for(String c : villes){
			if(VS.containsKey(c))
				continue;
			try{
				Class.forName("org.postgresql.Driver");

			    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
			    PreparedStatement ps = cnx.prepareStatement(sql);
			    ps.setString(1, c);
			    ResultSet rs = ps.executeQuery();
			    if(rs.next())
			    	VS.put(c, new Integer(rs.getInt(1)));
			    rs.close();
			    ps.close();
			    cnx.close();
				System.out.println("[DEBUG] INSERTION VILLE "+(villes.indexOf(c)+1)+"/"+villes.size());
			}catch(Exception exc){
				exc.printStackTrace();
			}
		}
		
		sql = "update user_adresse set fk_user_ville = ? where district=?";
		for(String c : villes){
			try{
				Class.forName("org.postgresql.Driver");

			    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
			    PreparedStatement ps = cnx.prepareStatement(sql);
			    ps.setInt(1, VS.get(c));
			    ps.setString(2, c);
			    ps.execute();
			    ps.close();
			    cnx.close();
				System.out.println("[DEBUG] MAJ Adresse "+(villes.indexOf(c)+1)+"/"+villes.size());
			}catch(Exception exc){
				exc.printStackTrace();
			}
		}
	}

}
