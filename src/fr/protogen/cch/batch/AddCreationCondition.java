package fr.protogen.cch.batch;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import fr.protogen.masterdata.dal.DBUtils;

public class AddCreationCondition {
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
		    	sql="ALTER TABLE "+t+"  ADD COLUMN created timestamp with time zone NOT NULL DEFAULT '2014-03-04 00:00:00+00'::timestamp with time zone";
		    	ps = cnx.prepareStatement(sql);
		    	try{
		    		ps.execute();
		    	}catch(Exception e){
		    		e.printStackTrace();
		    	}finally{
		    		ps.close();
		    	}
		    	ps.close();
		    	sql="ALTER TABLE "+t+"  ADD COLUMN updated timestamp with time zone NOT NULL DEFAULT '2014-03-04 00:00:00+00'::timestamp with time zone";
		    	ps = cnx.prepareStatement(sql);
		    	try{
		    		ps.execute();
		    	}catch(Exception e){
		    		e.printStackTrace();
		    	}finally{
		    		ps.close();
		    	}
		    	ps.close();
		    	sql="ALTER TABLE "+t+"  ADD COLUMN created_by integer NOT NULL DEFAULT 0";
		    	ps = cnx.prepareStatement(sql);
		    	try{
		    		ps.execute();
		    	}catch(Exception e){
		    		e.printStackTrace();
		    	}finally{
		    		ps.close();
		    	}
		    	ps.close();
		    	sql="ALTER TABLE "+t+"  ADD COLUMN updated_by integer NOT NULL DEFAULT 0";
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
