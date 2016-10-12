/**
 * 
 */
package fr.protogen.cch.control;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.bean.SessionScoped;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.apache.commons.lang.StringUtils;

import fr.protogen.cch.cache.ApplicationRegistery;
import fr.protogen.cch.cache.SessionCache;
import fr.protogen.cch.configuration.Parameters;
import fr.protogen.cch.genration.GenerationService;
import fr.protogen.masterdata.model.CAttribute;
import fr.protogen.masterdata.model.CBusinessClass;
import fr.protogen.masterdata.model.CCHUser;
import fr.protogen.masterdata.model.CFactTable;
import fr.protogen.masterdata.model.CFactTableAttribute;
import fr.protogen.masterdata.model.CoreRole;
import fr.protogen.masterdata.model.CoreUser;

/**
 * @author developer
 *
 */
@ManagedBean
@ViewScoped
public class DecisionalDashboardCtrl implements Serializable{
	private static final long serialVersionUID = 7197448678958238368L;
	
	private SessionCache cache;
	private Boolean maintainmode;
	private List<CBusinessClass> entities = new ArrayList<CBusinessClass>();
	private List<CAttribute> attributes = new ArrayList<CAttribute>();
	
	private String tableName;
	private int entityId;
	private int attributeId;
	private String labelAttribute;
	private String indexed;
	private String rowDimension;
	private String columnDimension;
	private String groupedBy;
	private String aggregationFct;
	private String dateFormat;
	
	private List<String> aggregationFonctions = Arrays.asList("min", "max", "sum", "count", "avg");
	
	private String constraintOperator;
	private String constraintValue;
	private String generatedQuery = "";
	
	private String requestText;
	
	private List<CFactTableAttribute> factAttributes = new ArrayList<CFactTableAttribute>();
	private CFactTable factTable;
	private List<CFactTable> factTables = new ArrayList<CFactTable>();
	
	private int[] selectedRoles;
	private List<CoreRole> roles = new ArrayList<CoreRole>();
	
	@PostConstruct
	public void postLoad(){
		CCHUser user = (CCHUser)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(Parameters.USER_TOKEN);
    	cache = ApplicationRegistery.getInstance().getSession(user);
    	roles = cache.getRoles();
		//	Load entities
		entities = cache.getEntities();
		entityId = entities.get(0).getId();
		//setAttributes(cache.getAttributes());
		resetAttributes();
		
		retrieveFactTables();
		String ftId = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("fact_table_id");
		if(ftId != null && !ftId.isEmpty()) {
			retrieveFactTable(Integer.parseInt(ftId));
		}
		maintainmode = (Boolean)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(Parameters.MAINTAIN_MODE);
		if(maintainmode){
		}
		
	}
	
	private void retrieveFactTables() {
		GenerationService gservice = new GenerationService();
		setFactTables(gservice.retrieveFactTables(cache.getAppKey()));
	}
	
	private void retrieveFactTable(Integer id) {
		for(CFactTable cft : factTables) {
			if(cft.getId() == id) {
				factTable = cft;
				break;
			}
		}
		GenerationService gservice = new GenerationService();
		factTable = gservice.retrieveFactTable(factTable);
		factAttributes = factTable.getAttributes();
		requestText = factTable.getQuery();
	}
	
	public void resetAttributes() {
		List<CAttribute> allAttributes = cache.getAttributes();
		attributes.clear();
		for(CAttribute attr : allAttributes) {
			if(attr.getEntity().getId() == entityId) {
				attributes.add(attr);
			}
		}
	}
	
	public void deleteFactTable() {
		String ftId = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("fact_table_id");
		GenerationService gservice = new GenerationService();
		Integer id = Integer.parseInt(ftId);
		CFactTable toDelete = null;
		
		for(int i= 0; i<factTables.size(); i++  ){
			if(factTables.get(i).getId()==id) {
				toDelete = factTables.get(i);
				break;
			}
		}
		if(gservice.deleteFactTable(id, toDelete.getViewName())) {
			if(toDelete != null) {
				factTables.remove(toDelete);
			}
		}
		
	}
	
	public void addAttribute() {
		CFactTableAttribute ftAttr = new CFactTableAttribute();
		long randInt = (int)Math.random()*1000000;
		ftAttr.setTempId(new Date().getTime()+randInt);
		ftAttr.setAggregationFct(aggregationFct);
		ftAttr.setAttributeId(attributeId);
		ftAttr.setEntityId(entityId);
		ftAttr.setLabelAttribute(labelAttribute);
		ftAttr.setIndex(indexed.equals("O")?true:false);
		ftAttr.setxDimension(rowDimension.equals("O")?true:false);
		ftAttr.setyDimension(columnDimension.equals("O")?true:false);
		ftAttr.setGroupBy(groupedBy.equals("O")?true:false);
		ftAttr.setConstraintOperator(constraintOperator);
		ftAttr.setConstraintValue(constraintValue);
		ftAttr.getConstraints().put(constraintOperator, constraintValue);
		for(CAttribute a : attributes) {
			if(a.getId() == attributeId) {
				ftAttr.setAttribute(a);
				break;
			}
		}
		
		if(ftAttr.getAttribute().getCAttributetype().getId()== 3) {
			ftAttr.setDateFormat(dateFormat);
		}
		
		
		ftAttr.setEntity(ftAttr.getAttribute().getEntity());
		
		factAttributes.add(ftAttr);
		
		FacesContext.getCurrentInstance().addMessage("Attribute ajouté avec succès", new FacesMessage("something"));
	}
	
	public void deleteAttribute() {
		String parmTempId = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("tempId");
		try{
			long tempId = Long.parseLong(parmTempId);
			System.out.println(tempId);
			CFactTableAttribute toDel = null;
			for(int i = 0; i < factAttributes.size(); i++) {
				if(tempId == factAttributes.get(i).getTempId()) {
					toDel = factAttributes.get(i);
					break;
				}
			}
			if(toDel != null) {
				factAttributes.remove(toDel);
			}
		}catch(NumberFormatException e) {
			e.printStackTrace();
		}
	}
	
	public String validDecisonDashboard() {
		Map<String, String> operators = new HashMap<String, String>();
		operators.put("e", "=");
		operators.put("ne", "<>");
		operators.put("l", "<");
		operators.put("le", "<=");
		operators.put("g", ">");
		operators.put("ge", ">=");
		operators.put("in", "in");
		
		List<String> tables = new ArrayList<String>();
		List<CBusinessClass> entities =  new ArrayList<CBusinessClass>();
		Map<String, CBusinessClass> tableEntity = new HashMap<String, CBusinessClass>();
		String select = "SELECT "; 
		String from = " FROM ";
		String where = " WHERE ";
		String groupBy = " GROUP BY ";
		
		for(CFactTableAttribute attr : factAttributes) {
			// from part
			if(!tables.contains(attr.getEntity().getDataReference())) {

				tables.add(attr.getEntity().getDataReference());
				entities.add(attr.getEntity());
				tableEntity.put(attr.getEntity().getDataReference(), attr.getEntity());
			}
		}
		
		from += tables.get(0)+ " ";
//		for(CFactTableAttribute attr : factAttributes) {
//			if(attr.isxDimension()) {
//				from += attr.getEntity().getDataReference()+",";
//			}
//		}
		List<String> wheres = new ArrayList<String>();
		List<String> joinTables = new ArrayList<String>();
		joinTables.add(tables.get(0));
		String join =  joinRequest(entities, tables, joinTables, wheres);
		String multipleJoin1 = multipleJoin(entities);
		String multipleJoin2 = multipleJoin2(tableEntity);
		join += " "+multipleJoin1+" "+multipleJoin2;
	
		if(from.endsWith(",")) {
			from = from.substring(0, from.length()-1);
		}
		
		for(Iterator<CFactTableAttribute> it = factAttributes.iterator(); it.hasNext();) {
			CFactTableAttribute attr = it.next();
			// select part
			String prop = attr.getEntity().getDataReference()+"."+attr.getAttribute().getDataReference();
			if(!"".equals(attr.getAggregationFct())) {
				prop=attr.getAggregationFct()+"("+prop+") ";
			}else if(attr.getDateFormat()!=null && !"".equals(attr.getDateFormat())) {
				prop = "to_char("+prop+",'"+attr.getDateFormat()+"')";
			}
			select += prop+  " AS \""+attr.getLabelAttribute()+"\", ";
			
			
			
			if(!"".equals(attr.getConstraintOperator())) {
				where +=attr.getEntity().getDataReference()+"."+attr.getAttribute().getDataReference()+" ";
				where +=operators.get(attr.getConstraintOperator())+" ";
				where +=attr.getConstraintValue();
				if(it.hasNext()) {
					where +=" AND ";
				}
			}
			
			if(attr.isGroupBy()) {
				groupBy += attr.getEntity().getDataReference()+"."+attr.getAttribute().getDataReference()+", " ;
			}
		}
		
		if(select.trim().endsWith(",")) {
			select = select.trim().substring(0, select.trim().length()-1);
		}
		String finalQuery = select+from+join;
		if(where.trim().endsWith("WHERE")) {
			where = "";
		}
		for(String w:wheres) {
			where+= " AND "+w;
		}
		if(groupBy.trim().endsWith("GROUP BY")) {
			groupBy = "";
		}else if(groupBy.trim().endsWith(",")) {
			groupBy = groupBy.trim().substring(0, groupBy.trim().length()-1);
		}
		finalQuery += where+" "+groupBy;
		
		setRequestText(finalQuery);
		CFactTable factTable = new CFactTable();
		factTable.setTableName(tableName);
		factTable.setQuery(finalQuery);
		factTable.setAppKey(cache.getAppKey());
		factTable.setAttributes(factAttributes);
		factTable.setRoles(new ArrayList<CoreRole>());
		
		for(CoreRole r : roles) {
			if(roleIsSelected(r)) {
				factTable.getRoles().add(r);
			}
		}
		
		GenerationService gservice = new GenerationService();
		gservice.addNewFactTable(factTable);
		
		
		return "dashboardlist.xhtml";
		
	}
	private boolean roleIsSelected(CoreRole r) {
		for(int id : selectedRoles) {
			if(r.getId() == id) {
				return true;
			}
		}
		
		return false;
	}
	
	private String joinRequest(List<CBusinessClass> entities, List<String> tables, List<String> joinTables, List<String> wheres) {
//		
		
		String join=" ";
		for(CBusinessClass entity : entities) {
			String tableEntity = entity.getDataReference();
			for(CAttribute attr : entity.getAttributes()) {
				if(attr.isReference() && attr.getDataReference().startsWith("fk_")) {
					String tableRef = attr.getDataReference().substring(3);
					if( tables.contains(tableRef)) {
						if(!joinTables.contains(tableRef)) {
							join += " LEFT JOIN "+tableEntity+" ON "+ tableEntity+"."+attr.getDataReference();
							join += " = ";
							join += tableRef+".pk_"+tableRef;
							
							joinTables.add(tableRef);
						}else {
							String where = " ";
							where += tableEntity+"."+attr.getDataReference();
							where += " = ";
							where += tableRef+".pk_"+tableRef;
							wheres.add(where);
						}
					}
				} else if(attr.isMultiple()) {
					String attrDataRef = attr.getDataReference();
					String nameTables = attrDataRef.substring(3);
					String[] lesTables= nameTables.split("__");
					if(tables.contains(lesTables[0])) {
						if(joinTables.contains(lesTables[0])) {
							join += " LEFT JOIN "+lesTables[0]+" ON "+ lesTables[0]+"."+attrDataRef;
							join += " = ";
							join += lesTables[1]+".pk_"+lesTables[1];
						
							joinTables.add(lesTables[0]);
							
						} else {
							String where = " ";
							join += lesTables[0]+"."+attrDataRef;
							join += " = ";
							join += lesTables[1]+".pk_"+lesTables[1];
							wheres.add(where);
						}
					}
					
				}
			}
		}
		
		return join;
	}
	
	private String multipleJoin(List<CBusinessClass> entities) {
		String join = "";
		List<CAttribute> allAttributes = cache.getAttributes();
		// la table 1
		for(CBusinessClass e : entities) {
			for(CAttribute at : allAttributes ) {
				// chercher la table intermediaire
				if(at.getDataReference().equals("fk_"+e.getDataReference())) {
					CBusinessClass e2 = at.getEntity();
					for(CAttribute at2 : e2.getAttributes()) {
						for(CBusinessClass e3 : entities) {
							if(e3.getDataReference().equals(e.getDataReference()) || e3.getDataReference().equals(e2.getDataReference())) {
								continue;
							}
							if(at2.getDataReference().equals("fk_"+e3.getDataReference())) {
								join += "LEFT JOIN "+e.getDataReference()+" ON "+e.getDataReference()+".pk_"+e.getDataReference()+"="+e2.getDataReference()+".fk"+e.getDataReference();
								join += " AND ";
								join += e3.getDataReference()+".pk_"+e3.getDataReference()+"="+e2.getDataReference()+".fk"+e3.getDataReference();
								
								break;
							}
						}
					} 
				}
			}
		}
		return join;
	}
	
	private String multipleJoin2(Map<String, CBusinessClass> tabEntities) {
		String join = "";
		List<CBusinessClass> entities = new ArrayList<CBusinessClass>(tabEntities.values());
		for(CBusinessClass e1 : entities ) {
			for(CAttribute att1 : e1.getAttributes()) {
				if(att1.isMultiple()){
					String t2 = att1.getDataReference().substring(3,att1.getDataReference().lastIndexOf(e1.getDataReference())-2);
					CBusinessClass e2 = getTheEntity(t2);
					if(e2 != null) {
						for(CAttribute att2 : e2.getAttributes()) {
							if(att2.isMultiple()) {
								// TODO : sana3oud
							} else if(att2.isReference()) {
								String t3 = att2.getDataReference().substring(3);
								if(tabEntities.get(t3) != null) {
									join += " LEFT JOIN "+t2+" ON "+e1.getDataReference()+".pk_"+e1.getDataReference()+"="+t2+"."+att1.getDataReference();
									join += " LEFT JOIN "+t3+" ON ";
									join += t3+".pk_"+t3+"="+e2.getDataReference()+".fk_"+t3;
								}
							}
						}
					}
				}
			}
		}
		return join;
		
	}
	
	private CBusinessClass getTheEntity(String dataRef) {
		for(CBusinessClass entity : entities) {
			if(entity.getDataReference().equals(dataRef)) {
				return entity;
			}
		}
		return null;
	}

	/**
	 * @return the cache
	 */
	public SessionCache getCache() {
		return cache;
	}

	/**
	 * @param cache the cache to set
	 */
	public void setCache(SessionCache cache) {
		this.cache = cache;
	}

	/**
	 * @return the maintainmode
	 */
	public Boolean getMaintainmode() {
		return maintainmode;
	}

	/**
	 * @param maintainmode the maintainmode to set
	 */
	public void setMaintainmode(Boolean maintainmode) {
		this.maintainmode = maintainmode;
	}

	/**
	 * @return the entities
	 */
	public List<CBusinessClass> getEntities() {
		return entities;
	}

	/**
	 * @param entities the entities to set
	 */
	public void setEntities(List<CBusinessClass> entities) {
		this.entities = entities;
	}

	/**
	 * @return the attributes
	 */
	public List<CAttribute> getAttributes() {
		return attributes;
	}

	/**
	 * @param attributes the attributes to set
	 */
	public void setAttributes(List<CAttribute> attributes) {
		this.attributes = attributes;
	}

	/**
	 * @return the tableName
	 */
	public String getTableName() {
		return tableName;
	}

	/**
	 * @param tableName the tableName to set
	 */
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	/**
	 * @return the entityId
	 */
	public int getEntityId() {
		return entityId;
	}

	/**
	 * @param entityId the entityId to set
	 */
	public void setEntityId(int entityId) {
		this.entityId = entityId;
	}

	/**
	 * @return the attrbuteId
	 */
	public int getAttrbuteId() {
		return attributeId;
	}

	/**
	 * @param attrbuteId the attrbuteId to set
	 */
	public void setAttrbuteId(int attrbuteId) {
		this.attributeId = attrbuteId;
	}

	/**
	 * @return the labelAttribute
	 */
	public String getLabelAttribute() {
		return labelAttribute;
	}

	/**
	 * @param labelAttribute the labelAttribute to set
	 */
	public void setLabelAttribute(String labelAttribute) {
		this.labelAttribute = labelAttribute;
	}

	/**
	 * @return the aggregationProcedure
	 */
	public String getAggregationFct() {
		return aggregationFct;
	}

	/**
	 * @param aggregationProcedure the aggregationProcedure to set
	 */
	public void setAggregationFct(String aggregationProcedure) {
		this.aggregationFct = aggregationProcedure;
	}

	/**
	 * @return the generatedQuery
	 */
	public String getGeneratedQuery() {
		return generatedQuery;
	}

	/**
	 * @param generatedQuery the generatedQuery to set
	 */
	public void setGeneratedQuery(String generatedQuery) {
		this.generatedQuery = generatedQuery;
	}

	/**
	 * @return the attributeId
	 */
	public int getAttributeId() {
		return attributeId;
	}

	/**
	 * @param attributeId the attributeId to set
	 */
	public void setAttributeId(int attributeId) {
		this.attributeId = attributeId;
	}

	/**
	 * @return the aggregationfonctions
	 */
	public List<String> getAggregationfonctions() {
		return aggregationFonctions;
	}

	/**
	 * @return the indexed
	 */
	public String getIndexed() {
		return indexed;
	}

	/**
	 * @param indexed the indexed to set
	 */
	public void setIndexed(String indexed) {
		this.indexed = indexed;
	}

	/**
	 * @return the rowDimension
	 */
	public String getRowDimension() {
		return rowDimension;
	}

	/**
	 * @param rowDimension the rowDimension to set
	 */
	public void setRowDimension(String rowDimension) {
		this.rowDimension = rowDimension;
	}

	/**
	 * @return the columnDimension
	 */
	public String getColumnDimension() {
		return columnDimension;
	}

	/**
	 * @param columnDimension the columnDimension to set
	 */
	public void setColumnDimension(String columnDimension) {
		this.columnDimension = columnDimension;
	}

	/**
	 * @return the groupedBy
	 */
	public String getGroupedBy() {
		return groupedBy;
	}

	/**
	 * @param groupedBy the groupedBy to set
	 */
	public void setGroupedBy(String groupedBy) {
		this.groupedBy = groupedBy;
	}

	/**
	 * @return the attributesId
	 */
	public List<CFactTableAttribute> getAttributesId() {
		return factAttributes;
	}

	/**
	 * @param attributesId the attributesId to set
	 */
	public void setAttributesId(List<CFactTableAttribute> attributesId) {
		this.factAttributes = attributesId;
	}

	/**
	 * @return the constraintOperator
	 */
	public String getConstraintOperator() {
		return constraintOperator;
	}

	/**
	 * @param constraintOperator the constraintOperator to set
	 */
	public void setConstraintOperator(String constraintOperator) {
		this.constraintOperator = constraintOperator;
	}

	/**
	 * @return the constraintValue
	 */
	public String getConstraintValue() {
		return constraintValue;
	}

	/**
	 * @param constraintValue the constraintValue to set
	 */
	public void setConstraintValue(String constraintValue) {
		this.constraintValue = constraintValue;
	}

	/**
	 * @return the factAttributes
	 */
	public List<CFactTableAttribute> getFactAttributes() {
		return factAttributes;
	}

	/**
	 * @param factAttributes the factAttributes to set
	 */
	public void setFactAttributes(List<CFactTableAttribute> factAttributes) {
		this.factAttributes = factAttributes;
	}

	/**
	 * @return the requestText
	 */
	public String getRequestText() {
		return requestText;
	}

	/**
	 * @param requestText the requestText to set
	 */
	public void setRequestText(String requestText) {
		this.requestText = requestText;
	}

	/**
	 * @return the factTables
	 */
	public List<CFactTable> getFactTables() {
		return factTables;
	}

	/**
	 * @param factTables the factTables to set
	 */
	public void setFactTables(List<CFactTable> factTables) {
		this.factTables = factTables;
	}

	/**
	 * @return the factTable
	 */
	public CFactTable getFactTable() {
		return factTable;
	}

	/**
	 * @param factTable the factTable to set
	 */
	public void setFactTable(CFactTable factTable) {
		this.factTable = factTable;
	}

	/**
	 * @return the selectedRoles
	 */
	public int[] getSelectedRoles() {
		return selectedRoles;
	}

	/**
	 * @param selectedRoles the selectedRoles to set
	 */
	public void setSelectedRoles(int[] selectedRoles) {
		this.selectedRoles = selectedRoles;
	}

	/**
	 * @return the roles
	 */
	public List<CoreRole> getRoles() {
		return roles;
	}

	/**
	 * @param roles the roles to set
	 */
	public void setRoles(List<CoreRole> roles) {
		this.roles = roles;
	}

	/**
	 * @return the dateFormat
	 */
	public String getDateFormat() {
		return dateFormat;
	}

	/**
	 * @param dateFormat the dateFormat to set
	 */
	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}

}
