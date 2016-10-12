package fr.protogen.masterdata.model;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings("serial")
public class GOrganization implements Serializable {

	private int id;
	private int idBean;
	private List<GOrganization> children;
	private GOrganization parent;
	private CBusinessClass representativeEntity;
	private String name;
	private boolean root;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getIdBean() {
		return idBean;
	}
	public void setIdBean(int idBean) {
		this.idBean = idBean;
	}
	public List<GOrganization> getChildren() {
		return children;
	}
	public void setChildren(List<GOrganization> children) {
		this.children = children;
	}
	public CBusinessClass getRepresentativeEntity() {
		return representativeEntity;
	}
	public void setRepresentativeEntity(CBusinessClass representativeEntity) {
		this.representativeEntity = representativeEntity;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public GOrganization getParent() {
		return parent;
	}
	public void setParent(GOrganization parent) {
		this.parent = parent;
	}
	public boolean isRoot() {
		return root;
	}
	public void setRoot(boolean root) {
		this.root = root;
	}
}
