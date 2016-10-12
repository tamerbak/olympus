package fr.protogen.cch.control;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import fr.protogen.cch.cache.SessionCache;
import fr.protogen.cch.control.ui.beans.UIStep;

@ManagedBean
@SessionScoped
public class ApplicationControl {
	private UIStep step;
	private SessionCache cache;
	
	public ApplicationControl(){
		
		step = new UIStep("Nouvelle application", "Création d'une nouvelle application", "Le texte de copyright doit référencer l'espace de droit intellectuel utilisé et le type de licence (GNU, Freeware Licence)");
		cache = new SessionCache();
	}

	public UIStep getStep() {
		return step;
	}

	public void setStep(UIStep step) {
		this.step = step;
	}

	public SessionCache getCache() {
		return cache;
	}

	public void setCache(SessionCache cache) {
		this.cache = cache;
	}

	
}
