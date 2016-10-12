package fr.protogen.masterdata.model;

import java.io.Serializable;
import java.util.List;


@SuppressWarnings("serial")
public class CParametersWindow extends CWindow implements Serializable {
	private int id;
	private String title;
	private String description;
	private List<CUIParameter> uiParameters;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
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
	public List<CUIParameter> getUiParameters() {
		return uiParameters;
	}
	public void setUiParameters(List<CUIParameter> uiParameters) {
		this.uiParameters = uiParameters;
	}
	
}
