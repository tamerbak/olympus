package fr.protogen.cch.batch;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;

import fr.protogen.masterdata.dal.DBUtils;
import fr.protogen.masterdata.model.MDocument;


public class DocumentConvertToZip {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		try{
					
			Class.forName("org.postgresql.Driver");
			
			Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
			Statement st = cnx.createStatement();
			
			String sql = "select id, stream, jasper_file from m_document";
			ResultSet rs = st.executeQuery(sql);
			List<MDocument> docs = new ArrayList<MDocument>();
			while(rs.next()){
				MDocument d = new MDocument();
				d.setId(rs.getInt(1));
				d.setStream(rs.getString(2)+"#:::#"+rs.getString(3));
				docs.add(d);
			}
			rs.close();
			st.close();
			
			//	for each document
			for(MDocument d : docs){
				//	Construct tempfile
				File tfile = new File("main.jrxml");
				String ziptitle = d.getStream().split("#:::#")[0];
				String jcontent = d.getStream().split("#:::#")[1];
				
				if(!tfile.exists())
					tfile.createNewFile();
				
				FileUtils.writeStringToFile(tfile, jcontent);
				
				//	Zip it
				
				FileOutputStream fos = new FileOutputStream(ziptitle+".zip");
	    		ZipOutputStream zos = new ZipOutputStream(fos);
	    		ZipEntry ze= new ZipEntry("main.jrxml");
	    		zos.putNextEntry(ze);
	    		FileInputStream in = new FileInputStream("main.jrxml");
	    		int len;
	    		byte[] buffer = new byte[1024];
	    		while ((len = in.read(buffer)) > 0) {
	    			zos.write(buffer, 0, len);
	    		}
	 
	    		in.close();
	    		zos.closeEntry();
	 
	    		zos.close();
	    		
	    		
	    		// Now insert it
	    		File zipfile = new File(ziptitle+".zip");
	    		InputStream is = new FileInputStream(zipfile);
	    		sql = "update m_document set zip_content=? where id=?";
	    		PreparedStatement ps = cnx.prepareStatement(sql);
	    		ps.setBinaryStream(1, is, (int)zipfile.length());
	    		ps.setInt(2, d.getId());
	    		
	    		
	    		ps.execute();
	    		
	    		//	Delete temporary
	    		tfile.delete();
			}
		} catch(Exception exc){
			exc.printStackTrace();
		}
	}

}
