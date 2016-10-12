package fr.protogen.cch.batch;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import fr.protogen.masterdata.dal.DBUtils;

public class MenuFix {

	public static void main(String[] args) {
		try{
			Class.forName("org.postgresql.Driver");

		    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
		    
			Map<Integer, Integer> windows = new HashMap<Integer, Integer>();
			String sql = "select id, id_entity from c_window where id_windowtype=1";
			PreparedStatement ps = cnx.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				windows.put(new Integer(rs.getInt(1)), new Integer(rs.getInt(2)));
			}
			rs.close();
			ps.close();
			
			for(Integer idOWindow : windows.keySet()){
				int idw = idOWindow.intValue();
				int ide = windows.get(idOWindow).intValue();
				
				sql = "select id from c_window where id_entity=? and id_windowtype=2";
				ps = cnx.prepareStatement(sql);
				ps.setInt(1, ide);
				rs = ps.executeQuery();
				int idform=0;
				if(rs.next())
					idform = rs.getInt(1);
				rs.close();
				ps.close();
				
				if(idform==0)
					continue;
				
				sql = "update c_window set form_window=? where id_windowtype=1 and id=?";
				ps = cnx.prepareStatement(sql);
				ps.setInt(1, idform);
				ps.setInt(2, idw);
				ps.execute();
				ps.close();
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}

	}

}
