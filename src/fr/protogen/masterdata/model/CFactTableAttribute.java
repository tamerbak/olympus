/**
 * 
 */
package fr.protogen.masterdata.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author developer
 *
 */
public class CFactTableAttribute implements Serializable {
	private static final long serialVersionUID = 1L;
	private int id;
	private long tempId;
	private int entityId;
	private int attributeId;
	private String labelAttribute;
	private boolean index;
	private boolean xDimension;
	private boolean yDimension;
	private boolean groupBy;
	private String aggregationFct;
	private Map<String, String> constraints = new HashMap<String, String>();
	private String constraintOperator;
	private String constraintValue;
	private String dateFormat;
	
	
	private CAttribute attribute;
	private CBusinessClass entity;
	/**
	 * @return the entityId
	 */
	public int getEntityId() {
		return entityId;
	}
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
	 * @param entityId the entityId to set
	 */
	public void setEntityId(int entityId) {
		this.entityId = entityId;
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
	 * @return the aggregationFct
	 */
	public String getAggregationFct() {
		return aggregationFct;
	}
	/**
	 * @param aggregationFct the aggregationFct to set
	 */
	public void setAggregationFct(String aggregationFct) {
		this.aggregationFct = aggregationFct;
	}
	/**
	 * @return the attribute
	 */
	public CAttribute getAttribute() {
		return attribute;
	}
	/**
	 * @param attribute the attribute to set
	 */
	public void setAttribute(CAttribute attribute) {
		this.attribute = attribute;
	}
	/**
	 * @return the entity
	 */
	public CBusinessClass getEntity() {
		return entity;
	}
	/**
	 * @param entity the entity to set
	 */
	public void setEntity(CBusinessClass entity) {
		this.entity = entity;
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
	 * @return the serialversionuid
	 */
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	/**
	 * @return the constraints
	 */
	public Map<String, String> getConstraints() {
		return constraints;
	}
	/**
	 * @param constraints the constraints to set
	 */
	public void setConstraints(Map<String, String> constraints) {
		this.constraints = constraints;
	}
	/**
	 * @return the tempId
	 */
	public long getTempId() {
		return tempId;
	}
	/**
	 * @param tempId the tempId to set
	 */
	public void setTempId(long tempId) {
		this.tempId = tempId;
	}
	/**
	 * @return the index
	 */
	public boolean isIndex() {
		return index;
	}
	/**
	 * @param index the index to set
	 */
	public void setIndex(boolean index) {
		this.index = index;
	}
	/**
	 * @return the xDimension
	 */
	public boolean isxDimension() {
		return xDimension;
	}
	/**
	 * @param xDimension the xDimension to set
	 */
	public void setxDimension(boolean xDimension) {
		this.xDimension = xDimension;
	}
	/**
	 * @return the yDimension
	 */
	public boolean isyDimension() {
		return yDimension;
	}
	/**
	 * @param yDimension the yDimension to set
	 */
	public void setyDimension(boolean yDimension) {
		this.yDimension = yDimension;
	}
	/**
	 * @return the groupBy
	 */
	public boolean isGroupBy() {
		return groupBy;
	}
	/**
	 * @param groupBy the groupBy to set
	 */
	public void setGroupBy(boolean groupBy) {
		this.groupBy = groupBy;
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
