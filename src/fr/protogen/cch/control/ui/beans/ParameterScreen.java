package fr.protogen.cch.control.ui.beans;

import java.util.List;

public class ParameterScreen {
	private String title;
	private String description;
	private List<DocumentParameter> params;
	
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
	public List<DocumentParameter> getParams() {
		return params;
	}
	public void setParams(List<DocumentParameter> params) {
		this.params = params;
	}
}
