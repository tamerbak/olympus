package fr.protogen.cch.control;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.bean.SessionScoped;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.RandomStringUtils;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FlowEvent;
import org.primefaces.event.RowEditEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import fr.protogen.cch.cache.ApplicationRegistery;
import fr.protogen.cch.cache.SessionCache;
import fr.protogen.cch.configuration.Parameters;
import fr.protogen.cch.configuration.StringFormat;
import fr.protogen.cch.genration.GenerationService;
import fr.protogen.masterdata.dal.DAL;
import fr.protogen.masterdata.model.CActionbutton;
import fr.protogen.masterdata.model.CAttribute;
import fr.protogen.masterdata.model.CAttributetype;
import fr.protogen.masterdata.model.CBusinessClass;
import fr.protogen.masterdata.model.CCHUser;
import fr.protogen.masterdata.model.CDocumentbutton;
import fr.protogen.masterdata.model.CWindow;
import fr.protogen.masterdata.model.SProcess;

@SuppressWarnings("serial")
@ManagedBean
@SessionScoped
public class EntitiesCtrl implements Serializable {
	
	
	private String stepTitle;
	private String stepDescription;
	
	private String title;
	private String description;
	private String stepHelp;
	private List<CBusinessClass> entities = new ArrayList<CBusinessClass>();
	private List<CAttribute> attributes = new ArrayList<CAttribute>();
	private String selectedEntity = "";
	private String currentEntity = "";
	private String attributeTitle = "";
	private String selectedType = "";
	private boolean reference;
	private boolean mandatory;
	private boolean keyField;
	private boolean multiple;
	private String referencedTitle = "";
    private boolean textarea;
    
	//	Formula and calculated attributes
	private boolean calculated;
	private String formula="";
	private TreeNode rootAttributes;  
    private TreeNode selectedNode;

    //	Maintenance mode
    private CBusinessClass maintenanceEntity;
    private CAttribute attToDelete;
    private List<String> windows;
    private List<String> satts;
	private List<String> actions;
	private List<String> documents;
	private List<String> processes;
    private SessionCache cache;
	private Boolean maintainmode;
	
	//	Auto value
	private boolean autoValue;
	
	//	Field requires validation
	private boolean requiresValidation;
	
	//	Conditional layout
	private boolean conditionalLayout;
	
	//	Default value
	private String defaultValue;
    
	//	File content fields
	private String fmdTitle;
	private String fmdExtension;
	
	//	Lock field
	private String lockLabel="";
	private String unlockLabel="";
	
	//	User restriction
	private String userRestrict="";
	
	// class diagram
	private String jsonDiagram="";

	//	HISTORISATION
	private boolean historisation = false;
	private List<CAttribute> histoAttributes;
	private String selectedAttribute;
	
	
	private List<String> entitesIndependantes;
	private List<String> entitesLabel = new ArrayList<String>();
	
	private int fieldLength = 80;
	
	public EntitiesCtrl(){
		stepTitle = "Entités métier";
		stepDescription = "Etape 1 : Enumérer les entités métier, Etape 2 : Mise en liaison et affectation des attributs";
		
	}
	

	@PostConstruct
	public void postLoad(){
		maintainmode = (Boolean)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(Parameters.MAINTAIN_MODE);
		if(maintainmode){
			CCHUser user = (CCHUser)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(Parameters.USER_TOKEN);
	    	cache = ApplicationRegistery.getInstance().getSession(user);
			entities = cache.getEntities();
			attributes = cache.getAttributes();
			entitesLabel = new ArrayList<String>();
			for(CBusinessClass e : entities)
				entitesLabel.add(e.getName());
		}
		
		String action = (String)FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("source");
		if("fromDC".equals(action)) {
			String diagram = (String)FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("diagram");
			System.out.println(diagram);
			if(diagram != null && diagram != "") {
				parseDiagram(diagram);
			}
			
		}
	}
	
	public void validerIndependance(){
		if(!maintainmode){
			return;
		}
		
		GenerationService srv = new GenerationService();
		List<CBusinessClass> selent = new ArrayList<CBusinessClass>();
		for(String s : entitesIndependantes)
			for(CBusinessClass e : entities)
				if(e.getName().equals(s)){
					selent.add(e);
					break;
				}
		srv.updateIndependentEntities(selent,cache.getAppKey());
	}
	
	//	Historisation
	public void prepareHistoAttributes(){
		histoAttributes = new ArrayList<CAttribute>();
		CBusinessClass sentity = new CBusinessClass();
		for(CBusinessClass e : entities)
			if(e.getDataReference().equals(selectedEntity)){
				sentity = e;
				break;
			}
		
		for(CAttribute a : attributes){
			if(a.getEntity().getDataReference().equals(sentity.getDataReference())){
				histoAttributes.add(a);
			}
		}
	}
	
	private void parseDiagram(String diagram) {
		try{
		JSONObject jsonDiagram = JSONObject.fromObject(diagram);
		JSONArray diagramClasses = jsonDiagram.getJSONArray("classes");
		
		for(Iterator _it = diagramClasses.iterator(); _it.hasNext();){
			JSONObject jsonClass = (JSONObject)_it.next();
			if(!cbBuissnesAlreadyExiste(jsonClass.getString("className"))) {
				CBusinessClass entity = addBusinessClass(jsonClass);
				JSONObject jsonAttrs = jsonClass.getJSONObject("attributes");
				for(Iterator _itAttr = jsonAttrs.keys(); _itAttr.hasNext();) {
					String key = (String) _itAttr.next();
					addAttribute(jsonAttrs.getJSONObject(key), entity);
				}
			}
		}
		JSONArray jsonAssociations = jsonDiagram.getJSONArray("associations");
		for(Iterator _itAssoc = jsonAssociations.iterator(); _itAssoc.hasNext();) {
			JSONObject  jsonAssocObj = (JSONObject) _itAssoc.next();
			if(!associationAlreadyExiste(jsonAssocObj.getString("name"),jsonAssocObj.getString("classAName"),jsonAssocObj.getString("classBName"))){
				addAssoication(jsonAssocObj);
			}
		}
		
		saveDiagram(diagram);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private boolean cbBuissnesAlreadyExiste(String className) {
		String dataRefA = StringFormat.getInstance().tableDataReferenceFormat(className);
		boolean exist = false;
		for(CBusinessClass e : entities) {
			if(e.getDataReference().equals(dataRefA)){
				exist = true; 
				break;
			}
			
		}
		return exist;
	}

	private boolean associationAlreadyExiste(String assocName, String classNameA, String classNameB) {
		String dataRefA = StringFormat.getInstance().tableDataReferenceFormat(classNameA);
		String dataRefB = StringFormat.getInstance().tableDataReferenceFormat(classNameB);
		boolean exist = false;
		for(CAttribute a : attributes) {
			if(a.getDataReference().startsWith("fk_")){
				String ref = "fk_"+dataRefB;
				if(a.isMultiple()) {
					ref +="__"+dataRefA;
				} 
				if(a.getDataReference().equals(ref)) {
					exist = true; 
					break;
				}
			}
			
		}
		return exist;
	}

	public String  generateDiagram() {
		JSONObject diagram = null;
		String rDiagram = retrieveDiagram();
		if (rDiagram == null) {
			diagram = new JSONObject();
			JSONArray classes = new JSONArray();
			JSONArray assocations = new JSONArray();
			int dx= 7;
			int dy=7;
			for(CBusinessClass entity : entities) {
				
				JSONObject clazz = new JSONObject();
				clazz.put("className", entity.getName());
				clazz.put("key", RandomStringUtils.randomAlphanumeric(9));
				clazz.put("x", 50);
				clazz.put("y", 50);
				clazz.put("dx", dx);
				clazz.put("dy", dy);
				clazz.put("width", 170);
				dx +=170;
				if(dx>1400){dx=0; dy+=30;}
				clazz.put("height", entity.getAttributes().size()*10+20);
				JSONObject attributes = new JSONObject();
				int cpt = 1;
				for(CAttribute cAttr : entity.getAttributes()) {
					JSONObject attr = new JSONObject();
					if(!cAttr.getDataReference().startsWith("fk_")) {
						attr.put("key_field",cAttr.isKeyAttribute());
						attr.put("auto_value",cAttr.isAutoValue());
						attr.put("name",cAttr.getName());
						attr.put("type",cAttr.getTypeLabel());
						attr.put("key_reference",cAttr.isKeyAttribute());
						attr.put("mandatory",cAttr.isMandatory());
						attr.put("is_calculated",cAttr.isCalculated());
						attr.put("formula",cAttr.getFormula());
						attr.put("requires_validation",cAttr.isRequiresValidation());
						attr.put("validation_formula",cAttr. getValidationFormula());
						attr.put("unicity", false);//TODO: unicity value
						attr.put("conditional_layout",cAttr.isConditionalLayout());
						attr.put("conditional_layout_formula", "");
						attr.put("lock_label",cAttr.getLockLabel());
						attr.put("unlock_label",cAttr.getUnlockLabel());
						attr.put("default_value",cAttr.getDefaultValue());
						attr.put("file_title",cAttr.getFileName());
						attr.put("file_extension",cAttr.getFileExtension());
						
						attributes.put("attribute_"+(cpt++), attr);
						
					} else {
						attr.put("key", RandomStringUtils.randomAlphanumeric(6));
						attr.put("name",cAttr.getName());
						attr.put("key_field",cAttr.isKeyAttribute());
						attr.put("auto_value",cAttr.isAutoValue());
						attr.put("mandatory",cAttr.isMandatory());
						attr.put("default_value",cAttr.getDefaultValue());
						attr.put("reference",cAttr.isReference());
						attr.put("multiple",cAttr.isMultiple());
						attr.put("classAName", cAttr.getEntity().getName());
						attr.put("classBName", getClassCible(cAttr));
						assocations.add(attr);
					}
				}
				clazz.put("attributes", attributes);
				classes.add(clazz);
			}
			diagram.put("classes", classes);
			diagram.put("associations", assocations);
		} else {
			diagram = JSONObject.fromObject(rDiagram);
		}
		
		jsonDiagram = diagram.toString();
		System.out.println("charged diagram : "+jsonDiagram);
		return "diagram_class";
	}
	private Object getClassCible(CAttribute cAttr) {
		String className = null;
		String dataRef = cAttr.getDataReference();
		String tableName = null;
		if(cAttr.isReference()){
			tableName = dataRef.substring(3);
		} else if(cAttr.isMultiple()){
			tableName = dataRef.substring(8);
			int idx = tableName.lastIndexOf("user_");
			tableName = tableName.substring(idx);
		}
		
		for(CBusinessClass e : entities) {
			if(e.getDataReference().equals(tableName)) {
				className = e.getName();
				break;
			}
		}
		
		return className;
	}


	private String retrieveDiagram(){
		GenerationService service = new GenerationService();
		return service.retrieveDiagram( cache);
	}
	
	private void saveDiagram(String diagram){
		GenerationService service = new GenerationService();
		service.saveDiagram(diagram, cache);
	}
	private CBusinessClass addBusinessClass(JSONObject jsonClObject){
		CBusinessClass entity = new CBusinessClass();
		entity.setName(jsonClObject.getString("className"));
		entity.setDescription(jsonClObject.getString("className"));
		entity.setDataReference(StringFormat.getInstance().tableDataReferenceFormat(jsonClObject.getString("className")));
		entities.add(entity);
		
		CAttributetype atype = new CAttributetype(1, entity.getName());
		
		CAttribute attribute = new CAttribute();
		attribute.setAttribute("ID "+entity.getName());
		attribute.setCAttributetype(atype);
		attribute.setDataReference("pk_"+entity.getDataReference());
		attribute.setEntity(entity);
		attribute.setKeyAttribute(false);
		attribute.setMandatory(false);
		attribute.setReference(false);
		attribute.setTypeLabel(atype.getType());
		attribute.setDescription("Clé primaire");
		
		attributes.add(attribute);
		entity.setAttributes(new ArrayList<CAttribute>());
		entity.getAttributes().add(attribute);
		entity.setUserRestrict('N'/*userRestrict.charAt(0)*/);
	//	if(maintainmode){
			
			GenerationService service = new GenerationService();
			service.addNewEntity(entity,cache);
	//	}
		return entity;
	}
	
	private void addAttribute(JSONObject jsonAttrObj, CBusinessClass sentity) {

		DAL dal = new DAL();

		//	CBusinessClass sentity = new CBusinessClass();
//			for (CBusinessClass e : entities)
//				if (e.getDataReference().equals(className))
//					sentity = e;
		CAttributetype atype = dal.getType(jsonAttrObj.getString("type"));

		CAttribute attribute = new CAttribute();
		attribute.setAttribute(jsonAttrObj.getString("name"));
		attribute.setCAttributetype(atype);
		attribute.setDataReference(StringFormat.getInstance().attributeDataReferenceFormat(jsonAttrObj.getString("name")));
		attribute.setEntity(sentity);
		attribute.setKeyAttribute(jsonAttrObj.getBoolean("key_field"));
		attribute.setMandatory(jsonAttrObj.getBoolean("mandatory"));
		attribute.setReference(false);
		attribute.setTypeLabel(atype.getType());
		String description = "";
		attribute.setLockLabel(jsonAttrObj.getString("lock_label"));
		attribute.setUnlockLabel(jsonAttrObj.getString("unlock_label"));
		if (jsonAttrObj.getBoolean("key_field"))
			description = "Champs identificateur - ";
		if (jsonAttrObj.getBoolean("mandatory"))
			description = description + "Champs obligatoire - ";
		if (reference)
			description = description + "Référence - ";

		attribute.setAutoValue(jsonAttrObj.getBoolean("auto_value"));

		if (jsonAttrObj.getBoolean("is_calculated")) {
			attribute.setCalculated(true);
			attribute.setFormula(formula);
			description = description + "Formule de calcul = " + formula
					+ " - ";
		} else if (jsonAttrObj.getBoolean("requires_validation")) {
			attribute.setRequiresValidation(true);
			attribute.setValidationFormula(jsonAttrObj.getString("formula"));
			description = description + "Formule de validation = "
					+ formula + " - ";
		} else if (jsonAttrObj.getBoolean("conditional_layout")) {
			attribute.setConditionalLayout(true);
			attribute.setFormula(jsonAttrObj.getString("conditional_layout_formula"));
			description = description + "condition de mise en page = "
					+ formula + " - ";
		} else {
			attribute.setFormula("");
		}
		if (description.length() > 1) {
			description = description
					.substring(0, description.length() - 3);
		}
		attribute.setDescription(description);
		attributes.add(attribute);
		if (sentity.getAttributes() == null) {
			sentity.setAttributes(new ArrayList<CAttribute>());
		}
		sentity.getAttributes().add(attribute);

		if (attribute.getCAttributetype().getId() == 6) {
			// File content
			attribute.setFileName(jsonAttrObj.getString("file_title"));
			attribute.setFileExtension(jsonAttrObj.getString("file_extension"));
		}

		attribute.setDefaultValue(jsonAttrObj.getString("default_value"));
	//	if(maintainmode){
			for(CBusinessClass b : cache.getEntities()){
				if(b.getId() == attribute.getEntity().getId()){
					if(!b.getAttributes().contains(attribute))
						b.getAttributes().add(attribute);
				}
			}
			GenerationService service = new GenerationService();
			service.addNewAttribute(attribute, sentity);
	//	}
	}
	
	private void addAssoication(JSONObject jsonAssocObj) {

		CBusinessClass sentity = new CBusinessClass();
		CBusinessClass rentity = new CBusinessClass();
		String dataRefA = StringFormat.getInstance().tableDataReferenceFormat(jsonAssocObj.getString("classAName"));
		String dataRefB = StringFormat.getInstance().tableDataReferenceFormat(jsonAssocObj.getString("classBName"));
		for(CBusinessClass e : entities) {
			if(e.getDataReference().equals(dataRefA))
				sentity = e;
			if(e.getDataReference().equals(dataRefB))
				rentity = e;
		}

		CAttributetype atype = new CAttributetype(1, rentity.getName());
		
		CAttribute attribute = new CAttribute();
		if(!jsonAssocObj.getBoolean("multiple") && !jsonAssocObj.getBoolean("reference")){
			attribute.setAttribute("ID "+rentity.getName());
			attribute.setDataReference("fk_"+dataRefB);
		} else if(jsonAssocObj.getBoolean("multiple")) {
			attribute.setAttribute(rentity.getName()+"s");
			attribute.setDataReference("fk_"+dataRefB+"__"+dataRefA);
		} else if(jsonAssocObj.getBoolean("reference")){
			attribute.setAttribute(jsonAssocObj.getString("name"));
			attribute.setDataReference("fk_"+dataRefB);
		}
		attribute.setCAttributetype(atype);
		
		attribute.setEntity(sentity);
		attribute.setKeyAttribute(jsonAssocObj.getBoolean("key_field"));
		attribute.setMandatory(jsonAssocObj.getBoolean("mandatory"));
		attribute.setReference(jsonAssocObj.getBoolean("reference"));
		attribute.setTypeLabel(atype.getType());
		String description = "";
		if(jsonAssocObj.getBoolean("key_field"))
			description="Champs identificateur - ";
		if(jsonAssocObj.getBoolean("mandatory"))
			description=description + "Champs obligatoire - ";
		if(jsonAssocObj.getBoolean("reference"))
			description=description + "Référence - ";

		attribute.setDefaultValue(jsonAssocObj.getString("default_value"));
		if(description.length()>1){
			description = description.substring(0, description.length()-3);
		}
		attribute.setDescription(description);
		attribute.setMultiple(jsonAssocObj.getBoolean("multiple"));
		attributes.add(attribute);
		if(sentity.getAttributes() == null){
			sentity.setAttributes(new ArrayList<CAttribute>());
		}
		sentity.getAttributes().add(attribute);
		
//		if(maintainmode){
			
			for(CBusinessClass b : cache.getEntities()){
				if(b.getId() == attribute.getEntity().getId()){
					if(!b.getAttributes().contains(attribute))
						b.getAttributes().add(attribute);
				}
			}
			
			GenerationService service = new GenerationService();
			if(!attribute.isMultiple())
				service.addNewReference(attribute, sentity, rentity);
			else
				service.addNewMultipleReference(attribute, sentity, rentity);
//		} 
	
	}
	
	public void onRowEdit(RowEditEvent evt){
		String dummy = "";
		dummy=dummy+"";
	}
	
	public String wzflowListener(FlowEvent evt){
		RequestContext.getCurrentInstance().execute("dlg2.hide()");
		return evt.getNewStep();
	}
	
	public String constructAttributeDependencies(){
		
		windows =new ArrayList<String>();
		for(CWindow w : cache.getWindows()){
			if(w.getCAttributes().contains(attToDelete))
				windows.add(w.getTitle());
		}
		
		return "";
	}
	
	public void constructDependencies(){
		windows =new ArrayList<String>();
		documents = new ArrayList<String>();
		actions = new ArrayList<String>();
		processes = new ArrayList<String>();
		satts = new ArrayList<String>();
		
		for(CAttribute a : cache.getAttributes()){
			if(a.getDataReference().equals("fk_"+maintenanceEntity.getDataReference()))
				satts.add(a.getEntity().getName()+" - "+a.getAttribute());
		}
		
		for(CWindow w : cache.getWindows()){
			if(w.getMainEntity().equals(maintenanceEntity.getDataReference())){
				windows.add(w.getTitle());
				for(CActionbutton a : w.getCActionbuttons()){
					actions.add(a.getTitle());
				}
				for(CDocumentbutton d : w.getCDocumentbuttons()){
					documents.add(d.getTitle());
				}
				for(SProcess p : cache.getProcesses()){
					for(CWindow pw : p.getWindows())
						if(pw.getId() == w.getId())
							processes.add(p.getTitle());
				}
			}
		}
	}
	
	public void doDeleteAtt(){
		cache.getAttributes().remove(attToDelete);
		
		for(CBusinessClass e : cache.getEntities())
			if(e.getAttributes().contains(attToDelete))
				e.getAttributes().remove(attToDelete);
		
		for(CWindow w : cache.getWindows())
			if(w.getCAttributes().contains(attToDelete))
				w.getCAttributes().remove(attToDelete);
		
		if(maintainmode){
			GenerationService service = new GenerationService();
			List<CAttribute> latt = new ArrayList<CAttribute>();
			latt.add(attToDelete);
			service.deleteAttributes(latt);
		}
	}
	
	public void doDelete(){
		
		List<CWindow> idWindows = new ArrayList<CWindow>();
		List<CActionbutton> idActions = new ArrayList<CActionbutton>();
		List<CDocumentbutton> idDocuments = new ArrayList<CDocumentbutton>();
		List<CAttribute> idAttributes = new ArrayList<CAttribute>();
		
		for(CAttribute a : cache.getAttributes()){
			if(a.getDataReference().equals("fk_"+maintenanceEntity.getDataReference())) {
				CBusinessClass e = a.getEntity();
				e.getAttributes().remove(a);
				cache.getAttributes().remove(a);
				for(CWindow w : cache.getWindows()){
					if(w.getCAttributes().contains(a))
						w.getCAttributes().remove(a);
				}
				idAttributes.add(a);
			}
		}
		
		for(CWindow w : cache.getWindows()){
			if(w.getMainEntity().equals(maintenanceEntity.getDataReference())){
				idWindows.add(w);
				for(CActionbutton a : w.getCActionbuttons()){
					idActions.add(a);
				}
				for(CDocumentbutton d : w.getCDocumentbuttons()){
					idDocuments.add(d);
				}
				
			}
		}
		
		cache.getWindows().removeAll(idWindows);
		cache.getActions().removeAll(idActions);
		cache.getDocuments().removeAll(idDocuments);
		cache.getAttributes().removeAll(idAttributes);
		
		
		entities.remove(maintenanceEntity);
		cache.getEntities().remove(maintenanceEntity);
		
		//	DB Operations
		if(maintainmode){
			GenerationService service = new GenerationService();
			service.deleteActions(idActions);
			service.deleteDocuments(idDocuments);
			service.deleteWindows(idWindows);
			service.deleteAttributes(idAttributes);
			service.deleteEntity(maintenanceEntity);
		}
	}
	
	public void cancelDeleteAtt(){
		
	}
	
	public void cancelDelete(){
		
	}
	
	public void add(ActionEvent evt){
		CBusinessClass entity = new CBusinessClass();
		entity.setName(title);
		entity.setDescription(description);
		entity.setDataReference(StringFormat.getInstance().tableDataReferenceFormat(title));
		entities.add(entity);
		
		CAttributetype atype = new CAttributetype(1, entity.getName());
		
		CAttribute attribute = new CAttribute();
		attribute.setAttribute("ID "+entity.getName());
		attribute.setCAttributetype(atype);
		attribute.setDataReference("pk_"+entity.getDataReference());
		attribute.setEntity(entity);
		attribute.setKeyAttribute(false);
		attribute.setMandatory(false);
		attribute.setReference(false);
		attribute.setTypeLabel(atype.getType());
		attribute.setDescription("Clé primaire");
		
		attributes.add(attribute);
		entity.setAttributes(new ArrayList<CAttribute>());
		entity.getAttributes().add(attribute);
		entity.setUserRestrict(userRestrict.charAt(0));
		
		if(maintainmode){
			
			GenerationService service = new GenerationService();
			service.addNewEntity(entity,cache);
		}
		
        title="";
        description="";
	}
	
	
		
	public void addAttrib(ActionEvent evt) {
		
		DAL dal = new DAL();
		
		if(selectedType.length() == 1){
			CBusinessClass sentity = new CBusinessClass();
			for(CBusinessClass e : entities)
				if(e.getDataReference().equals(selectedEntity))
					sentity = e;
			CAttributetype atype = dal.getType(selectedType);
			
			CAttribute attribute = new CAttribute();
			attribute.setAttribute(attributeTitle);
			attribute.setCAttributetype(atype);
			attribute.setDataReference(StringFormat.getInstance().attributeDataReferenceFormat(attributeTitle));
			attribute.setEntity(sentity);
			attribute.setKeyAttribute(keyField);
			attribute.setMandatory(mandatory);
			attribute.setReference(reference);
			attribute.setTextarea(textarea);
			attribute.setTypeLabel(atype.getType());
			attribute.setFieldWidth(fieldLength);
			String description = "";
			attribute.setLockLabel(lockLabel);
			attribute.setUnlockLabel(unlockLabel);
			if(keyField)
				description="Champs identificateur - ";
			if(mandatory)
				description=description + "Champs obligatoire - ";
			if(reference)
				description=description + "Référence - ";
		
			
			attribute.setAutoValue(autoValue);
				
			
			if(calculated){
				attribute.setCalculated(true);
				attribute.setFormula(formula);
				description=description+"Formule de calcul = "+formula+" - ";
			} else if(requiresValidation) { 
				attribute.setRequiresValidation(true);
				attribute.setValidationFormula(formula);
				description=description+"Formule de validation = "+formula+" - ";
			} else if(conditionalLayout){
				attribute.setConditionalLayout(true);
				attribute.setFormula(formula);
				description=description+"condition de mise en page = "+formula+" - ";
			}else {
				attribute.setFormula("");
			}
			if(description.length()>1){
				description = description.substring(0, description.length()-3);
			}
			attribute.setDescription(description);
			attributes.add(attribute);
			if(sentity.getAttributes() == null){
				sentity.setAttributes(new ArrayList<CAttribute>());
			}
			sentity.getAttributes().add(attribute);
			
			if(attribute.getCAttributetype().getId() == 6){
				//	File content
				attribute.setFileName(fmdTitle);
				attribute.setFileExtension(fmdExtension);
			}
			
			if(attribute.getCAttributetype().getId() == 11){
				attribute.setMetatableReference(formula);
			}
			
			attribute.setDefaultValue(defaultValue);
			
			for(CWindow w : cache.getWindows())
				if(w.getMainEntity().equals(selectedEntity)){
					w.getCAttributes().add(attribute);
				}
			
			if(maintainmode){
				for(CBusinessClass b : cache.getEntities()){
					if(b.getId() == attribute.getEntity().getId()){
						if(!b.getAttributes().contains(attribute))
							b.getAttributes().add(attribute);
					}
				}
				GenerationService service = new GenerationService();
				service.addNewAttribute(attribute, sentity);
			}
			
		} else {
			CBusinessClass sentity = new CBusinessClass();
			CBusinessClass rentity = new CBusinessClass();
			for(CBusinessClass e : entities){
				if(e.getDataReference().equals(selectedEntity))
					sentity = e;
				if(e.getDataReference().equals(selectedType))
					rentity = e;
			}

			CAttributetype atype = new CAttributetype(1, rentity.getName());
			
			CAttribute attribute = new CAttribute();
			if(!multiple && !reference){
				attribute.setAttribute("ID "+rentity.getName());
				attribute.setDataReference("fk_"+selectedType);
			} else if(multiple) {
				attribute.setAttribute(rentity.getName()+"s");
				attribute.setDataReference("fk_"+selectedType+"__"+selectedEntity);
			} else if(reference){
				attribute.setAttribute(attributeTitle);
				attribute.setDataReference("fk_"+selectedType);
			}
			attribute.setCAttributetype(atype);
			
			attribute.setEntity(sentity);
			attribute.setKeyAttribute(keyField);
			attribute.setMandatory(mandatory);
			attribute.setReference(reference);
			attribute.setTypeLabel(atype.getType());
			String description = "";
			if(keyField)
				description="Champs identificateur - ";
			if(mandatory)
				description=description + "Champs obligatoire - ";
			if(reference)
				description=description + "Référence - ";
			
			if(historisation){
				sentity.setHistory(true);
				sentity.setHistoryReference(attribute);
			}
			
			attribute.setDefaultValue(defaultValue);
			if(description.length()>1){
				description = description.substring(0, description.length()-3);
			}
			attribute.setDescription(description);
			attribute.setMultiple(multiple);
			attributes.add(attribute);
			if(sentity.getAttributes() == null){
				sentity.setAttributes(new ArrayList<CAttribute>());
			}
			sentity.getAttributes().add(attribute);
			
			for(CWindow w : cache.getWindows())
				if(w.getMainEntity().equals(selectedEntity)){
					w.getCAttributes().add(attribute);
				}
			
			if(maintainmode){
				
				for(CBusinessClass b : cache.getEntities()){
					if(b.getId() == attribute.getEntity().getId()){
						if(!b.getAttributes().contains(attribute))
							b.getAttributes().add(attribute);
					}
				}
				
				GenerationService service = new GenerationService();
				if(!attribute.isMultiple())
					service.addNewReference(attribute, sentity, rentity);
				else
					service.addNewMultipleReference(attribute, sentity, rentity);
			}
		}
		selectedEntity = entities.get(0).getDataReference();
		selectedType = "N";
		attributeTitle="";
		reference=false;
		mandatory=false;
		keyField=false;
		calculated=false;
		multiple=false;
		formula="";
	}
	public void typeChangeListener(ValueChangeEvent evt){
		selectedType = (String)evt.getNewValue();
		if(selectedType.length()>1){
			for(CBusinessClass entity : entities)
				if(entity.getDataReference().equals(selectedType))
					setReferencedTitle(entity.getName());
		}
	}
	public void entityChangeListener(){
		
		CBusinessClass sentity = new CBusinessClass();
		for(CBusinessClass e : entities)
			if(e.getDataReference().equals(selectedEntity))
				sentity = e;
		
		if(selectedEntity != null)
			currentEntity = sentity.getName();
		else
			currentEntity = "actuelle";
		prepareTreeNode();
	}
	
	public String next(){
		
		CCHUser user = (CCHUser)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(Parameters.USER_TOKEN);
    	SessionCache cache = ApplicationRegistery.getInstance().getSession(user);
		cache.setEntities(entities);
		cache.setAttributes(attributes);
		
		if(!maintainmode){    	
			GenerationService service = new GenerationService();
			service.createDBSchema(entities);
		}
		return "parametermodels";
	}
	
	@SuppressWarnings("unused")
	public void prepareTreeNode(){
		rootAttributes = new DefaultTreeNode("Root", null);
		//	select entity
		CBusinessClass sentity = new CBusinessClass();
		for(CBusinessClass e : entities)
			if(e.getDataReference().equals(selectedEntity))
				sentity = e;
		
		List<String> innerDR = new ArrayList<String>();
		//	add all direct attributes
		 
		for(CAttribute a : attributes){
			if(a.getEntity().getDataReference().equals(sentity.getDataReference())){
				if(a.getDataReference().startsWith("fk_")){
					innerDR.add(a.getDataReference().substring(3));
					continue;
				} else {
					TreeNode n = new DefaultTreeNode(a,rootAttributes);
				}
					
			}
		}
		
		
		//	add all inner entities attributes
		for(String dr : innerDR){
			CBusinessClass drentity = new CBusinessClass();
			
			String table = dr;
			
			
			
			for(CBusinessClass e : entities)
				if(e.getDataReference().equals(table))
					drentity  = e;
			
			TreeNode entitytn = new DefaultTreeNode(drentity,rootAttributes);
			
			for(CAttribute a : attributes){
				if(a.getEntity().getDataReference().equals(table)){
					TreeNode n = new DefaultTreeNode(a,entitytn);
				}
			}
		}
	}
	
	public void addFieldToFormula(ActionEvent evt){
		if(selectedNode.getData() instanceof CBusinessClass){
			CBusinessClass sentity = new CBusinessClass();
			for(CBusinessClass e : entities)
				if(e.getDataReference().equals(selectedEntity))
					sentity = e;
			CBusinessClass referenced = (CBusinessClass)selectedNode.getData();
			String arg = sentity.getName()+".ID "+referenced.getName();
			formula = formula+" <<"+arg+">> ";
		} else {
			CAttribute a = (CAttribute)selectedNode.getData();
			String arg = a.getEntity().getName()+"."+a.getAttribute();
			formula = formula+" <<"+arg+">> ";
		}
	}
	
	
	/*
	 * 	Getters and setters
	 * */
	
	public String getStepTitle() {
		return stepTitle;
	}

	public void setStepTitle(String stepTitle) {
		this.stepTitle = stepTitle;
	}

	public String getStepDescription() {
		return stepDescription;
	}

	public void setStepDescription(String stepDescription) {
		this.stepDescription = stepDescription;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<CBusinessClass> getEntities() {
		return entities;
	}

	public void setEntities(List<CBusinessClass> entities) {
		this.entities = entities;
	}

	public String getStepHelp() {
		return stepHelp;
	}

	public void setStepHelp(String stepHelp) {
		this.stepHelp = stepHelp;
	}

	public String getSelectedEntity() {
		return selectedEntity;
	}

	public void setSelectedEntity(String selectedEntity) {
		this.selectedEntity = selectedEntity;
	}

	public String getCurrentEntity() {
		return currentEntity;
	}

	public void setCurrentEntity(String currentEntity) {
		this.currentEntity = currentEntity;
	}

	public String getAttributeTitle() {
		return attributeTitle;
	}

	public void setAttributeTitle(String attributeTitle) {
		this.attributeTitle = attributeTitle;
	}

	public String getSelectedType() {
		return selectedType;
	}

	public void setSelectedType(String selectedType) {
		this.selectedType = selectedType;
	}
	public boolean isReference() {
		return reference;
	}

	public void setReference(boolean reference) {
		this.reference = reference;
	}

	public boolean isMandatory() {
		return mandatory;
	}

	public void setMandatory(boolean mandatory) {
		this.mandatory = mandatory;
	}

	public boolean isKeyField() {
		return keyField;
	}

	public void setKeyField(boolean keyField) {
		this.keyField = keyField;
	}

	public String getReferencedTitle() {
		return referencedTitle;
	}

	public void setReferencedTitle(String referencedTitle) {
		this.referencedTitle = referencedTitle;
	}

	public List<CAttribute> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<CAttribute> attributes) {
		this.attributes = attributes;
	}

	
	public String getFormula() {
		return formula;
	}

	public void setFormula(String formula) {
		this.formula = formula;
	}

	public TreeNode getRootAttributes() {
		return rootAttributes;
	}

	public void setRootAttributes(TreeNode rootAttributes) {
		this.rootAttributes = rootAttributes;
	}

	public TreeNode getSelectedNode() {
		return selectedNode;
	}

	public void setSelectedNode(TreeNode selectedNode) {
		this.selectedNode = selectedNode;
	}

	public boolean isCalculated() {
		return calculated;
	}

	public void setCalculated(boolean calculated) {
		this.calculated = calculated;
	}

	public boolean isMultiple() {
		return multiple;
	}

	public void setMultiple(boolean multiple) {
		this.multiple = multiple;
	}

	public CBusinessClass getMaintenanceEntity() {
		return maintenanceEntity;
	}

	public void setMaintenanceEntity(CBusinessClass maintenanceEntity) {
		this.maintenanceEntity = maintenanceEntity;
	}

	public List<String> getWindows() {
		return windows;
	}

	public void setWindows(List<String> windows) {
		this.windows = windows;
	}

	public List<String> getActions() {
		return actions;
	}

	public void setActions(List<String> actions) {
		this.actions = actions;
	}

	public List<String> getDocuments() {
		return documents;
	}

	public void setDocuments(List<String> documents) {
		this.documents = documents;
	}

	public List<String> getProcesses() {
		return processes;
	}

	public void setProcesses(List<String> processes) {
		this.processes = processes;
	}

	public List<String> getSatts() {
		return satts;
	}

	public void setSatts(List<String> satts) {
		this.satts = satts;
	}

	public CAttribute getAttToDelete() {
		return attToDelete;
	}

	public void setAttToDelete(CAttribute attToDelete) {
		this.attToDelete = attToDelete;
	}

	public boolean isAutoValue() {
		return autoValue;
	}

	public void setAutoValue(boolean autoValue) {
		this.autoValue = autoValue;
	}

	public boolean isRequiresValidation() {
		return requiresValidation;
	}

	public void setRequiresValidation(boolean requiresValidation) {
		this.requiresValidation = requiresValidation;
	}

	public SessionCache getCache() {
		return cache;
	}

	public void setCache(SessionCache cache) {
		this.cache = cache;
	}

	public Boolean getMaintainmode() {
		return maintainmode;
	}

	public void setMaintainmode(Boolean maintainmode) {
		this.maintainmode = maintainmode;
	}

	public String getFmdTitle() {
		return fmdTitle;
	}

	public void setFmdTitle(String fmdTitle) {
		this.fmdTitle = fmdTitle;
	}

	public String getFmdExtension() {
		return fmdExtension;
	}

	public void setFmdExtension(String fmdExtension) {
		this.fmdExtension = fmdExtension;
	}

	public boolean isConditionalLayout() {
		return conditionalLayout;
	}

	public void setConditionalLayout(boolean conditionalLayout) {
		this.conditionalLayout = conditionalLayout;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public String getLockLabel() {
		return lockLabel;
	}

	public void setLockLabel(String lockLabel) {
		this.lockLabel = lockLabel;
	}

	public String getUnlockLabel() {
		return unlockLabel;
	}

	public void setUnlockLabel(String unlockLabel) {
		this.unlockLabel = unlockLabel;
	}

	public String getUserRestrict() {
		return userRestrict;
	}

	public void setUserRestrict(String userRestrict) {
		this.userRestrict = userRestrict;
	}


	public String getJsonDiagram() {
		return jsonDiagram;
	}


	public void setJsonDiagram(String jsonDiagram) {
		this.jsonDiagram = jsonDiagram;
	}


	public boolean isHistorisation() {
		return historisation;
	}


	public void setHistorisation(boolean historisation) {
		this.historisation = historisation;
	}


	public List<CAttribute> getHistoAttributes() {
		return histoAttributes;
	}


	public void setHistoAttributes(List<CAttribute> histoAttributes) {
		this.histoAttributes = histoAttributes;
	}


	public String getSelectedAttribute() {
		return selectedAttribute;
	}


	public void setSelectedAttribute(String selectedAttribute) {
		this.selectedAttribute = selectedAttribute;
	}


	public List<String> getEntitesIndependantes() {
		return entitesIndependantes;
	}


	public void setEntitesIndependantes(List<String> entitesIndependantes) {
		this.entitesIndependantes = entitesIndependantes;
	}


	public List<String> getEntitesLabel() {
		return entitesLabel;
	}


	public void setEntitesLabel(List<String> entitesLabel) {
		this.entitesLabel = entitesLabel;
	}


	/**
	 * @return the textarea
	 */
	public boolean isTextarea() {
		return textarea;
	}


	/**
	 * @param textarea the textarea to set
	 */
	public void setTextarea(boolean textarea) {
		this.textarea = textarea;
	}


	public int getFieldLength() {
		return fieldLength;
	}


	public void setFieldLength(int fieldLength) {
		this.fieldLength = fieldLength;
	}
}
