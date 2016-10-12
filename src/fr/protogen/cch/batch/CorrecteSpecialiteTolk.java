package fr.protogen.cch.batch;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

public class CorrecteSpecialiteTolk {

	public static void main(String[] args) {
		String fileName = "C:\\Users\\jakjoud\\Documents\\projects support files\\TOLK\\specialites.csv";
		
		List<String> lines = new ArrayList<String>();
		try{
			lines = IOUtils.readLines(new FileInputStream(fileName));
			lines.remove(0);
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		try{
			String script = "";
			for(String l : lines){
				if(l.split(";").length<2)
					continue;
				String code = l.split(";")[0];
				String newlib = l.split(";")[1];
				
				String sql = "update user_savoir_faire set libelle='"+newlib+"' where code='"+code+"'";
				script = script+sql+";";
			}
			IOUtils.write(script, new FileOutputStream("C:\\Users\\jakjoud\\Documents\\projects support files\\TOLK\\specialites.sql"));
		}catch(Exception exc){
			exc.printStackTrace();
		}
	}

}
