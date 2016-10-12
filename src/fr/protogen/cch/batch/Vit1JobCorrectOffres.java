package fr.protogen.cch.batch;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import fr.protogen.masterdata.dal.DBUtils;

public class Vit1JobCorrectOffres {
	public static void main(String[] args) {
		try{
			Class.forName("org.postgresql.Driver");
			Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
			String sql = "select pk_user_salarie, nom, prenom from user_salarie";
			Map<Integer, String> vals = new HashMap<Integer, String>();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				vals.put(rs.getInt(1), rs.getString(2)+" "+rs.getString(3));
			}
			rs.close();
			ps.close();
			sql = "insert into user_offre_salarie (fk_user_salarie, intitule) values (?,?)";
			for(Integer id : vals.keySet()){
				ps = cnx.prepareStatement(sql);
				ps.setInt(1, id);
				ps.setString(2, vals.get(id));
				ps.execute();
				ps.close();
			}
			cnx.close();
		} catch(Exception exc){
			exc.printStackTrace();
		}
		
	}
}
