package fr.protogen.cch.batch;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import fr.protogen.masterdata.dal.DBUtils;

public class ImportCPVilleTolk {
	public static void main(String[] args) {
		importFromFile();
		importCorrespondance();
	}

	private static void importCorrespondance() {
		
		
	}

	private static void importFromFile() {
		String fileName = "C:\\Users\\jakjoud\\Documents\\projects support files\\TOLK\\cp_villes1.csv";
		List<String> lines = new ArrayList<String>();
		Connection cnx = null;
		BufferedWriter bw = null;
		try{
			File file = new File("C:\\Users\\jakjoud\\Documents\\projects support files\\TOLK\\cp_villes1.csv.log");
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			bw = new BufferedWriter(fw);
			Class.forName("org.postgresql.Driver");
			cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
			
			lines = IOUtils.readLines(new FileInputStream(fileName));
			lines.remove(0);
			bw.write("[VIT1JOB]Nombre des correspondances\t"+lines.size());
			bw.newLine();
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		System.out.println("[VIT1JOB]Nombre des correspondances\t"+lines.size());
		
		int index=0;
		for(String l : lines){
			index++;
			System.out.println("[VIT1JOB]Correspondances\t"+index+"/"+lines.size());
			String[] T = l.split(",");
			String cp = T[1];
			String ville = T[0];
			int idcp = 0;
			int idv = 0;
			
			String sql = "select pk_user_code_postal from user_code_postal where libelle=? or libelle=?";
			try{
				bw.write("[VIT1JOB]Correspondances\t"+index+"/"+lines.size());
				PreparedStatement ps = cnx.prepareStatement(sql);
				ps.setString(1, cp);
				ps.setString(2, "0"+cp);
				ResultSet rs = ps.executeQuery();
				if(rs.next())
					idcp = rs.getInt(1);
				rs.close();
				ps.close();
			}catch(Exception exc){
				exc.printStackTrace();
			}
			
			if(idcp == 0){
				sql = "insert into user_code_postal (libelle) values (?) returning pk_user_code_postal";
				try{
					PreparedStatement ps = cnx.prepareStatement(sql);
					ps.setString(1, cp);
					ResultSet rs = ps.executeQuery();
					if(rs.next())
						idcp = rs.getInt(1);
					rs.close();
					ps.close();
				}catch(Exception exc){
					exc.printStackTrace();
				}
			}
			
			
			sql = "select pk_user_ville from user_ville where libelle=?";
			try{
				PreparedStatement ps = cnx.prepareStatement(sql);
				ps.setString(1, ville);
				ResultSet rs = ps.executeQuery();
				if(rs.next())
					idv = rs.getInt(1);
				rs.close();
				ps.close();
			}catch(Exception exc){
				exc.printStackTrace();
			}
			
			if(idv==0){
				sql = "insert into user_ville (libelle, fk_user_code_postal) values (?, ?) returning pk_user_ville";
				try{
					PreparedStatement ps = cnx.prepareStatement(sql);
					ps.setString(1, ville);
					ps.setInt(2, idcp);
					ResultSet rs = ps.executeQuery();
					if(rs.next())
						idv = rs.getInt(1);
					rs.close();
					ps.close();
				}catch(Exception exc){
					exc.printStackTrace();
				}
			} else {
				sql = "update user_ville set fk_user_code_postal=? where pk_user_ville=?";
				try{
					PreparedStatement ps = cnx.prepareStatement(sql);
					ps.setInt(1, idcp);
					ps.setInt(2, idv);
					ps.execute();
					ps.close();
				}catch(Exception exc){
					exc.printStackTrace();
				}
			}
		}
		
		try{
		    cnx.close();
		    bw.close();
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
	}
}
