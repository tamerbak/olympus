package fr.protogen.masterdata.model;

// Generated 12 oct. 2012 16:40:54 by Hibernate Tools 3.4.0.CR1

import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * CAttributetype generated by hbm2java
 */
@Entity
@Table(name = "c_attributetype", schema = "public")
public class CAttributetype implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int id;
	private String type;
	private Set<CAttribute> CAttributes = new HashSet<CAttribute>(0);
	private Set<CWindowattribute> CWindowattributes = new HashSet<CWindowattribute>(
			0);

	public CAttributetype() {
	}

	public CAttributetype(int id, String type) {
		this.id = id;
		this.type = type;
	}

	public CAttributetype(int id, String type, Set<CAttribute> CAttributes,
			Set<CWindowattribute> CWindowattributes) {
		this.id = id;
		this.type = type;
		this.CAttributes = CAttributes;
		this.CWindowattributes = CWindowattributes;
	}

	@Id
	@Column(name = "id", unique = true, nullable = false)
	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Column(name = "type", nullable = false, length = 256)
	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "CAttributetype")
	public Set<CAttribute> getCAttributes() {
		return this.CAttributes;
	}

	public void setCAttributes(Set<CAttribute> CAttributes) {
		this.CAttributes = CAttributes;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "CAttributetype")
	public Set<CWindowattribute> getCWindowattributes() {
		return this.CWindowattributes;
	}

	public void setCWindowattributes(Set<CWindowattribute> CWindowattributes) {
		this.CWindowattributes = CWindowattributes;
	}

}