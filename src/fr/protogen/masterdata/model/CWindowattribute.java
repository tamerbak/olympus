package fr.protogen.masterdata.model;

// Generated 12 oct. 2012 16:40:54 by Hibernate Tools 3.4.0.CR1

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * CWindowattribute generated by hbm2java
 */
@Entity
@Table(name = "c_windowattribute", schema = "public")
public class CWindowattribute implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6172386392313168411L;
	private int id;
	private CAttributetype CAttributetype;
	private CBusinessentity CBusinessentity;
	private String propertyreference;

	public CWindowattribute() {
	}

	public CWindowattribute(int id, CAttributetype CAttributetype,
			CBusinessentity CBusinessentity, String propertyreference) {
		this.id = id;
		this.CAttributetype = CAttributetype;
		this.CBusinessentity = CBusinessentity;
		this.propertyreference = propertyreference;
	}

	@Id
	@Column(name = "id", unique = true, nullable = false)
	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_attributetype", nullable = false)
	public CAttributetype getCAttributetype() {
		return this.CAttributetype;
	}

	public void setCAttributetype(CAttributetype CAttributetype) {
		this.CAttributetype = CAttributetype;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_businessentity", nullable = false)
	public CBusinessentity getCBusinessentity() {
		return this.CBusinessentity;
	}

	public void setCBusinessentity(CBusinessentity CBusinessentity) {
		this.CBusinessentity = CBusinessentity;
	}

	@Column(name = "propertyreference", nullable = false, length = 256)
	public String getPropertyreference() {
		return this.propertyreference;
	}

	public void setPropertyreference(String propertyreference) {
		this.propertyreference = propertyreference;
	}

}
