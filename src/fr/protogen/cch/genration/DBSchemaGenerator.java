package fr.protogen.cch.genration;

import java.util.ArrayList;
import java.util.List;

import fr.protogen.masterdata.model.*;

public class DBSchemaGenerator {
	public String createSchema(List<CBusinessClass> entities){
		String script = "";
		
		//	Sorting
		List<String> done = new ArrayList<String>();
		List<CBusinessClass> allentities = new ArrayList<CBusinessClass>();
		allentities.addAll(entities);
		
		for(CBusinessClass e : entities){
			for(CAttribute a : e.getAttributes()){
				if(a.isAutoValue()){
					String sequery = "CREATE SEQUENCE autoseq_"+a.getEntity().getDataReference()+"_"+a.getDataReference()+"  INCREMENT 1  MINVALUE 10  MAXVALUE 9223372036854775807  START 21  CACHE 1;";
					script = script+sequery;
					sequery = "ALTER TABLE autoseq_"+a.getEntity().getDataReference()+"_"+a.getDataReference()+"  OWNER TO jakj;";
					script = script+sequery;
				}
			}
		}
		
		while(allentities.size()>0){
			List<CBusinessClass> toRemove = new ArrayList<CBusinessClass>();
			boolean flag=true;
			for(CBusinessClass e : allentities){
				
				for(CAttribute a : e.getAttributes()){
					if(a.getDataReference().startsWith("fk") && !a.isMultiple()){
						String referencedDR = a.getDataReference().substring(3);
						flag=false;
						for(String ee : done){
							if(ee.equals(referencedDR)){
								flag=true;
								break;
							}
						}
					}
				}
				if(!flag){
					flag=true;
					continue;
				}
					
				
				for(CBusinessClass se: allentities){
					if(e==se)
						continue;
					for(CAttribute a : se.getAttributes()){
						if(a.getDataReference().startsWith("fk") && a.isMultiple() && a.getDataReference().endsWith(e.getDataReference())){
							flag=false;
							for(String ee : done){
								if(ee.equals(se.getDataReference())){
									flag=true;
									break;
								}
							}
						}
					}
				}
				if(!flag){
					flag=true;
					continue;
				}
				
				script = script+buildTableScript(e,entities);
				done.add(e.getDataReference());
				toRemove.add(e);
			}
			allentities.removeAll(toRemove);
		}
		
		//	generate multiple fks
		for(CBusinessClass e : entities){
			for(CAttribute a : e.getAttributes()){
				if(a.isMultiple()){
					String table = a.getDataReference().split("__")[0].substring(3);
					String reftable=a.getDataReference().split("__")[1];
					String query="alter table "+table+" add column "+a.getDataReference()+" ";
					String type="";
					switch(a.getCAttributetype().getId()){
						case 1:type = "integer ";break;
						case 2:type = "text ";break;
						case 3:type = "timestamp with time zone ";break;
						case 4:case 8:type = "double precision ";break;
						case 5:type = "integer ";break;
						case 6:type = "bytea ";break;
					}
					
					query = query+type+";";
					query = query+"alter table "+table+" add foreign key ("+a.getDataReference()+") references "+reftable+" (pk_"+reftable+");";
					script=script+query;
				}
			}
		}
		
		
		return script;
	}

	private String buildTableScript(CBusinessClass e,
			List<CBusinessClass> entities) {
		// TODO Auto-generated method stub
		String tableName = e.getDataReference();
		String primaryKey = "pk_"+e.getDataReference();
		List<String> references = new ArrayList<String>();
		//	SEQUENCE
		String query = "CREATE SEQUENCE "+tableName+"_seq  INCREMENT 1  MINVALUE 1  MAXVALUE 9223372036854775807  START 40  CACHE 1; ALTER TABLE "+tableName+"_seq  OWNER TO jakj;";
		//	TABLE
		query = query+"CREATE TABLE "+e.getDataReference()+" ( pk_"+tableName+" integer NOT NULL DEFAULT nextval('"+tableName+"_seq'::regclass),protogen_user_id integer NOT NULL DEFAULT 0, "
									+ " created timestamp with time zone NOT NULL DEFAULT '2014-03-04 00:00:00+00',"
									+ " updated timestamp with time zone NOT NULL DEFAULT '2014-03-04 00:00:00+00',"
									+ " created_by integer NOT NULL DEFAULT 0,"
									+ " updated_by integer NOT NULL DEFAULT 0,"
									+ " dirty character(1) NOT NULL DEFAULT 'N'::bpchar,";
		//	ATTRIBUTES
		for(CAttribute a : e.getAttributes()){
			
			if(a.getDataReference().startsWith("pk_"))
				continue;
			if(a.isMultiple())
				continue;
			if(a.getDataReference().startsWith("fk_"))
				references.add(a.getDataReference().substring(3));
			String aline = a.getDataReference()+" ";
			switch(a.getCAttributetype().getId()){
			case 1:aline = aline+"integer ";break;
			case 2:aline = aline+"text ";break;
			case 3:aline = aline+"timestamp with time zone ";break;
			case 4:case 8:aline = aline+"double precision ";break;
			case 5:case 7:aline = aline+"integer ";break;
			case 6:aline = aline+"bytea ";break;
			case 9:aline = aline+" character(3)";
			}
			if(a.isMandatory())
				aline = aline+"NOT NULL ";
			
			if(a.isAutoValue()){
				aline = aline+" DEFAULT nextval('autoseq_"+a.getEntity().getDataReference()+"_"+a.getDataReference()+"'::regclass),";
			} else {
				if(a.getDefaultValue()==null || a.getDefaultValue().length()==0)
					aline = aline+",";
				else  {
					String defaultvalue=" DEFAULT "+a.getDefaultValue();
					switch(a.getCAttributetype().getId()){
						case 1:case 4:case 5:case 8:defaultvalue="DEFAULT "+a.getDefaultValue();break;
						case 2:defaultvalue="DEFAULT '"+a.getDefaultValue()+"'";break;
						case 3:defaultvalue="DEFAULT '"+a.getDefaultValue()+" 00:00:00+00'";break;
						case 9:defaultvalue="DEFAULT '"+(a.getDefaultValue().toLowerCase().equals("oui")?"Y":"N");
					}
					aline = aline+" "+defaultvalue+",";
				}
					
			}
			
			query = query+aline;
		}
		for(CBusinessClass en : entities){
			if(en.getDataReference().equals(e.getDataReference()))
				continue;
			for(CAttribute a : en.getAttributes()){
				if(a.getDataReference().equals("fk_"+e.getDataReference()) && a.isMultiple()){
					references.add(en.getDataReference());
					String aline = "fk_"+en.getDataReference()+" integer,";
					query = query+aline;
				}
			} 
		}
		//	PRIMARY KEY
		query = query+"CONSTRAINT "+primaryKey+" PRIMARY KEY ("+primaryKey+")";
		//	FOREIGN KEYS
		if(references.size()>0)
			query=query+", ";
		for(String r : references){
			
			String fkey = "fk_"+r;
			String pkey = "pk_"+r;
			String aline = " CONSTRAINT "+fkey+" FOREIGN KEY ("+fkey+") REFERENCES "+r+" ("+pkey+") MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION,";
			
			query = query+aline;
		}
		if(references.size()>0)
			query = query.substring(0,query.length()-1);
		//	CLOSE THE DEAL
		query = query+") WITH ( OIDS=FALSE ); ALTER TABLE "+e.getDataReference()+" OWNER TO jakj;";
		//	Add parametered column
		query = query+" ALTER TABLE "+e.getDataReference()+" ADD COLUMN parametered_for integer DEFAULT 0;";
		
		return query;
	}
}
