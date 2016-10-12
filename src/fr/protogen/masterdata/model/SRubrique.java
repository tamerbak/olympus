package fr.protogen.masterdata.model;

import java.io.Serializable;
import java.util.List;


@SuppressWarnings("serial")
public class SRubrique implements Serializable {
	private int id;
	private String titre;
	private String description;
	private boolean pilotage;
	private List<SMenuitem> items;
	private boolean oneColumne;
	private boolean technical;
	
	private String nomModule;
	private boolean display;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getTitre() {
		return titre;
	}
	public void setTitre(String titre) {
		this.titre = titre;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public boolean isPilotage() {
		return pilotage;
	}
	public void setPilotage(boolean pilotage) {
		this.pilotage = pilotage;
	}
	public List<SMenuitem> getItems() {
		return items;
	}
	public void setItems(List<SMenuitem> items) {
		this.items = items;
	}
	
	public boolean isOneColumne() {
		return oneColumne;
	}
	public void setOneColumne(boolean oneColumne) {
		this.oneColumne = oneColumne;
	}
	public boolean isTechnical() {
		return technical;
	}
	public void setTechnical(boolean technical) {
		this.technical = technical;
	}
	public boolean isDisplay() {
		return display;
	}
	public void setDisplay(boolean display) {
		this.display = display;
	}
	public String getNomModule() {
		return nomModule;
	}
	public void setNomModule(String nomModule) {
		this.nomModule = nomModule;
	}
	
}
