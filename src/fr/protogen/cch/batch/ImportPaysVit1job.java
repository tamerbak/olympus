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

public class ImportPaysVit1job {
	public static void main(String[] args) {
		String fileName = "C:\\Users\\jakjoud\\Documents\\projects support files\\Vit1job\\indicatifs.csv";
		List<String> lines = new ArrayList<String>();
		Connection cnx = null;
		BufferedWriter bw = null;
		try{
			File file = new File("C:\\Users\\jakjoud\\Documents\\projects support files\\Vit1job\\indicatifs.csv.log");
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			bw = new BufferedWriter(fw);
			Class.forName("org.postgresql.Driver");
			cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
			
			lines = IOUtils.readLines(new FileInputStream(fileName));
			
			bw.write("[VIT1JOB]Nombre des pays\t"+lines.size());
			bw.newLine();
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		System.out.println("[VIT1JOB]Nombre des pays\t"+lines.size());
		
		int index=0;
		for(String l : lines){
			index++;
			System.out.println("[VIT1JOB]Pays\t"+index+"/"+lines.size());
			String[] T = l.split(",");
			
			String sql = "insert into user_pays (nom, indicatif_telephonique) values (?,?)";
			try{
				PreparedStatement ps = cnx.prepareStatement(sql);
				ps.setString(1, T[0]);
				ps.setString(2, "00"+T[1]);
			    ps.execute();
			    
			    ps.close();
			    bw.write("[VIT1JOB]Pays\t"+index+"/"+lines.size());
			    bw.newLine();
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
