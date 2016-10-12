package fr.protogen.masterdata.model;

import java.io.Serializable;
import java.util.List;

import org.primefaces.model.chart.CartesianChartModel;
import org.primefaces.model.chart.PieChartModel;

public class SWidget implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int id;
	private String title;
	private String label;
	private String lvalues;
	private String type;
	private String query;
	private PieChartModel model;
	private CartesianChartModel lineModel;
	private List<String> dataCaptions;
	private List<List<String>> dataTable;
	private double max;
	
	
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
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getQuery() {
		return query;
	}
	public void setQuery(String query) {
		this.query = query;
	}
	public PieChartModel getModel() {
		return model;
	}
	public void setModel(PieChartModel model) {
		this.model = model;
	}
	public List<List<String>> getDataTable() {
		return dataTable;
	}
	public void setDataTable(List<List<String>> dataTable) {
		this.dataTable = dataTable;
	}
	public List<String> getDataCaptions() {
		return dataCaptions;
	}
	public void setDataCaptions(List<String> dataCaptions) {
		this.dataCaptions = dataCaptions;
	}
	public String getLvalues() {
		return lvalues;
	}
	public void setLvalues(String lvalues) {
		this.lvalues = lvalues;
	}
	public CartesianChartModel getLineModel() {
		return lineModel;
	}
	public void setLineModel(CartesianChartModel lineModel) {
		this.lineModel = lineModel;
	}
	public double getMax() {
		return max;
	}
	public void setMax(double max) {
		this.max = max;
	}
	
	
}
