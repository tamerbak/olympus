package fr.protogen.masterdata.model;

public class WorkflowTransition {
	private String key;
	private String title;
	private boolean auto;
	private String yes_no;
	private String nodeA;
	private String nodeAKey;
	private String nodeB;
	private String nodeBKey;
	
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public boolean isAuto() {
		return auto;
	}
	public void setAuto(boolean auto) {
		this.auto = auto;
	}
	public String getYes_no() {
		return yes_no;
	}
	public void setYes_no(String yes_no) {
		this.yes_no = yes_no;
	}
	public String getNodeA() {
		return nodeA;
	}
	public void setNodeA(String nodeA) {
		this.nodeA = nodeA;
	}
	public String getNodeAKey() {
		return nodeAKey;
	}
	public void setNodeAKey(String nodeAKey) {
		this.nodeAKey = nodeAKey;
	}
	public String getNodeB() {
		return nodeB;
	}
	public void setNodeB(String nodeB) {
		this.nodeB = nodeB;
	}
	public String getNodeBKey() {
		return nodeBKey;
	}
	public void setNodeBKey(String nodeBKey) {
		this.nodeBKey = nodeBKey;
	}
	
}
