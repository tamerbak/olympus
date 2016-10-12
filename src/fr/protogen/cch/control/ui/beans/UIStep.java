package fr.protogen.cch.control.ui.beans;

import java.io.Serializable;

public class UIStep implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7515135551070899471L;
	private String stepTitle;
	private String stepDescription;
	private String stepHelp;
	
		
	
	public UIStep(String stepTitle, String stepDescription, String stepHelp) {
		super();
		this.stepTitle = stepTitle;
		this.stepDescription = stepDescription;
		this.stepHelp = stepHelp;
	}
	
	
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
	public String getStepHelp() {
		return stepHelp;
	}
	public void setStepHelp(String stepHelp) {
		this.stepHelp = stepHelp;
	}
	
	
}
