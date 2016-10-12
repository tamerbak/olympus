package fr.protogen.cch.control;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.bean.ViewScoped;

import org.apache.commons.io.IOUtils;
import org.primefaces.event.FileUploadEvent;

import fr.protogen.masterdata.dal.dao.ResourcesDAO;
import fr.protogen.masterdata.model.GResource;

@ManagedBean
@SessionScoped
public class GresourcesCtrl {
	private List<GResource> resources = new ArrayList<GResource>();
	private GResource toDelete;
	private GResource toCreate = new GResource();
	
	public GresourcesCtrl(){
		resources = ResourcesDAO.getInstance().loadResources();
	}
	
	public void doDelete(){
		
	}
	
	public String saveGResource(){
		
		ResourcesDAO.getInstance().saveResource(toCreate);
		resources.add(toCreate);
		toCreate = new GResource();
		return null;
	}

	public void handleFileUpload(FileUploadEvent event) {
		try {
    	    InputStream jarFile = event.getFile().getInputstream();
    	    toCreate.setFile(IOUtils.toByteArray(jarFile));
    	    jarFile.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*
	 * GETTERS AND SETTERS
	 */
	public List<GResource> getResources() {
		return resources;
	}

	public void setResources(List<GResource> resources) {
		this.resources = resources;
	}

	public GResource getToDelete() {
		return toDelete;
	}

	public void setToDelete(GResource toDelete) {
		this.toDelete = toDelete;
	}

	public GResource getToCreate() {
		return toCreate;
	}

	public void setToCreate(GResource toCreate) {
		this.toCreate = toCreate;
	}
}
