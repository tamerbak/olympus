package fr.protogen.cch.batch;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import fr.protogen.masterdata.dal.DBUtils;

public class DefaultValuesBatch {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String dvsSql = "update c_attribute set default_value='' where id_attributetype=2";
		String dvnSql = "update c_attribute set default_value='0' where id_attributetype=1 OR id_attributetype=4 OR id_attributetype=5";
		String dvdSql = "update c_attribute set default_value='2013-07-15 00:00:00+00' where id_attributetype=3";
		
		try{
			Class.forName("org.postgresql.Driver");

		    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
		    
			Statement st = cnx.createStatement();
			
			st.execute(dvdSql);
			st.execute(dvnSql);
			st.execute(dvsSql);
			
			st.close();
			
		}catch(Exception exc){
			exc.printStackTrace();
		}
	}

}
