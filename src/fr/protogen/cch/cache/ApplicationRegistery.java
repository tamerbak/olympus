package fr.protogen.cch.cache;

import java.util.HashMap;
import java.util.Map;

import fr.protogen.masterdata.model.CCHUser;

public class ApplicationRegistery {
	private static ApplicationRegistery instance = null;
	public static synchronized ApplicationRegistery getInstance(){
		if(instance == null)
			instance = new ApplicationRegistery();
		return instance;
	}
	private ApplicationRegistery(){
		setInstances(new HashMap<CCHUser, SessionCache>());
	}
	
	
	
	private Map<CCHUser, SessionCache> instances;
	
	public Map<CCHUser, SessionCache> getInstances() { 
		return instances;
	}
	public void setInstances(Map<CCHUser, SessionCache> instances) {
		this.instances = instances;
	}
	
	public void newInstance(CCHUser user){
		instances.put(user, new SessionCache());
	}
	public SessionCache getSession(CCHUser user){
		if(instances.containsKey(user))
			return instances.get(user);
		return null;
	}
	
}
