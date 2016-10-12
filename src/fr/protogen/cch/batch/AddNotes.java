package fr.protogen.cch.batch;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import fr.protogen.masterdata.dal.DBUtils;

public class AddNotes {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try{
			Class.forName("org.postgresql.Driver");

		    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
		    String sql = "select data_reference from c_businessclass";
		    PreparedStatement ps = cnx.prepareStatement(sql);
		    
		    List<String> tables = new ArrayList<String>();
		    ResultSet rs = ps.executeQuery();
		    
		    while(rs.next())
		    	tables.add(rs.getString(1));
		    
		    rs.close();
		    ps.close();
		    
		    
		    for(String t : tables){
		    	sql="ALTER TABLE "+t+"  ADD COLUMN notes text";
		    	ps = cnx.prepareStatement(sql);
		    	try{
		    		ps.execute();
		    	}catch(Exception e){
		    		e.printStackTrace();
		    	}finally{
		    		ps.close();
		    	}
		    	ps.close();
		    	
		    }
		    
		}catch(Exception e){
			e.printStackTrace();
		}

	}

}
