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
 * MDocument generated by hbm2java
 */
@Entity
@Table(name = "m_document", schema = "public")
public class MDocument implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5725966755891298246L;
	private int id;
	private String title;
	private String stream;
	private char parameterMode;
	private Set<CDocumentbutton> CDocumentbuttons = new HashSet<CDocumentbutton>(
			0);

	public MDocument() {
	}

	public MDocument(int id, String title, String stream) {
		this.id = id;
		this.title = title;
		this.stream = stream;
	}

	public MDocument(int id, String title, String stream,
			Set<CDocumentbutton> CDocumentbuttons) {
		this.id = id;
		this.title = title;
		this.stream = stream;
		this.CDocumentbuttons = CDocumentbuttons;
	}

	@Id
	@Column(name = "id", unique = true, nullable = false)
	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Column(name = "title", nullable = false, length = 256)
	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Column(name = "stream", nullable = false)
	public String getStream() {
		return this.stream;
	}

	public void setStream(String stream) {
		this.stream = stream;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "MDocument")
	public Set<CDocumentbutton> getCDocumentbuttons() {
		return this.CDocumentbuttons;
	}

	public void setCDocumentbuttons(Set<CDocumentbutton> CDocumentbuttons) {
		this.CDocumentbuttons = CDocumentbuttons;
	}

	public char getParameterMode() {
		return parameterMode;
	}

	public void setParameterMode(char parameterMode) {
		this.parameterMode = parameterMode;
	}

}
