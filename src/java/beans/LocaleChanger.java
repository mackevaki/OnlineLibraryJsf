package beans;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.Locale;
import org.omnifaces.cdi.Eager;

@Named(value = "localeChanger")
@SessionScoped
@Eager
public class LocaleChanger implements Serializable {
    private Locale currentLocale; //= FacesContext.getCurrentInstance().getViewRoot().getLocale();
    
    public LocaleChanger() {
    }

    public Locale getCurrentLocale() {
        if (currentLocale == null) {
            currentLocale = new Locale("ru");// set by default russian language
        }        
        return currentLocale;
    }
    
    public void changeLocale(String localeCode) {
        currentLocale = new Locale(localeCode);
    }
}
