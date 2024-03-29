package fr.protogen.masterdata.model;

// Generated 12 oct. 2012 16:40:54 by Hibernate Tools 3.4.0.CR1

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class CWindow implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int id;
	private CWindowtype CWindowtype;
	private String title;
	private String stepDescription;
	private int percentage;
	private String helpVideo;
	private Set<CoreAclElement> coreAclElements = new HashSet<CoreAclElement>(0);
	private List<CDocumentbutton> CDocumentbuttons = new ArrayList<CDocumentbutton>(
			0);
	private List<CDocumentbutton> boundDocuments = new ArrayList<CDocumentbutton>(
			0);
	private Set<CBusinessentity> CBusinessentities = new HashSet<CBusinessentity>(
			0);
	private List<CAttribute> CAttributes = new ArrayList<CAttribute>();
	private List<CActionbutton> CActionbuttons = new ArrayList<CActionbutton>(0);
	private String mainEntity;
	private String parameters;
	private String appKey;
	private SScreensequence function;
	private List<CGlobalValue> globalValues = new ArrayList<CGlobalValue>();
	private CAttribute rappelReference;
	private List<CWindow> links = new ArrayList<CWindow>();
	private int formId;
	
	private boolean updateBtn;
	private boolean deleteBtn;
	
	private boolean forceFilter;
	
	private String selectConstraints = "";
	
	public CWindow() {
	}

	public CWindow(int id, CWindowtype CWindowtype, String title, int percentage) {
		this.id = id;
		this.CWindowtype = CWindowtype;
		this.title = title;
		this.percentage = percentage;
	}

	public CWindow(int id, CWindowtype CWindowtype, String title,
			String stepDescription, int percentage, String helpVideo,
			Set<CoreAclElement> coreAclElements,
			List<CDocumentbutton> CDocumentbuttons,
			Set<CBusinessentity> CBusinessentities,
			List<CAttribute> CAttributes, List<CActionbutton> CActionbuttons) {
		this.id = id;
		this.CWindowtype = CWindowtype;
		this.title = title;
		this.stepDescription = stepDescription;
		this.percentage = percentage;
		this.helpVideo = helpVideo;
		this.coreAclElements = coreAclElements;
		this.CDocumentbuttons = CDocumentbuttons;
		this.CBusinessentities = CBusinessentities;
		this.CAttributes = CAttributes;
		this.CActionbuttons = CActionbuttons;
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public CWindowtype getCWindowtype() {
		return this.CWindowtype;
	}

	public void setCWindowtype(CWindowtype CWindowtype) {
		this.CWindowtype = CWindowtype;
	}

	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getStepDescription() {
		return this.stepDescription;
	}

	public void setStepDescription(String stepDescription) {
		this.stepDescription = stepDescription;
	}

	public int getPercentage() {
		return this.percentage;
	}

	public void setPercentage(int percentage) {
		this.percentage = percentage;
	}

	public String getHelpVideo() {
		return this.helpVideo;
	}

	public void setHelpVideo(String helpVideo) {
		this.helpVideo = helpVideo;
	}

	public Set<CoreAclElement> getCoreAclElements() {
		return this.coreAclElements;
	}

	public void setCoreAclElements(Set<CoreAclElement> coreAclElements) {
		this.coreAclElements = coreAclElements;
	}

	public List<CDocumentbutton> getCDocumentbuttons() {
		return this.CDocumentbuttons;
	}

	public void setCDocumentbuttons(List<CDocumentbutton> CDocumentbuttons) {
		this.CDocumentbuttons = CDocumentbuttons;
	}

	public Set<CBusinessentity> getCBusinessentities() {
		return this.CBusinessentities;
	}

	public void setCBusinessentities(Set<CBusinessentity> CBusinessentities) {
		this.CBusinessentities = CBusinessentities;
	}

	public List<CAttribute> getCAttributes() {
		return this.CAttributes;
	}

	public void setCAttributes(List<CAttribute> CAttributes) {
		this.CAttributes = CAttributes;
	}

	public List<CActionbutton> getCActionbuttons() {
		return this.CActionbuttons;
	}

	public void setCActionbuttons(List<CActionbutton> CActionbuttons) {
		this.CActionbuttons = CActionbuttons;
	}

	public String getMainEntity() {
		return mainEntity;
	}

	public void setMainEntity(String mainEntity) {
		this.mainEntity = mainEntity;
	}

	public String getParameters() {
		return parameters;
	}

	public void setParameters(String parameters) {
		this.parameters = parameters;
	}

	public String getAppKey() {
		return appKey;
	}

	public void setAppKey(String appKey) {
		this.appKey = appKey;
	}

	public SScreensequence getFunction() {
		return function;
	}

	public void setFunction(SScreensequence function) {
		this.function = function;
	}

	public List<CGlobalValue> getGlobalValues() {
		return globalValues;
	}

	public void setGlobalValues(List<CGlobalValue> globalValues) {
		this.globalValues = globalValues;
	}

	public CAttribute getRappelReference() {
		return rappelReference;
	}

	public void setRappelReference(CAttribute rappelReference) {
		this.rappelReference = rappelReference;
	}

	public List<CWindow> getLinks() {
		return links;
	}

	public void setLinks(List<CWindow> links) {
		this.links = links;
	}

	public List<CDocumentbutton> getBoundDocuments() {
		return boundDocuments;
	}

	public void setBoundDocuments(List<CDocumentbutton> boundDocuments) {
		this.boundDocuments = boundDocuments;
	}

	public int getFormId() {
		return formId;
	}

	public void setFormId(int formId) {
		this.formId = formId;
	}

	public boolean isUpdateBtn() {
		return updateBtn;
	}

	public void setUpdateBtn(boolean updateBtn) {
		this.updateBtn = updateBtn;
	}

	public boolean isDeleteBtn() {
		return deleteBtn;
	}

	public void setDeleteBtn(boolean deleteBtn) {
		this.deleteBtn = deleteBtn;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public boolean isForceFilter() {
		return forceFilter;
	}

	public void setForceFilter(boolean forceFilter) {
		this.forceFilter = forceFilter;
	}

	public String getSelectConstraints() {
		return selectConstraints;
	}

	public void setSelectConstraints(String selectConstraints) {
		this.selectConstraints = selectConstraints;
	}

}