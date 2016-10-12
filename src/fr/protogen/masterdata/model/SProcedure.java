package fr.protogen.masterdata.model;

import java.io.Serializable;
import java.util.List;

public class SProcedure implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int id;
	private String title;
	private String description;
	private String appKey;
	private List<String> keyWords;
	private List<SStep> etapes;
	private List<SAtom> atoms;
	
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
	public String getAppKey() {
		return appKey;
	}
	public void setAppKey(String appKey) {
		this.appKey = appKey;
	}
	public List<String> getKeyWords() {
		return keyWords;
	}
	public void setKeyWords(List<String> keyWords) {
		this.keyWords = keyWords;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	public List<SStep> getEtapes() {
		return etapes;
	}
	public void setEtapes(List<SStep> etapes) {
		this.etapes = etapes;
	}
	
	public List<SAtom> getAtoms() {
		return atoms;
	}
	public void setAtoms(List<SAtom> atoms) {
		this.atoms = atoms;
	}
	
	

}
