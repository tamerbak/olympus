package fr.protogen.cch.control.ui.beans;

import fr.protogen.masterdata.model.CAttribute;
import fr.protogen.masterdata.model.CBusinessClass;

public class UITreeNode {
	private String name;
	private String type;
	private CAttribute attribute;
	private CBusinessClass entity;
	private boolean general;
	private boolean reference = false;
	private boolean visible=false;
	private boolean rappel=false;
	private boolean mtm=false;
	private int indirectFunction;
	private boolean filterEnabled;
	
	public UITreeNode(CBusinessClass mainEntity) {
		// TODO Auto-generated constructor stub
		name = mainEntity.getName();
		entity = mainEntity;
		general = true;
	}
	public UITreeNode(CAttribute a) {
		// TODO Auto-generated constructor stub
		name = a.getAttribute();
		attribute = a;
		general = false;
	}
	public UITreeNode(String name, String type){
		this.name = name;
		this.setType(type);
	}
	public CAttribute getAttribute() {
		return attribute;
	}
	public void setAttribute(CAttribute attribute) {
		this.attribute = attribute;
	}
	public CBusinessClass getEntity() {
		return entity;
	}
	public void setEntity(CBusinessClass entity) {
		this.entity = entity;
	}
	public boolean isGeneral() {
		return general;
	}
	public void setGeneral(boolean general) {
		this.general = general;
	}
	public boolean isReference() {
		return reference;
	}
	public void setReference(boolean reference) {
		this.reference = reference;
	}
	public boolean isVisible() {
		return visible;
	}
	public void setVisible(boolean visible) {
		this.visible = visible;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public boolean isRappel() {
		return rappel;
	}
	public void setRappel(boolean rappel) {
		this.rappel = rappel;
	}
	public boolean isMtm() {
		return mtm;
	}
	public void setMtm(boolean mtm) {
		this.mtm = mtm;
	}
	public int getIndirectFunction() {
		return indirectFunction;
	}
	public void setIndirectFunction(int indirectFunction) {
		this.indirectFunction = indirectFunction;
	}
	public boolean isFilterEnabled() {
		return filterEnabled;
	}
	public void setFilterEnabled(boolean filterEnabled) {
		this.filterEnabled = filterEnabled;
	}
}
