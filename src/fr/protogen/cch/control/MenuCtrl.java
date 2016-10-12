package fr.protogen.cch.control;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import fr.protogen.cch.cache.ApplicationRegistery;
import fr.protogen.cch.cache.SessionCache;
import fr.protogen.cch.configuration.Parameters;
import fr.protogen.cch.genration.GenerationService;
import fr.protogen.masterdata.model.*;

@ManagedBean
@ViewScoped
public class MenuCtrl implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7640196073447996748L;

	private String menuTitle="";
	private boolean regroupement=false;
	private List<SMenuitem> menuItemsParents;
	private List<SMenuitem> menuItems;
	private List<CWindow> screens;
	private List<CWindow> formScreens;
	private String selectedItem;
	private String selectedScreen;
	private CWindow[] currentLinks;
	private CWindow[] targetScreens;
	
	private SessionCache cache;
	private SMenuitem toDelete;
	private Boolean maintainmode;
	
	private int menuLevel;
	private List<SRubrique> rubriques = new ArrayList<SRubrique>();
	private int selectedRubrique;
	
	private String nextScreen;
	private String previousScreen;
	
	private String selectedSource;
	private String selectedTarget;
	
	private boolean technical=false;
	
	private int index = 1;
	
	@PostConstruct
	public void initialize(){
		//	Get screens
		CCHUser user = (CCHUser)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(Parameters.USER_TOKEN);
    	cache = ApplicationRegistery.getInstance().getSession(user);
    	
    	screens = new ArrayList<CWindow>();
    	for(CWindow w : cache.getWindows()){
    		//	ONLY GET GRID WINDOWS
    		if(w.getCWindowtype().getId()==1)
    			screens.add(w);
    	}
    	formScreens = new ArrayList<CWindow>();
    	for(CWindow w : cache.getWindows()){
    		//	ONLY GET GRID WINDOWS
    		if(w.getCWindowtype().getId()==2)
    			formScreens.add(w);
    	}
    	menuItems = new ArrayList<SMenuitem>();
    	menuItemsParents = new ArrayList<SMenuitem>();
    	maintainmode = (Boolean)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(Parameters.MAINTAIN_MODE);
		index=1;
    	if(maintainmode){
    		rubriques = cache.getRubriques();
    		menuItems = cache.getMenu();
			for(SMenuitem m : menuItems)
				if(m.isParent())
					menuItemsParents.add(m);
		}
	}
	
	public void saveFormMenu(){
		CWindow src = null;
		CWindow dest = null;
		
		for(CWindow w : screens){
			if(w.getTitle().equals(selectedSource)){
				src = w;
				break;
			}
		}
		
		for(CWindow w : formScreens){
			if(w.getTitle().equals(selectedTarget)){
				dest = w;
				break;
			}
		}
		src.setFormId(dest.getId());
		if(maintainmode){
			GenerationService service = new GenerationService();
			service.generateWindowFormLink(src);
		}
	}
	
	public void doDelete(){
		menuItems.remove(toDelete);
		menuItemsParents.remove(toDelete);
		cache.getMenu().remove(toDelete);
		
		if(maintainmode){
			GenerationService service = new GenerationService();
			service.deleteMenuEntry(toDelete);
		}
	}
	public void cancelDelete(){
		
	}
	public String constructDependencies(){
		return "";
	}
	
	public void saveMenu(){
		if(menuLevel>1){
			SMenuitem item = new SMenuitem();
			item.setIsParent(menuLevel == 2);
			item.setTitle(menuTitle);		
			
			for(CWindow s : screens)
				if(s.getTitle().equals(selectedScreen) && !regroupement)
					item.setWindow(s);
			
			if(menuLevel == 2){
				SMenuitem p = new SMenuitem(0, "", true);
				item.setParent(p);
			}else{
				for(SMenuitem m : menuItemsParents)
					if(m.getTitle().equals(selectedItem))
						item.setParent(m);
			}
			if(menuLevel == 2){
				menuItemsParents.add(item);
				CWindow window = new CWindow();
				window.setTitle("");
				item.setWindow(window);
				item.setRubrique(selectedRubrique);
			}
			item.setParentName(item.getParent().getTitle());
			
			if(maintainmode){
				GenerationService service = new GenerationService();
				service.addNewMenu(item, cache);
			}
			
			menuItems.add(item);
			menuTitle="";
			regroupement=false;
		} else {
			SRubrique r = new SRubrique();
			r.setTitre(menuTitle);
			r.setTechnical(technical);
			rubriques.add(r);
			
			menuLevel=1;
			if(maintainmode){
				GenerationService service = new GenerationService();
				service.addNewRubrique(r, cache);
			}else{
				r.setId(index);
				index++;
			}
				
			
		}
	}
	
	public void selectedScreenChange(){
		CWindow source = new CWindow();
		for(CWindow s : screens)
			if(s.getTitle().equals(selectedScreen)){
				source = s;
				break;
			}
		
		if(source.getLinks()!=null && source.getLinks().size()>0){
			currentLinks = new CWindow[source.getLinks().size()];
			for(int i = 0 ; i < source.getLinks().size() ; i++)
				currentLinks[i] = source.getLinks().get(i);
		} else {
			currentLinks = null;
		}
	}
	
	public void saveLinks(){
		CWindow source = new CWindow();
		for(CWindow s : screens)
			if(s.getTitle().equals(selectedScreen)){
				source = s;
				break;
			}
		
		source.setLinks(new ArrayList<CWindow>());
		if(currentLinks!=null){
			
			if((nextScreen!=null && nextScreen.length()>0) || (previousScreen!=null && previousScreen.length()>0)){
				CWindow f = null;
				for(CWindow w : screens)
					if(w.getTitle().equals(nextScreen)){
						f=w;
						break;
					}
				CWindow l = null;
				for(CWindow w : screens)
					if(w.getTitle().equals(previousScreen)){
						l=w;
						break;
					}
				
				if(f!=null){
					source.getLinks().add(f);
				}
				for(CWindow t : currentLinks){
					if(f==t || l==t)
						continue;
					source.getLinks().add(t);
				}
				if(l!=null){
					source.getLinks().add(l);
				}
			}else{
			
				for(CWindow t : currentLinks){
					source.getLinks().add(t);
				}
			}
		}
		if(maintainmode){
			GenerationService service = new GenerationService();
			service.saveLinks(source);
		}
	}
	
	public void razSelection(){
		currentLinks = null;
	}
	
	public String nextToLink(){
		
		CCHUser user = (CCHUser)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(Parameters.USER_TOKEN);
    	SessionCache cache = ApplicationRegistery.getInstance().getSession(user);
    	cache.setMenu(menuItems);
		cache.setRubriques(rubriques);
    	
		return "linkitem";
	}
	
	public String next(){
		
		return "security";
	}
	
	@SuppressWarnings("unused")
	public void levelChange(){
		String tt="";
	}

	public String getMenuTitle() {
		return menuTitle;
	}

	public void setMenuTitle(String menuTitle) {
		this.menuTitle = menuTitle;
	}

	public boolean isRegroupement() {
		return regroupement;
	}

	public void setRegroupement(boolean regroupement) {
		this.regroupement = regroupement;
	}

	public List<SMenuitem> getMenuItemsParents() {
		return menuItemsParents;
	}

	public void setMenuItemsParents(List<SMenuitem> menuItemsParents) {
		this.menuItemsParents = menuItemsParents;
	}

	public List<SMenuitem> getMenuItems() {
		return menuItems;
	}

	public void setMenuItems(List<SMenuitem> menuItems) {
		this.menuItems = menuItems;
	}

	public List<CWindow> getScreens() {
		return screens;
	}

	public void setScreens(List<CWindow> screens) {
		this.screens = screens;
	}

	public String getSelectedItem() {
		return selectedItem;
	}

	public void setSelectedItem(String selectedItem) {
		this.selectedItem = selectedItem;
	}

	public String getSelectedScreen() {
		return selectedScreen;
	}

	public void setSelectedScreen(String selectedScreen) {
		this.selectedScreen = selectedScreen;
	}

	public SessionCache getCache() {
		return cache;
	}

	public void setCache(SessionCache cache) {
		this.cache = cache;
	}

	public SMenuitem getToDelete() {
		return toDelete;
	}

	public void setToDelete(SMenuitem toDelete) {
		this.toDelete = toDelete;
	}

	public CWindow[] getCurrentLinks() {
		return currentLinks;
	}

	public void setCurrentLinks(CWindow[] currntLinks) {
		this.currentLinks = currntLinks;
	}

	public CWindow[] getTargetScreens() {
		return targetScreens;
	}

	public void setTargetScreens(CWindow[] targetScreens) {
		this.targetScreens = targetScreens;
	}

	public int getMenuLevel() {
		return menuLevel;
	}

	public void setMenuLevel(int menuLevel) {
		this.menuLevel = menuLevel;
	}

	public List<SRubrique> getRubriques() {
		return rubriques;
	}

	public void setRubriques(List<SRubrique> rubriques) {
		this.rubriques = rubriques;
	}

	public int getSelectedRubrique() {
		return selectedRubrique;
	}

	public void setSelectedRubrique(int selectedRubrique) {
		this.selectedRubrique = selectedRubrique;
	}

	public Boolean getMaintainmode() {
		return maintainmode;
	}

	public void setMaintainmode(Boolean maintainmode) {
		this.maintainmode = maintainmode;
	}

	public String getNextScreen() {
		return nextScreen;
	}

	public void setNextScreen(String nextScreen) {
		this.nextScreen = nextScreen;
	}

	public String getPreviousScreen() {
		return previousScreen;
	}

	public void setPreviousScreen(String previousScreen) {
		this.previousScreen = previousScreen;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public List<CWindow> getFormScreens() {
		return formScreens;
	}

	public void setFormScreens(List<CWindow> formScreens) {
		this.formScreens = formScreens;
	}

	public String getSelectedSource() {
		return selectedSource;
	}

	public void setSelectedSource(String selectedSource) {
		this.selectedSource = selectedSource;
	}

	public String getSelectedTarget() {
		return selectedTarget;
	}

	public void setSelectedTarget(String selectedTarget) {
		this.selectedTarget = selectedTarget;
	}

	public boolean isTechnical() {
		return technical;
	}

	public void setTechnical(boolean technical) {
		this.technical = technical;
	}
	
	
}
