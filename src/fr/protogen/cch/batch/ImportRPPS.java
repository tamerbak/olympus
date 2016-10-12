package fr.protogen.cch.batch;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import fr.protogen.masterdata.dal.DBUtils;


public class ImportRPPS {

	public static void main(String[] args) {
		importRPPS();
		//importAdresses();
	}
	private static void importAdresses() {
		String dir = "C:\\Users\\jakjoud\\Documents\\projects support files\\bano-data-master\\";
		File directory = new File(dir);
		String[] files = directory.list();
		int i = 1;
		for(String f : files){
			System.out.println("IMPORTING FILE "+f+" ("+i+"/"+files.length+")");
			importAdresseFile(dir+f);
			i++;
		}
	}
	private static void importAdresseFile(String fileName) {
		List<String> lines = new ArrayList<String>();
		try{
			lines = IOUtils.readLines(new FileInputStream(fileName));
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		for(String l : lines){
			String[] T = l.split(",");
			String id = T[0];
			String num = T[1];
			String add = T[2];
			String cp = T[3];
			String dist = T[4];
			
			String sql = "INSERT INTO user_adresse (adresse, num, cp, district, code) VALUES "
					+ "(?, ?, ?, ?, ?)";
			try{
				Class.forName("org.postgresql.Driver");

			    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
			    PreparedStatement ps = cnx.prepareStatement(sql);
			    ps.setString(1, add);
			    ps.setString(2, num);
			    ps.setString(3, cp);
			    ps.setString(4, dist);
			    ps.setString(5, id);
			    ps.execute();
			    ps.close();
			    cnx.close();
			}catch(Exception exc){
				exc.printStackTrace();
			}
			System.out.println("INSERT ADRESSE ("+lines.indexOf(l)+"/"+lines.size()+")");
		}
	}
	private static void importRPPS(){
		String fileName = "C:\\Users\\jakjoud\\Documents\\projects support files\\dentistes.csv";
		List<String> lines = new ArrayList<String>();
		try{
			lines = IOUtils.readLines(new FileInputStream(fileName));
			if(lines.size()>0)
				lines.remove(0);
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		for(String l : lines){
			String[] T = l.split(",");
			String id = T[0];
			String nom = T[3];
			String prenom = T[4];
			String cat = T[7];
			String sf = "";
			if(T.length>8)
				sf = T[8];
			
			String sql = "INSERT INTO user_praticien (identifiant_rpps, nom, prenom, fk_user_savoir_faire, fk_user_categorie_professionnelle, fk_user_civilite_exercice, fk_user_specialite) "
					+ "VALUES (?, ?, ?, ?, ?, 40, 40)";
			int idSF=43;
			if(sf.equals("SCD01"))
				idSF=40;
			if(sf.equals("SCD02"))
				idSF=41;
			if(sf.equals("SCD03"))
				idSF=42;
			
			int idCat = 40;
			if(cat.equals("Militaire"))
				idCat=41;
			
			try{
				Class.forName("org.postgresql.Driver");

			    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
			    PreparedStatement ps = cnx.prepareStatement(sql);
			    ps.setString(1, id);
			    ps.setString(2, nom);
			    ps.setString(3, prenom);
			    ps.setInt(4, idSF);
			    ps.setInt(5, idCat);
			    ps.execute();
			    ps.close();
			    cnx.close();
			}catch(Exception exc){
				exc.printStackTrace();
			}
			
		}
	}
}
