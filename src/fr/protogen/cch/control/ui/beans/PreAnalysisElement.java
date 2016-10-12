package fr.protogen.cch.control.ui.beans;

import java.io.Serializable;

public class PreAnalysisElement implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5217602085024854201L;
	private String title;
	private String description;
	private int count;
	
	public PreAnalysisElement(String functionTitle, String functionDescription) {
		title = functionTitle;
		description = functionDescription;
		count = (title+"-"+description).hashCode();
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
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
}
