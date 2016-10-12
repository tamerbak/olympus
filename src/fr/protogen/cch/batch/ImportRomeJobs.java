package fr.protogen.cch.batch;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import fr.protogen.masterdata.dal.DBUtils;

public class ImportRomeJobs {
	private static Connection cnx = null;
	public static void main(String[] args){
		//	Load file lines
		String fileName = "D:\\tmp\\jobs_rome.csv";
		
		List<String> lines = new ArrayList<String>();
		try{
			InputStream is = new FileInputStream(fileName);
			lines = IOUtils.readLines(is);
			is.close();
			Class.forName("org.postgresql.Driver");
			cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
		}catch(Exception exc){
			exc.printStackTrace();
			return;
		}
		
		System.out.println("Lines to import : "+lines.size());
		
		String codeMetier="";
		String codeRubriqueMetier="";
		int idMetier=0;
		int idRubrique=0;
		int i = 1;
		for(String l : lines){
			System.out.println("importing : "+i+"/"+lines.size());
			i++;
			
			String[] c = l.split(",");
			if(c == null || c.length==0)
				continue;
			
			if(!c[0].trim().equals(codeMetier.trim())){
				System.out.println("importing sector "+c[3]);
				codeMetier = c[0];
				idMetier = addNewMetier(c[3].replaceAll("\"", ""));
				continue;
			}
			if(!c[1].trim().equals(codeRubriqueMetier.trim())){
				System.out.println("importing group "+c[3]);
				codeRubriqueMetier = c[1];
				idRubrique = addNewRubrique(c[3].replaceAll("\"", ""), idMetier);
				continue;
			}
			System.out.println("importing job "+c[3]);
			String libelle = c[3].replaceAll("\"", "");
			String codeOGR = c.length==5?c[4]:"";
			int idCC = 40;
			int idNCC = 40;
			
			addNewJob(libelle, codeOGR, idCC, idNCC, idRubrique, idMetier);
		}
		
		try{
			cnx.close();
		}catch(Exception exc){
			exc.printStackTrace();
		}
	}

	private static void addNewJob(String libelle, String codeOGR, int idCC, int idNCC, int idRubrique, int idMetier) {
		String sql = "insert into user_job (libelle, code_ogr, fk_user_convention_collective, fk_user_niveau_convention_collective, fk_user_rubrique_metier, fk_user_metier) "
				+ "values (?, ?, ?, ?, ?, ?)";
		
		try{
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setString(1, libelle);
			ps.setString(2, codeOGR);
			ps.setInt(3, idCC);
			ps.setInt(4, idNCC);
			ps.setInt(5, idRubrique);
			ps.setInt(6, idMetier);
			ps.execute();
			ps.close();
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
	}

	private static int addNewRubrique(String libelle, int idMetier) {
		int id = 0;
		String sql = "insert into user_rubrique_metier (libelle, fk_user_metier) values (?,?) returning pk_user_rubrique_metier";
		try{
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setString(1, libelle);
			ps.setInt(2, idMetier);
			ResultSet rs = ps.executeQuery();
			if(rs.next())
				id = rs.getInt(1);
			rs.close();
			ps.close();
		}catch(Exception exc){
			exc.printStackTrace();
		}
		return id;
	}

	private static int addNewMetier(String libelle) {
		int id = 0;
		String sql = "insert into user_metier (libelle) values (?) returning pk_user_metier";
		try{
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setString(1, libelle);
			ResultSet rs = ps.executeQuery();
			if(rs.next())
				id = rs.getInt(1);
			rs.close();
			ps.close();
		}catch(Exception exc){
			exc.printStackTrace();
		}
		return id;
	}
}
