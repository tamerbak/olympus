package fr.protogen.cch.ws;

import java.util.ArrayList;
import java.util.List;

import fr.protogen.masterdata.model.CCHUser;

public class GUserSession {
	private List<CCHUser> onlineUsers;
	
	private static GUserSession instance = null;
	public static synchronized GUserSession getInstance(){
		if(instance == null)
			instance = new GUserSession();
		return instance;
	}
	private GUserSession(){
		setOnlineUsers(new ArrayList<CCHUser>());
	}
	
	/*
	 * GETTERS AND SETTERS
	 */
	public List<CCHUser> getOnlineUsers() {
		return onlineUsers;
	}
	public void setOnlineUsers(List<CCHUser> onlineUsers) {
		this.onlineUsers = onlineUsers;
	}
}
