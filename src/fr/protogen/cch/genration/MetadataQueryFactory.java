package fr.protogen.cch.genration;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.protogen.cch.configuration.StringFormat;
import fr.protogen.masterdata.model.*;

public class MetadataQueryFactory {
	public String InsertNewApplication(SApplication app){
		String query="INSERT INTO s_application (project_name,version,author,description,licence_text,appkey) values " +
							"('"+StringFormat.getInstance().formatQuery(app.getProjectName())+"','"+app.getVersion()+"','"+StringFormat.getInstance().formatQuery(app.getAuthor())+
							"','"+StringFormat.getInstance().formatQuery(app.getDescription())+"','"+StringFormat.getInstance().formatQuery(app.getLicence())+"','"+app.getAppKey()+"')";
		return query;
	}
	public List<String> InsertNewFunctions(List<SScreensequence> functions){
		List<String> queries = new ArrayList<String>();
		
		for(SScreensequence f : functions){
			String query = "insert into s_screensequence (title, description, appkey) values ('"+StringFormat.getInstance().formatQuery(f.getTitle())+"','"+StringFormat.getInstance().formatQuery(f.getDescription())+"','"+f.getAppKey()+"')";
			queries.add(query);
		}
		
		return queries;
	}
	
	public Map<Object, String> InsertNewEntities(List<CBusinessClass> entities){
		Map<Object, String> queries = new HashMap<Object, String>();
		
		
		for(CBusinessClass e : entities){
			String equery = "insert into c_businessclass (data_reference, name, description, appkey, user_restrict) values " +
					"('"+e.getDataReference()+"','"+StringFormat.getInstance().formatQuery(e.getName())+"','"+StringFormat.getInstance().formatQuery(e.getDescription())+"','"+e.getAppKey()+"','"+(e.getUserRestrict()=='Y'?'Y':'N')+"')";
			queries.put(e,equery);
			
			for(CAttribute a : e.getAttributes()){
				String aquery = "insert into c_attribute (data_reference, id_attributetype ,  attribute ,  id_class ,  key_attribute ,  formula ,  is_calculated ,  mandatory ,  reference ,  multiple,autovalue,requires_validation,validation_formula,file_title,file_extension,lock_attribute,lock_label,unlock_label,meta_table_ref, field_width) values" +
						"('"+a.getDataReference()+"','"+a.getCAttributetype().getId()+"','"+StringFormat.getInstance().formatQuery(a.getAttribute())+
						"','___eid___','"+(a.isKeyAttribute()?"1":"0")+"','"+a.getFormula()+"','"+(a.isCalculated()?"Y":"N")+"','"+(a.isMandatory()?"Y":"N")+"','"+(a.isReference()?"Y":"N")+"','"+(a.isMultiple()?"Y":"N")+"', '"+(a.isAutoValue()?'Y':'N')+"'," +
							" '"+(a.isRequiresValidation()?'Y':'N')+"','"+a.getValidationFormula()+"','"+a.getFileName()+"','"+a.getFileExtension()+"', " +
							" '" +((a.getCAttributetype().getId()==9)?'Y':'N')+"', '"+a.getLockLabel()+"','"+a.getUnlockLabel()+"'"+
									",'"+a.getMetatableReference()+"',"+a.getFieldWidth()+")";
				
				queries.put(a,aquery);
			}
		}
		
		return queries;
	}
	public String InsertNewWindow(CWindow w, int idEntity, int idSequence, String appkey) {
		// TODO Auto-generated method stub
		String query=" insert into c_window (title, \"stepDescription\" ,percentage ,\"helpVideo\" ,id_windowtype ,id_screen_sequence ,id_entity ,appkey, rappel_reference) values " +
				"('"+StringFormat.getInstance().formatQuery(w.getTitle())+"','"+StringFormat.getInstance().formatQuery(w.getStepDescription())+"',0,'','"+w.getCWindowtype().getId()+"','"+idSequence+"','"+idEntity+"','"+appkey+"',"+w.getRappelReference().getId()+")";
		return query;
	}

	public String generatePieWidgetQuery(CBusinessClass entity, CAttribute label, CAttribute value){
		String query = "select "+label.getDataReference()+" as wlabel, "+value.getDataReference()+" as wvalue from "+entity.getDataReference();
		return query;
	}
	public String generateTableWidgetQuery(CBusinessClass sEntity,
			CAttribute[] selectedEntities) {
		// TODO Auto-generated method stub
		String query = "select ";
		for(CAttribute a : selectedEntities){
			query = query+a.getDataReference()+",";
		}
		
		query = query.substring(0, query.length()-1);
		query = query+" from "+sEntity.getDataReference()+" limit 10";
		return query;
	}
	public String InsertNewMainWindow(SApplication app) {
		// TODO Auto-generated method stub
		
		return "insert into s_mainwindow (application_title,s_application_id,appkey,logo) values (?,?,?,?)";
	}
	public String generateCourbesWidgetQuery(CBusinessClass entity, CAttribute label, CAttribute value) {
		String query = "select "+label.getDataReference()+" as wlabel, "+value.getDataReference()+" as wvalue from "+entity.getDataReference();
		return query;
	}
}
