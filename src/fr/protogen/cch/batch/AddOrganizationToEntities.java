package fr.protogen.cch.batch;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import fr.protogen.masterdata.dal.DBUtils;

public class AddOrganizationToEntities {
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
		    	sql="ALTER TABLE <<tablename>>    ADD COLUMN id_organization integer NOT NULL DEFAULT 0;  ";
		    	sql = sql.replaceAll("<<tablename>>", t);
		    	ps = cnx.prepareStatement(sql);
		    	System.out.println("*************************************");
		    	System.out.println("\t"+sql);
		    	System.out.println("*************************************");
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
