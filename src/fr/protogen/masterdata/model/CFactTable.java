/**
 * 
 */
package fr.protogen.masterdata.model;

import java.io.Serializable;
import java.util.List;

/**
 * @author developer
 *
 */
// c_fact_table
public class CFactTable implements Serializable{
	private static final long serialVersionUID = -1269609465301371965L;
	private int id;
	private String tableName;
	private String query;
	private List<CoreRole> roles;
	private List<CFactTableAttribute> attributes;
	private String appKey;
	private String viewName;
	
	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
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
	 * @return the query
	 */
	public String getQuery() {
		return query;
	}
	/**
	 * @param query the query to set
	 */
	public void setQuery(String query) {
		this.query = query;
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
	 * @return the attributes
	 */
	public List<CFactTableAttribute> getAttributes() {
		return attributes;
	}
	/**
	 * @param attributes the attributes to set
	 */
	public void setAttributes(List<CFactTableAttribute> attributes) {
		this.attributes = attributes;
	}
	/**
	 * @return the appKey
	 */
	public String getAppKey() {
		return appKey;
	}
	/**
	 * @param appKey the appKey to set
	 */
	public void setAppKey(String appKey) {
		this.appKey = appKey;
	}
	/**
	 * @return the viewName
	 */
	public String getViewName() {
		return viewName;
	}
	/**
	 * @param viewName the viewName to set
	 */
	public void setViewName(String viewName) {
		this.viewName = viewName;
	}
	

}
