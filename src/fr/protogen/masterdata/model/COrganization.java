package fr.protogen.masterdata.model;

import java.io.Serializable;

@SuppressWarnings("serial")
public class COrganization implements Serializable {
	private int id;
	private String label;
	private CBusinessClass representative;
	
	/*
	 * 	GETTERS AND SETTERS
	 */
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public CBusinessClass getRepresentative() {
		return representative;
	}
	public void setRepresentative(CBusinessClass representative) {
		this.representative = representative;
	}
	
}
