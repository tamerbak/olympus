package fr.protogen.masterdata.model;

import java.io.Serializable;
import java.util.Date;

public class ButtonParameter implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2011585009870777943L;
	private String title;
	private String parameter;
	private String value;
	private String type;
	private boolean ctrlDate;
	private Date dateValue;
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getParameter() {
		return parameter;
	}
	public void setParameter(String parameter) {
		this.parameter = parameter;
	}
	
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public Date getDateValue() {
		return dateValue;
	}
	public void setDateValue(Date dateValue) {
		this.dateValue = dateValue;
		this.value = dateValue.toString();
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public boolean isCtrlDate() {
		return ctrlDate;
	}
	public void setCtrlDate(boolean ctrlDate) {
		this.ctrlDate = ctrlDate;
	}
	
	
}
