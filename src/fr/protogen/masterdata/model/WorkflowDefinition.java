package fr.protogen.masterdata.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class WorkflowDefinition implements Serializable {
	private int id;
	private String title;
	private String appKey;
	private CWindow screen;
	private List<WorkflowNode> nodes = new ArrayList<WorkflowNode>();
	private List<WorkflowTransition> transitions = new ArrayList<WorkflowTransition>();
	
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
	public String getAppKey() {
		return appKey;
	}
	public void setAppKey(String appKey) {
		this.appKey = appKey;
	}
	public CWindow getScreen() {
		return screen;
	}
	public void setScreen(CWindow screen) {
		this.screen = screen;
	}
	public List<WorkflowNode> getNodes() {
		return nodes;
	}
	public void setNodes(List<WorkflowNode> nodes) {
		this.nodes = nodes;
	}
	public List<WorkflowTransition> getTransitions() {
		return transitions;
	}
	public void setTransitions(List<WorkflowTransition> transitions) {
		this.transitions = transitions;
	}
}
