package fr.protogen.cch.batch;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import fr.protogen.masterdata.dal.DBUtils;

public class AngraMainyu {

	public static void main(String[] args) {
		
		try{
			Class.forName("org.postgresql.Driver");

		    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
		    
		    //	Get all tables
		    String sql = "select data_reference from c_businessclass";
		    PreparedStatement ps = cnx.prepareStatement(sql);
		    ResultSet rs = ps.executeQuery();
		    List<String> tables = new ArrayList<String>();
		    while(rs.next()){
		    	tables.add(rs.getString(1));
		    }
		    rs.close();
		    ps.close();
		    for(String t : tables){
		    	try{
			    	sql = "truncate "+t+" cascade";
			    	ps = cnx.prepareStatement(sql);
			    	ps.execute();
			    	ps.close();
		    	}catch(Exception exc){
		    		System.out.println("************\nTRUNCATE\n************\n");
		    		exc.printStackTrace();
		    	}
		    }
		    for(String t : tables){
		    	try{
			    	sql = "drop table "+t +" cascade";
			    	ps = cnx.prepareStatement(sql);
			    	ps.execute();
			    	ps.close();
		    	}catch(Exception exc){
		    		System.out.println("************\nDROP\n************\n");
		    		exc.printStackTrace();
		    	}
		    }
		    for(String t : tables){
		    	try{
			    	sql = "drop sequence "+t+"_seq";
			    	ps = cnx.prepareStatement(sql);
			    	ps.execute();
			    	ps.close();
		    	}catch(Exception exc){
		    		System.out.println("************\nDROP\n************\n");
		    		exc.printStackTrace();
		    	}
		    }
		    
		    // Table de Generium
		    List<String> metatables = new ArrayList<String>();
		    metatables.add("c_action_arguments");
		    metatables.add("c_actionbutton");
		    metatables.add("c_attribute");
		    metatables.add("c_businessclass");
		    metatables.add("c_businessentity");
		    metatables.add("c_composite_bcalss");
		    metatables.add("c_documentbutton");
		    metatables.add("c_globalvalue");
		    metatables.add("c_meta_entity_mapping");
		    metatables.add("c_metaparameters");
		    metatables.add("c_org_instance");
		    metatables.add("c_organization");
		    metatables.add("c_orgrole");
		    metatables.add("c_parameter_ctrl");
		    metatables.add("c_parameters_window");
		    metatables.add("c_trigger");
		    metatables.add("c_user_bean");
		    metatables.add("c_window");
		    metatables.add("c_window_businessentity");
		    metatables.add("c_window_globalvalue");
		    metatables.add("c_window_link");
		    metatables.add("c_window_windowattribute");
		    metatables.add("c_windowattribute");
		    metatables.add("core_role");
		    metatables.add("core_user");
		    metatables.add("exp_export_button");
		    metatables.add("exp_export_driver");
		    metatables.add("exp_export_map");
		    metatables.add("exp_map_tuple");
		    metatables.add("ged_ocr_history");
		    metatables.add("m_action");
		    metatables.add("m_arguments");
		    metatables.add("m_document");
		    metatables.add("m_postaction");
		    metatables.add("m_postaction_attribute");
		    metatables.add("m_postaction_type");
		    metatables.add("s_alert");
		    metatables.add("s_alert_instance");
		    metatables.add("s_alert_type");
		    metatables.add("s_application");
		    metatables.add("s_application_parameters");
		    metatables.add("s_asgard_drive");
		    metatables.add("s_atom");
		    metatables.add("s_import_button");
		    metatables.add("s_import_format");
		    metatables.add("s_link");
		    metatables.add("s_link_mapping");
		    metatables.add("s_mainwindow");
		    metatables.add("s_menuitem");
		    metatables.add("s_ocr_drvier");
		    metatables.add("s_procedure");
		    metatables.add("s_procedure_s_parameter");
		    metatables.add("s_procedure_step");
		    metatables.add("s_process");
		    metatables.add("s_process_session");
		    metatables.add("s_process_window");
		    metatables.add("s_resource");
		    metatables.add("s_rubrique");
		    metatables.add("s_schedule_entry");
		    metatables.add("s_scheduled_com");
		    metatables.add("s_screensequence");
		    metatables.add("s_social");
		    metatables.add("s_ui_alert");
		    metatables.add("s_wf_definition");
		    metatables.add("s_wf_execution");
		    metatables.add("s_wf_info");
		    metatables.add("s_wf_node");
		    metatables.add("s_wf_node_type");
		    metatables.add("s_wf_request");
		    metatables.add("s_wf_screen");
		    metatables.add("s_wf_transition");
		    metatables.add("s_widget");
		    metatables.add("step_type");
		    
		    for(String t : metatables){
		    	try{
			    	sql = "truncate "+t+" cascade";
			    	ps = cnx.prepareStatement(sql);
			    	ps.execute();
			    	ps.close();
		    	}catch(Exception exc){
		    		System.out.println("************\nTRUNCATE\n************\n");
		    		exc.printStackTrace();
		    	}
		    }
		    
		    sql = "insert into c_attributetype (id,type) values (1,'ENTIER'),"
		    		+ "(2,'TEXT'),"
		    		+ "(3,'DATE'),"
		    		+ "(4,'DOUBLE'),"
		    		+ "(5,'HEURE'),"
		    		+ "(6,'FICHIER'),"
		    		+ "(7,'Utilisateur'),"
		    		+ "(8,'Monétaire'),"
		    		+ "(9,'LOCK'),"
		    		+ "(10,'META TABLE'),"
		    		+ "(11,'META REFERENCE'),"
		    		+ "(12,'Booléen')";
		    ps = cnx.prepareStatement(sql);
		    ps.execute();
		    ps.close();
		    
		    sql = "insert into m_postaction_type (id,type) values (1,'SAVE'),"
		    		+ "(2,'UPDATE')";
		    ps = cnx.prepareStatement(sql);
		    ps.execute();
		    ps.close();
		    
		    cnx.close();
		   
		}catch(Exception exc){
			exc.printStackTrace();
		}

	}

}
