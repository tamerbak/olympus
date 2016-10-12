package fr.protogen.cch.batch;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import fr.protogen.masterdata.dal.DBUtils;

public class ImportEmployeurVit1job {
	public static void main(String[] args) {
		String fileName = "C:\\Users\\jakjoud\\Documents\\projects support files\\Vit1job\\employeurs4.csv";
		List<String> lines = new ArrayList<String>();
		Connection cnx = null;
		BufferedWriter bw = null;
		try{
			File file = new File("C:\\Users\\jakjoud\\Documents\\projects support files\\Vit1job\\employeurs4.csv.log");
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			bw = new BufferedWriter(fw);
			Class.forName("org.postgresql.Driver");
			cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
			
			lines = IOUtils.readLines(new FileInputStream(fileName));
			if(lines.size()>0)
				lines.remove(0);
			bw.write("[VIT1JOB]Nombre des employeurs\t"+lines.size());
			bw.newLine();
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		System.out.println("[VIT1JOB]Nombre des employeurs\t"+lines.size());
		
		int index=0;
		for(String l : lines){
			index++;
			System.out.println("[VIT1JOB]Employeur\t"+index+"/"+lines.size());
			String[] T = l.split(",");
			String raisonSociale = T[0].trim();
			String adresse = T[1].trim();
			String cp = T[2].trim();
			String ville = T[3].trim();
			String civilite = T[4].trim();
			String nom = T[5].trim();
			String prenom = T[6].trim();
			String telephone = T[7].trim();
			String naf = T[8].trim();
			String activite = T[9].trim();
			String rubriquePro = T[10].trim();
			String siege = T[11].trim();
			String email = T.length>12?T[12].trim():"";
			int idciv=civilite.equals("M.")?40:44;
			int idcp = 0;
			int idville=0;
			
			/*
			 * CODES POSTAUX
			 */
			String sql = "select pk_user_code_postal from user_code_postal where libelle=?";
			try{
				PreparedStatement ps = cnx.prepareStatement(sql);
			    ps.setString(1, cp);
			    ResultSet rs = ps.executeQuery();
			    if(rs.next())
			    	idcp = rs.getInt(1);
			    rs.close();
			    ps.close();
			    bw.write("[VIT1JOB]Employeur\t"+index+"/"+lines.size());
			    bw.newLine();
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
			
			/*
			 * VILLES
			 */
			sql = "select pk_user_ville from user_ville where nom=?";
			try{
				PreparedStatement ps = cnx.prepareStatement(sql);
			    ps.setString(1, ville);
			    ResultSet rs = ps.executeQuery();
			    if(rs.next())
			    	idville = rs.getInt(1);
			    rs.close();
			    ps.close();
			}catch(Exception exc){
				exc.printStackTrace();
			}
			if(idville == 0){
				sql = "insert into user_ville (nom) values (?) returning pk_user_ville";
				try{
					PreparedStatement ps = cnx.prepareStatement(sql);
				    ps.setString(1, ville);
				    ResultSet rs = ps.executeQuery();
				    if(rs.next())
				    	idville = rs.getInt(1);
				    rs.close();
				    ps.close();
				}catch(Exception exc){
					exc.printStackTrace();
				}
			}
			
			sql = "select pk_user_employeur from user_employeur where nom_ou_raison_sociale=?";
			boolean flag = false;
			try{
				PreparedStatement ps = cnx.prepareStatement(sql);
			    ps.setString(1, cp);
			    ResultSet rs = ps.executeQuery();
			    if(rs.next())
			    	flag=true;
			    rs.close();
			    ps.close();
			}catch(Exception exc){
				exc.printStackTrace();
			}
			
			if(flag)
				continue;
			
			sql = "insert into user_employeur (nom_ou_raison_sociale, fk_user_ville, fk_user_code_postal, "
					+ "num, fk_user_civilite, nom_du_dirigeant, prenom_du_dirigeant, code_ape, telephone, email) VALUES "
					+ "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			try{
				PreparedStatement ps = cnx.prepareStatement(sql);
				ps.setString(1, raisonSociale);
				ps.setInt(2, idville);
				ps.setInt(3, idcp);
				ps.setString(4, adresse);
				ps.setInt(5, idciv);
				ps.setString(6, nom);
				ps.setString(7, prenom);
				ps.setString(8, naf);
				ps.setString(9, telephone);
				ps.setString(10, email);
			    ps.execute();
			    ps.close();
			}catch(Exception exc){
				exc.printStackTrace();
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
