package fr.protogen.masterdata.dal.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import fr.protogen.masterdata.dal.DBUtils;
import fr.protogen.masterdata.model.GResource;

public class ResourcesDAO {
	private static ResourcesDAO instance = null;
	public static synchronized ResourcesDAO getInstance(){
		if(instance == null)
			instance = new ResourcesDAO();
		return instance;
	}
	private ResourcesDAO(){}
	
	public List<GResource> loadResources(){
		List<GResource> res = new ArrayList<GResource>();
		
		String sql = "select id, res_key, res_name, res_type from g_resource";
		
		try{
			Class.forName("org.postgresql.Driver");
		    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
		    PreparedStatement ps = cnx.prepareStatement(sql);
		    ResultSet rs = ps.executeQuery();
		    while(rs.next()){
		    	GResource r = new GResource();
		    	r.setId(rs.getInt(1));
		    	r.setKey(rs.getString(2));
		    	r.setName(rs.getString(3));
		    	r.setType(rs.getString(4));
		    	res.add(r);
		    }
		    rs.close();
		    ps.close();
		    cnx.close();
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		return res;
	}
	
	public void saveResource(GResource r) {
		String sql = "insert into g_resource (res_key, res_name, res_type, fichier) values (?, ?, ?, ?) returning id";
		try{
			Class.forName("org.postgresql.Driver");
		    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
		    PreparedStatement ps = cnx.prepareStatement(sql);
		    ps.setString(1, r.getKey());
		    ps.setString(2, r.getName());
		    ps.setString(3, r.getType());
		    ps.setBytes(4, r.getFile());
		    ResultSet rs = ps.executeQuery();
		    if(rs.next()){
		    	r.setId(rs.getInt(1));
		    }
		    rs.close();
		    ps.close();
		    cnx.close();
		}catch(Exception exc){
			exc.printStackTrace();
		}
	}
}
