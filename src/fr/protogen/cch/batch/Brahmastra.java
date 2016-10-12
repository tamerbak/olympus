package fr.protogen.cch.batch;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import fr.protogen.masterdata.dal.DBUtils;
import fr.protogen.masterdata.model.CBusinessClass;

public class Brahmastra {

	public static void main(String[] args) {
		String sql = "select data_reference,id from c_businessclass";
	    List<CBusinessClass> tables = new ArrayList<CBusinessClass>();
		try{
			Class.forName("org.postgresql.Driver");

		    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
		    
		    PreparedStatement ps = cnx.prepareStatement(sql);
		    ResultSet rs = ps.executeQuery();
		    while(rs.next()){
		    	CBusinessClass c = new CBusinessClass();
		    	c.setDataReference(rs.getString(1));
		    	c.setId(rs.getInt(2));
		    	tables.add(c);
		    }

		    
		    rs.close();
		    ps.close();
		    
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		while(tables.size()>0){
			List<CBusinessClass> toremove = new ArrayList<CBusinessClass>();
			Connection cnx=null;
			for(CBusinessClass e  : tables){
				try{
					
					Class.forName("org.postgresql.Driver");
					cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
					Statement st = cnx.createStatement();
					
					String query = "drop table if exists "+e.getDataReference();
					st.execute(query);
					
					query = "drop sequence if exists "+e.getDataReference()+"_seq";
					st.execute(query);
					
					query = "delete from c_attribute where id_class="+e.getId();
					st.execute(query);
					
					query = "delete from c_businessclass where id="+e.getId();
					st.execute(query);
					
					
					toremove.add(e);
					st.close();
					
				}catch(Exception exc){
					String dummy = "";
					dummy = dummy+exc.getLocalizedMessage();
				}
			}
			tables.removeAll(toremove);
		}

	}

}
